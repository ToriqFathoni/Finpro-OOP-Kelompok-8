package com.fernanda.finpro.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.fernanda.finpro.components.ItemType;
import com.fernanda.finpro.singleton.GameAssetManager;

public class Orc extends Monster {

    private static final float ORC_SPEED = 80f;
    private static final int   ORC_HP = 50;
    private static final int   ORC_DMG = 10;

    private static final float WIDTH = 20f;
    private static final float HEIGHT = 25f;

    private static final float DETECT_RANGE = 100f;
    private static final float ATTACK_RANGE = 35f;

    private static final float HABITAT_MIN = 2000f;
    private static final float HABITAT_MAX = 3500f;

    private static final float WINDUP_TIME = 0.1f;
    private static final float ACTIVE_TIME = 0.6f;
    private static final float RECOVERY_TIME = 0.5f;

    private static final float HIT_START_TIME = 0.3f;
    private static final float HIT_END_TIME = 0.5f;
    private Animation<TextureRegion> idleAnim;
    private Animation<TextureRegion> walkAnim;
    private Animation<TextureRegion> attackAnim;
    private Animation<TextureRegion> hurtAnim;
    private Animation<TextureRegion> deathAnim;

    private Vector2 wanderTarget = new Vector2();
    private float wanderWaitTimer = 0f;
    private boolean isWanderWalking = false;
    private boolean forceReverse = false;

    public Orc(float x, float y) {
        super(x, y, ORC_SPEED, ORC_HP, ORC_DMG, WIDTH, HEIGHT, HABITAT_MIN, HABITAT_MAX);

        this.detectionRadius = DETECT_RANGE;
        this.attackRadius = ATTACK_RANGE;
        this.knockbackDistance = 20f;
        this.wanderTarget.set(x, y);

        idleAnim = createAnimation(GameAssetManager.ORC_IDLE, 6, 0.1f, Animation.PlayMode.LOOP);
        walkAnim = createAnimation(GameAssetManager.ORC_WALK, 8, 0.1f, Animation.PlayMode.LOOP);
        attackAnim = createAnimation(GameAssetManager.ORC_ATTACK, 6, 0.1f, Animation.PlayMode.NORMAL);
        hurtAnim = createAnimation(GameAssetManager.ORC_HURT, 4, 0.1f, Animation.PlayMode.NORMAL);
        deathAnim = createAnimation(GameAssetManager.ORC_DEATH, 4, 0.1f, Animation.PlayMode.NORMAL);

        this.deathDuration = 1.5f;
    }

    private Rectangle getPredictedAttackHitbox() {
        float atkWidth = 25f;
        float atkHeight = 60f;

        float offsetIn = 5f;
        float offsetDown = 0f;

        float atkX;
        if (facingRight) {
            atkX = (position.x + WIDTH) - offsetIn;
        } else {
            atkX = (position.x - atkWidth) + offsetIn;
        }

        float atkY = position.y + (HEIGHT / 2) - (atkHeight / 2) - offsetDown;

        return new Rectangle(atkX, atkY, atkWidth, atkHeight);
    }

    @Override
    public void aiBehavior(float dt, Player player) {
        if (isDead) return;

        float orcCenterX = position.x + (WIDTH / 2);
        float orcCenterY = position.y + (HEIGHT / 2);

        float playerCenterX = player.position.x + (player.getWidth() / 2);
        float playerCenterY = player.position.y + (player.getHeight() / 2);

        float distToPlayer = Vector2.dst(orcCenterX, orcCenterY, playerCenterX, playerCenterY);

        switch (currentState) {
            case IDLE:
                break;

            case HURT:
                if (stateTimer > 0.4f) currentState = State.CHASE;
                break;

            case WANDER:
                handleWander(dt);
                if (distToPlayer < detectionRadius && player.position.dst(spawnPosition) < wanderRadius * 1.5f) {
                    currentState = State.CHASE;
                }
                break;

            case CHASE:
                moveTowards(new Vector2(playerCenterX, playerCenterY));

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

            case DEAD:
                break;
        }

        position.mulAdd(velocity, dt);

        if (Math.abs(velocity.x) > 0.1f) {
            facingRight = velocity.x > 0;
        }
    }

