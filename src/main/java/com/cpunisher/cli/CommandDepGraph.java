package com.cpunisher.cli;

import com.cpunisher.common.ParserUtil;
import com.cpunisher.dep.DepGraph;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.utils.SourceRoot;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@CommandLine.Command(name = "dep-graph")
public class CommandDepGraph implements Runnable {
    @CommandLine.Parameters(paramLabel = "PROJECT")
    Path project;

    @CommandLine.Parameters(paramLabel = "TEST")
    Path test;

    @Override
    public void run() {
        DepGraph.run(project, test);
    }
}
