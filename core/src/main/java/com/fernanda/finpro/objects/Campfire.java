package com.fernanda.finpro.objects;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.fernanda.finpro.entities.Player;
import com.fernanda.finpro.singleton.GameAssetManager;

public class Campfire {
    private Vector2 position;
    private static final float SIZE = 40f;
    private static final float INTERACTION_RANGE = 60f;
    private Texture texture;

    public Campfire(float x, float y) {
        this.position = new Vector2(x, y);
        this.texture = GameAssetManager.getInstance().getTexture(GameAssetManager.CAMPFIRE);
    }

    public boolean isPlayerNearby(Player player) {
        float distance = Vector2.dst(
            player.position.x + player.getWidth() / 2,
            player.position.y + player.getHeight() / 2,
            position.x + SIZE / 2,
            position.y + SIZE / 2
        );
        return distance < INTERACTION_RANGE;
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture, position.x, position.y, SIZE, SIZE);
    }

    public Vector2 getPosition() {
        return position;
    }

    public float getSize() {
        return SIZE;
    }
}
