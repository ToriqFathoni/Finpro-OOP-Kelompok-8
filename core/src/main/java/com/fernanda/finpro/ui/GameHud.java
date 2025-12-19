package com.fernanda.finpro.ui;

import com. badlogic.gdx. Gdx;
import com.badlogic.gdx.graphics. Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx. graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.fernanda.finpro.components.StatsListener;
import com.fernanda.finpro.entities.Boss;
import com.fernanda.finpro.entities.Player;

public class GameHud implements StatsListener {
    private ShapeRenderer shapeRenderer;
    private Viewport hudViewport;
    private OrthographicCamera hudCamera;
    private BitmapFont font;

    private float currentHp, maxHp;
    private float currentStamina, maxStamina;

    private Boss activeBoss;

    private static final float BAR_WIDTH = 120f;
    private static final float BAR_HEIGHT = 12f;
    private static final float MARGIN_LEFT = 10f;
    private static final float MARGIN_TOP = 10f;

    private static final float BOSS_BAR_WIDTH = 200f; 
    private static final float BOSS_BAR_HEIGHT = 16f;
    private static final float BOSS_BAR_TOP_MARGIN = 20f;

    public GameHud() {
        this.shapeRenderer = new ShapeRenderer();
        this.font = new BitmapFont();
        this.font.getData().setScale(0.6f);
        hudCamera = new OrthographicCamera();
        hudViewport = new FitViewport(480, 270, hudCamera);

        this.currentHp = 1;
        this.maxHp = 1;
        this.currentStamina = 1;
        this.maxStamina = 1;
    }

    public void render(SpriteBatch batch, Player player) {
        hudViewport.apply();
        shapeRenderer.setProjectionMatrix(hudCamera.combined);

        shapeRenderer.begin(ShapeRenderer.ShapeType. Filled);

        float hpBarX = MARGIN_LEFT;
        float hpBarY = hudViewport.getWorldHeight() - MARGIN_TOP - BAR_HEIGHT;
        float stBarX = MARGIN_LEFT;
        float stBarY = hpBarY - BAR_HEIGHT - 6;

        shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f);
        shapeRenderer. rect(hpBarX, hpBarY, BAR_WIDTH, BAR_HEIGHT);

        float hpPercentage = currentHp / maxHp;
        if (hpPercentage < 0) hpPercentage = 0;
        shapeRenderer.setColor(Color. SCARLET);
        shapeRenderer. rect(hpBarX, hpBarY, BAR_WIDTH * hpPercentage, BAR_HEIGHT);

        shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f);
        shapeRenderer. rect(stBarX, stBarY, BAR_WIDTH, BAR_HEIGHT);

        float stPercentage = currentStamina / maxStamina;
        if (stPercentage < 0) stPercentage = 0;
        shapeRenderer.setColor(Color.GOLD);
        shapeRenderer.rect(stBarX, stBarY, BAR_WIDTH * stPercentage, BAR_HEIGHT);

        // --- RENDER BOSS HP BAR (Jika Boss Aktif) ---
        if (activeBoss != null && !activeBoss.isDead()) {
            float bossHpPercent = (float) activeBoss.getCurrentHealth() / activeBoss.getMaxHealth();

            // Kembalikan ke tengah layar
            float bossBarX = (hudViewport.getWorldWidth() - BOSS_BAR_WIDTH) / 2; 
            float bossBarY = hudViewport.getWorldHeight() - BOSS_BAR_TOP_MARGIN - BOSS_BAR_HEIGHT;

            shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f);
            shapeRenderer.rect(bossBarX, bossBarY, BOSS_BAR_WIDTH, BOSS_BAR_HEIGHT);

            shapeRenderer.setColor(Color.PURPLE); 
            shapeRenderer.rect(bossBarX, bossBarY, BOSS_BAR_WIDTH * bossHpPercent, BOSS_BAR_HEIGHT);
        }

        shapeRenderer.end();

        Gdx.gl.glLineWidth(2);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(hpBarX, hpBarY, BAR_WIDTH, BAR_HEIGHT);
        shapeRenderer.rect(stBarX, stBarY, BAR_WIDTH, BAR_HEIGHT);
        
        // Outline Boss HP Bar
        if (activeBoss != null && !activeBoss.isDead()) {
            float bossBarX = (hudViewport.getWorldWidth() - BOSS_BAR_WIDTH) / 2;
            float bossBarY = hudViewport.getWorldHeight() - BOSS_BAR_TOP_MARGIN - BOSS_BAR_HEIGHT;
            shapeRenderer.setColor(Color.WHITE);
            shapeRenderer.rect(bossBarX, bossBarY, BOSS_BAR_WIDTH, BOSS_BAR_HEIGHT);
        }
        
        shapeRenderer.end();
        Gdx.gl.glLineWidth(1);
        
        // Render Damage Boost Timer
        if (player != null && player.hasDamageBoost()) {
            batch.setProjectionMatrix(hudCamera.combined);
            batch.begin();
            
            float buffTimer = player.getDamageBoostTimer();
            int minutes = (int)(buffTimer / 60);
            int seconds = (int)(buffTimer % 60);
            String timerText = String.format("Damage Boost: %02d:%02d", minutes, seconds);
            
            float buffX = MARGIN_LEFT;
            float buffY = hudViewport.getWorldHeight() - MARGIN_TOP - (BAR_HEIGHT * 2) - 18;
            
            font.setColor(Color.ORANGE);
            font.getData().setScale(0.5f);
            font.draw(batch, timerText, buffX, buffY);
            font.getData().setScale(0.6f);
            
            batch.end();
        }
    }

    public void setBoss(Boss boss) {
        this.activeBoss = boss;
    }

    public void resize(int width, int height) {
        hudViewport.update(width, height, true);
    }

    public void dispose() {
        shapeRenderer.dispose();
        font.dispose();
    }

    @Override
    public void onHealthChanged(float currentHp, float maxHp) {
        this.currentHp = currentHp;
        this. maxHp = maxHp;
    }

    @Override
    public void onStaminaChanged(float currentStamina, float maxStamina) {
        this.currentStamina = currentStamina;
        this. maxStamina = maxStamina;
    }

    @Override
    public void onDead() { }
}
