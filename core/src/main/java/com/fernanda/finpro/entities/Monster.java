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
        float distFromCenter = position.len();

        // Jika masuk terlalu dalam (misal masuk ke Ice)
        if (distFromCenter < zoneMinRadius) {
            position.setLength(zoneMinRadius + 5f); // Dorong keluar
            velocity.set(0,0);
        }
        // Jika keluar terlalu jauh (keluar peta)
        else if (distFromCenter > zoneMaxRadius) {
            position.setLength(zoneMaxRadius - 5f); // Dorong masuk
            velocity.set(0,0);
        }
    }

    // Getters
    public Rectangle getBodyHitbox() { return bodyRect; }
    public Rectangle getAttackHitbox() { return attackRect; }
    public boolean isDead() { return isDead; }
    public int getDamage() { return attackDamage; }

    // Abstract
    public abstract void aiBehavior(float dt, Player player);
    public abstract void renderDebug(ShapeRenderer shapeRenderer);
}
