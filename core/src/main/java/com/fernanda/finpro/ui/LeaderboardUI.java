package com.fernanda.finpro.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.fernanda.finpro.managers.NetworkManager;
import java.util.List;

public class LeaderboardUI {
    private boolean visible;
    private BitmapFont font;
    private List<NetworkManager.LeaderboardEntry> entries;
    private GlyphLayout layout;

    public LeaderboardUI() {
        this.visible = false;
        this.font = new BitmapFont();
        this.font.getData().setScale(1.5f);
        this.layout = new GlyphLayout();
    }

    public void show(List<NetworkManager.LeaderboardEntry> entries) {
        this.entries = entries;
        this.visible = true;
    }

    public void render(SpriteBatch batch, ShapeRenderer sr) {
        if (!visible) return;

        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();
        float menuW = screenW * 0.8f;
        float menuH = screenH * 0.8f;
        float menuX = (screenW - menuW) / 2;
        float menuY = (screenH - menuH) / 2;

        // Draw Background
        Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA, com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA);
        
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0, 0, 0, 0.8f);
        sr.rect(menuX, menuY, menuW, menuH);
        sr.end();
        
        Gdx.gl.glDisable(com.badlogic.gdx.graphics.GL20.GL_BLEND);

        batch.begin();
        
        // Title
        font.setColor(Color.GOLD);
        layout.setText(font, "CONGRATS! BOSS DEFEATED!");
        font.draw(batch, layout, menuX + (menuW - layout.width) / 2, menuY + menuH - 50);

        font.setColor(Color.WHITE);
        layout.setText(font, "LEADERBOARD");
        font.draw(batch, layout, menuX + (menuW - layout.width) / 2, menuY + menuH - 100);

        // Entries
        if (entries != null) {
            float y = menuY + menuH - 150;
            for (int i = 0; i < entries.size(); i++) {
                NetworkManager.LeaderboardEntry entry = entries.get(i);
                String text = (i + 1) + ". " + entry.username + " - Score: " + (entry.cookingScore + entry.monsterKillScore);
                layout.setText(font, text);
                font.draw(batch, layout, menuX + 50, y);
                y -= 40;
            }
        }

        // Close instruction
        font.setColor(Color.GRAY);
        layout.setText(font, "Press ESC to Close");
        font.draw(batch, layout, menuX + (menuW - layout.width) / 2, menuY + 50);

        batch.end();
        
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            visible = false;
        }
    }
    
    public boolean isVisible() {
        return visible;
    }
}
