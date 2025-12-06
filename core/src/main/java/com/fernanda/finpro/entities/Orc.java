package com.fernanda.finpro.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class Orc extends Monster {

    // --- CONFIG KHUSUS ORC ---
    private static final float ORC_SPEED = 60f;
    private static final int   ORC_HP = 100;
    private static final int   ORC_DMG = 15;

    // Dimensi Hitbox Orc
    private static final float WIDTH = 30f;
    private static final float HEIGHT = 40f;

    // AI Range
    private static final float DETECT_RANGE = 300f;
    private static final float ATTACK_RANGE = 40f;

    // --- BATAS WILAYAH ORC (HUTAN) ---
    // Sesuai diskusi: Orc hanya hidup di Hutan (Radius 2000 - 3500)
    // Parameter ini dikirim ke Parent agar Parent yang mengurus logikanya
    private static final float HABITAT_MIN = 2000f; // Batas Ice
    private static final float HABITAT_MAX = 3500f; // Batas Laut

    // Attack Timing (Detik)
    private static final float WINDUP_TIME = 0.4f;
    private static final float ACTIVE_TIME = 0.2f;
    private static final float RECOVERY_TIME = 1.0f;

    public Orc(float x, float y) {
        // Pass parameter ke Parent Constructor (termasuk Zone Min/Max)
        super(x, y, ORC_SPEED, ORC_HP, ORC_DMG, WIDTH, HEIGHT, HABITAT_MIN, HABITAT_MAX);

        this.detectionRadius = DETECT_RANGE;
        this.attackRadius = ATTACK_RANGE;
    }

    @Override
    public void aiBehavior(float dt, Player player) {
        if (isDead) return;

        float distToPlayer = position.dst(player.position);

        // Logika Scalable: Cek apakah player ada di dalam habitat Orc
        // Agar Orc tidak mengejar player sampai ke Ice/Laut jika player kabur
        boolean playerInHabitat = player.position.len() >= HABITAT_MIN && player.position.len() <= HABITAT_MAX;

        switch (currentState) {
            case HURT:
                if (stateTimer > 0.2f) currentState = State.CHASE;
                break;

            case WANDER:
                handleWander(dt);
                if (distToPlayer < detectionRadius && playerInHabitat) {
                    currentState = State.CHASE;
                }
                break;

            case CHASE:
                moveTowards(player.position);

                // Stop kejar jika player keluar habitat
                if (!playerInHabitat) {
                    currentState = State.WANDER;
                }
                else if (distToPlayer <= attackRadius) {
                    currentState = State.PREPARE_ATTACK;
                    stateTimer = 0;
                    velocity.set(0, 0);
                } else if (distToPlayer > detectionRadius * 1.5f) {
                    currentState = State.WANDER;
                }
                break;

            case PREPARE_ATTACK:
                if (stateTimer >= WINDUP_TIME) {
                    currentState = State.ATTACKING;
                    stateTimer = 0;
                    createAttackHitbox();
                }
                break;

            case ATTACKING:
                if (stateTimer >= ACTIVE_TIME) {
                    currentState = State.COOLDOWN;
                    stateTimer = 0;
                    attackRect.set(0, 0, 0, 0);
                }
                break;

            case COOLDOWN:
                if (stateTimer >= RECOVERY_TIME) {
                    currentState = State.CHASE;
                }
                break;
        }

        // Apply movement
        position.mulAdd(velocity, dt);

        if (velocity.x > 0) facingRight = true;
        if (velocity.x < 0) facingRight = false;
    }

    private void handleWander(float dt) {
        if (stateTimer > 2.0f) {
            float randomAngle = MathUtils.random(0, 360);
            velocity.set(1, 0).setAngleDeg(randomAngle).scl(speed * 0.5f);
            stateTimer = 0;
        }
    }

    private void moveTowards(Vector2 target) {
        velocity.set(target).sub(position).nor().scl(speed);
    }

    private void createAttackHitbox() {
        float atkSize = 30f;
        float atkX = facingRight ? (position.x + WIDTH) : (position.x - atkSize);
        float atkY = position.y + (HEIGHT / 2) - (atkSize / 2);

        attackRect.set(atkX, atkY, atkSize, atkSize);
    }

    @Override
    public void renderDebug(ShapeRenderer sr) {
        if (isDead) return;

        if (immunityTimer > 0) sr.setColor(Color.WHITE);
        else sr.setColor(Color.RED);
        sr.rect(bodyRect.x, bodyRect.y, bodyRect.width, bodyRect.height);

        if (currentState == State.ATTACKING) {
            sr.setColor(Color.YELLOW);
            sr.rect(attackRect.x, attackRect.y, attackRect.width, attackRect.height);
        }

        if (currentState == State.CHASE) {
            sr.setColor(Color.ORANGE);
            sr.circle(position.x + WIDTH/2, position.y + HEIGHT + 10, 5);
        }
    }
}
