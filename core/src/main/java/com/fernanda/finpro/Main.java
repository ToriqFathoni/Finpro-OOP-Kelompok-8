package com.fernanda.finpro;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils; // Digunakan untuk random
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.fernanda.finpro.entities.Monster;
import com.fernanda.finpro.entities.Player;
import com.fernanda.finpro.factories.MonsterFactory;
import com.fernanda.finpro.singleton.GameAssetManager;
import com.fernanda.finpro.ui.GameHud;
import com.fernanda.finpro.world.WorldManager;

import java.util.ArrayList;
import java.util.List;

public class Main extends ApplicationAdapter {
    SpriteBatch batch;
    Player player;
    OrthographicCamera camera;
    Viewport viewport;
    GameHud gameHud;
    WorldManager worldManager;
    List<Monster> monsters;

    private static final float VIEWPORT_WIDTH = 960f;
    private static final float VIEWPORT_HEIGHT = 540f;

    ShapeRenderer worldRenderer;
    ShapeRenderer debugRenderer;
    boolean debugMode = true;

    BitmapFont font;

    // Radius Visual (Untuk menggambar lantai)
    private static final float RADIUS_FIRE = 800f;
    private static final float RADIUS_ICE = 2000f;
    private static final float RADIUS_FOREST = 3500f;

    private Vector2 tempVector = new Vector2();

    // --- SYSTEM SPAWN BARU ---
    private float spawnTimer = 0;
    private float nextSpawnDelay = 0;
    private int maxOrcCount; // Jumlah target orc maksimum di peta

    @Override
    public void create() {
        batch = new SpriteBatch();
        worldRenderer = new ShapeRenderer();
        debugRenderer = new ShapeRenderer();
        font = new BitmapFont();
        font.getData().setScale(1.5f);

        GameAssetManager.getInstance().loadImages();
        GameAssetManager.getInstance().finishLoading();

        // Spawn player di Hutan
        player = new Player(2800, 0);

        camera = new OrthographicCamera();
        viewport = new FitViewport(VIEWPORT_WIDTH, VIEWPORT_HEIGHT, camera);

        gameHud = new GameHud();
        player.stats.addListener(gameHud);
        gameHud.onHealthChanged(player.stats.getCurrentHealth(), player.stats.maxHealth);
        gameHud.onStaminaChanged(player.stats.getCurrentStamina(), player.stats.maxStamina);

        worldManager = new WorldManager();

        monsters = new ArrayList<>();

        // Setup Random Populasi (40 - 80 orc)
        maxOrcCount = MathUtils.random(40, 80);
        System.out.println("Target Populasi Orc: " + maxOrcCount);
        nextSpawnDelay = 0.5f; // Jeda awal spawn
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        gameHud.resize(width, height);
    }

    @Override
    public void render() {
        float dt = Gdx.graphics.getDeltaTime();

        // 1. UPDATE PLAYER
        player.update(dt);
        handleInvisibleWall();

        // 2. SPAWN SYSTEM & CLEANUP (Bertahap)

        // Hapus monster mati dari list (penting untuk efisiensi)
        for (int i = monsters.size() - 1; i >= 0; i--) {
            if (monsters.get(i).isDead()) {
                monsters.remove(i);
            }
        }

        // Spawn jika populasi kurang dari target maksimum
        if (monsters.size() < maxOrcCount) {
            spawnTimer += dt;
            if (spawnTimer >= nextSpawnDelay) {
                // Panggil factory untuk spawn orc baru di lokasi acak hutan
                monsters.add(MonsterFactory.createOrcInForest());

                spawnTimer = 0;
                // Random waktu spawn berikutnya (cepat: 0.2 detik - 1.5 detik)
                nextSpawnDelay = MathUtils.random(0.2f, 1.5f);
            }
        }

        // 3. UPDATE MONSTERS & COLLISION
        for (Monster m : monsters) {
            // m.update() berisi logika boundary monster
            m.update(dt);
            m.aiBehavior(dt, player);

            if (m.isDead()) continue;

            // A. Player -> Monster
            if (player.isHitboxActive()) {
                if (player.getAttackHitbox().overlaps(m.getBodyHitbox())) {
                    m.takeDamage(10);
                    Vector2 knockback = new Vector2(m.position).sub(player.position).nor().scl(10);
                    m.position.add(knockback);
                }
            }

            // B. Monster -> Player
            Rectangle mAtkRect = m.getAttackHitbox();
            if (mAtkRect.width > 0) {
                Rectangle playerBody = new Rectangle(player.position.x, player.position.y, player.getWidth(), player.getHeight());
                if (mAtkRect.overlaps(playerBody)) {
                    player.takeDamage(m.getDamage());
                }
            }
        }

        // Camera Follow
        float targetX = player.position.x + (player.getWidth() / 2);
        float targetY = player.position.y + (player.getHeight() / 2);
        camera.position.x = targetX;
        camera.position.y = targetY;
        camera.update();

        // --- RENDER ---
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Render World
        viewport.apply();
        worldRenderer.setProjectionMatrix(camera.combined);
        worldRenderer.begin(ShapeRenderer.ShapeType.Filled);
        worldRenderer.setColor(0.1f, 0.4f, 0.1f, 1f); worldRenderer.circle(0, 0, RADIUS_FOREST);
        worldRenderer.setColor(0.4f, 0.5f, 0.8f, 1f); worldRenderer.circle(0, 0, RADIUS_ICE);
        worldRenderer.setColor(0.6f, 0.2f, 0.2f, 1f); worldRenderer.circle(0, 0, RADIUS_FIRE);
        worldRenderer.end();

        // Render Player Sprite
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        player.render(batch);
        batch.end();

        // Render Monster Debug (Hitbox Only)
        Gdx.gl.glEnable(GL20.GL_BLEND);
        debugRenderer.setProjectionMatrix(camera.combined);
        debugRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Monster m : monsters) {
            m.renderDebug(debugRenderer);
        }
        debugRenderer.end();

        // Render Player Debug Hitbox
        if (debugMode) {
            debugRenderer.begin(ShapeRenderer.ShapeType.Line);
            debugRenderer.setColor(Color.RED);
            debugRenderer.rect(player.position.x, player.position.y, player.getWidth(), player.getHeight());
            if (player.isHitboxActive()) {
                debugRenderer.setColor(Color.YELLOW);
                debugRenderer.rect(player.getAttackHitbox().x, player.getAttackHitbox().y, player.getAttackHitbox().width, player.getAttackHitbox().height);
            }
            debugRenderer.end();
        }

        gameHud.render();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) Gdx.app.exit();
    }

    private void handleInvisibleWall() {
        float halfWidth = player.getWidth() / 2f;
        float halfHeight = player.getHeight() / 2f;
        float centerX = player.position.x + halfWidth;
        float centerY = player.position.y + halfHeight;

        tempVector.set(centerX, centerY);
        if (tempVector.len() > RADIUS_FOREST) {
            tempVector.setLength(RADIUS_FOREST);
            player.position.x = tempVector.x - halfWidth;
            player.position.y = tempVector.y - halfHeight;
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        worldRenderer.dispose();
        debugRenderer.dispose();
        gameHud.dispose();
        font.dispose();
        GameAssetManager.getInstance().dispose();
    }
}
