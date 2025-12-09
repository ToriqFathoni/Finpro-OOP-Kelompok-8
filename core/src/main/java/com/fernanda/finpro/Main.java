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

    private boolean isGameOver = false;

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

        if (!isGameOver) {
            player.update(dt);
            handleInvisibleWall();

            if (player.stats.getCurrentHealth() <= 0) {
                if (player.isDeathAnimationFinished()) {
                    isGameOver = true;
                }
            }

            // 2. SPAWN SYSTEM & CLEANUP
            for (int i = monsters.size() - 1; i >= 0; i--) {
                if (monsters.get(i).canBeRemoved()) {
                    monsters.remove(i);
                }
            }

            if (monsters.size() < maxOrcCount) {
                spawnTimer += dt;
                if (spawnTimer >= nextSpawnDelay) {
                    monsters.add(MonsterFactory.createOrcInForest());

                    spawnTimer = 0;
                    // Random waktu spawn berikutnya (cepat: 0.2 detik - 1.2 detik)
                    nextSpawnDelay = MathUtils.random(0.2f, 1.2f);
                }
            }

            // 3. UPDATE MONSTERS & COLLISION
            for (Monster m : monsters) {
                m.update(dt);
                m.aiBehavior(dt, player);

                if (m.isDead()) continue;

                Rectangle playerBody = new Rectangle(player.position.x, player.position.y, player.getWidth(), player.getHeight());

                // Player Serang Monster (Player Menyerang)
                if (player.isHitboxActive()) {
                    if (player.getAttackHitbox().overlaps(m.getBodyHitbox())) {
                        m.takeDamage(10);
                        // Knockback Monster
                        Vector2 knockback = new Vector2(m.position).sub(player.position).nor().scl(10);
                        m.position.add(knockback);
                    }
                }

                // Monster Serang Player (Monster Mengayunkan Senjata)
                Rectangle mAtkRect = m.getAttackHitbox();
                if (mAtkRect.width > 0) {
                    if (mAtkRect.overlaps(playerBody)) {
                        player.takeDamage(m.getDamage());
                    }
                }

                // C. Contact Damage
                if (playerBody.overlaps(m.getBodyHitbox())) {
                    player.takeDamage(5);

                    // 2. Efek Push Back Player
                    if (!player.isDodging()) {
                        Vector2 pushDirection = new Vector2(player.position).sub(m.position).nor();

                        // Kekuatan dorongan (sesuaikan)
                        float pushForce = 300f * dt;

                        player.position.mulAdd(pushDirection, pushForce);
                    }
                }
            }
        } else {
            // Game Over Logic
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
                restartGame();
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

        // --- RENDER GRID (Agar pergerakan terlihat) ---
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        worldRenderer.begin(ShapeRenderer.ShapeType.Line);
        worldRenderer.setColor(1f, 1f, 1f, 0.15f); // Putih transparan

        float gridSize = 100f;

        // Hitung batas kamera agar tidak menggambar grid di seluruh dunia (Optimasi)
        float startX = camera.position.x - camera.viewportWidth / 2;
        float endX = camera.position.x + camera.viewportWidth / 2;
        float startY = camera.position.y - camera.viewportHeight / 2;
        float endY = camera.position.y + camera.viewportHeight / 2;

        // Snap ke grid terdekat
        startX = (float)Math.floor(startX / gridSize) * gridSize;
        startY = (float)Math.floor(startY / gridSize) * gridSize;

        // Gambar Garis Vertikal
        for (float x = startX; x <= endX; x += gridSize) {
            worldRenderer.line(x, startY, x, endY);
        }

        // Gambar Garis Horizontal
        for (float y = startY; y <= endY; y += gridSize) {
            worldRenderer.line(startX, y, endX, y);
        }

        worldRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // Render Player & Monster Sprites
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        for (Monster m : monsters) {
            m.render(batch);
        }
        player.render(batch);
        batch.end();

        // Render Monster Debug (Hitbox Only)
        Gdx.gl.glEnable(GL20.GL_BLEND);
        debugRenderer.setProjectionMatrix(camera.combined);
        debugRenderer.begin(ShapeRenderer.ShapeType.Line);
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

        if (isGameOver) {
            batch.begin();
            font.setColor(Color.RED);
            font.draw(batch, "GAME OVER - Press R to Restart", player.position.x - 100, player.position.y + 100);
            batch.end();
        }

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

    private void restartGame() {
        player.reset(2800, 0);
        monsters.clear();
        spawnTimer = 0;
        isGameOver = false;
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
