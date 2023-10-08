package com.cpunisher;

import com.cpunisher.hydrate.Hydrate;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HydrateTest {
    @Test
    void transform() {
        String source = """
                public class Main {
                    /**
                    * doc
                    */
                    void f1() {
                        int a = 1;
                        int b = 2;
                    }
                }
                """;

        String expected = """
                public class Main {
                    /**
                    * doc
                    */
                    void f1() {
                    
                    }
                }
                """;

        StaticJavaParser.getParserConfiguration().setSymbolResolver(new JavaSymbolSolver(new ReflectionTypeSolver()));
        CompilationUnit compilationUnit = StaticJavaParser.parse(source);
        Hydrate hydrate = new Hydrate(true);
        hydrate.transform(compilationUnit);

        assertEquals(StaticJavaParser.parse(expected), compilationUnit);
    }

    @Test
    void transformWithoutDoc() {
        String source = """
                public class Main {
                    /**
                    * doc
                    */
                    void f1() {
                        int a = 1;
                        int b = 2;
                    }
                }
                """;

        String expected = """
                public class Main {
                    void f1() {
                    
                    }
                }
                """;

        StaticJavaParser.getParserConfiguration().setSymbolResolver(new JavaSymbolSolver(new ReflectionTypeSolver()));
        CompilationUnit compilationUnit = StaticJavaParser.parse(source);
        Hydrate hydrate = new Hydrate(false);
        hydrate.transform(compilationUnit);

        assertEquals(StaticJavaParser.parse(expected), compilationUnit);
    }

    @Test
    void transformConstructor() {
        String source = """
                public class Main {
                    public Main() {
                        int a = 1;
                    }
                }
                """;

        String expected = """
                public class Main {
                    public Main() {
                    }
                }
                """;

        StaticJavaParser.getParserConfiguration().setSymbolResolver(new JavaSymbolSolver(new ReflectionTypeSolver()));
        CompilationUnit compilationUnit = StaticJavaParser.parse(source);
        Hydrate hydrate = new Hydrate(false);
        hydrate.transform(compilationUnit);

        assertEquals(StaticJavaParser.parse(expected), compilationUnit);
    }
}
