package com.cpunisher.dep;

import com.cpunisher.common.ParserUtil;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.utils.SourceRoot;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public class DepGraph {
    private final List<CompilationUnit> projectCompilationUnits;
    private final List<CompilationUnit> testCompilationUnits;
    private final MutableGraph<String> projectGraph;
    private final MutableGraph<String> testGraph;

    public DepGraph(List<CompilationUnit> projectCompilationUnits, List<CompilationUnit> testCompilationUnits) {
        this.projectCompilationUnits = projectCompilationUnits;
        this.testCompilationUnits = testCompilationUnits;
        this.projectGraph = build(projectCompilationUnits);
        this.testGraph = build(testCompilationUnits);
    }

    public Set<String> getProjectDeps(String target) {
        return getDeps(target, projectGraph);
    }

    public Map<String, Set<String>> getProjectAllDeps() {
        return getAllDeps(projectGraph);
    }

    public Map<String, Set<String>> getAllTestDeps() {
        Map<String, Set<String>> result = new HashMap<>();
        Set<String> projectNames = projectCompilationUnits.stream()
                .flatMap(unit -> unit.getTypes().stream())
                .map(type -> type.getName().getIdentifier())
                .collect(Collectors.toSet());
        for (var testUnit: testCompilationUnits) {
            var types = testUnit.getTypes();
            var imports = testUnit.getImports();
            for (var type : types) {
                String typeName = type.getName().getIdentifier();
                Set<String> deps = new HashSet<>();
                for (var ipt : imports) {
                    String importTarget = ipt.getName().getIdentifier();
                    if (projectNames.contains(importTarget)) {
                        deps.add(importTarget);
                        deps.addAll(getProjectDeps(importTarget));
                    }
                }
                result.put(typeName, deps);
            }
        }
        return result;
    }

    private Set<String> getDeps(String target, MutableGraph<String> graph) {
        Set<String> result = new HashSet<>();
        Queue<String> workList = new LinkedList<>();
        workList.add(target);
        while (!workList.isEmpty()) {
            var next = workList.poll();
            result.add(next);
            workList.addAll(graph.successors(next)
                    .stream()
                    .filter(name -> !result.contains(name))
                    .collect(Collectors.toSet()));
        }
        result.remove(target);
        return result;
    }

    private Map<String, Set<String>> getAllDeps(MutableGraph<String> graph) {
        Map<String, Set<String>> result = new HashMap<>();
        for (var node : graph.nodes()) {
            result.put(node, getDeps(node, graph));
        }
        return result;
    }

    private MutableGraph<String> build(List<CompilationUnit> compilationUnitList) {
        MutableGraph<String> graph = GraphBuilder.directed().build();
        Set<String> projectNames = compilationUnitList.stream()
                .flatMap(unit -> unit.getTypes().stream())
                .map(type -> type.getName().getIdentifier())
                .collect(Collectors.toSet());
        for (var unit : compilationUnitList) {
            var types = unit.getTypes();
            var imports = unit.getImports();
            for (var type : types) {
                String typeName = type.getName().getIdentifier();
                graph.addNode(typeName);
                for (var ipt : imports) {
                    String importTarget = ipt.getName().getIdentifier();
                    if (projectNames.contains(importTarget)) {
                        graph.putEdge(typeName, importTarget);
                    }
                }
            }
        }
        return graph;
    }

    public static void run(Path project, Path test) {
        SourceRoot projectSourceRoot = ParserUtil.getProjectLevelParser(project);
        for (ParseResult<CompilationUnit> compilationUnitParseResult : projectSourceRoot.tryToParseParallelized()) {
            if (!compilationUnitParseResult.isSuccessful()) {
                throw new RuntimeException(compilationUnitParseResult.toString());
            }
        }
        SourceRoot testSourceRoot = ParserUtil.getProjectLevelParser(test);
        for (ParseResult<CompilationUnit> compilationUnitParseResult : testSourceRoot.tryToParseParallelized()) {
            if (!compilationUnitParseResult.isSuccessful()) {
                throw new RuntimeException(compilationUnitParseResult.toString());
            }
        }

        DepGraph depGraph = new DepGraph(projectSourceRoot.getCompilationUnits(), testSourceRoot.getCompilationUnits());
        var maps = depGraph.getAllTestDeps();

        var entries = new ArrayList<>(maps.entrySet());
        entries.sort(Comparator.comparingInt(e -> e.getValue().size()));
        for (var entry : entries) {
            System.out.println(entry.getKey() + "[" + entry.getValue()
                    .size() + "]" + ": " + String.join(", ", entry.getValue()));
        }
    }
}
