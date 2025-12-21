package com.fernanda.finpro.pool;

import com.fernanda.finpro.components.ItemType;
import com.fernanda.finpro.entities.GroundItem;

public class GroundItemPool extends ObjectPool<GroundItem> {

    public GroundItemPool(int initialSize, int maxSize) {
        super(initialSize, maxSize);
    }

    @Override
    protected GroundItem create() {
        return new GroundItem(ItemType.ORC_MEAT, 0, 0);
    }

    @Override
    protected void reset(GroundItem item) {
    }

    public GroundItem obtain(ItemType type, float x, float y) {
        GroundItem item = obtain();
        if (item != null) {
            item.reset(type, x, y);
        }
        return item;
    }
}
