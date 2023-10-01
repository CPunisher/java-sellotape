package com.cpunisher;

import com.cpunisher.todolify.Todolify;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TodolifyTest {
    @Test
    void transformMethod() {
        String source = """
                public class Main {
                    public void blank() {
                    
                    }
                    public void comment() {
                        // Something...
                    }
                    public void code() {
                        int a = 1;
                    }
                    public void keep() {
                    
                    }
                }
                """;

        String expected = """
                public class Main {
                
                    public void blank() {
                        // TODO
                    }
                    public void comment() {
                        // TODO
                    }
                    public void code() {
                        // TODO
                    }
                    public void keep() {
                    
                    }
                }
                """;
        StaticJavaParser.getParserConfiguration().setSymbolResolver(new JavaSymbolSolver(new ReflectionTypeSolver()));
        CompilationUnit compilationUnit = StaticJavaParser.parse(source);
        Todolify todolify = new Todolify(List.of("Main.blank", "Main.comment", "Main.code"));
        todolify.transform(compilationUnit);

        assertEquals(StaticJavaParser.parse(expected), compilationUnit);
    }

    @Test
    void transformFunction() {
        String source = """
                public class Main {
                    public static void blank() {
                    
                    }
                    public static void comment() {
                        // Something...
                    }
                    public static void code() {
                        int a = 1;
                    }
                    public static void keep() {
                    
                    }
                }
                """;

        String expected = """
                public class Main {
                    public static void blank() {
                        // TODO
                    }
                    public static void comment() {
                        // TODO
                    }
                    public static void code() {
                        // TODO
                    }
                    public static void keep() {
                    
                    }
                }
                """;
        StaticJavaParser.getParserConfiguration().setSymbolResolver(new JavaSymbolSolver(new ReflectionTypeSolver()));
        CompilationUnit compilationUnit = StaticJavaParser.parse(source);
        Todolify todolify = new Todolify(List.of("Main.blank", "Main.comment", "Main.code"));
        todolify.transform(compilationUnit);

        assertEquals(StaticJavaParser.parse(expected), compilationUnit);
    }
}