package com.uepb.compiler;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class SymbolTable {

    private final Deque<Map<String, Integer>> scopes = new ArrayDeque<>();
    private int nextAddress = 0;

    public SymbolTable() {
        pushScope(); // escopo global
    }

    public void pushScope() {
        scopes.push(new HashMap<>());
    }

    public void popScope() {
        scopes.pop();
    }

    /** Declara no escopo atual (topo da pilha) */
    public void declare(String name) {
        scopes.peek().put(name, nextAddress++);
    }

    /** Reserva um endereço anônimo para temporários do compilador. */
    public int allocateTemp() {
        return nextAddress++;
    }

    /** Verifica só no escopo atual — impede redeclaração no mesmo bloco */
    public boolean isDeclaredInCurrentScope(String name) {
        return scopes.peek().containsKey(name);
    }

    /** Busca em todos os escopos, do mais interno ao mais externo */
    public boolean isDeclared(String name) {
        for (var scope : scopes) {
            if (scope.containsKey(name)) return true;
        }
        return false;
    }

    public int getAddress(String name) {
        for (var scope : scopes) {
            if (scope.containsKey(name)) return scope.get(name);
        }
        throw new RuntimeException("Variável não declarada: " + name);
    }
}