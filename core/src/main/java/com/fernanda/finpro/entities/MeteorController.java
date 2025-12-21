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
import com.fernanda.finpro.singleton.GameAssetManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MeteorController {

    private class Meteor {
        Vector2 position;
        float targetY;
        float startY;
        float stateTimer;
        boolean isExploding;
        boolean shouldRemove;

        Rectangle hitbox;

        public Meteor(float x, float targetY) {
            this.targetY = targetY;
            this.startY = 1168f + 200f;
            this.position = new Vector2(x, startY);

            this.stateTimer = 0;
            this.isExploding = false;
            this.shouldRemove = false;

            this.hitbox = new Rectangle(x - 15, startY, 30, 30);
        }
    }

    private List<Meteor> activeMeteors;
    private boolean isRaining;
    private float rainDurationTimer;
    private float spawnTimer;

    private final float RAIN_DURATION = 3.0f;
    private final float SPAWN_INTERVAL = 0.25f;
    private final float FALL_SPEED = 700f;
    private final int DAMAGE = 70;

    private Animation<TextureRegion> fireballAnim;
    private Animation<TextureRegion> explosionAnim;
    private Texture shadowTexture;

    public MeteorController() {
        activeMeteors = new ArrayList<>();
        isRaining = false;

        Texture fireballTex = GameAssetManager.getInstance().getTexture("Fireball.png");
        if (fireballTex != null) {
            fireballAnim = createAnimation(fireballTex, 3, 0.1f, Animation.PlayMode.LOOP);
        }

        Texture explosionTex = GameAssetManager.getInstance().getTexture("Explosion.png");
        if (explosionTex != null) {
            explosionAnim = createAnimation(explosionTex, 5, 0.08f, Animation.PlayMode.NORMAL);
        }

        this.shadowTexture = GameAssetManager.getInstance().getTexture(GameAssetManager.SHADOW);
    }

    private Animation<TextureRegion> createAnimation(Texture texture, int cols, float duration, Animation.PlayMode mode) {
        TextureRegion[][] tmp = TextureRegion.split(texture, texture.getWidth() / cols, texture.getHeight());
        TextureRegion[] frames = new TextureRegion[cols];
        System.arraycopy(tmp[0], 0, frames, 0, cols);
        Animation<TextureRegion> anim = new Animation<>(duration, frames);
        anim.setPlayMode(mode);
        return anim;
    }

    public void startRain() {
        isRaining = true;
        rainDurationTimer = 0;
        spawnTimer = 0;
    }

    public void update(float dt, Player player) {
        if (isRaining) {
            rainDurationTimer += dt;
            spawnTimer += dt;
            if (spawnTimer >= SPAWN_INTERVAL) {
                spawnMeteor(player);
                spawnTimer = 0;
            }
            if (rainDurationTimer >= RAIN_DURATION) isRaining = false;
        }

        Iterator<Meteor> iter = activeMeteors.iterator();
        while (iter.hasNext()) {
            Meteor m = iter.next();
            m.stateTimer += dt;

            m.hitbox.setPosition(m.position.x - m.hitbox.width/2, m.position.y);

            if (!m.isExploding) {
                m.position.y -= FALL_SPEED * dt;

                if (m.position.y <= m.targetY) {
                    m.position.y = m.targetY;
                    m.isExploding = true;
                    m.stateTimer = 0;

                    // CONTOH: Mengubah ukuran menjadi 150 (Lebar) x 100 (Tinggi)
                    float newWidth = 200f;
                    float newHeight = 90f;

                    m.hitbox.setSize(newWidth, newHeight);

                    // Geser X ke kiri sebesar setengah dari lebar baru agar pas di tengah
                    m.hitbox.setPosition(m.position.x - (newWidth / 2), m.position.y);

                    checkDamage(m, player);
                }
            } else {
                if (explosionAnim.isAnimationFinished(m.stateTimer)) {
                    m.shouldRemove = true;
                }
            }

            if (m.shouldRemove) iter.remove();
        }
    }

    private void spawnMeteor(Player player) {
        // --- KONFIGURASI AREA SPAWN (SESUAI KOTAK BIRU) ---
        // X: Beri sedikit margin kiri kanan agar tidak terlalu mepet tembok (50 - 1118)
        float minX = 50f;
        float maxX = 1168f - 50f;

        // Y: Area bawah saja.
        // Y=50 (sedikit di atas batas bawah layar)
        // Y=600 (kira-kira batas antara lantai dan dinding lava)
        float minY = 50f;
        float maxY = 600f;
        // ---------------------------------------------------

        float randX = MathUtils.random(minX, maxX);
        float randY = MathUtils.random(minY, maxY);

        // Mekanisme 50% meteor mengincar posisi player
        if (MathUtils.randomBoolean(0.5f)) {
            randX = player.position.x;
            randY = player.position.y;
        }

        activeMeteors.add(new Meteor(randX, randY));
    }

    private void checkDamage(Meteor m, Player player) {
        if (player.getHitbox().overlaps(m.hitbox)) {
            player.takeDamage(DAMAGE);
        }
    }

    public void render(SpriteBatch batch) {
        for (Meteor m : activeMeteors) {

            // --- RENDER BAYANGAN (JUMBO VERSION) ---
            if (!m.isExploding && shadowTexture != null) {

                float distTotal = m.startY - m.targetY;
                float distCurrent = m.position.y - m.targetY;
                float progress = 1.0f - (distCurrent / distTotal);

                // Scale effect: Membesar dari 60% ke 120%
                float scale = MathUtils.clamp(progress, 0.6f, 1.2f);

                // --- BAGIAN INI DIUBAH MENJADI LEBIH BESAR DARI 200f ---
                // Kita set Base Width 300f.
                // Hasil akhirnya saat jatuh akan menjadi 360f (Sangat Besar).
                float baseWidth = 350f;
                float baseHeight = 175f; // Setengah dari width agar gepeng proporsional

                float shadowW = baseWidth * scale;
                float shadowH = baseHeight * scale;

                // WARNA HITAM TRANSPARAN
                // 0f, 0f, 0f = Hitam
                // 0.6f = Transparansi (semakin tinggi semakin gelap/pekat)
                batch.setColor(0f, 0f, 0f, 0.6f);

                batch.draw(shadowTexture,
                    m.position.x - shadowW/2,
                    m.targetY - shadowH/2 + 5,
                    shadowW, shadowH);

                // RESET WARNA (PENTING)
                batch.setColor(Color.WHITE);
            }
            // ---------------------------------------

            TextureRegion currentFrame;
            float w, h;
            float drawOffsetY;

            if (!m.isExploding) {
                currentFrame = fireballAnim.getKeyFrame(m.stateTimer, true);
                w = currentFrame.getRegionWidth() * 1.5f;
                h = currentFrame.getRegionHeight() * 1.5f;
                drawOffsetY = 0;
            } else {
                currentFrame = explosionAnim.getKeyFrame(m.stateTimer, false);
                w = currentFrame.getRegionWidth() * 2.0f;
                h = currentFrame.getRegionHeight() * 2.0f;
                drawOffsetY = 0;
            }

            if (currentFrame != null) {
                batch.draw(currentFrame,
                    m.position.x - w/2,
                    m.position.y + drawOffsetY,
                    w, h);
            }
        }
    }

    public void renderDebug(ShapeRenderer shapeRenderer) {
        /*
        for (Meteor m : activeMeteors) {
            if (m.isExploding) {
                shapeRenderer.setColor(Color.RED);
            } else {
                shapeRenderer.setColor(Color.YELLOW);
            }
            shapeRenderer.rect(m.hitbox.x, m.hitbox.y, m.hitbox.width, m.hitbox.height);

            if (!m.isExploding) {
                shapeRenderer.setColor(Color.GRAY);
                shapeRenderer.line(m.position.x, m.position.y, m.position.x, m.targetY);
            }
        }
        */
    }

    public void reset() {
        this.isRaining = false;
        this.activeMeteors.clear();
        this.rainDurationTimer = 0;
        this.spawnTimer = 0;
    }
}
