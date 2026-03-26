package com.uepb.compiler;

import java.io.PrintWriter;

import com.uepb.ExprBaseVisitor;
import com.uepb.ExprParser;

public class CodeGeneratorVisitor extends ExprBaseVisitor<Void> {

    private final SymbolTable symbolTable;
    private final PrintWriter out;
    private int labelCounter = 0;

    public CodeGeneratorVisitor(SymbolTable symbolTable, PrintWriter out) {
        this.symbolTable = symbolTable;
        this.out = out;
    }

    private String newLabel() {
        return "L" + labelCounter++;
    }

    private void emit(String instruction) {
        out.println(instruction);
    }

    @Override
    public Void visitBlock(ExprParser.BlockContext ctx) {
        symbolTable.pushScope();
        visitChildren(ctx);
        symbolTable.popScope();
        return null;
    }

    // ── program ───────────────────────────────────────────────────────────────

    @Override
    public Void visitProgram(ExprParser.ProgramContext ctx) {
        visitChildren(ctx);
        emit("hlt");
        return null;
    }

    // ── varDeclaration ────────────────────────────────────────────────────────

    @Override
    public Void visitVarDeclaration(ExprParser.VarDeclarationContext ctx) {
        int addr = symbolTable.getAddress(ctx.ID().getText());

        emit("push $" + addr);

        if (ctx.expr() != null) {
            visit(ctx.expr());
        } else {
            emit("push 0");
        }

        emit("sto");
        return null;
    }

    // ── assignment ────────────────────────────────────────────────────────────

    @Override
    public Void visitAssignment(ExprParser.AssignmentContext ctx) {
        int addr = symbolTable.getAddress(ctx.ID().getText());

        emit("push $" + addr);
        visit(ctx.expr());
        emit("sto");
        return null;
    }

    // ── ifStatement ───────────────────────────────────────────────────────────

    @Override
    public Void visitIfStatement(ExprParser.IfStatementContext ctx) {
        String elseLabel = newLabel();
        String endLabel  = newLabel();

        visit(ctx.condition());
        emit("fjp " + elseLabel);

        visit(ctx.block(0));

        if (ctx.ELSE() != null) {
            emit("ujp " + endLabel);
            emit(elseLabel + ":");
            visit(ctx.block(1));
            emit(endLabel + ":");
        } else {
            emit(elseLabel + ":");
        }

        return null;
    }

    // ── whileStatement ────────────────────────────────────────────────────────

    @Override
    public Void visitWhileStatement(ExprParser.WhileStatementContext ctx) {
        String loopLabel = newLabel();
        String endLabel  = newLabel();

        emit(loopLabel + ":");
        visit(ctx.condition());
        emit("fjp " + endLabel);
        visit(ctx.block());
        emit("ujp " + loopLabel);
        emit(endLabel + ":");
        return null;
    }

    // ── printStatement ────────────────────────────────────────────────────────

    @Override
    public Void visitPrintStatement(ExprParser.PrintStatementContext ctx) {
        if (ctx.STRING() != null) {
            emit("push " + ctx.STRING().getText());
        } else {
            visit(ctx.expr());
        }

        emit("out");
        return null;
    }

    // ── inputStatement ────────────────────────────────────────────────────────

    @Override
    public Void visitInputStatement(ExprParser.InputStatementContext ctx) {
        int addr = symbolTable.getAddress(ctx.ID().getText());

        emit("push $" + addr);
        emit("in number");
        emit("sto");
        return null;
    }

    // ── expressões ────────────────────────────────────────────────────────────

