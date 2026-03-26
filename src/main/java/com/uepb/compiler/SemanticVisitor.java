package com.uepb.compiler;

import com.uepb.ExprBaseVisitor;
import com.uepb.ExprParser;

public class SemanticVisitor extends ExprBaseVisitor<Void> {

    private final SymbolTable symbolTable;
    private boolean hasErrors = false;

    public SemanticVisitor(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    public boolean hasErrors() {
        return hasErrors;
    }
    
    @Override
    public Void visitBlock(ExprParser.BlockContext ctx) {
        symbolTable.pushScope();
        visitChildren(ctx);
        symbolTable.popScope();
        return null;
    }

    @Override
    public Void visitVarDeclaration(ExprParser.VarDeclarationContext ctx) {
        String name = ctx.ID().getText();

        if (symbolTable.isDeclaredInCurrentScope(name)) {
            System.err.println("Erro semântico: variável '" + name + "' já declarada neste escopo.");
            hasErrors = true;
        } else {
            symbolTable.declare(name);
        }

        return visitChildren(ctx);
    }

    @Override
    public Void visitAssignment(ExprParser.AssignmentContext ctx) {
        String name = ctx.ID().getText();

        if (!symbolTable.isDeclared(name)) {
            System.err.println("Erro semântico: variável '" + name + "' não declarada.");
            hasErrors = true;
        }

        return visitChildren(ctx);
    }

    @Override
    public Void visitInputStatement(ExprParser.InputStatementContext ctx) {
        String name = ctx.ID().getText();

        if (!symbolTable.isDeclared(name)) {
            System.err.println("Erro semântico: variável '" + name + "' não declarada.");
            hasErrors = true;
        }

        return null;
    }

    @Override
    public Void visitPowerExpr(ExprParser.PowerExprContext ctx) {
        ExprParser.ExprContext expExpr = ctx.expr(1);
        if (expExpr instanceof ExprParser.AtomExprContext atomExpr) {
            var atom = atomExpr.atom();
            if (atom.NUMBER() != null) {
                String text = atom.NUMBER().getText();
                double val  = Double.parseDouble(text);
                if (text.contains(".")) {
                    System.err.println("Erro semântico: expoente decimal '" + text + "' não é suportado. Use um inteiro positivo.");
                    hasErrors = true;
                } else if (val < 0) {
                    System.err.println("Erro semântico: expoente negativo '" + text + "' não é suportado. Use um inteiro positivo.");
                    hasErrors = true;
                }
            }
        }
        return visitChildren(ctx);
    }

    @Override
    public Void visitAtom(ExprParser.AtomContext ctx) {
        if (ctx.ID() != null && ctx.LPAREN() == null) {
            String name = ctx.ID().getText();
            if (!symbolTable.isDeclared(name)) {
                System.err.println("Erro semântico: variável '" + name + "' não declarada.");
                hasErrors = true;
            }
        }

        return visitChildren(ctx);
    }
}
