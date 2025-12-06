package com.fernanda.finpro.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.fernanda.finpro.components.PlayerStats;
import com.fernanda.finpro.input.InputHandler;
import com.fernanda.finpro.states.*;

public class Player {
    // --- FISIKA & POSISI ---
    public Vector2 position;
    public Vector2 velocity;
    public boolean facingRight = true;

    // --- COMPONENT (Stats HP/Stamina) ---
    public PlayerStats stats;

    // --- CONFIG BADAN (LOGIKA) ---
    private static final int LOGICAL_WIDTH = 14;
    private static final int LOGICAL_HEIGHT = 20;

    // --- CONFIG VISUAL (GAMBAR) ---
    private static final float DRAW_OFFSET_X = 0f;
    private static final float DRAW_OFFSET_Y = 0f;

    // --- CONFIG HITBOX SERANGAN ---
    private static final float ATTACK_WIDTH = 25f;
    private static final float ATTACK_HEIGHT = 25f;
    private static final float ATTACK_OFFSET_Y = 5f;

    // Timing Hitbox Serangan
    private static final float DAMAGE_START_TIME = 0.2f;
    private static final float DAMAGE_END_TIME   = 0.4f;

    private Rectangle attackRect = new Rectangle();

    // --- COOLDOWN & TIMERS ---
    private float attackTimer = 0f;
    private final float ATTACK_COOLDOWN = 0.5f;

    // --- DODGE CONFIG ---
    private DodgeState dodgeState;
    private float dodgeTimer = 0f;          // Timer durasi berlangsungnya dash
    private float dodgeCooldownTimer = 0f;  // Timer jeda antar dash

    private final float DODGE_DURATION = 0.2f;
    private final float DODGE_SPEED = 300f;
    private final float DODGE_COST = 20f;

    // PERUBAHAN: Ubah nilai ini untuk tes (misal 1.0f atau 2.0f)
    private final float DODGE_WAIT_TIME = 0.3f; // Jeda PASTI setelah dodge selesai

    private Vector2 dodgeDirection = new Vector2();

    // --- STATE MANAGEMENT ---
    private PlayerState currentState;
    private PlayerState idleState;
    private PlayerState walkState;
    private AttackState attackState;

    private InputHandler inputHandler;
    private float stateTime;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================
    public Player(float startX, float startY) {
        this.position = new Vector2(startX, startY);
        this.velocity = new Vector2(0, 0);
        this.inputHandler = new InputHandler();

        this.idleState = new IdleState();
        this.walkState = new WalkState();
        this.attackState = new AttackState();
        this.dodgeState = new DodgeState();

        this.currentState = idleState;
        this.stateTime = 0f;

        // Inisialisasi Stats
        this.stats = new PlayerStats(50f, 100f, 1.0f, 15f);
    }

    // =========================================================
    // GAME LOOP (UPDATE)
    // =========================================================
    public void update(float dt) {
        // 1. Update Component Stats
        stats.update(dt);

        // 2. Cooldowns
        if (attackTimer > 0) attackTimer -= dt;

        // Update Cooldown Dodge
        if (dodgeCooldownTimer > 0) {
            dodgeCooldownTimer -= dt;
        }

        // 3. Input & Fisika
        inputHandler.handleInput(this);

        // --- KHUSUS DODGE MOVEMENT ---
        if (currentState == dodgeState) {
            // Paksa velocity ke arah dodge dengan kecepatan tinggi
            velocity.set(dodgeDirection).scl(DODGE_SPEED);

            // Kurangi timer durasi dash
            dodgeTimer -= dt;
            if (dodgeTimer <= 0) {
                // DODGE SELESAI
                velocity.set(0, 0);
                changeState(idleState);

                // PERUBAHAN PENTING:
                // Set cooldown timer DI SINI (saat dodge selesai), bukan saat mulai.
                // Ini memastikan ada jeda "istirahat" sebelum bisa dodge lagi.
                dodgeCooldownTimer = DODGE_WAIT_TIME;

                System.out.println("Dodge Selesai. Masuk Cooldown: " + DODGE_WAIT_TIME + " detik.");
            }
        }

        position.mulAdd(velocity, dt);

        // --- UPDATE FLIP MANUAL ---
        if (velocity.x > 0) {
            facingRight = true;
        } else if (velocity.x < 0) {
            facingRight = false;
        }

        // 4. State Machine Logic
        if (currentState == dodgeState) {
            // Tunggu logika di atas
        }
        else if (currentState == attackState) {
            if (attackState.isFinished(stateTime)) {
                changeState(idleState);
            }
        }
        else {
            if (currentState != idleState) {
                changeState(idleState);
            }
        }

        // 5. Update Timer State
        stateTime += dt;
        currentState.update(dt);
    }

