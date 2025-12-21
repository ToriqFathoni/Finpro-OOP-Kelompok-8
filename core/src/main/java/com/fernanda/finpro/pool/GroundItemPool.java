package com.fernanda.finpro.pool;

import com.fernanda.finpro.components.ItemType;
import com.fernanda.finpro.entities.GroundItem;

/**
 * Object Pool for GroundItem
 * Reduces garbage collection by reusing dropped items
 */
public class GroundItemPool extends ObjectPool<GroundItem> {

    public GroundItemPool(int initialSize, int maxSize) {
        super(initialSize, maxSize);
    }

    @Override
    protected GroundItem create() {
        // Create with dummy values, will be reset when obtained
        return new GroundItem(ItemType.ORC_MEAT, 0, 0);
    }

    @Override
    protected void reset(GroundItem item) {
        // Reset is handled by GroundItem.reset() method
        // No additional reset needed here
    }

    /**
     * Obtain an item with specific type and position
     */
    public GroundItem obtain(ItemType type, float x, float y) {
        GroundItem item = obtain();
        if (item != null) {
            item.reset(type, x, y);
        }
        return item;
    }
}
