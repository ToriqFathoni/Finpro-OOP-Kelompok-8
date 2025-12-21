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

    private static final float BOSS_SPEED = 55f;
    private static final int   BOSS_HP = 700;
    private static final int   BOSS_DMG = 60;

    private static final float WIDTH = 60f;
    private static final float HEIGHT = 175f;
    private static final float VISUAL_OFFSET_Y = -17f;

    private static final float DETECT_RANGE = 80f;
    private static final float ATTACK_RANGE = 70f;

    private static final float HABITAT_MIN = 0f;
    private static final float HABITAT_MAX = 2000f;

    private static final float FRAME_DURATION = 0.15f;
    private static final float WINDUP_TIME = 0.2f;
    private static final float ACTIVE_TIME = 1.2f;
    private static final float RECOVERY_TIME = 2.0f;

    private static final float HIT_START_TIME = 0.75f;
    private static final float HIT_END_TIME   = 1.2f;

    private Animation<TextureRegion> idleAnim;
    private Animation<TextureRegion> walkAnim;
    private Animation<TextureRegion> attackAnim;

    private Vector2 wanderTarget = new Vector2();
    private float wanderWaitTimer = 0f;
    private boolean isWanderWalking = false;

    public MiniBoss(float x, float y) {
        super(x, y, BOSS_SPEED, BOSS_HP, BOSS_DMG, WIDTH, HEIGHT, HABITAT_MIN, HABITAT_MAX);

        this.detectionRadius = DETECT_RANGE;
        this.attackRadius = ATTACK_RANGE;
        this.knockbackDistance = 0f;
        this.wanderTarget.set(x, y);
        this.deathDuration = 2.0f;

        idleAnim = createAnimation(GameAssetManager.MINIBOSS_IDLE, 3, 0.2f, Animation.PlayMode.LOOP);
        walkAnim = createAnimation(GameAssetManager.MINIBOSS_WALK, 4, 0.15f, Animation.PlayMode.LOOP);
        attackAnim = createAnimation(GameAssetManager.MINIBOSS_ATTACK, 8, FRAME_DURATION, Animation.PlayMode.NORMAL);
    }

    @Override
    public void takeDamage(int amount) {
        if (isDead || immunityTimer > 0) return;

        currentHealth -= amount;
        immunityTimer = 0.1f;

        if (currentHealth <= 0) {
            isDead = true;
            currentState = State.DEAD;
        }
    }

    @Override
    public void aiBehavior(float dt, Player player) {
        if (isDead) return;

        float myCenterX = position.x + (WIDTH / 2);
        float myCenterY = position.y + (HEIGHT / 2);

        float playerCenterX = player.position.x + (player.getWidth() / 2);
        float playerCenterY = player.position.y + (player.getHeight() / 2);

        float distToPlayer = Vector2.dst(myCenterX, myCenterY, playerCenterX, playerCenterY);

        if (currentState != State.WANDER && currentState != State.DEAD && currentState != State.ATTACKING) {

        }

        if (Math.abs(velocity.x) > 0.1f && currentState != State.ATTACKING) {
            facingRight = velocity.x > 0;
        }

        switch (currentState) {
            case IDLE:
            case WANDER:
                handleWander(dt);
                if (distToPlayer < detectionRadius) {
                    currentState = State.CHASE;
                }
                break;

            case HURT:
                if (stateTimer > 0.1f) currentState = State.CHASE;
                break;

            case CHASE:
                moveTowards(new Vector2(playerCenterX, playerCenterY));

                if (distToPlayer <= attackRadius) {
                    currentState = State.PREPARE_ATTACK;
                    stateTimer = 0;
                    velocity.set(0, 0);
                } else if (distToPlayer > detectionRadius * 1.5f) {
                    currentState = State.WANDER;
                    wanderTarget.set(spawnPosition);
                }
                break;

            case PREPARE_ATTACK:
                facingRight = playerCenterX > myCenterX;
                
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
                velocity.set(0, 0);
                break;
        }

        position.mulAdd(velocity, dt);
    }

    private void handleWander(float dt) {
        if (isWanderWalking) {
            if (position.dst(wanderTarget) > 10f) {
                moveTowards(wanderTarget);
                if (stateTimer > 5.0f) {
                    isWanderWalking = false;
                    wanderWaitTimer = MathUtils.random(1.0f, 3.0f);
                    velocity.set(0, 0);
                }
            } else {
                isWanderWalking = false;
                wanderWaitTimer = MathUtils.random(2.0f, 4.0f);
                velocity.set(0, 0);
            }
        } else {
            wanderWaitTimer -= dt;
            velocity.set(0, 0);

            if (wanderWaitTimer <= 0) {
                float rx = spawnPosition.x + MathUtils.random(-300, 300);
                float ry = spawnPosition.y + MathUtils.random(-300, 300);
                wanderTarget.set(rx, ry);
                isWanderWalking = true;
                stateTimer = 0;
            }
        }
    }

    private void createAttackHitbox() {
        float atkWidth = 125f;
        float atkHeight = 200f;

        float atkX;
        if (facingRight) {
            atkX = position.x + WIDTH;
        } else {
            atkX = position.x - atkWidth;
        }

        float atkY = position.y + (HEIGHT / 2) - (atkHeight / 2);

        attackRect.set(atkX, atkY, atkWidth, atkHeight);
    }

    @Override
    public void render(SpriteBatch batch) {
        Animation<TextureRegion> currentAnim = idleAnim;
        boolean loop = true;

        switch (currentState) {
            case IDLE:
            case PREPARE_ATTACK:
            case COOLDOWN:
                currentAnim = idleAnim;
                break;
            case CHASE:
            case WANDER:
                if (velocity.len2() > 0.1f) {
                    currentAnim = walkAnim;
                } else {
                    currentAnim = idleAnim;
                }
                break;
            case ATTACKING:
                currentAnim = attackAnim;
                loop = false;
                break;
            case DEAD:
                currentAnim = idleAnim;
                loop = false;
                break;
            default:
                currentAnim = idleAnim;
                break;
        }

        float animTime = stateTimer;
        if (loop) animTime = stateTimer;

        TextureRegion currentFrame = currentAnim.getKeyFrame(animTime, loop);

        if (currentState == State.DEAD) {
            currentFrame = idleAnim.getKeyFrame(0);
        }

        if (currentFrame != null) {
            if (immunityTimer > 0) {
                batch.setColor(1f, 0.5f, 0.5f, 1f);
            } else if (currentState == State.DEAD) {
                batch.setColor(0.5f, 0.5f, 0.5f, 1f - (stateTimer / deathDuration));
            } else {
                batch.setColor(Color.WHITE);
            }

            float scale = 1.5f;
            float width = currentFrame.getRegionWidth() * scale;
            float height = currentFrame.getRegionHeight() * scale;

            float drawX = position.x + (WIDTH - width) / 2f;
            float drawY = (position.y + (HEIGHT - height) / 2f) + VISUAL_OFFSET_Y;

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
        /*
        if (isDead) return;

        sr.setColor(Color.PURPLE);
        sr.rect(bodyRect.x, bodyRect.y, bodyRect.width, bodyRect.height);

        if (currentState == State.ATTACKING && attackRect.width > 0) {
            sr.setColor(Color.RED);
            sr.rect(attackRect.x, attackRect.y, attackRect.width, attackRect.height);
        }

         */
    }

    @Override
    public ItemType rollDrop() {
        return ItemType.ORC_SKULL ;
    }
}
