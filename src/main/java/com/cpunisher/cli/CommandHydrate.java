package com.cpunisher.cli;

import com.cpunisher.common.ParserUtil;
import com.cpunisher.hydrate.Hydrate;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.utils.SourceRoot;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@CommandLine.Command(name = "hydrate")
public class CommandHydrate implements Runnable {

    @CommandLine.Parameters(paramLabel = "PROJECT")
    Path project;

    @CommandLine.Option(names = { "-o", "--output" })
    Path output;

    @CommandLine.Option(names = { "-d", "--doc"})
    boolean keepDoc = false;

    @CommandLine.Option(names = { "-i", "--import"})
    boolean keepImport = false;

    @CommandLine.Option(names = { "-p", "--private"})
    boolean keepPrivate = false;

    @Override
    public void run() {
        if (output == null) {
            output = project.getParent().resolve(project.getFileName() + "-HYDRATE");
        }

        SourceRoot sourceRoot = ParserUtil.getProjectLevelParser(project);
        List<CompilationUnit> parseResults = sourceRoot.tryToParseParallelized()
                .stream()
                .map(ParseResult::getResult)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        for (var compilationUnit : parseResults) {
            Hydrate hydrate = new Hydrate(keepDoc, keepImport, keepPrivate);
            hydrate.transform(compilationUnit);
        }

        sourceRoot.saveAll(output);
    }
}