    private void handleWander(float dt) {
        if (isWanderWalking) {
            if (position.dst(wanderTarget) > 5f) {
                velocity.set(wanderTarget).sub(position).nor().scl(speed);

                float checkDist = 16f;
                float centerX = position.x + (WIDTH / 2);
                float centerY = position.y + (HEIGHT / 2);
                Vector2 dir = new Vector2(velocity).nor();

                float checkX = centerX + dir.x * checkDist;
                float checkY = centerY + dir.y * checkDist;

                if (isTileBlocked(checkX, checkY)) {
                     velocity.set(0, 0);
                     isWanderWalking = false;
                     wanderWaitTimer = MathUtils.random(0.5f, 1.0f);
                     forceReverse = true;
                }

                if (stateTimer > 5.0f) {
                     isWanderWalking = false;
                     wanderWaitTimer = MathUtils.random(1.0f, 3.0f);
                     velocity.set(0, 0);
                }

            } else {
                isWanderWalking = false;
                wanderWaitTimer = MathUtils.random(2.0f, 5.0f);
                velocity.set(0, 0);
            }
        } else {
            wanderWaitTimer -= dt;
            velocity.set(0, 0);

            if (wanderWaitTimer <= 0) {
                if (forceReverse) {
                    float baseAngle = facingRight ? 180f : 0f;
                    float randomOffset = MathUtils.random(-45f, 45f);
                    float angle = baseAngle + randomOffset;

                    float dist = MathUtils.random(30f, wanderRadius);
                    wanderTarget.set(position).add(new Vector2(dist, 0).rotateDeg(angle));

                    forceReverse = false;
                } else {
                    float angle = MathUtils.random(0f, 360f);
                    float dist = MathUtils.random(10f, wanderRadius);
                    wanderTarget.set(spawnPosition).add(new Vector2(dist, 0).rotateDeg(angle));
                }

                isWanderWalking = true;
                stateTimer = 0;
            }
        }
    }

    private void createAttackHitbox() {
        float atkWidth = 20f;
        float atkHeight = 30f;
        float offsetIn = 3f;
        float offsetDown = 0.5f;

        float atkX;
        if (facingRight) {
            atkX = (position.x + WIDTH) - offsetIn;
        } else {
            atkX = (position.x - atkWidth) + offsetIn;
        }

        float atkY = position.y + (HEIGHT / 2) - (atkHeight / 2) - offsetDown;

        attackRect.set(atkX, atkY, atkWidth, atkHeight);
    }

    @Override
    public void render(SpriteBatch batch) {
        Animation<TextureRegion> currentAnim = idleAnim;
        boolean loop = true;

        switch (currentState) {
            case IDLE: currentAnim = idleAnim; break;
            case WANDER:
                if (velocity.len2() > 0.1f) {
                    currentAnim = walkAnim;
                } else {
                    currentAnim = idleAnim;
                }
                break;
            case CHASE: currentAnim = walkAnim; break;
            case PREPARE_ATTACK: currentAnim = idleAnim; break;
            case ATTACKING:
                currentAnim = attackAnim;
                loop = false;
                break;
            case HURT:
                currentAnim = hurtAnim;
                loop = false;
                break;
            case DEAD:
                currentAnim = deathAnim;
                loop = false;
                break;
            default: currentAnim = idleAnim; break;
        }

        TextureRegion currentFrame = currentAnim.getKeyFrame(stateTimer, loop);

        if (currentFrame == null) return;

        float width = currentFrame.getRegionWidth();
        float height = currentFrame.getRegionHeight();
        float drawX = position.x + (WIDTH - width) / 2f;
        float drawY = position.y + (HEIGHT - height) / 2f;

        if (facingRight) {
            batch.draw(currentFrame, drawX, drawY, width, height);
        } else {
            batch.draw(currentFrame, drawX + width, drawY, -width, height);
        }
    }

    @Override
    public void renderDebug(ShapeRenderer sr) {
        /*
        if (isDead) return;

        sr.setColor(Color.BLUE); // Set warna biru
        sr.rect(bodyRect.x, bodyRect.y, bodyRect.width, bodyRect.height);

        // Visualisasi Hitbox Serangan (Hanya muncul saat timing pas / kotak kuning)
        if (currentState == State.ATTACKING && attackRect.width > 0) {
            sr.setColor(Color.YELLOW);
            sr.rect(attackRect.x, attackRect.y, attackRect.width, attackRect.height);
        }

        if (currentState == State.CHASE) {
            sr.setColor(Color.ORANGE);
            sr.circle(position.x + WIDTH/2, position.y + HEIGHT + 10, 5);
        }

         */
    }
    @Override
    public ItemType rollDrop() {
        float roll = MathUtils.random();
        if (roll < 0.5f) {
            return ItemType.ORC_MEAT;
        } else {
            return ItemType.ORC_SKULL;
        }
    }
}
