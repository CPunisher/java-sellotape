package com.cpunisher.cli;

import com.cpunisher.common.ParserUtil;
import com.cpunisher.dep.DepMetricVisitor;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.printer.DefaultPrettyPrinter;
import com.github.javaparser.printer.YamlPrinter;
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

    @Override
    public void run() {
        SourceRoot generatedSourceRoot = ParserUtil.getParserAndParse(generatedProject);
        SourceRoot solutionSourceRoot = ParserUtil.getParserAndParse(solutionProject);
        SourceRoot todoSourceRoot = ParserUtil.getParserAndParse(todoProject);

        System.out.println("=============== Todos =============== ");
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
        for (var entry : todos.entrySet()) {
            System.out.printf("%-20s(%02d) %s%n", entry.getKey(), entry.getValue().size(), entry.getValue());
        }

        System.out.println("=============== Final Result =============== ");
        DepMetricVisitor generatedMetric = DepMetricVisitor.calculate(todos, generatedSourceRoot.getCompilationUnits());
        DepMetricVisitor solutionMetric = DepMetricVisitor.calculate(todos, solutionSourceRoot.getCompilationUnits());
        System.out.println("G(F) = " + generatedMetric.getProjectDepFieldAccessCount() + ", S(F) = " + solutionMetric.getProjectDepFieldAccessCount());
        System.out.println("G(M) = " + generatedMetric.getProjectDepMethodCallCount() + ", S(M) = " + solutionMetric.getProjectDepMethodCallCount());
        System.out.println("[G] FieldAccess Failures = " + generatedMetric.getFieldAccessFailures() + ", [G] MethodCall Failures = " + generatedMetric.getMethodCallFailures());
        System.out.println("[S] FieldAccess Failures = " + solutionMetric.getFieldAccessFailures() + ", [S] MethodCall Failures = " + solutionMetric.getMethodCallFailures());
        for (var key : todos.keySet()) {
            var gf = generatedMetric.getDepFieldAccessCountOf(key);
            var sf = solutionMetric.getDepFieldAccessCountOf(key);
            var gm = generatedMetric.getDepMethodCallCountOf(key);
            var sm = solutionMetric.getDepMethodCallCountOf(key);
            System.out.println("[" + key + "] " + "G(F) = " + gf + ", S(F) = " + sf);
            System.out.println("[" + key + "] " + "G(M) = " + gm + ", S(M) = " + sm);
        }
    }
}