    // =========================================================
    // LOGIKA ACTION (ATTACK & DODGE)
    // =========================================================

    public void dodge() {
        if (currentState == dodgeState || currentState == attackState) return;

        // DEBUG: Cek kenapa tidak bisa dodge
        if (dodgeCooldownTimer > 0) {
            // Print sisa waktu ke konsol
            System.out.println("Gagal Dodge! Sedang Cooldown. Sisa: " + String.format("%.2f", dodgeCooldownTimer));
            return;
        }

        if (stats.useStamina(DODGE_COST)) {
            System.out.println("Dodge Dimulai!"); // Debug print
            changeState(dodgeState);
            dodgeTimer = DODGE_DURATION;

            // Tentukan arah dodge
            if (velocity.len2() > 0) {
                dodgeDirection.set(velocity).nor();
            } else {
                dodgeDirection.set(facingRight ? 1 : -1, 0);
            }
        } else {
            System.out.println("Stamina tidak cukup!");
        }
    }

    public void attack() {
        if (attackTimer > 0) return;
        if (currentState == dodgeState) return;

        if (currentState != attackState) {
            performAttack();
        }
        else if (currentState == attackState && attackState.isFinished(stateTime)) {
            performAttack();
        }
    }

    private void performAttack() {
        changeState(attackState);
        attackTimer = ATTACK_COOLDOWN;
    }

    // =========================================================
    // FUNGSI LAIN (Collision, Render, Helpers)
    // =========================================================

    public void takeDamage(float amount) {
        // --- MEKANIK INVINCIBILITY (KEBAL) ---
        if (currentState == dodgeState) {
            System.out.println("DODGED! (Damage diabaikan)");
            return;
        }

        stats.takeDamage(amount);
    }

    public Rectangle getAttackHitbox() {
        float x = position.x;
        float y = position.y + ATTACK_OFFSET_Y;
        if (facingRight) x += LOGICAL_WIDTH;
        else x -= ATTACK_WIDTH;

        attackRect.set(x, y, ATTACK_WIDTH, ATTACK_HEIGHT);
        return attackRect;
    }

    public boolean isHitboxActive() {
        if (currentState != attackState) return false;
        return stateTime >= DAMAGE_START_TIME && stateTime <= DAMAGE_END_TIME;
    }

    public boolean isAttacking() { return currentState == attackState; }
    public boolean isDodging() { return currentState == dodgeState; }

    private void changeState(PlayerState newState) {
        currentState = newState;
        currentState.enter();
        stateTime = 0;
    }

    // Getters
    public float getWidth() { return LOGICAL_WIDTH; }
    public float getHeight() { return LOGICAL_HEIGHT; }

    public void render(SpriteBatch batch) {
        TextureRegion currentFrame = currentState.getCurrentFrame(stateTime);

        if (currentFrame != null) {
            // Visual Effect saat Dodge
            if (currentState == dodgeState) {
                batch.setColor(0.5f, 0.5f, 1f, 0.7f);
            } else {
                batch.setColor(Color.WHITE);
            }

            // 1. Logika Flip
            boolean isFlipX = currentFrame.isFlipX();
            if ((!facingRight && !isFlipX) || (facingRight && isFlipX)) {
                currentFrame.flip(true, false);
            }

            // 2. Logika Centering (Half-Pixel Fix)
            float textureWidth = currentFrame.getRegionWidth();
            float textureHeight = currentFrame.getRegionHeight();

            float rawCenterX = (LOGICAL_WIDTH - textureWidth) / 2f;
            float rawCenterY = (LOGICAL_HEIGHT - textureHeight) / 2f;

            if (textureWidth % 2 != 0) rawCenterX += 0.5f;
            if (textureHeight % 2 != 0) rawCenterY += 0.5f;

            int fixedCenterX = (int) rawCenterX;
            int fixedCenterY = (int) rawCenterY;

            // 3. Posisi Akhir
            float drawX = position.x + fixedCenterX + DRAW_OFFSET_X;
            float drawY = position.y + fixedCenterY + DRAW_OFFSET_Y;

            // 4. Gambar
            batch.draw(currentFrame, (int)drawX, (int)drawY);

            // Reset warna batch
            batch.setColor(Color.WHITE);
        }
    }
}
