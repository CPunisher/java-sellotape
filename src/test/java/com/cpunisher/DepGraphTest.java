package com.cpunisher;

import com.cpunisher.common.ParserUtil;
import com.cpunisher.dep.DepGraph;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.utils.SourceRoot;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;

public class DepGraphTest {

    @Test
    void testAllDeps() {
        var project = Path.of(DepGraphTest.class.getClassLoader().getResource("PA22/main").getPath());
        var test = Path.of(DepGraphTest.class.getClassLoader().getResource("PA22/test").getPath());
        DepGraph.run(project, test);
    }
}
