package ru.demonorium.timeline.impl;

import java.util.TreeMap;

public class IntTreeMap<T> extends TreeMap<Integer, T> {
    private final IntTreeMap<IntTreeMap<T>> parent;
    private int count = 0;

    public IntTreeMap(IntTreeMap<IntTreeMap<T>> parent) {
        this.parent = parent;
    }

    public void inc() {
        ++count;
    }

    public void dec() {
        --count;
    }

    @Override
    public T put(Integer key, T value) {
        inc();
        return super.put(key, value);
    }
}