    @Override
    public Void visitPowerExpr(ExprParser.PowerExprContext ctx) {
        int tResult = symbolTable.allocateTemp();
        int tExp    = symbolTable.allocateTemp();

        String loopLabel = newLabel();
        String endLabel  = newLabel();

        emit("push $" + tResult);
        emit("push 1");
        emit("sto");

        emit("push $" + tExp);
        visit(ctx.expr(1));
        emit("sto");

        emit(loopLabel + ":");
        emit("push $" + tExp);
        emit("lod");
        emit("push 0");
        emit("grt");          // ← sempre grt, exp nunca é negativo em runtime
        emit("fjp " + endLabel);  // ← fjp, não tjp (salta quando FALSO)

        emit("push $" + tResult);
        emit("push $" + tResult);
        emit("lod");
        visit(ctx.expr(0));
        emit("mul");
        emit("sto");

        emit("push $" + tExp);
        emit("push $" + tExp);
        emit("lod");
        emit("push 1");
        emit("sub");
        emit("sto");

        emit("ujp " + loopLabel);
        emit(endLabel + ":");

        emit("push $" + tResult);
        emit("lod");

        return null;
    }

    @Override
    public Void visitMulDivExpr(ExprParser.MulDivExprContext ctx) {
        visit(ctx.expr(0));
        visit(ctx.expr(1));

        if (ctx.MUL() != null) emit("mul");
        else                    emit("div");
        return null;
    }

    @Override
    public Void visitAddSubExpr(ExprParser.AddSubExprContext ctx) {
        visit(ctx.expr(0));
        visit(ctx.expr(1));

        if (ctx.ADD() != null) emit("add");
        else                    emit("sub");
        return null;
    }

    @Override
    public Void visitAtomExpr(ExprParser.AtomExprContext ctx) {
        return visit(ctx.atom());
    }

    @Override
    public Void visitAtom(ExprParser.AtomContext ctx) {
        if (ctx.NUMBER() != null) {
            emit("push " + ctx.NUMBER().getText());

        } else if (ctx.ID() != null && ctx.LPAREN() == null) {
            // variável standalone (não é 'input(ID)')
            int addr = symbolTable.getAddress(ctx.ID().getText());
            emit("push $" + addr);
            emit("lod");

        } else if (ctx.expr() != null) {
            // expressão entre parênteses
            visit(ctx.expr());

        } else {
            // 'input' LPAREN ID RPAREN dentro de expressão
            String name = ctx.ID().getText();
            int addr = symbolTable.getAddress(name);
            emit("push $" + addr);
            emit("in number");
            emit("sto");
            emit("push $" + addr);
            emit("lod");
        }

        return null;
    }

    // ── condições ─────────────────────────────────────────────────────────────

    @Override
    public Void visitNotCond(ExprParser.NotCondContext ctx) {
        visit(ctx.condition());
        emit("not");
        return null;
    }

    @Override
    public Void visitAndCond(ExprParser.AndCondContext ctx) {
        visit(ctx.condition(0));
        visit(ctx.condition(1));
        emit("and");
        return null;
    }

    @Override
    public Void visitOrCond(ExprParser.OrCondContext ctx) {
        visit(ctx.condition(0));
        visit(ctx.condition(1));
        emit("or");
        return null;
    }

    @Override
    public Void visitComparisonCond(ExprParser.ComparisonCondContext ctx) {
        visit(ctx.expr(0));  // Y (segundo da pilha)
        visit(ctx.expr(1));  // X (topo)

        // P-Code: let = Y < X, grt = Y > X, lte = Y <= X, gte = Y >= X
        if      (ctx.LT() != null) emit("let");
        else if (ctx.GT() != null) emit("grt");
        else if (ctx.EQ() != null) emit("equ");
        else if (ctx.LE() != null) emit("lte");
        else if (ctx.GE() != null) emit("gte");

        return null;
    }

    @Override
    public Void visitTrueCond(ExprParser.TrueCondContext ctx) {
        emit("push true");
        return null;
    }

    @Override
    public Void visitFalseCond(ExprParser.FalseCondContext ctx) {
        emit("push false");
        return null;
    }

    @Override
    public Void visitParenCond(ExprParser.ParenCondContext ctx) {
        return visit(ctx.condition());
    }
}
