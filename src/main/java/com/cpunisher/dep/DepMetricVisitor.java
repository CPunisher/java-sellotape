package com.cpunisher.dep;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DepMetricVisitor extends VoidVisitorAdapter<Void> {
    private final List<CompilationUnit> projectCompilationUnits;
    private final Set<String> projectClassIdentifiers;
    private final List<FieldAccessExpr> depFieldAccess = new ArrayList<>();
    private final List<MethodCallExpr> depMethodCalls = new ArrayList<>();
    private int fieldAccessFailures = 0;
    private int methodCallFailures = 0;

    public DepMetricVisitor(List<CompilationUnit> projectCompilationUnits) {
        this.projectCompilationUnits = projectCompilationUnits;
        this.projectClassIdentifiers = getProjectClassIdentifiers();
        System.out.println("[Classes] " + this.projectClassIdentifiers);
    }

    @Override
    public void visit(FieldAccessExpr n, Void arg) {
        try {
            String name = n.getScope().calculateResolvedType().describe();
            if (projectClassIdentifiers.contains(name)) {
                depFieldAccess.add(n);
            }
        } catch (Exception e) {
            System.err.println("[FAILED] [FieldAccess] " + n + " " + e);
            fieldAccessFailures += 1;
        }
        super.visit(n, arg);
    }

    @Override
    public void visit(MethodCallExpr n, Void arg) {
        try {
            String name = null;
            if (n.getScope().isPresent()) {
                name = n.getScope().get().calculateResolvedType().describe();
            }
            if (name == null || projectClassIdentifiers.contains(name)) {
                depMethodCalls.add(n);
            }
        } catch (Exception e) {
            System.err.println("[FAILED] [MethodCall] " + n + " " + e);
            methodCallFailures += 1;
        }
        super.visit(n, arg);
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

    public List<FieldAccessExpr> getDepFieldAccess() {
        return Collections.unmodifiableList(depFieldAccess);
    }

    public List<MethodCallExpr> getDepMethodCalls() {
        return Collections.unmodifiableList(depMethodCalls);
    }

    public int getFieldAccessFailures() {
        return fieldAccessFailures;
    }

    public int getMethodCallFailures() {
        return methodCallFailures;
    }

    public static DepMetricVisitor calculate(Map<String, List<CallableDeclaration.Signature>> todos, List<CompilationUnit> compilationUnitList) {
        DepMetricVisitor depMetricVisitor = new DepMetricVisitor(compilationUnitList);
        for (var compilationUnit : compilationUnitList) {
            compilationUnit.walk(TypeDeclaration.class, type -> {
                String name = type.getFullyQualifiedName().get().toString();
                if (!todos.containsKey(name)) {
                    return;
                }

                todos.get(name).stream()
                        .flatMap(signature -> type.getCallablesWithSignature(signature).stream())
                        .forEach(callable -> ((CallableDeclaration<?>) callable).accept(depMetricVisitor, null));
            });
        }
        return depMetricVisitor;
    }
}
