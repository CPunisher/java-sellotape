package com.cpunisher.common;

import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.utils.SourceRoot;

import java.nio.file.Path;

public class ParserUtil {
    public static SourceRoot getProjectLevelParser(Path project) {
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());
        combinedTypeSolver.add(new JavaParserTypeSolver(project));

        JavaSymbolSolver javaSymbolSolver = new JavaSymbolSolver(combinedTypeSolver);
        ParserConfiguration configuration = new ParserConfiguration();
        configuration.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17);
        configuration.setSymbolResolver(javaSymbolSolver);
        return new SourceRoot(project, configuration);
    }

    public static SourceRoot getParserAndParse(Path project) {
        SourceRoot sourceRoot = ParserUtil.getProjectLevelParser(project);
        for (ParseResult<CompilationUnit> compilationUnitParseResult : sourceRoot.tryToParseParallelized()) {
            if (!compilationUnitParseResult.isSuccessful()) {
                throw new RuntimeException(compilationUnitParseResult.toString());
            }
        }
        return sourceRoot;
    }
}
