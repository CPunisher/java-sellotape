package com.cpunisher.cli;

import com.cpunisher.common.ParserUtil;
import com.cpunisher.todolify.Todolify;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.utils.SourceRoot;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@CommandLine.Command(name = "todolify")
public class CommandTodolify implements Runnable {

    @CommandLine.Parameters(paramLabel = "PROJECT")
    Path project;

    @CommandLine.Option(names = { "-o", "--output" })
    Path output;

    @CommandLine.Option(names = { "-m", "--methods" }, split = ",")
    List<String> methodNames;

    @Override
    public void run() {
        System.out.println("Matching " + Arrays.toString(methodNames.toArray()) + "...");
        if (output == null) {
            output = project.getParent().resolve(project.getFileName() + "-TODOLIFY");
        }

        SourceRoot sourceRoot = ParserUtil.getProjectLevelParser(project);
        List<CompilationUnit> parseResults = sourceRoot.tryToParseParallelized()
                .stream()
                .map(ParseResult::getResult)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        for (var compilationUnit : parseResults) {
            Todolify todolify = new Todolify(methodNames);
            todolify.transform(compilationUnit);
        }

        sourceRoot.saveAll(output);
    }
}
