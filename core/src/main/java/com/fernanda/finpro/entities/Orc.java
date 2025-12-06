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
    private static final float ACTIVE_TIME = 0.6f; // Disesuaikan dengan durasi animasi (6 frame * 0.1s)
    private static final float RECOVERY_TIME = 1.0f;

    // --- ANIMATIONS ---
    private Animation<TextureRegion> idleAnim;
    private Animation<TextureRegion> walkAnim;
    private Animation<TextureRegion> attackAnim;
    private Animation<TextureRegion> hurtAnim;
    private Animation<TextureRegion> deathAnim;

    public Orc(float x, float y) {
        // Pass parameter ke Parent Constructor (termasuk Zone Min/Max)
        super(x, y, ORC_SPEED, ORC_HP, ORC_DMG, WIDTH, HEIGHT, HABITAT_MIN, HABITAT_MAX);

        this.detectionRadius = DETECT_RANGE;
        this.attackRadius = ATTACK_RANGE;

        // Init Animations
        // Asumsi frame count: Idle(6), Walk(8), Attack(6), Hurt(4), Death(4)
        idleAnim = createAnimation(GameAssetManager.ORC_IDLE, 6, 0.1f, Animation.PlayMode.LOOP);
        walkAnim = createAnimation(GameAssetManager.ORC_WALK, 8, 0.1f, Animation.PlayMode.LOOP);
        attackAnim = createAnimation(GameAssetManager.ORC_ATTACK, 6, 0.1f, Animation.PlayMode.NORMAL);
        hurtAnim = createAnimation(GameAssetManager.ORC_HURT, 4, 0.1f, Animation.PlayMode.NORMAL);
        deathAnim = createAnimation(GameAssetManager.ORC_DEATH, 4, 0.1f, Animation.PlayMode.NORMAL);
        
        this.deathDuration = 0.4f; // 4 frame * 0.1s
    }

    private Animation<TextureRegion> createAnimation(String assetName, int cols, float frameDuration, Animation.PlayMode mode) {
        Texture texture = GameAssetManager.getInstance().getTexture(assetName);
        
        int totalWidth = texture.getWidth();
        int totalHeight = texture.getHeight();
        
        // 1. Estimasi awal frameWidth berdasarkan input cols
        int frameWidth = totalWidth / cols;
        
        // 2. Deteksi Rows (Apakah ini Sprite Sheet Grid?)
        // Jika tinggi gambar jauh lebih besar dari lebar estimasi, kemungkinan ada banyak baris.
        int rows = 1;
        if (totalHeight > frameWidth * 1.5f) {
            rows = Math.round((float)totalHeight / frameWidth);
            if (rows < 1) rows = 1;
        }
        int frameHeight = totalHeight / rows;
        
        // 3. AUTO-CORRECT WIDTH (Enforce Square Frames)
        // Masalah: "Masih terlalu lebar" -> frameWidth > frameHeight
        // Solusi: Jika rasio tidak wajar, paksa frame menjadi kotak (Square)
        // Ini sangat efektif untuk aset pixel art RPG standar (biasanya grid kotak)
        float ratio = (float)frameWidth / frameHeight;
        if (ratio > 1.2f || ratio < 0.8f) {
            frameWidth = frameHeight; // Paksa kotak
        }
        
        TextureRegion[][] tmp = TextureRegion.split(texture, frameWidth, frameHeight);
        
        // 4. Gunakan semua frame yang valid di baris pertama
        // Ini memperbaiki masalah jika jumlah kolom asli ternyata lebih banyak dari 'cols'
        int framesToUse = tmp[0].length;
        
        TextureRegion[] frames = new TextureRegion[framesToUse];
        for (int i = 0; i < framesToUse; i++) {
            TextureRegion region = tmp[0][i];
            
            // 5. Trim Safety (Mencegah Bleeding/Terhit gambar sebelah)
            // Mengurangi lebar region 2 pixel dari kanan
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

        float distToPlayer = position.dst(player.position);

        // Logika Scalable: Cek apakah player ada di dalam habitat Orc
        // Agar Orc tidak mengejar player sampai ke Ice/Laut jika player kabur
        boolean playerInHabitat = player.position.len() >= HABITAT_MIN && player.position.len() <= HABITAT_MAX;

        switch (currentState) {
            case HURT:
                // Durasi Hurt disesuaikan dengan animasi (4 frame * 0.1s = 0.4s)
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

        // Fix Rapid Flipping (Jitter)
        // Hanya ubah arah jika kecepatan horizontal signifikan (> 1.0f)
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
        float atkSize = 30f;
        float atkX = facingRight ? (position.x + WIDTH) : (position.x - atkSize);
        float atkY = position.y + (HEIGHT / 2) - (atkSize / 2);

        attackRect.set(atkX, atkY, atkSize, atkSize);
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

        // Draw centered
        float width = currentFrame.getRegionWidth();
        float height = currentFrame.getRegionHeight();
        float drawX = position.x + (WIDTH - width) / 2f;
        float drawY = position.y + (HEIGHT - height) / 2f;

        // Handle Flipping (Tanpa merusak TextureRegion asli)
        // Asumsi: Asset asli menghadap ke KANAN
        if (facingRight) {
            batch.draw(currentFrame, drawX, drawY, width, height);
        } else {
            // Flip Horizontal: Geser X sejauh width, lalu gambar dengan width negatif
            batch.draw(currentFrame, drawX + width, drawY, -width, height);
        }
    }

    @Override
    public void renderDebug(ShapeRenderer sr) {
        if (isDead) return;

        // Block merah dihapus agar tidak menutupi sprite
        // if (immunityTimer > 0) sr.setColor(Color.WHITE);
        // else sr.setColor(Color.RED);
        // sr.rect(bodyRect.x, bodyRect.y, bodyRect.width, bodyRect.height);

        if (currentState == State.ATTACKING) {
            // sr.setColor(Color.YELLOW);
            // sr.rect(attackRect.x, attackRect.y, attackRect.width, attackRect.height);
        }

        if (currentState == State.CHASE) {
            sr.setColor(Color.ORANGE);
            sr.circle(position.x + WIDTH/2, position.y + HEIGHT + 10, 5);
        }
    }
}
