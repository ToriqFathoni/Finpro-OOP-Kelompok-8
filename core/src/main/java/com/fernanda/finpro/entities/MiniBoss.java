package com.fernanda.finpro.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.fernanda.finpro.components.ItemType;
import com.fernanda.finpro.singleton.GameAssetManager;

public class MiniBoss extends Monster {

    // --- CONFIG KHUSUS MINIBOSS ---
    private static final float MINIBOSS_SPEED = 45f;
    private static final int   MINIBOSS_HP = 300;
    private static final int   MINIBOSS_DMG = 50;

    // Dimensi Fisik
    private static final float WIDTH = 30f;
    private static final float HEIGHT = 60f;

    private static final float DETECT_RANGE = 300f;
    private static final float ATTACK_RANGE = 50f;

    private static final float HABITAT_MIN = 0f;
    private static final float HABITAT_MAX = 2000f;

    private static final float WINDUP_TIME = 0.5f;
    private static final float ACTIVE_TIME = 0.8f; // 8 frames * 0.1s
    private static final float RECOVERY_TIME = 1.5f;

    // Hitbox timing (Frame 6-7 -> 0.5s - 0.7s)
    private static final float HIT_START_TIME = 0.5f;
    private static final float HIT_END_TIME = 0.7f;

    private Animation<TextureRegion> idleAnim;
    private Animation<TextureRegion> walkAnim;
    private Animation<TextureRegion> attackAnim;

    private Vector2 wanderTarget = new Vector2();
    private float wanderWaitTimer = 0f;
    private boolean isWanderWalking = false;

    public MiniBoss(float x, float y) {
        super(x, y, MINIBOSS_SPEED, MINIBOSS_HP, MINIBOSS_DMG, WIDTH, HEIGHT, HABITAT_MIN, HABITAT_MAX);

        this.detectionRadius = DETECT_RANGE;
        this.attackRadius = ATTACK_RANGE;
        this.knockbackDistance = 0.8f;
        this.wanderTarget.set(x, y);

        // Init Animations
        // Load MiniBoss-Idle (Asumsi 3 frame, speed lambat 0.2f)
        idleAnim = createAnimation(GameAssetManager.MINIBOSS_IDLE, 3, 0.2f, Animation.PlayMode.LOOP);
        // Load MiniBoss-Walk (4 frame)
        walkAnim = createAnimation(GameAssetManager.MINIBOSS_WALK, 4, 0.15f, Animation.PlayMode.LOOP);
        // Load MiniBoss-Attack (8 frame)
        attackAnim = createAnimation(GameAssetManager.MINIBOSS_ATTACK, 8, 0.1f, Animation.PlayMode.NORMAL);

        this.deathDuration = 2.0f;
    }

    @Override
    public void aiBehavior(float dt, Player player) {
        if (isDead) return;

        float distToPlayer = position.dst(player.position);

        if (currentState != State.WANDER && currentState != State.DEAD) {
            float dx = player.position.x - position.x;
            if (Math.abs(dx) > 5f) {
                facingRight = dx > 0;
            }
        }

        if (Math.abs(velocity.x) > 1.0f) {
            facingRight = velocity.x > 0;
        }

        // Update posisi manual jika tidak ada di Monster.java
        position.mulAdd(velocity, dt);

        switch (currentState) {
            case IDLE:
                break;

            case HURT:
                if (stateTimer > 0.2f) currentState = State.CHASE;
                break;

            case WANDER:
                handleWander(dt);
                if (distToPlayer < detectionRadius) {
                    currentState = State.CHASE;
                }
                break;

            case CHASE:
                moveTowards(player.position);

                if (distToPlayer <= attackRadius) {
                    currentState = State.PREPARE_ATTACK;
                    stateTimer = 0;
                    velocity.setZero();
                }
                if (distToPlayer > detectionRadius * 1.5f) {
                    currentState = State.WANDER;
                    wanderTarget.set(spawnPosition);
                }
                break;

            case PREPARE_ATTACK:
                if (stateTimer >= WINDUP_TIME) {
                    currentState = State.ATTACKING;
                    stateTimer = 0;
                }
                break;

            case ATTACKING:
                if (stateTimer >= HIT_START_TIME && stateTimer <= HIT_END_TIME) {
                    attackRect.set(
                        facingRight ? position.x + bodyRect.width : position.x - ATTACK_RANGE,
                        position.y,
                        ATTACK_RANGE, bodyRect.height
                    );
                } else {
                    attackRect.set(0, 0, 0, 0);
                }

                if (stateTimer >= ACTIVE_TIME) {
                    currentState = State.COOLDOWN;
                    stateTimer = 0;
                }
                break;

            case COOLDOWN:
                if (stateTimer >= RECOVERY_TIME) {
                    currentState = State.CHASE;
                }
                break;

            case DEAD:
                break;
        }
    }

    private void handleWander(float dt) {
        if (isWanderWalking) {
            Vector2 dir = new Vector2(wanderTarget).sub(position);
            if (dir.len() < 5f || stateTimer > 5f) {
                isWanderWalking = false;
                wanderWaitTimer = MathUtils.random(1f, 3f);
                velocity.setZero();
            } else {
                dir.nor();
                velocity.set(dir).scl(speed * 0.5f);
            }
        } else {
            wanderWaitTimer -= dt;
            if (wanderWaitTimer <= 0) {
                float rx = spawnPosition.x + MathUtils.random(-wanderRadius, wanderRadius);
                float ry = spawnPosition.y + MathUtils.random(-wanderRadius, wanderRadius);
                wanderTarget.set(rx, ry);
                isWanderWalking = true;
                stateTimer = 0;
            }
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        TextureRegion currentFrame = idleAnim.getKeyFrame(stateTimer);

        if (currentState == State.WANDER || currentState == State.CHASE) {
            currentFrame = walkAnim.getKeyFrame(stateTimer);
        } else if (currentState == State.ATTACKING || currentState == State.PREPARE_ATTACK || currentState == State.COOLDOWN) {
             currentFrame = attackAnim.getKeyFrame(stateTimer);
        } else if (currentState == State.DEAD) {
             currentFrame = idleAnim.getKeyFrame(0);
        }

        // Flip logic
        if (facingRight && !currentFrame.isFlipX()) {
            currentFrame.flip(true, false);
        } else if (!facingRight && currentFrame.isFlipX()) {
            currentFrame.flip(true, false);
        }

        if (currentState == State.HURT) {
            batch.setColor(1, 0.5f, 0.5f, 1);
        } else if (currentState == State.DEAD) {
            batch.setColor(0.5f, 0.5f, 0.5f, 1f - (stateTimer / deathDuration));
        } else {
            batch.setColor(1, 1, 1, 1);
        }

        // Draw lebih besar sesuai ukuran baru
        batch.draw(currentFrame, position.x - 60, position.y - 40, WIDTH + 120, HEIGHT + 80);
        batch.setColor(1, 1, 1, 1);
    }

    @Override
    public void renderDebug(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(Color.PURPLE);
        shapeRenderer.rect(bodyRect.x, bodyRect.y, bodyRect.width, bodyRect.height);

        if (currentState == State.ATTACKING) {
            shapeRenderer.setColor(Color.RED);
            shapeRenderer.rect(attackRect.x, attackRect.y, attackRect.width, attackRect.height);
        }
    }

    @Override
    public ItemType rollDrop() {
        return MathUtils.randomBoolean(0.5f) ? ItemType.ORC_SKULL : null;
    }
}
