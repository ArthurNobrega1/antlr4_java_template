package com.uepb.compiler;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayDeque;
import java.util.Deque;

public class SymbolTable {

    private final Map<String, Integer> table = new HashMap<>();
    private final Deque<Integer> markers = new ArrayDeque<>();
    private int nextAddress = 0;

    public SymbolTable() {
        markers.push(0);
    }

    public void pushScope() {
        markers.push(nextAddress);
    }

    public void popScope() {
        if (markers.size() > 1) {
            nextAddress = markers.pop();
        }
    }

    public int allocateTemp() {
        return nextAddress++;
    }

    public void declare(String name) {
        table.put(name, nextAddress++);
    }

    public boolean isDeclared(String name) {
        return table.containsKey(name);
    }

    public int getAddress(String name) {
        return table.get(name);
    }
}