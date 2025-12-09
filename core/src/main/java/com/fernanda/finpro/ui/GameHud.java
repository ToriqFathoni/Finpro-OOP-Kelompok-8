package com.fernanda.finpro.ui;

import com. badlogic.gdx. Gdx;
import com.badlogic.gdx.graphics. Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx. graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com. badlogic.gdx.utils.viewport.Viewport;
import com.fernanda.finpro.components.StatsListener;

public class GameHud implements StatsListener {
    private ShapeRenderer shapeRenderer;
    private Viewport hudViewport;
    private OrthographicCamera hudCamera;

    private float currentHp, maxHp;
    private float currentStamina, maxStamina;

    private static final float BAR_WIDTH = 120f;
    private static final float BAR_HEIGHT = 12f;
    private static final float MARGIN_LEFT = 10f;
    private static final float MARGIN_TOP = 10f;

    public GameHud() {
        this.shapeRenderer = new ShapeRenderer();
        hudCamera = new OrthographicCamera();
        hudViewport = new FitViewport(480, 270, hudCamera);

        this.currentHp = 1;
        this.maxHp = 1;
        this.currentStamina = 1;
        this.maxStamina = 1;
    }

    public void render() {
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

        shapeRenderer.end();

        Gdx.gl.glLineWidth(2);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(hpBarX, hpBarY, BAR_WIDTH, BAR_HEIGHT);
        shapeRenderer.rect(stBarX, stBarY, BAR_WIDTH, BAR_HEIGHT);
        shapeRenderer.end();
        Gdx.gl.glLineWidth(1);
    }

    public void resize(int width, int height) {
        hudViewport.update(width, height, true);
    }

    public void dispose() {
        shapeRenderer.dispose();
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
