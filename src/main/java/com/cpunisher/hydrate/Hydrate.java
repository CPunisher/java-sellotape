package com.cpunisher.hydrate;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;

public class Hydrate {
    private final boolean keepDoc;

    private final boolean keepImport;

    public Hydrate(boolean keepDoc, boolean keepImport) {
        this.keepDoc = keepDoc;
        this.keepImport = keepImport;
    }

    public void transform(CompilationUnit compilationUnit) {
        var visitor = new ModifierVisitor<>() {
            @Override
            public Visitable visit(MethodDeclaration n, Object arg) {
                BlockStmt blockStmt = new BlockStmt();
                n.setBody(blockStmt);
                return super.visit(n, arg);
            }

            @Override
            public Visitable visit(ConstructorDeclaration n, Object arg) {
                BlockStmt blockStmt = new BlockStmt();
                n.setBody(blockStmt);
                return super.visit(n, arg);
            }
        };
        visitor.visit(compilationUnit, null);

        if (!keepDoc) {
            var docVisitor = new ModifierVisitor<>() {
                @Override
                public Visitable visit(AnnotationDeclaration n, Object arg) {
                    n.removeJavaDocComment();
                    return super.visit(n, arg);
                }


                @Override
                public Visitable visit(AnnotationMemberDeclaration n, Object arg) {
                    n.removeJavaDocComment();
                    return super.visit(n, arg);
                }


                @Override
                public Visitable visit(ClassOrInterfaceDeclaration n, Object arg) {
                    n.removeJavaDocComment();
                    return super.visit(n, arg);
                }


                @Override
                public Visitable visit(ConstructorDeclaration n, Object arg) {
                    n.removeJavaDocComment();
                    return super.visit(n, arg);
                }

                @Override
                public Visitable visit(CompactConstructorDeclaration n, Object arg) {
                    n.removeJavaDocComment();
                    return super.visit(n, arg);
                }

                @Override
                public Visitable visit(EnumConstantDeclaration n, Object arg) {
                    n.removeJavaDocComment();
                    return super.visit(n, arg);
                }

                @Override
                public Visitable visit(EnumDeclaration n, Object arg) {
                    n.removeJavaDocComment();
                    return super.visit(n, arg);
                }

                @Override
                public Visitable visit(FieldDeclaration n, Object arg) {
                    n.removeJavaDocComment();
                    return super.visit(n, arg);
                }

                @Override
                public Visitable visit(InitializerDeclaration n, Object arg) {
                    n.removeJavaDocComment();
                    return super.visit(n, arg);
                }

                @Override
                public Visitable visit(MethodDeclaration n, Object arg) {
                    n.removeJavaDocComment();
                    return super.visit(n, arg);
                }

                @Override
                public Visitable visit(RecordDeclaration n, Object arg) {
                    n.removeJavaDocComment();
                    return super.visit(n, arg);
                }
            };

            docVisitor.visit(compilationUnit, null);
        }

        if (!keepImport) {
            var importVisitor = new ModifierVisitor<>() {
                @Override
                public Node visit(ImportDeclaration n, Object arg) {
                    n.remove();
                    return super.visit(n, arg);
                }
            };

            importVisitor.visit(compilationUnit, null);
        }
    }
}
