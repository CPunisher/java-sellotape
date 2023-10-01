package com.cpunisher.todolify;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;

import java.util.List;

public class Todolify {
    private final List<String> methodNames;

    public Todolify(List<String> methodNames) {
        this.methodNames = methodNames;
    }

    public void transform(CompilationUnit compilationUnit) {
        var visitor = new ModifierVisitor<>() {
            @Override
            public Visitable visit(MethodDeclaration n, Object arg) {
                String name = n.resolve().getQualifiedName();
                if (methodNames.contains(name)) {
                    BlockStmt blockStmt = new BlockStmt();
                    blockStmt.addOrphanComment(new LineComment(" TODO"));
                    n.setBody(blockStmt);
                }
                return super.visit(n, arg);
            }
        };
        visitor.visit(compilationUnit, null);
    }
}
