package com.fernanda.finpro.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.fernanda.finpro.singleton.GameAssetManager;

public class Werewolf extends Monster {

    private static final float WW_SPEED = 50f;
    private static final int   WW_HP = 90;
    private static final int   WW_DMG = 20;

    // Dimensi Fisik
    private static final float WIDTH = 30f;
    private static final float HEIGHT = 35f;

    // AI Range
    private static final float DETECT_RANGE = 300f;
    private static final float ATTACK_RANGE = 35f;

    // Habitat
    private static final float HABITAT_MIN = 2000f;
    private static final float HABITAT_MAX = 3500f;

    // Attack Timing
    private static final float WINDUP_TIME = 0.2f;
    private static final float ACTIVE_TIME = 0.6f;
    private static final float RECOVERY_TIME = 0.5f;

    private static final float HIT_START_TIME = 0.3f;
    private static final float HIT_END_TIME = 0.5f;

    // --- ANIMASI ---
    private Animation<TextureRegion> idleAnim;
    private Animation<TextureRegion> attackAnim;
    private Animation<TextureRegion> walkAnim;

    public Werewolf(float x, float y) {
        super(x, y, WW_SPEED, WW_HP, WW_DMG, WIDTH, HEIGHT, HABITAT_MIN, HABITAT_MAX);

        this.detectionRadius = DETECT_RANGE;
        this.attackRadius = ATTACK_RANGE;

        idleAnim = createAnimation(GameAssetManager.WEREWOLF_IDLE, 4, 0.15f, Animation.PlayMode.LOOP);
        attackAnim = createAnimation(GameAssetManager.WEREWOLF_ATTACK, 6, 0.1f, Animation.PlayMode.NORMAL);
        walkAnim = createAnimation(GameAssetManager.WEREWOLF_WALK, 4, 0.15f, Animation.PlayMode.LOOP);

        this.deathDuration = 1.0f;
    }

    private Animation<TextureRegion> createAnimation(String assetName, int cols, float frameDuration, Animation.PlayMode mode) {
        Texture texture = GameAssetManager.getInstance().getTexture(assetName);
        TextureRegion[][] tmp = TextureRegion.split(texture, texture.getWidth() / cols, texture.getHeight());
        TextureRegion[] frames = new TextureRegion[cols];
        for (int i = 0; i < cols; i++) {
            frames[i] = tmp[0][i];
        }
        Animation<TextureRegion> anim = new Animation<>(frameDuration, frames);
        anim.setPlayMode(mode);
        return anim;
    }

    @Override
    public void aiBehavior(float dt, Player player) {
        if (isDead) return;

        // Logic Facing (Hadap Kanan/Kiri)
        if (Math.abs(velocity.x) > 1.0f) {
            facingRight = velocity.x > 0;
        }

        float distToPlayer = position.dst(player.position);

        switch (currentState) {
            case HURT:
                if (stateTimer > 0.3f) currentState = State.CHASE;
                break;

            case WANDER:
                handleWander(dt);
                if (distToPlayer < detectionRadius) {
                    currentState = State.CHASE;
                }
                break;

            case CHASE:
                moveTowards(player.position);

                // Jika dekat, serang
                if (distToPlayer <= attackRadius) {
                    currentState = State.PREPARE_ATTACK;
                    stateTimer = 0;
                    velocity.set(0, 0);
                }
                else if (distToPlayer > detectionRadius * 1.5f) {
                    currentState = State.WANDER;
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
        float atkWidth = 30f;
        float atkHeight = 30f;

        float offsetIn = 5f;

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
        Animation<TextureRegion> currentAnim = idleAnim;
        boolean loop = true;

        switch (currentState) {
            case IDLE:
                currentAnim = idleAnim;
                break;
            case WANDER:
            case CHASE:
                currentAnim = walkAnim;
                break;
            case PREPARE_ATTACK:
                currentAnim = idleAnim;
                break;
            case ATTACKING:
                currentAnim = attackAnim;
                loop = false;
                break;
            case HURT:
                currentAnim = idleAnim;
                break;
        }

        TextureRegion currentFrame = currentAnim.getKeyFrame(stateTimer, loop);

        if (currentFrame != null) {
            if (currentState == State.HURT) {
                batch.setColor(1f, 0.5f, 0.5f, 1f);
            } else {
                batch.setColor(Color.WHITE);
            }

            // Draw Logic
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

        sr.setColor(Color.BLUE);
        sr.rect(bodyRect.x, bodyRect.y, bodyRect.width, bodyRect.height);

        if (currentState == State.ATTACKING && attackRect.width > 0) {
            sr.setColor(Color.YELLOW);
            sr.rect(attackRect.x, attackRect.y, attackRect.width, attackRect.height);
        }

        if (currentState == State.CHASE) {
            sr.setColor(Color.ORANGE);
            sr.circle(position.x + WIDTH/2, position.y + HEIGHT + 5, 3);
        }
    }
}
