package com.fernanda.finpro.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic. gdx.graphics.Color;
import com.badlogic.gdx.graphics. Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com. badlogic.gdx.graphics.g2d.SpriteBatch;
import com. badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic. gdx.math.Rectangle;
import com.fernanda.finpro.components. ItemType;
import com.fernanda.finpro.singleton.GameAssetManager;

public class InventorySlot {
    private ItemType itemType;
    private int count;
    private Rectangle bounds;

    private static final float SLOT_SIZE = 32f;    // Sesuaikan dengan InventoryUI
    private static final float ICON_SIZE = 24f;
    private static final float ICON_OFFSET = (SLOT_SIZE - ICON_SIZE) / 2f;

    public InventorySlot(float x, float y) {
        this.bounds = new Rectangle(x, y, SLOT_SIZE, SLOT_SIZE);
        this.itemType = null;
        this.count = 0;
    }

    public void render(SpriteBatch batch, ShapeRenderer sr, BitmapFont font, boolean isSelected) {
        // 1. Slot background
        sr.setProjectionMatrix(batch.getProjectionMatrix());
        sr.begin(ShapeRenderer.ShapeType. Filled);

        if (isEmpty()) {
            sr.setColor(0.76f, 0.6f, 0.42f, 1f); // Tan
        } else {
            sr. setColor(0.82f, 0.66f, 0.48f, 1f); // Lighter tan
        }
        sr. rect(bounds.x, bounds. y, bounds.width, bounds. height);
        sr.end();

        // 2. Border
        sr.begin(ShapeRenderer.ShapeType.Line);

        if (isSelected) {
            sr.setColor(1f, 0.84f, 0f, 1f); // Gold
            Gdx.gl.glLineWidth(3);
        } else {
            sr.setColor(1f, 1f, 1f, 0.6f); // White
            Gdx.gl.glLineWidth(1.5f);
        }

        sr.rect(bounds.x, bounds.y, bounds.width, bounds.height);
        sr.end();
        Gdx.gl.glLineWidth(1);

        // 3. Item icon
        if (! isEmpty()) {
            batch.begin();

            Texture icon = getIconTexture();
            if (icon != null) {
                batch.draw(icon,
                    bounds.x + ICON_OFFSET,
                    bounds.y + ICON_OFFSET,
                    ICON_SIZE,
                    ICON_SIZE
                );
            }

            // 4. Count
            if (count > 1) {
                font.setColor(Color.WHITE);
                font.getData().setScale(0.45f);
                font.draw(batch, "" + count,
                    bounds.x + SLOT_SIZE - 11,
                    bounds.y + 9
                );
                font.getData().setScale(0.6f);
            }

            batch.end();
        }
    }

    private Texture getIconTexture() {
        if (itemType == null) return null;

        switch (itemType) {
            case ORC_SKULL:
                return GameAssetManager.getInstance().getTexture(GameAssetManager.ORC_SKULL);
            default:
                return null;
        }
    }

    public void addItem(ItemType type, int amount) {
        if (isEmpty()) {
            this.itemType = type;
            this.count = amount;
        } else if (this.itemType == type) {
            this.count += amount;
        }
    }

    public boolean removeItem(int amount) {
        if (count >= amount) {
            count -= amount;
            if (count <= 0) {
                clear();
            }
            return true;
        }
        return false;
    }

    public void clear() {
        this.itemType = null;
        this. count = 0;
    }

    public boolean isEmpty() { return itemType == null || count <= 0; }
    public ItemType getItemType() { return itemType; }
    public int getCount() { return count; }
    public Rectangle getBounds() { return bounds; }
    public boolean contains(float x, float y) { return bounds.contains(x, y); }
}
