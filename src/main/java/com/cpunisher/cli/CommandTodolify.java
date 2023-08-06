package com.cpunisher.cli;

import com.cpunisher.todolify.Todolify;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.utils.SourceRoot;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@CommandLine.Command(name = "todolify")
public class CommandTodolify implements Runnable {

    @CommandLine.Parameters(paramLabel = "PROJECT")
    File project;

    @CommandLine.Option(names = { "-o", "--output" })
    Path output;

    @CommandLine.Option(names = { "-m", "--methods" }, split = ",")
    List<String> methodNames;

    @Override
    public void run() {
        System.out.println("Matching " + Arrays.toString(methodNames.toArray()) + "...");
        Path projectRoot = project.getAbsoluteFile().toPath();
        if (output == null) {
            output = projectRoot.getParent().resolve(projectRoot.getFileName() + "-TODOLIFY");
        }

        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());
        combinedTypeSolver.add(new JavaParserTypeSolver(project));

        JavaSymbolSolver javaSymbolSolver = new JavaSymbolSolver(combinedTypeSolver);
        ParserConfiguration configuration = new ParserConfiguration();
        configuration.setSymbolResolver(javaSymbolSolver);

        SourceRoot sourceRoot = new SourceRoot(projectRoot, configuration);
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

    public static void writeToFile(Path dest, String content) throws IOException {
        if (!Files.exists(dest.getParent())) {
            Files.createDirectories(dest.getParent());
        }
        Files.writeString(dest, content);
    }
}
