package com.fernanda.finpro.pool;

import java.util.ArrayList;
import java.util.List;

public abstract class ObjectPool<T> {
    private List<T> available;
    private List<T> inUse;
    private int maxSize;

    public ObjectPool(int initialSize, int maxSize) {
        this.available = new ArrayList<>();
        this.inUse = new ArrayList<>();
        this.maxSize = maxSize;

        for (int i = 0; i < initialSize; i++) {
            available.add(create());
        }
    }

    protected abstract T create();

    protected abstract void reset(T object);

    public T obtain() {
        T object;
        
        if (!available.isEmpty()) {
            object = available.remove(available.size() - 1);
        } else if (inUse.size() < maxSize) {
            object = create();
        } else {
            System.err.println("Pool exhausted! Max size: " + maxSize);
            return null;
        }

        inUse.add(object);
        reset(object);
        return object;
    }

    public void free(T object) {
        if (inUse.remove(object)) {
            available.add(object);
        }
    }

    public int getAvailableCount() {
        return available.size();
    }

    public int getInUseCount() {
        return inUse.size();
    }

    public int getTotalCount() {
        return available.size() + inUse.size();
    }

    public void clear() {
        available.clear();
        inUse.clear();
    }
}
