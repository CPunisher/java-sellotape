package com.cpunisher.hydrate;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;

public class Hydrate {
    private final boolean keepDoc;

    public Hydrate() {
        this(true);
    }

    public Hydrate(boolean keepDoc) {
        this.keepDoc = keepDoc;
    }

    public void transform(CompilationUnit compilationUnit) {
        var visitor = new ModifierVisitor<>() {
            @Override
            public Visitable visit(MethodDeclaration n, Object arg) {
                BlockStmt blockStmt = new BlockStmt();
                n.setBody(blockStmt);
                if (!keepDoc) {
                    n.removeJavaDocComment();
                }
                return super.visit(n, arg);
            }

            @Override
            public Visitable visit(ConstructorDeclaration n, Object arg) {
                BlockStmt blockStmt = new BlockStmt();
                n.setBody(blockStmt);
                if (!keepDoc) {
                    n.removeJavaDocComment();
                }
                return super.visit(n, arg);
            }
        };
        visitor.visit(compilationUnit, null);
    }
}
