package com.fernanda.finpro.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math. Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.fernanda.finpro.components.ItemType;
import com.fernanda.finpro.singleton.GameAssetManager;

public class GroundItem {
    private final ItemType type;
    private final Vector2 position;
    private final Rectangle hitbox;
    private boolean active;

    private static final float ITEM_DISPLAY_SIZE = 16f;
    private static final float PICKUP_RADIUS = 25f;

    private float bobTimer = 0f;
    private static final float BOB_SPEED = 2f;
    private static final float BOB_HEIGHT = 4f;

    public GroundItem(ItemType type, float x, float y) {
        this.type = type;
        this.position = new Vector2(x, y);
        this.hitbox = new Rectangle(
            x - PICKUP_RADIUS / 2,
            y - PICKUP_RADIUS / 2,
            PICKUP_RADIUS,
            PICKUP_RADIUS
        );
        this.active = true;
    }

    public void update(float dt) {
        if (!active) return;
        bobTimer += dt;
    }

    public void render(SpriteBatch batch) {
        if (!active) return;

        Texture texture = getTextureForItem();
        if (texture == null) return;

        float offsetY = (float) Math.sin(bobTimer * BOB_SPEED) * BOB_HEIGHT;
        float drawX = position.x - (ITEM_DISPLAY_SIZE / 2);
        float drawY = position.y - (ITEM_DISPLAY_SIZE / 2) + offsetY;

        batch.draw(texture, drawX, drawY, ITEM_DISPLAY_SIZE, ITEM_DISPLAY_SIZE);
    }

    private Texture getTextureForItem() {
        switch (type) {
            case ORC_SKULL:
                return GameAssetManager.getInstance().getTexture(GameAssetManager.ORC_SKULL);
            default:
                return null;
        }
    }

    public void renderDebug(ShapeRenderer sr) {
        if (!active) return;
        
        // Color-coded items for visual distinction
        switch (type) {
            case RAW_MEAT:
                sr.setColor(Color.YELLOW);
                break;
            case ORC_SKULL:
                sr.setColor(Color.LIGHT_GRAY);
                break;
            default:
                sr.setColor(Color.WHITE);
                break;
        }
        
        sr.rect(hitbox.x, hitbox.y, hitbox.width, hitbox.height);
    }

    public ItemType getType() { return type; }
    public Rectangle getHitbox() { return hitbox; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
