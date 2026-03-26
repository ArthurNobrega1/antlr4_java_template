package com.uepb.compiler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import com.uepb.ExprLexer;
import com.uepb.ExprParser;
import com.uepb.gui.GuiVizualizerTask;
import com.uepb.interfaces.CompilerEngine;

public class Antlr4BasicExample implements CompilerEngine {

    @Override
    public void execute(File input, File output, boolean verbose) throws IOException {

        var charStream = CharStreams.fromPath(input.toPath());
        var lexer      = new ExprLexer(charStream);
        var tokens     = new CommonTokenStream(lexer);
        var parser     = new ExprParser(tokens);
        var tree       = parser.program();

        if (verbose) {
            var guiTask = new GuiVizualizerTask(parser, tree);
            guiTask.run();
        }

        var symbolTable     = new SymbolTable();
        var semanticVisitor = new SemanticVisitor(symbolTable);
        semanticVisitor.visit(tree);

        if (semanticVisitor.hasErrors()) {
            System.err.println("Compilação abortada por erros semânticos.");
            return;
        }

        try (var writer = new PrintWriter(new FileWriter(output))) {
            var codeGen = new CodeGeneratorVisitor(symbolTable, writer);
            codeGen.visit(tree);
        }

        System.out.println("Compilação concluída: " + output.getPath());
    }
}
