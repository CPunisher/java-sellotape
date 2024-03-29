package com.cpunisher.dep;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DepMetricVisitor extends VoidVisitorAdapter<String> {
    private final List<CompilationUnit> projectCompilationUnits;
    private final Set<String> projectClassIdentifiers;
    private final Map<String, List<FieldAccessExpr>> depFieldAccess = new HashMap<>();
    private final Map<String, List<MethodCallExpr>> depMethodCalls = new HashMap<>();
    private int fieldAccessFailures = 0;
    private int methodCallFailures = 0;
    private boolean verbose;

    public DepMetricVisitor(List<CompilationUnit> projectCompilationUnits, boolean verbose) {
        this.projectCompilationUnits = projectCompilationUnits;
        this.projectClassIdentifiers = getProjectClassIdentifiers();
        this.verbose = verbose;
        if (verbose) {
            System.out.println("[Classes] " + this.projectClassIdentifiers);
        }
    }

    @Override
    public void visit(FieldAccessExpr n, String className) {
        try {
            String name = n.getScope().calculateResolvedType().describe();
            if (projectClassIdentifiers.contains(name)) {
                depFieldAccess.computeIfAbsent(className, key -> new ArrayList<>()).add(n);
            }
        } catch (Exception e) {
            System.err.println("[FAILED] [FieldAccess] " + n + " " + e);
            fieldAccessFailures += 1;
        }
        super.visit(n, className);
    }

    @Override
    public void visit(MethodCallExpr n, String className) {
        try {
            String name = null;
            if (n.getScope().isPresent()) {
                name = n.getScope().get().calculateResolvedType().describe();
            }
            if (name == null || projectClassIdentifiers.contains(name)) {
                depMethodCalls.computeIfAbsent(className, key -> new ArrayList<>()).add(n);
            }
        } catch (Exception e) {
            System.err.println("[FAILED] [MethodCall] " + n + " " + e);
            methodCallFailures += 1;
        }
        super.visit(n, className);
    }

    private Set<String> getProjectClassIdentifiers() {
        Set<String> classes = new HashSet<>();
        projectCompilationUnits.stream()
                .flatMap(compilationUnit -> compilationUnit.getNodesByType(TypeDeclaration.class).stream())
                .forEach(type -> type.walk(TypeDeclaration.class, (t) -> {
                    classes.add(t.getFullyQualifiedName().get().toString());
                }));
        return classes;
    }

    public int getProjectDepFieldAccessCount() {
        return depFieldAccess.values()
                .stream().mapToInt(List::size)
                .sum();
    }

    public int getDepFieldAccessCountOf(String name) {
        return depFieldAccess.getOrDefault(name, Collections.EMPTY_LIST).size();
    }

    public int getProjectDepMethodCallCount() {
        return depMethodCalls.values()
                .stream().mapToInt(List::size)
                .sum();
    }

    public int getDepMethodCallCountOf(String name) {
        return depMethodCalls.getOrDefault(name, Collections.EMPTY_LIST).size();
    }

    public Map<String, List<FieldAccessExpr>> getDepFieldAccess() {
        return Collections.unmodifiableMap(depFieldAccess);
    }

    public Map<String, List<MethodCallExpr>> getDepMethodCalls() {
        return Collections.unmodifiableMap(depMethodCalls);
    }

    public int getFieldAccessFailures() {
        return fieldAccessFailures;
    }

    public int getMethodCallFailures() {
        return methodCallFailures;
    }

    public static DepMetricVisitor calculate(Map<String, List<CallableDeclaration.Signature>> todos, List<CompilationUnit> compilationUnitList, boolean verbose) {
        DepMetricVisitor depMetricVisitor = new DepMetricVisitor(compilationUnitList, verbose);
        for (var compilationUnit : compilationUnitList) {
            compilationUnit.walk(TypeDeclaration.class, type -> {
                String name = type.getFullyQualifiedName().get().toString();
                if (!todos.containsKey(name)) {
                    return;
                }

                todos.get(name).stream()
                        .flatMap(signature -> type.getCallablesWithSignature(signature).stream())
                        .forEach(callable -> ((CallableDeclaration<?>) callable).accept(depMetricVisitor, name));
            });
        }
        return depMetricVisitor;
    }
}
