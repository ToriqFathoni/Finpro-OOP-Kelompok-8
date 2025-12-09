package com.fernanda.finpro.entities;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public abstract class Monster {

    public enum State {
        IDLE, WANDER, CHASE, PREPARE_ATTACK, ATTACKING, COOLDOWN, HURT, DEAD
    }

    // --- PHYSICS ---
    public Vector2 position;
    public Vector2 velocity;
    public Rectangle bodyRect;
    public Rectangle attackRect;

    // --- STATS ---
    protected float speed;
    protected float detectionRadius;
    protected float attackRadius;
    protected int maxHealth;
    protected int currentHealth;
    protected int attackDamage;

    // --- ZONA HABITAT (SCALABILITY) ---
    // Batas wilayah dimana monster boleh hidup
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

        // --- LOGIKA BATAS WILAYAH (SCALABLE) ---
        // Monster menjaga dirinya sendiri agar tidak keluar zona
        enforceZoneBoundaries();

        // Update posisi hitbox mengikuti body
        bodyRect.setPosition(position.x, position.y);
    }

    protected void enforceZoneBoundaries() {
        // Clamp to Map Bounds (0 to 7040)
        if (position.x < 0) position.x = 0;
        if (position.x > 7040 - bodyRect.width) position.x = 7040 - bodyRect.width;
        if (position.y < 0) position.y = 0;
        if (position.y > 7040 - bodyRect.height) position.y = 7040 - bodyRect.height;
    }

    // Getters
    public Rectangle getBodyHitbox() { return bodyRect; }
    public Rectangle getAttackHitbox() { return attackRect; }
    public boolean isDead() { return isDead; }
    public boolean canBeRemoved() { return isDead && stateTimer > deathDuration; }
    public int getDamage() { return attackDamage; }

    // Abstract
    public abstract void aiBehavior(float dt, Player player);
    public abstract void render(com.badlogic.gdx.graphics.g2d.SpriteBatch batch);
    public abstract void renderDebug(ShapeRenderer shapeRenderer);
}
