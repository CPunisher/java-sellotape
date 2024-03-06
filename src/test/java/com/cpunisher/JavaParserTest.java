package com.cpunisher;

import com.github.javaparser.JavaParser;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.printer.YamlPrinter;
import org.junit.jupiter.api.Test;

public class JavaParserTest {
    @Test
    void test() {
        String source = """
                public class Test {
                    public static final class Down extends Move {
                        public @NotNull Position nextPosition(@NotNull Position currentPosition) {
                            return Position.of(currentPosition.x(), currentPosition.y() + 1);
                        }
                    }
                }
                """;
        CompilationUnit cu = StaticJavaParser.parse(source);
        System.out.println(new YamlPrinter(true).output(cu));
    }
}
