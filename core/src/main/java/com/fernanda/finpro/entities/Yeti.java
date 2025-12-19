package com.fernanda.finpro.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.fernanda.finpro.components.ItemType;
import com.fernanda.finpro.singleton.GameAssetManager;

public class Yeti extends Monster {

    // --- CONFIG KHUSUS YETI ---
    private static final float YETI_SPEED = 40f;
    private static final int   YETI_HP = 150;
    private static final int   YETI_DMG = 30;

    // Dimensi Fisik
    private static final float WIDTH = 40f;
    private static final float HEIGHT = 45f;

    private static final float DETECT_RANGE = 250f;
    private static final float ATTACK_RANGE = 45f;

    private static final float HABITAT_MIN = 0f;
    private static final float HABITAT_MAX = 2000f;

    private static final float WINDUP_TIME = 0.5f;
    private static final float ACTIVE_TIME = 0.72f;
    private static final float RECOVERY_TIME = 1.5f;

    // Hitbox timing
    private static final float HIT_START_TIME = 0.36f;
    private static final float HIT_END_TIME = 0.72f;

    private Animation<TextureRegion> idleAnim;
    private Animation<TextureRegion> attackAnim;

    private Vector2 wanderTarget = new Vector2();
    private float wanderWaitTimer = 0f;
    private boolean isWanderWalking = false;
    private boolean forceReverse = false;

    public Yeti(float x, float y) {
        super(x, y, YETI_SPEED, YETI_HP, YETI_DMG, WIDTH, HEIGHT, HABITAT_MIN, HABITAT_MAX);

        this.detectionRadius = DETECT_RANGE;
        this.attackRadius = ATTACK_RANGE;
        this.knockbackDistance = 0.5f;
        this.wanderTarget.set(x, y);

        // Init Animations
        // Load Yeti-Idle (Asumsi 4 frame, speed lambat 0.2f)
        idleAnim = createAnimation(GameAssetManager.YETI_IDLE, 4, 0.2f, Animation.PlayMode.LOOP);
        attackAnim = createAnimation(GameAssetManager.YETI_ATTACK, 6, 0.12f, Animation.PlayMode.NORMAL);

        this.deathDuration = 2.0f; // Mayat hilang lebih lama
    }

    @Override
    public void aiBehavior(float dt, Player player) {
        if (isDead) return;

        float distToPlayer = position.dst(player.position);

        // Update facing direction berdasarkan posisi player (kecuali saat wander)
        if (currentState != State.WANDER && currentState != State.DEAD) {
            float dx = player.position.x - position.x;
            if (Math.abs(dx) > 5f) { // Threshold untuk menghindari jitter
                facingRight = dx > 0;
            }
        }

        // Logic Facing saat bergerak
        if (Math.abs(velocity.x) > 1.0f) {
            facingRight = velocity.x > 0;
        }

        switch (currentState) {
            case HURT:
                if (stateTimer > 0.2f) currentState = State.CHASE;
                break;

            case WANDER:
                handleWander(dt);
                if (distToPlayer < detectionRadius && player.position.dst(spawnPosition) < wanderRadius * 1.5f) {
                    currentState = State.CHASE;
                }
                break;

            case CHASE:
                moveTowards(player.position);

                if (distToPlayer <= attackRadius) {
                    currentState = State.PREPARE_ATTACK;
                    stateTimer = 0;
                    velocity.set(0, 0);
                } else if (player.position.dst(spawnPosition) > wanderRadius * 2.0f) {
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
                    createAttackHitbox();
                } else {
                    attackRect.set(0, 0, 0, 0);
                }
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

        position.mulAdd(velocity, dt);
    }

    private void handleWander(float dt) {
        if (isWanderWalking) {
            if (position.dst(wanderTarget) > 5f) {
                // Panggil moveTowards dari Parent (Monster.java) agar menghindari tembok
                moveTowards(wanderTarget);

                if (stateTimer > 8.0f) { // Yeti jalan lebih lama sebelum timeout
                    isWanderWalking = false;
                    wanderWaitTimer = MathUtils.random(1.0f, 3.0f);
                    velocity.set(0, 0);
                }
            } else {
                isWanderWalking = false;
                wanderWaitTimer = MathUtils.random(3.0f, 6.0f); // Yeti diam lebih lama
                velocity.set(0, 0);
            }
        } else {
            wanderWaitTimer -= dt;
            velocity.set(0, 0);

            if (wanderWaitTimer <= 0) {
                if (forceReverse) {
                    float baseAngle = facingRight ? 180f : 0f;
                    float angle = baseAngle + MathUtils.random(-45f, 45f);
                    float dist = MathUtils.random(30f, wanderRadius);
                    wanderTarget.set(position).add(new Vector2(dist, 0).rotateDeg(angle));
                    forceReverse = false;
                } else {
                    float angle = MathUtils.random(0f, 360f);
                    float dist = MathUtils.random(20f, wanderRadius);
                    wanderTarget.set(spawnPosition).add(new Vector2(dist, 0).rotateDeg(angle));
                }
                isWanderWalking = true;
                stateTimer = 0;
            }
        }
    }

    private void createAttackHitbox() {
        float atkWidth = 60f;
        float atkHeight = 50f;
        float offsetIn = 1f;

        float atkX;
        if (facingRight) {
            atkX = (position.x + WIDTH) - offsetIn;
        } else {
            atkX = (position.x - atkWidth) + offsetIn;
        }
        float atkY = position.y + (HEIGHT/2) - (atkHeight/2);
        attackRect.set(atkX, atkY, atkWidth, atkHeight);
    }

    @Override
    public void render(SpriteBatch batch) {
        // Default animasi adalah Idle
        Animation<TextureRegion> currentAnim = idleAnim;
        boolean loop = true;

        switch (currentState) {
            case IDLE: currentAnim = idleAnim; break;
            case ATTACKING:
                currentAnim = attackAnim;
                loop = false;
                break;
            case PREPARE_ATTACK: currentAnim = idleAnim; break;
            case HURT:
                currentAnim = idleAnim;
                break;
        }

        TextureRegion currentFrame = currentAnim.getKeyFrame(stateTimer, loop);

        if (currentFrame != null) {
            if (currentState == State.HURT) {
                batch.setColor(0.5f, 0.5f, 1f, 1f);
            } else {
                batch.setColor(Color.WHITE);
            }

            float width = currentFrame.getRegionWidth();
            float height = currentFrame.getRegionHeight();
            float drawX = position.x + (WIDTH - width) / 2f;
            float drawY = position.y + (HEIGHT - height) / 2f;

            if (facingRight) {
                batch.draw(currentFrame, drawX, drawY, width, height);
            } else {
                batch.draw(currentFrame, drawX + width, drawY, -width, height);
            }
            batch.setColor(Color.WHITE);
        }
    }

    @Override
    public void renderDebug(ShapeRenderer sr) {
        if (isDead) return;

        sr.setColor(Color.CYAN); // Warna Cyan khas Es
        sr.rect(bodyRect.x, bodyRect.y, bodyRect.width, bodyRect.height);

        if (currentState == State.ATTACKING && attackRect.width > 0) {
            sr.setColor(Color.RED); // Serangan Yeti warna Merah (Bahaya)
            sr.rect(attackRect.x, attackRect.y, attackRect.width, attackRect.height);
        }
    }

    @Override
    public ItemType rollDrop() {
        return ItemType.YETI_HEART;
    }
}
