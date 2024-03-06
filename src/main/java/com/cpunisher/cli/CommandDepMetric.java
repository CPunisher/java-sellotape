package com.cpunisher.cli;

import com.cpunisher.common.ParserUtil;
import com.cpunisher.dep.DepMetricVisitor;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.utils.SourceRoot;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@CommandLine.Command(name = "dep-metric")
public class CommandDepMetric implements Runnable {
    @CommandLine.Option(names = "--generated-project", required = true)
    Path generatedProject;

    @CommandLine.Option(names = "--solution-project", required = true)
    Path solutionProject;

    @CommandLine.Option(names = "--todo-project", required = true)
    Path todoProject;

    @CommandLine.Option(names = "--verbose")
    boolean verbose = false;

    @Override
    public void run() {
        SourceRoot generatedSourceRoot = ParserUtil.getParserAndParse(generatedProject);
        SourceRoot solutionSourceRoot = ParserUtil.getParserAndParse(solutionProject);
        SourceRoot todoSourceRoot = ParserUtil.getParserAndParse(todoProject);

        if (verbose) {
            System.out.println("=============== Todos =============== ");
        }
        Map<String, List<CallableDeclaration.Signature>> todos = new HashMap<>();
        for (var compilationUnit : todoSourceRoot.getCompilationUnits()) {
            for (var type : compilationUnit.getNodesByType(TypeDeclaration.class)) {
                type.walk(ConstructorDeclaration.class, constructorDeclaration -> {
                    boolean hasTodo = constructorDeclaration.getBody()
                            .getAllContainedComments()
                            .stream()
                            .anyMatch(comment -> comment.getContent().toLowerCase().contains("todo"));
                    if (hasTodo) {
                        todos.computeIfAbsent(type.getFullyQualifiedName().get().toString(), (key) -> new ArrayList<>())
                                .add(constructorDeclaration.getSignature());
                    }
                });
                type.walk(MethodDeclaration.class, methodDeclaration -> {
                    if (methodDeclaration.getBody().isEmpty()) {
                        return;
                    }

                    boolean hasTodo = methodDeclaration.getBody()
                            .get()
                            .getAllContainedComments()
                            .stream()
                            .anyMatch(comment -> comment.getContent().toLowerCase().contains("todo"));
                    if (hasTodo) {
                        todos.computeIfAbsent(type.getFullyQualifiedName().get().toString(), (key) -> new ArrayList<>())
                                .add(methodDeclaration.getSignature());
                    }
                });
            }
        }
        if (verbose) {
            for (var entry : todos.entrySet()) {
                System.out.printf("%-20s(%02d) %s%n", entry.getKey(), entry.getValue().size(), entry.getValue());
            }
        }

        if (verbose) {
            System.out.println("=============== Final Result =============== ");
        }
        DepMetricVisitor generatedMetric = DepMetricVisitor.calculate(todos, generatedSourceRoot.getCompilationUnits(), verbose);
        DepMetricVisitor solutionMetric = DepMetricVisitor.calculate(todos, solutionSourceRoot.getCompilationUnits(), verbose);
        if (verbose) {
            System.out.println("[G] FieldAccess Failures = " + generatedMetric.getFieldAccessFailures() + ", [G] MethodCall Failures = " + generatedMetric.getMethodCallFailures());
            System.out.println("[S] FieldAccess Failures = " + solutionMetric.getFieldAccessFailures() + ", [S] MethodCall Failures = " + solutionMetric.getMethodCallFailures());
        }
        System.out.println("ID\tG(F)\tS(F)\tG(M)\tS(M)");
        System.out.println("PROJECT"
                + "\t" + generatedMetric.getProjectDepFieldAccessCount()
                + "\t" + solutionMetric.getProjectDepFieldAccessCount()
                + "\t" + generatedMetric.getProjectDepMethodCallCount()
                + "\t" + solutionMetric.getProjectDepMethodCallCount()
        );
        for (var key : todos.keySet()) {
            var gf = generatedMetric.getDepFieldAccessCountOf(key);
            var sf = solutionMetric.getDepFieldAccessCountOf(key);
            var gm = generatedMetric.getDepMethodCallCountOf(key);
            var sm = solutionMetric.getDepMethodCallCountOf(key);
            System.out.println(key
                    + "\t" + gf
                    + "\t" + sf
                    + "\t" + gm
                    + "\t" + sm
            );
        }
    }
}