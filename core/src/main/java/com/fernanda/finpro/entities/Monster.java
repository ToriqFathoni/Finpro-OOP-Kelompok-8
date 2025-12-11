package com.fernanda.finpro.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.fernanda.finpro.singleton.GameAssetManager;

import java.util.ArrayList;
import java.util.List;

public abstract class Monster {

    public enum State {
        IDLE, WANDER, CHASE, PREPARE_ATTACK, ATTACKING, COOLDOWN, HURT, DEAD
    }

    // --- PHYSICS ---
    public Vector2 position;
    public Vector2 spawnPosition; // Posisi awal spawn
    public Vector2 velocity;
    public Rectangle bodyRect;
    public Rectangle attackRect;

    // --- STATS ---
    protected float speed;
    protected float detectionRadius;
    protected float wanderRadius = 64f; // Radius patroli (4 tiles)
    protected float attackRadius;
    protected int maxHealth;
    protected int currentHealth;
    protected int attackDamage;
    protected float knockbackDistance;

    // --- ZONA HABITAT ---
    protected float zoneMinRadius;
    protected float zoneMaxRadius;

    // --- TIMERS ---
    protected float stateTimer;
    protected float immunityTimer;
    protected boolean isDead;
    protected boolean facingRight;
    protected float deathDuration = 1.0f; // Default durasi animasi mati

    protected State currentState;

    // Constructor menerima zoneMin dan zoneMax
    public Monster(float x, float y, float speed, int maxHp, int damage, float width, float height, float zoneMin, float zoneMax) {
        this.position = new Vector2(x, y);
        this.spawnPosition = new Vector2(x, y); // Simpan posisi spawn
        this.velocity = new Vector2(0, 0);
        this.speed = speed;
        this.maxHealth = maxHp;
        this.currentHealth = maxHp;
        this.attackDamage = damage;

        // Set Zona Habitat
        this.zoneMinRadius = zoneMin;
        this.zoneMaxRadius = zoneMax;

        this.bodyRect = new Rectangle(x, y, width, height);
        this.attackRect = new Rectangle(0, 0, 0, 0);

        this.currentState = State.WANDER;
        this.stateTimer = 0;
        this.isDead = false;
        this.facingRight = true;
    }

    protected Animation<TextureRegion> createAnimation(String assetName, int cols, float frameDuration, Animation.PlayMode mode) {
        Texture texture = GameAssetManager.getInstance().getTexture(assetName);

        int totalWidth = texture.getWidth();
        int totalHeight = texture.getHeight();

        int frameWidth = totalWidth / cols;
        int rows = 1;

        // Deteksi otomatis jika sprite sheet memiliki lebih dari 1 baris
        if (totalHeight > frameWidth * 1.5f) {
            rows = Math.round((float) totalHeight / frameWidth);
            if (rows < 1) rows = 1;
        }
        int frameHeight = totalHeight / rows;

        TextureRegion[][] tmp = TextureRegion.split(texture, frameWidth, frameHeight);

        // Ratakan Array 2D menjadi 1D List agar aman
        List<TextureRegion> framesList = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (i < tmp.length && j < tmp[i].length) {
                    framesList.add(tmp[i][j]);
                }
            }
        }

        TextureRegion[] frames = framesList.toArray(new TextureRegion[0]);

        Animation<TextureRegion> anim = new Animation<>(frameDuration, frames);
        anim.setPlayMode(mode);
        return anim;
    }

    public void takeDamage(int amount) {
        if (isDead || immunityTimer > 0) return;

        currentHealth -= amount;
        immunityTimer = 0.4f; // Jeda kebal visual

        if (currentHealth <= 0) {
            isDead = true;
            currentState = State.DEAD;
        } else {
            currentState = State.HURT;
            stateTimer = 0;
        }
    }

    public void update(float dt) {
        if (immunityTimer > 0) immunityTimer -= dt;
        stateTimer += dt;

        enforceZoneBoundaries();

        bodyRect.setPosition(position.x, position.y);
    }

    protected void enforceZoneBoundaries() {
        if (position.x < 0) position.x = 0;
        if (position.x > 1168 - bodyRect.width) position.x = 1168 - bodyRect.width;
        if (position.y < 0) position.y = 0;
        if (position.y > 1168 - bodyRect.height) position.y = 1168 - bodyRect.height;
    }

    protected void moveTowards(Vector2 target) {
        // Arah ideal (Garis lurus ke player)
        Vector2 direction = new Vector2(target).sub(position).nor();

        // Jarak sensor (cek 32 pixel ke depan)
        float checkDist = 32f;

        // Titik tengah badan monster
        float centerX = position.x + (bodyRect.width / 2);
        float centerY = position.y + (bodyRect.height / 2);

        // Posisi ujung sensor
        float feelerX = centerX + (direction.x * checkDist);
        float feelerY = centerY + (direction.y * checkDist);

        // Jika sensor menabrak tembok
        if (isTileBlocked(feelerX, feelerY)) {
            // Coba cari jalan ke KANAN (serong 45 derajat)
            Vector2 rightDir = new Vector2(direction).rotateDeg(-45);
            if (!isTileBlocked(centerX + rightDir.x * checkDist, centerY + rightDir.y * checkDist)) {
                direction = rightDir;
            }
            // Coba cari jalan ke KIRI (serong 45 derajat)
            else {
                Vector2 leftDir = new Vector2(direction).rotateDeg(45);
                if (!isTileBlocked(centerX + leftDir.x * checkDist, centerY + leftDir.y * checkDist)) {
                    direction = leftDir;
                }
            }
        }

        velocity.set(direction).scl(speed);
    }

    // Method Helper untuk membaca Map (Mendeteksi Tembok)
    protected boolean isTileBlocked(float x, float y) {
        com.badlogic.gdx.maps.tiled.TiledMap map = GameAssetManager.getInstance().getMap();

        int tileX = (int) (x / 16);
        int tileY = (int) (y / 16);

        if (tileX < 0 || tileX >= 73 || tileY < 0 || tileY >= 73) return true;

        String[] collisionLayers = { "building_coklat", "building_hijau" };

        for (String layerName : collisionLayers) {
            TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(layerName);
            if (layer != null) {
                TiledMapTileLayer.Cell cell = layer.getCell(tileX, tileY);
                if (cell != null && cell.getTile() != null) {
                    return true;
                }
            }
        }
        return false;
    }

    // Getters
    public Rectangle getBodyHitbox() { return bodyRect; }
    public Rectangle getAttackHitbox() { return attackRect; }
    public boolean isDead() { return isDead; }
    public boolean canBeRemoved() { return isDead && stateTimer > deathDuration; }
    public int getDamage() { return attackDamage; }
    public float getKnockbackDistance() {
        return knockbackDistance;
    }

    // Abstract
    public abstract void aiBehavior(float dt, Player player);
    public abstract void render(com.badlogic.gdx.graphics.g2d.SpriteBatch batch);
    public abstract void renderDebug(ShapeRenderer shapeRenderer);
}
