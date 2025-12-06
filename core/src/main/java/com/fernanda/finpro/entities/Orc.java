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

public class Orc extends Monster {

    // --- CONFIG KHUSUS ORC ---
    private static final float ORC_SPEED = 80f;
    private static final int   ORC_HP = 100;
    private static final int   ORC_DMG = 15;

    // Dimensi Hitbox Orc
    private static final float WIDTH = 30f;
    private static final float HEIGHT = 35f;

    // AI Range
    private static final float DETECT_RANGE = 300f;
    private static final float ATTACK_RANGE = 35f;

    // --- BATAS WILAYAH ORC (HUTAN) ---
    private static final float HABITAT_MIN = 2000f; // Batas Ice
    private static final float HABITAT_MAX = 3500f; // Batas Laut

    // Attack Timing (Detik)
    private static final float WINDUP_TIME = 0.4f;
    private static final float ACTIVE_TIME = 0.6f; // Disesuaikan dengan durasi animasi (6 frame * 0.1s)
    private static final float RECOVERY_TIME = 1.0f;

    // Hitbox baru muncul di detik ke-0.3 (Frame ke-3) agar pas dengan ayunan tangan
    private static final float HIT_START_TIME = 0.3f;
    // Hitbox menghilang di detik ke-0.5 (Sebelum animasi selesai)
    private static final float HIT_END_TIME = 0.5f;

    // --- ANIMATIONS ---
    private Animation<TextureRegion> idleAnim;
    private Animation<TextureRegion> walkAnim;
    private Animation<TextureRegion> attackAnim;
    private Animation<TextureRegion> hurtAnim;
    private Animation<TextureRegion> deathAnim;

    public Orc(float x, float y) {
        super(x, y, ORC_SPEED, ORC_HP, ORC_DMG, WIDTH, HEIGHT, HABITAT_MIN, HABITAT_MAX);

        this.detectionRadius = DETECT_RANGE;
        this.attackRadius = ATTACK_RANGE;

        // Init Animations
        idleAnim = createAnimation(GameAssetManager.ORC_IDLE, 6, 0.1f, Animation.PlayMode.LOOP);
        walkAnim = createAnimation(GameAssetManager.ORC_WALK, 8, 0.1f, Animation.PlayMode.LOOP);
        attackAnim = createAnimation(GameAssetManager.ORC_ATTACK, 6, 0.1f, Animation.PlayMode.NORMAL);
        hurtAnim = createAnimation(GameAssetManager.ORC_HURT, 4, 0.1f, Animation.PlayMode.NORMAL);
        deathAnim = createAnimation(GameAssetManager.ORC_DEATH, 4, 0.1f, Animation.PlayMode.NORMAL);

        this.deathDuration = 0.4f;
    }

    // Method helper canggih dari kode asli Anda (dipertahankan)
    private Animation<TextureRegion> createAnimation(String assetName, int cols, float frameDuration, Animation.PlayMode mode) {
        Texture texture = GameAssetManager.getInstance().getTexture(assetName);

        int totalWidth = texture.getWidth();
        int totalHeight = texture.getHeight();

        int frameWidth = totalWidth / cols;
        int rows = 1;
        if (totalHeight > frameWidth * 1.5f) {
            rows = Math.round((float)totalHeight / frameWidth);
            if (rows < 1) rows = 1;
        }
        int frameHeight = totalHeight / rows;

        float ratio = (float)frameWidth / frameHeight;
        if (ratio > 1.2f || ratio < 0.8f) {
            frameWidth = frameHeight;
        }

        TextureRegion[][] tmp = TextureRegion.split(texture, frameWidth, frameHeight);
        int framesToUse = tmp[0].length;

        TextureRegion[] frames = new TextureRegion[framesToUse];
        for (int i = 0; i < framesToUse; i++) {
            TextureRegion region = tmp[0][i];
            if (region.getRegionWidth() > 2) {
                region.setRegionWidth(region.getRegionWidth() - 2);
            }
            frames[i] = region;
        }

        Animation<TextureRegion> anim = new Animation<>(frameDuration, frames);
        anim.setPlayMode(mode);
        return anim;
    }

    @Override
    public void aiBehavior(float dt, Player player) {
        if (isDead) return;

        // float distToPlayer = position.dst(player.position);
        float orcCenterX = position.x + (WIDTH / 2);
        float orcCenterY = position.y + (HEIGHT / 2);

        float playerCenterX = player.position.x + (player.getWidth() / 2);
        float playerCenterY = player.position.y + (player.getHeight() / 2);

        float distToPlayer = Vector2.dst(orcCenterX, orcCenterY, playerCenterX, playerCenterY);

        boolean playerInHabitat = player.position.len() >= HABITAT_MIN && player.position.len() <= HABITAT_MAX;

        switch (currentState) {
            case HURT:
                if (stateTimer > 0.4f) currentState = State.CHASE;
                break;

            case WANDER:
                handleWander(dt);
                if (distToPlayer < detectionRadius && playerInHabitat) {
                    currentState = State.CHASE;
                }
                break;

            case CHASE:
                moveTowards(player.position);

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
                }
                break;

            case ATTACKING:
                // --- LOGIKA TIMING HITBOX ---
                if (stateTimer >= HIT_START_TIME && stateTimer <= HIT_END_TIME) {
                    createAttackHitbox();
                } else {
                    attackRect.set(0, 0, 0, 0);
                }

                // Cooldown
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

        if (Math.abs(velocity.x) > 1.0f) {
            facingRight = velocity.x > 0;
        }
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
        float atkWidth = 20f;
        float atkHeight = 30f;
        float offsetIn = 0.5f;   // Jarak geser "ke dalam" (mendekat ke badan)
        float offsetDown = 0.5f; // Jarak geser "ke bawah"

        float atkX;
        if (facingRight) {
            // Jika hadap kanan, posisi X dikurangi offsetIn agar mundur ke kiri
            atkX = (position.x + WIDTH) - offsetIn;
        } else {
            // Jika hadap kiri, posisi X ditambah offsetIn agar mundur ke kanan
            atkX = (position.x - atkWidth) + offsetIn;
        }

        // Mengurangi Y dengan offsetDown agar posisi turun
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
        if (isDead) return;

        // Visualisasi Hitbox Serangan (Hanya muncul saat timing pas / kotak kuning)
        if (currentState == State.ATTACKING && attackRect.width > 0) {
            sr.setColor(Color.YELLOW);
            sr.rect(attackRect.x, attackRect.y, attackRect.width, attackRect.height);
        }

        if (currentState == State.CHASE) {
            sr.setColor(Color.ORANGE);
            sr.circle(position.x + WIDTH/2, position.y + HEIGHT + 10, 5);
        }
    }
}
