package com.uepb.compiler;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class SymbolTable {

    // Endereços persistentes — nunca removidos. Usado pelo CodeGeneratorVisitor.
    private final Map<String, Integer> registry = new HashMap<>();

    // Pilha de escopos — só para checagem semântica de visibilidade.
    private final Deque<Map<String, Integer>> scopes = new ArrayDeque<>();

    private int nextAddress = 0;

    public SymbolTable() {
        scopes.push(new HashMap<>());
    }

    public void pushScope() {
        scopes.push(new HashMap<>());
    }

    public void popScope() {
        if (scopes.size() > 1) {
            scopes.pop();
            // Não remove do registry — endereços continuam acessíveis.
        }
    }

    public void declare(String name) {
        int addr = nextAddress++;
        scopes.peek().put(name, addr);
        registry.put(name, addr);
    }

    public int allocateTemp() {
        return nextAddress++;
    }

    /** Verifica redeclaração só no escopo atual. */
    public boolean isDeclaredInCurrentScope(String name) {
        return scopes.peek().containsKey(name);
    }

    /** Verifica visibilidade em qualquer escopo ativo. */
    public boolean isDeclared(String name) {
        for (var scope : scopes) {
            if (scope.containsKey(name)) return true;
        }
        return false;
    }

    /** Busca no registry persistente — sempre funciona independente do escopo. */
    public int getAddress(String name) {
        Integer addr = registry.get(name);
        if (addr == null) throw new RuntimeException("Variável não declarada: " + name);
        return addr;
    }
}
