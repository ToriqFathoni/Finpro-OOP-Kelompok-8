package com.fernanda.finpro.pool;

import java.util.ArrayList;
import java.util.List;

/**
 * Object Pool Pattern - Generic Implementation
 * Reuses objects instead of creating new ones to improve performance
 * and reduce garbage collection overhead.
 */
public abstract class ObjectPool<T> {
    private List<T> available;
    private List<T> inUse;
    private int maxSize;

    public ObjectPool(int initialSize, int maxSize) {
        this.available = new ArrayList<>();
        this.inUse = new ArrayList<>();
        this.maxSize = maxSize;

        // Pre-create initial objects
        for (int i = 0; i < initialSize; i++) {
            available.add(create());
        }
    }

    /**
     * Subclass must implement this to create new instances
     */
    protected abstract T create();

    /**
     * Subclass can override this to reset object state before reuse
     */
    protected abstract void reset(T object);

    /**
     * Get an object from the pool
     */
    public T obtain() {
        T object;
        
        if (!available.isEmpty()) {
            // Reuse from pool
            object = available.remove(available.size() - 1);
        } else if (inUse.size() < maxSize) {
            // Create new if under limit
            object = create();
        } else {
            // Pool exhausted
            System.err.println("Pool exhausted! Max size: " + maxSize);
            return null;
        }

        inUse.add(object);
        reset(object);
        return object;
    }

    /**
     * Return an object to the pool
     */
    public void free(T object) {
        if (inUse.remove(object)) {
            available.add(object);
        }
    }

    /**
     * Get pool statistics
     */
    public int getAvailableCount() {
        return available.size();
    }

    public int getInUseCount() {
        return inUse.size();
    }

    public int getTotalCount() {
        return available.size() + inUse.size();
    }

    /**
     * Clear all objects
     */
    public void clear() {
        available.clear();
        inUse.clear();
    }
}
