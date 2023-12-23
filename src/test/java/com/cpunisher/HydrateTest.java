package com.cpunisher;

import com.cpunisher.hydrate.Hydrate;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HydrateTest {
    void assertCodeTransform(String expected, String source, boolean keepDoc, boolean keepImport, boolean keepPrivate) {
        StaticJavaParser.getParserConfiguration().setSymbolResolver(new JavaSymbolSolver(new ReflectionTypeSolver()));
        CompilationUnit compilationUnit = StaticJavaParser.parse(source);
        Hydrate hydrate = new Hydrate(keepDoc, keepImport, keepPrivate);
        hydrate.transform(compilationUnit);

        assertEquals(StaticJavaParser.parse(expected), compilationUnit);
    }

    @Test
    void transform() {
        String source = """
                import java.util.*;
                public class Main {
                    private int n = 1;
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
                import java.util.*;
                public class Main {
                    private int n = 1;
                    /**
                    * doc
                    */
                    void f1() {
                    
                    }
                }
                """;
        assertCodeTransform(expected, source, true, true, true);
    }

    @Test
    void transformWithoutDoc() {
        String source = """
                /**
                * doc
                */
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

        assertCodeTransform(expected, source, false, false, false);
    }

    @Test
    void transformFieldDoc() {
        String source = """
                public class Main {
                    /**
                    * doc
                    */
                    int a = 1;
                }
                """;

        String expected = """
                public class Main {
                    int a = 1;
                }
                """;
        assertCodeTransform(expected, source, false, false, false);
    }

    @Test
    void transformConstructor() {
        String source = """
                public class Main {
                    /**
                    * doc
                    */
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
        assertCodeTransform(expected, source, false, false, false);
    }

    @Test
    void transformImport() {
        String source = """
                import java.util.*;
                public class Main {
                }
                """;

        String expected = """
                public class Main {
                }
                """;
        assertCodeTransform(expected, source, false, false, false);
    }

    @Test
    void transformPrivate() {
        String source = """
                public class Main {
                    private int a;
                    private void f() {
                    }
                }
                """;

        String expected = """
                public class Main {
                }
                """;
        assertCodeTransform(expected, source, false, false, false);
    }
}
