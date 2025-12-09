package com.fernanda.finpro.objects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.fernanda.finpro.entities.Player;

public class SignBoard {
    private Vector2 position;
    private String message;
    private static final float SIZE = 30f;
    private static final float READ_RANGE = 50f;

    private BitmapFont font;
    private boolean playerNearby = false;

    public SignBoard(float x, float y, String message) {
        this.position = new Vector2(x, y);
        this.message = message;
        this.font = new BitmapFont();
        this.font.getData().setScale(1.0f);
        this.font.setColor(Color.WHITE);
    }

    public void update(Player player) {
        float distance = Vector2.dst(
            player.position.x + player.getWidth() / 2,
            player.position.y + player.getHeight() / 2,
            position.x + SIZE / 2,
            position.y + SIZE / 2
        );

        playerNearby = distance < READ_RANGE;
    }

    public void render(ShapeRenderer sr) {
        sr.setColor(new Color(0.6f, 0.4f, 0.2f, 1f)); // Brown
        sr.rect(position.x, position.y, SIZE, SIZE);
    }

    public void renderText(SpriteBatch batch) {
        if (playerNearby) {
            // Draw message text above the sign
            font.draw(batch, message, position.x - 50, position.y + SIZE + 30);
        }
    }

    public Vector2 getPosition() {
        return position;
    }

    public void dispose() {
        font.dispose();
    }
}
