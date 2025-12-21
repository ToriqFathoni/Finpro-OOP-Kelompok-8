package com.fernanda.finpro;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.fernanda.finpro.components.ItemType;
import com.fernanda.finpro.entities.Boss;
import com.fernanda.finpro.entities.GroundItem;
import com.fernanda.finpro.entities.Monster;
import com.fernanda.finpro.entities.Player;
import com.fernanda.finpro.managers.CollisionManager;
import com.fernanda.finpro.managers.SpawnManager;
import com.fernanda.finpro.objects.Campfire;
import com.fernanda.finpro.pool.GroundItemPool;
import com.fernanda.finpro.singleton.GameAssetManager;
import com.fernanda.finpro.ui.GameHud;
import com.fernanda.finpro.ui.InventoryUI;
import com.fernanda.finpro.ui.CookingMenu;
import com.fernanda.finpro.ui.LeaderboardUI;
import com.fernanda.finpro.ui.TutorialPopup;
import com.fernanda.finpro.ui.LoginUI;
import com.fernanda.finpro.managers.NetworkManager;
import com.fernanda.finpro.enums.WorldType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Main extends ApplicationAdapter {
    SpriteBatch batch;
    Player player;
    OrthographicCamera camera;
    Viewport viewport;
    GameHud gameHud;
    InventoryUI inventoryUI;
    CookingMenu cookingMenu;
    LeaderboardUI leaderboardUI;
    LoginUI loginUI;
    TiledMap map;
    OrthogonalTiledMapRenderer mapRenderer;
    List<Monster> monsters;
    List<GroundItem> groundItems;
    List<Renderable> renderQueue;

    // Object Pool Pattern - Reuse GroundItems
    GroundItemPool groundItemPool;

    Campfire campfire;
    TutorialPopup tutorialPopup;

    private boolean isInventoryOpen = false;
    private boolean gamePaused = false;
    private boolean isCookingMenuOpen = false;

    private static final float VIEWPORT_WIDTH = 800f;
    private static final float VIEWPORT_HEIGHT = 450f;

    ShapeRenderer worldRenderer;
    ShapeRenderer debugRenderer;
    boolean debugMode = false;
    BitmapFont font;

    private SpawnManager spawnManager;
    private CollisionManager collisionManager;
    private Vector2 playerSpawnPoint = new Vector2(100, 100);
    private boolean isGameOver = false;
    private WorldType currentWorld = WorldType.FOREST;
    private float gameOverTimer = 0f;
    private static final float GAME_OVER_FADE_DURATION = 1.5f;
    private com.badlogic.gdx.audio.Music currentMusic;

    @Override
    public void create() {
        batch = new SpriteBatch();
        worldRenderer = new ShapeRenderer();
        debugRenderer = new ShapeRenderer();
        font = new BitmapFont();
        font.getData().setScale(0.8f);

        GameAssetManager.getInstance().loadImages();
        GameAssetManager.getInstance().finishLoading();
        GameAssetManager.getInstance().loadMusic();
        GameAssetManager.getInstance().loadSounds();

        map = GameAssetManager.getInstance().getMap();
        mapRenderer = new OrthogonalTiledMapRenderer(map, 1.0f);

        MapLayer layer = map.getLayers().get("spawn_player");
        if (layer instanceof TiledMapTileLayer) {
            TiledMapTileLayer spawnLayer = (TiledMapTileLayer) layer;
            boolean found = false;
            for (int x = 0; x < spawnLayer.getWidth(); x++) {
                for (int y = 0; y < spawnLayer.getHeight(); y++) {
                    TiledMapTileLayer.Cell cell = spawnLayer.getCell(x, y);
                    if (cell != null) {
                        playerSpawnPoint.set(x * 16, y * 16);
                        found = true;
                        break;
                    }
                }
                if (found) break;
            }
        }

        player = new Player(playerSpawnPoint.x, playerSpawnPoint.y);
        camera = new OrthographicCamera();
        viewport = new FitViewport(VIEWPORT_WIDTH, VIEWPORT_HEIGHT, camera);
        gameHud = new GameHud();
        inventoryUI = new InventoryUI();
        cookingMenu = new CookingMenu();
        leaderboardUI = new LeaderboardUI();

        loginUI = new LoginUI(viewport, new LoginUI.LoginListener() {
            @Override
            public void onLoginSuccess(String username, Map<String, Integer> inventoryData, boolean miniBossDefeated, boolean bossKilled) {
                player.inventory.clear();
                for (Map.Entry<String, Integer> entry : inventoryData.entrySet()) {
                    try {
                        ItemType type = ItemType.valueOf(entry.getKey());
                        player.inventory.addItem(type, entry.getValue());
                    } catch (IllegalArgumentException e) {
                        System.err.println("Unknown item type: " + entry.getKey());
                    }
                }
                // Set MiniBoss defeated state
                NetworkManager.getInstance().setMiniBossDefeated(miniBossDefeated);
                spawnManager.setMiniBossDefeated(miniBossDefeated);
                
                // Set Boss Killed state
                player.bossKilled = bossKilled;
                
                System.out.println("Logged in as: " + username + ", MiniBoss defeated: " + miniBossDefeated + ", Boss Killed: " + bossKilled);
            }
        });

        player.stats.addListener(gameHud);
        gameHud.onHealthChanged(player.stats.getCurrentHealth(), player.stats.maxHealth);
        gameHud.onStaminaChanged(player.stats.getCurrentStamina(), player.stats.maxStamina);

        monsters = new ArrayList<>();
        groundItems = new ArrayList<>();
        renderQueue = new ArrayList<>();

        // Object Pool Pattern - Initialize with 50 initial, max 200
        groundItemPool = new GroundItemPool(50, 200);
        System.out.println("✅ GroundItemPool initialized (Object Pool Pattern)");

        Vector2 campfirePos = new Vector2(playerSpawnPoint.x + 64, playerSpawnPoint.y);
        MapLayer campfireLayer = map.getLayers().get("campfire");
        if (campfireLayer instanceof TiledMapTileLayer) {
            TiledMapTileLayer cfLayer = (TiledMapTileLayer) campfireLayer;
            boolean found = false;
            for (int x = 0; x < cfLayer.getWidth(); x++) {
                for (int y = 0; y < cfLayer.getHeight(); y++) {
                    if (cfLayer.getCell(x, y) != null) {
                        campfirePos.set(x * 16, y * 16);
                        found = true;
                        break;
                    }
                }
                if (found) break;
            }
        }
        campfire = new Campfire(campfirePos.x, campfirePos.y);

        tutorialPopup = new TutorialPopup();
        spawnManager = new SpawnManager(monsters);
        collisionManager = new CollisionManager(player, monsters);

        com.badlogic.gdx.audio.Music lobby = GameAssetManager.getInstance().getLobbyMusic();
        if (lobby != null && !lobby.isPlaying()) {
            lobby.play();
        }

        System.out.println("✅ DEBUG: Game Started Successfully!");
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        gameHud.resize(width, height);
        inventoryUI.resize(width, height);
        cookingMenu.resize(width, height);
    }

    @Override
    public void render() {
        if (loginUI.isVisible()) {
            Gdx.gl.glClearColor(0, 0, 0, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            loginUI.render();
            return;
        }

        if (!loginUI.isVisible() && currentMusic == null) {
            com.badlogic.gdx.audio.Music lobby = GameAssetManager.getInstance().getLobbyMusic();
            if (lobby != null && lobby.isPlaying()) {
                lobby.stop();
            }
            playMusic(currentWorld);
        }

        float dt = Gdx.graphics.getDeltaTime();

        // Handle tutorial and menu inputs even when game over (for consistency)
        if (Gdx.input.isKeyJustPressed(Input.Keys.H) && !isGameOver) {
            tutorialPopup.toggle();
        }

        if (tutorialPopup.isVisible() && Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            tutorialPopup.hide();
        }

        if (!isGameOver) {
            if (!tutorialPopup.isVisible()) {
                if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
                    if (!isCookingMenuOpen) {
                        isInventoryOpen = !isInventoryOpen;
                        gamePaused = isInventoryOpen;
                        if (!isInventoryOpen) {
                            NetworkManager.getInstance().saveInventory(player);
                        }
                    }
                }

                if (Gdx.input.isKeyJustPressed(Input.Keys.C)) {
                    if (!isInventoryOpen && campfire.isPlayerNearby(player)) {
                        isCookingMenuOpen = !isCookingMenuOpen;
                        cookingMenu.setVisible(isCookingMenuOpen);
                        gamePaused = isCookingMenuOpen;
                    }
                }

                if (isCookingMenuOpen) {
                    cookingMenu.update(player.inventory, player);
                    if (!cookingMenu.isVisible()) {
                        isCookingMenuOpen = false;
                        gamePaused = false;
                    }
                }

                if (!gamePaused) {
                    player.update(dt);
                    handleMapCollision();

                    player.position.x = MathUtils.clamp(player.position.x, 0, 1168 - player.getWidth());
                    player.position.y = MathUtils.clamp(player.position.y, 0, 1168 - player.getHeight());

                    Iterator<GroundItem> itemIterator = groundItems.iterator();
                    while (itemIterator.hasNext()) {
                        GroundItem item = itemIterator.next();
                        item.update(dt);
                        if (item.isActive() && item.getHitbox().overlaps(player.getHitbox())) {
                            player.inventory.addItem(item.getType(), 1);
                            itemIterator.remove();
                            // Object Pool Pattern - Return to pool instead of garbage collection
                            groundItemPool.free(item);
                            NetworkManager.getInstance().saveInventory(player);
                        }
                    }

                    Iterator<Monster> monsterIterator = monsters.iterator();
                    while (monsterIterator.hasNext()) {
                        Monster m = monsterIterator.next();
                        if (m.canBeRemoved()) {
                            // Check if it's MiniBoss being removed
                            if (m instanceof com.fernanda.finpro.entities.MiniBoss) {
                                NetworkManager.getInstance().setMiniBossDefeated(true);
                                spawnManager.setMiniBossDefeated(true);
                                System.out.println("MiniBoss defeated! State saved.");
                            }
                            
                            ItemType drop = m.rollDrop();
                            if (drop != null) {
                                // Object Pool Pattern - Obtain from pool instead of new
                                GroundItem droppedItem = groundItemPool.obtain(drop, m.position.x, m.position.y);
                                if (droppedItem != null) {
                                    groundItems.add(droppedItem);
                                }
                            }
                            
                            // Update Score
                            player.monsterKillScore += 10;
                            
                            monsterIterator.remove();
                        }
                    }

                    spawnManager.update(dt);

                    // --- 4. UPDATE BOSS ---
                    Boss boss = spawnManager.getBoss();
                    if (boss != null) {
                        boss.update(dt, player);

                        // Cek Player pukul Boss (Menggunakan Hitbox Player)
                        if (player.isHitboxActive()) {
                            boss.checkHitByPlayer(player.getAttackHitbox(), 25);
                        }
                        
                        // Check Boss Death
                        if (boss.isDead() && !player.bossKilled) {
                            player.bossKilled = true;
                            
                            // Send Score
                            NetworkManager.getInstance().updateScore(
                                NetworkManager.getInstance().getCurrentUsername(),
                                player.cookingScore,
                                player.monsterKillScore,
                                true,
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        // Fetch Leaderboard AFTER score update succeeds
                                        NetworkManager.getInstance().getLeaderboard(new NetworkManager.LeaderboardCallback() {
                                            @Override
                                            public void onSuccess(java.util.List<NetworkManager.LeaderboardEntry> entries) {
                                                leaderboardUI.show(entries);
                                            }
                                            
                                            @Override
                                            public void onFailure(Throwable t) {
                                                System.err.println("Failed to fetch leaderboard: " + t.getMessage());
                                            }
                                        });
                                    }
                                }
                            );
                        }
                    }
                    // ----------------------

                    for (Monster m : monsters) {
                        m.update(dt);
                        m.aiBehavior(dt, player);
                        handleEntityCollision(m.getBodyHitbox(), m.position, m.getBodyHitbox().width, m.getBodyHitbox().height);
                    }
                    collisionManager.update(dt);
                } else {
                    boolean shouldClose = inventoryUI.handleInput(player, player.inventory);
                    if (shouldClose) {
                        isInventoryOpen = false;
                        gamePaused = false;
                    }
                }
            }

            if (player.stats.getCurrentHealth() <= 0) {
                if (player.isDeathAnimationFinished()) {
                    isGameOver = true;
                    gameOverTimer = 0f;  // Reset timer when first entering game over
                }
            }
        }

        // === GAME OVER STATE: Continue world animation but disable player control ===
        if (isGameOver) {
            gameOverTimer += dt;
            
            // Keep monsters and world alive/animated
            for (Monster m : monsters) {
                m.update(dt);
                m.aiBehavior(dt, player);
            }
            
            // Update boss if present
            Boss boss = spawnManager.getBoss();
            if (boss != null) {
                boss.update(dt, player);
            }
            
            // Handle restart input
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
                restartGame();
            }
        }

        if (currentWorld == WorldType.INFERNO) {
            camera.position.set(584, 584, 0);
        } else {
            float targetX = player.position.x + (player.getWidth() / 2);
            float targetY = player.position.y + (player.getHeight() / 2);
            float mapWidth = 1168f;
            float mapHeight = 1168f;
            float visibleWidth = camera.viewportWidth * camera.zoom;
            float visibleHeight = camera.viewportHeight * camera.zoom;
            float minX = visibleWidth / 2;
            float maxX = mapWidth - visibleWidth / 2;
            float minY = visibleHeight / 2;
            float maxY = mapHeight - visibleHeight / 2;

            if (mapWidth < visibleWidth) {
                camera.position.x = mapWidth / 2;
            } else {
                camera.position.x = MathUtils.clamp(targetX, minX, maxX);
            }

            if (mapHeight < visibleHeight) {
                camera.position.y = mapHeight / 2;
            } else {
                camera.position.y = MathUtils.clamp(targetY, minY, maxY);
            }
        }
        camera.update();

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply();

        mapRenderer.setView(camera);
        mapRenderer.render();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        renderQueue.clear();
        for (Monster m : monsters) {
            renderQueue.add(new Renderable(m.position.y, () -> m.render(batch)));
        }
        renderQueue.add(new Renderable(player.position.y, () -> player.render(batch)));
        for (GroundItem item : groundItems) {
            renderQueue.add(new Renderable(item.getPosition().y, () -> item.render(batch)));
        }
        renderQueue.add(new Renderable(campfire.getPosition().y, () -> campfire.render(batch)));

        // --- RENDER BOSS ---
        Boss boss = spawnManager.getBoss();
        if (boss != null) {
            renderQueue.add(new Renderable(boss.position.y, () -> boss.render(batch)));
        }
        // -------------------

        Collections.sort(renderQueue);
        for (Renderable r : renderQueue) {
            r.renderTask.run();
        }

        batch.end();

        worldRenderer.setProjectionMatrix(camera.combined);
        worldRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Monster m : monsters) {
            if (!m.isDead()) {
                float hpPercent = (float) m.getCurrentHealth() / m.getMaxHealth();
                float barWidth = 20f;
                float barHeight = 4f;
                float barX = m.position.x + (m.getBodyHitbox().width - barWidth) / 2;
                float barY = m.position.y + m.getBodyHitbox().height + 5f;

                worldRenderer.setColor(Color.RED);
                worldRenderer.rect(barX, barY, barWidth, barHeight);
                worldRenderer.setColor(Color.GREEN);
                worldRenderer.rect(barX, barY, barWidth * hpPercent, barHeight);
            }
        }
        worldRenderer.end();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        debugRenderer.setProjectionMatrix(camera.combined);
        debugRenderer.begin(ShapeRenderer.ShapeType.Line);
        for (Monster m : monsters) {
            m.renderDebug(debugRenderer);
        }

        // --- 4. RENDER DEBUG BOSS ---
        Boss bossDebug = spawnManager.getBoss();
        if (bossDebug != null) {
            bossDebug.renderDebug(debugRenderer);
        }
        // --------------------

        debugRenderer.end();

        if (debugMode) {
            debugRenderer.begin(ShapeRenderer.ShapeType.Line);
            debugRenderer.setColor(Color.RED);
            Rectangle hitbox = player.getHitbox();
            debugRenderer.rect(hitbox.x, hitbox.y, hitbox.width, hitbox.height);

            if (player.isHitboxActive()) {
                debugRenderer.setColor(Color.YELLOW);
                debugRenderer.rect(player.getAttackHitbox().x, player.getAttackHitbox().y,
                    player.getAttackHitbox().width, player.getAttackHitbox().height);
            }
            debugRenderer.end();
        }

        gameHud.render(batch, player);

        if (isInventoryOpen) {
            inventoryUI.render(player.inventory);
        }

        if (isCookingMenuOpen) {
            com.badlogic.gdx.math.Matrix4 uiMatrix = new com.badlogic.gdx.math.Matrix4();
            uiMatrix.setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            batch.setProjectionMatrix(uiMatrix);
            worldRenderer.setProjectionMatrix(uiMatrix);
            cookingMenu.render(batch, worldRenderer, player.inventory);
        }

        if (leaderboardUI.isVisible()) {
            com.badlogic.gdx.math.Matrix4 uiMatrix = new com.badlogic.gdx.math.Matrix4();
            uiMatrix.setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            batch.setProjectionMatrix(uiMatrix);
            worldRenderer.setProjectionMatrix(uiMatrix);
            leaderboardUI.render(batch, worldRenderer);
        }

        if (tutorialPopup.isVisible()) {
            com.badlogic.gdx.math.Matrix4 uiMatrix = new com.badlogic.gdx.math.Matrix4();
            uiMatrix.setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            batch.setProjectionMatrix(uiMatrix);
            debugRenderer.setProjectionMatrix(uiMatrix);
            tutorialPopup.render(batch, debugRenderer);
        }

        // === DRAMATIC DARK SOULS STYLE GAME OVER SCREEN ===
        if (isGameOver) {
            com.badlogic.gdx.math.Matrix4 uiMatrix = new com.badlogic.gdx.math.Matrix4();
            uiMatrix.setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            
            // Calculate fade alpha (gradually darken the screen)
            float fadeAlpha = Math.min(gameOverTimer / GAME_OVER_FADE_DURATION, 0.7f);
            
            // A. DIM THE SCREEN with semi-transparent black overlay
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            worldRenderer.setProjectionMatrix(uiMatrix);
            worldRenderer.begin(ShapeRenderer.ShapeType.Filled);
            worldRenderer.setColor(0, 0, 0, fadeAlpha);
            worldRenderer.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            worldRenderer.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);

            // B. DRAW LARGE "YOU DIED" TEXT (only after fade is visible)
            if (gameOverTimer > 0.3f) {
                batch.setProjectionMatrix(uiMatrix);
                batch.begin();
                
                // Calculate text alpha (fade in the text after slight delay)
                float textAlpha = Math.min((gameOverTimer - 0.3f) / 0.8f, 1.0f);
                
                // LARGE RED "YOU DIED" TEXT
                font.getData().setScale(5.0f);  // Extra large
                font.setColor(1f, 0f, 0f, textAlpha);  // Red with alpha
                
                GlyphLayout youDiedLayout = new GlyphLayout(font, "YOU DIED");
                float youDiedX = (Gdx.graphics.getWidth() - youDiedLayout.width) / 2f;
                float youDiedY = (Gdx.graphics.getHeight() / 2f) + 60f;
                font.draw(batch, youDiedLayout, youDiedX, youDiedY);
                
                // SMALLER "Press R to Restart" TEXT
                font.getData().setScale(2.0f);
                font.setColor(1f, 1f, 1f, textAlpha * 0.9f);  // White with alpha
                
                GlyphLayout restartLayout = new GlyphLayout(font, "Press R to Restart");
                float restartX = (Gdx.graphics.getWidth() - restartLayout.width) / 2f;
                float restartY = youDiedY - 100f;
                font.draw(batch, restartLayout, restartX, restartY);
                
                // Reset font scale to normal
                font.getData().setScale(0.8f);
                
                batch.end();
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                Gdx.app.exit();
            }
        }
    }

    private void handleMapCollision() {
        Rectangle playerRect = player.getHitbox();
        handleEntityCollision(playerRect, player.position, player.getWidth(), player.getHeight());
        checkWorldTransition(playerRect);
    }

    private void checkWorldTransition(Rectangle playerRect) {
        TiledMapTileLayer transitionLayer;

        if (currentWorld == WorldType.FOREST) {
            transitionLayer = (TiledMapTileLayer) map.getLayers().get("next_map");
            if (checkLayerCollision(playerRect, transitionLayer)) {
                switchToIceWorld();
            }
        } else if (currentWorld == WorldType.ICE) {
            transitionLayer = (TiledMapTileLayer) map.getLayers().get("back_map_forest");
            if (checkLayerCollision(playerRect, transitionLayer)) {
                switchToForestWorld();
                return;
            }

            transitionLayer = (TiledMapTileLayer) map.getLayers().get("inferno_next_map");
            if (checkLayerCollision(playerRect, transitionLayer)) {
                switchToInfernoWorld();
            }
        } else if (currentWorld == WorldType.INFERNO) {
            transitionLayer = (TiledMapTileLayer) map.getLayers().get("exit_player_inferno");
            if (checkLayerCollision(playerRect, transitionLayer)) {
                switchToIceWorldFromInferno();
            }
        }
    }

    private boolean checkLayerCollision(Rectangle playerRect, TiledMapTileLayer layer) {
        if (layer != null) {
            int tileX = (int) ((playerRect.x + playerRect.width / 2) / 16);
            int tileY = (int) ((playerRect.y + playerRect.height / 2) / 16);
            TiledMapTileLayer.Cell cell = layer.getCell(tileX, tileY);
            return cell != null && cell.getTile() != null;
        }
        return false;
    }

    private void switchToIceWorld() {
        System.out.println("Switching to Ice World!");
        currentWorld = WorldType.ICE;
        spawnManager.setWorld(currentWorld);
        spawnManager.despawnBoss();
        gameHud.setBoss(null);
        map = GameAssetManager.getInstance().getIceMap();
        mapRenderer.setMap(map);
        camera.zoom = 1.0f;
        setPlayerSpawn("spawn_ice_player");
        resetWorldState();
        playMusic(WorldType.ICE);
    }

    private void switchToIceWorldFromInferno() {
        System.out.println("Switching to Ice World (From Inferno)!");
        currentWorld = WorldType.ICE;
        spawnManager.setWorld(currentWorld);
        spawnManager.despawnBoss();
        gameHud.setBoss(null);
        map = GameAssetManager.getInstance().getIceMap();
        mapRenderer.setMap(map);
        camera.zoom = 1.0f;
        setPlayerSpawn("back_spawn_inferno");
        resetWorldState();
        playMusic(WorldType.ICE);
    }

    private void switchToForestWorld() {
        System.out.println("Switching to Forest World!");
        currentWorld = WorldType.FOREST;
        spawnManager.setWorld(currentWorld);
        spawnManager.despawnBoss();
        gameHud.setBoss(null);
        map = GameAssetManager.getInstance().getMap();
        mapRenderer.setMap(map);
        camera.zoom = 1.0f;
        setPlayerSpawn("spawn_player_back");
        resetWorldState();
        playMusic(WorldType.FOREST);
    }

    private void switchToInfernoWorld() {
        System.out.println("Switching to Inferno World!");
        currentWorld = WorldType.INFERNO;
        spawnManager.setWorld(currentWorld);
        map = GameAssetManager.getInstance().getLavaMap();
        mapRenderer.setMap(map);
        camera.zoom = 2.6f;
        setPlayerSpawn("spawn_player_inferno");
        resetWorldState();
        
        // Only spawn boss if not already killed
        if (!player.bossKilled) {
            spawnManager.spawnBoss();
            gameHud.setBoss(spawnManager.getBoss());
        } else {
            System.out.println("Boss already killed, not spawning.");
            // Optionally show leaderboard immediately or just let player explore
        }

        playMusic(WorldType.INFERNO);
    }

    private void setPlayerSpawn(String layerName) {
        MapLayer layer = map.getLayers().get(layerName);
        if (layer instanceof TiledMapTileLayer) {
            TiledMapTileLayer spawnLayer = (TiledMapTileLayer) layer;
            boolean found = false;
            for (int x = 0; x < spawnLayer.getWidth(); x++) {
                for (int y = 0; y < spawnLayer.getHeight(); y++) {
                    if (spawnLayer.getCell(x, y) != null) {
                        player.position.set(x * 16, y * 16);
                        found = true;
                        break;
                    }
                }
                if (found) break;
            }
        }
    }

    private void resetWorldState() {
        monsters.clear();
        groundItems.clear();
        
        // Spawn monsters untuk world baru
        spawnManager.reset();

        if (currentWorld == WorldType.FOREST) {
            initForestEnvironment();
        } else {
            campfire = new Campfire(-1000, -1000);
        }
    }

    private void initForestEnvironment() {
        Vector2 campfirePos = new Vector2(player.position.x + 64, player.position.y);
        MapLayer campfireLayer = map.getLayers().get("campfire");
        if (campfireLayer instanceof TiledMapTileLayer) {
            TiledMapTileLayer cl = (TiledMapTileLayer) campfireLayer;
            boolean found = false;
            for (int x = 0; x < cl.getWidth(); x++) {
                for (int y = 0; y < cl.getHeight(); y++) {
                    if (cl.getCell(x, y) != null) {
                        campfirePos.set(x * 16, y * 16);
                        found = true;
                        break;
                    }
                }
                if (found) break;
            }
        }
        campfire = new Campfire(campfirePos.x, campfirePos.y);
    }

    private void handleEntityCollision(Rectangle hitbox, Vector2 position, float width, float height) {
        checkCollision(hitbox.x, hitbox.y, position, width, height);
        checkCollision(hitbox.x + hitbox.width, hitbox.y, position, width, height);
        checkCollision(hitbox.x, hitbox.y + hitbox.height, position, width, height);
        checkCollision(hitbox.x + hitbox.width, hitbox.y + hitbox.height, position, width, height);
    }

    private void checkCollision(float x, float y, Vector2 position, float width, float height) {
        if (isCellBlocked(x, y)) {
            int tileX = (int) (x / 16);
            int tileY = (int) (y / 16);
            float tileCenterX = tileX * 16 + 8;
            float tileCenterY = tileY * 16 + 8;

            Vector2 pushDir = new Vector2(position.x + width/2 - tileCenterX,
                position.y + height/2 - tileCenterY).nor();
            position.add(pushDir.scl(2.0f));
        }
    }

    private boolean isCellBlocked(float x, float y) {
        int tileX = (int) (x / 16);
        int tileY = (int) (y / 16);

        if (tileX < 0 || tileX >= 73 || tileY < 0 || tileY >= 73) return true;

        String[] collisionLayers;
        if (currentWorld == WorldType.ICE) {
            collisionLayers = new String[] { "ice_building" };
        } else if (currentWorld == WorldType.INFERNO) {
            collisionLayers = new String[] { "building_inferno", "lava_obstacle", "batas_bos" };
        } else {
            collisionLayers = new String[] { "building_coklat", "building_hijau" };
        }

        for (String layerName : collisionLayers) {
            TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(layerName);
            if (layer != null) {
                TiledMapTileLayer.Cell cell = layer.getCell(tileX, tileY);
                if (cell != null && cell.getTile() != null) {
                    return true;
                }
            }
        }
        return false;
    }

    private void restartGame() {
        // Clear inventory hanya jika mati di INFERNO (oleh boss terakhir)
        if (currentWorld == WorldType.INFERNO) {
            System.out.println("Died in INFERNO - Clearing inventory, resetting permanent stats, and saving state");
            player.inventory.clear();
            // Reset permanent stats upgrades (HP/Stamina dari resep)
            player.stats.resetPermanentStats(50f, 100f);
            // Clear consumed legendaries tracking
            player.clearConsumedLegendaries();
            NetworkManager.getInstance().saveInventory(player);
            
            // Kembali ke FOREST world (spawn awal)
            currentWorld = WorldType.FOREST;
            map = GameAssetManager.getInstance().getMap();
            mapRenderer.setMap(map);
            camera.zoom = 1.0f; // Reset zoom dari INFERNO
            setPlayerSpawn("spawn_player");
            player.reset(playerSpawnPoint.x, playerSpawnPoint.y);
        } else {
            // Mati di world lain (forest/ice) - inventory tetap disimpan
            System.out.println("Died in " + currentWorld + " - Keeping inventory and permanent stats");
            NetworkManager.getInstance().saveInventory(player);
            
            // Reset player di world saat ini
            if (currentWorld == WorldType.ICE) {
                setPlayerSpawn("spawn_ice_player");
                player.reset(player.position.x, player.position.y);
            } else {
                player.reset(playerSpawnPoint.x, playerSpawnPoint.y);
            }
        }

        // Clear dan respawn monsters
        monsters.clear();
        groundItems.clear();
        spawnManager.setWorld(currentWorld);
        spawnManager.reset();

        // Setup boss jika di INFERNO
        if (currentWorld == WorldType.INFERNO && !player.bossKilled) {
            spawnManager.spawnBoss();

            Boss boss = spawnManager.getBoss();
            if (boss != null) {
                boss.reset();
                System.out.println("Boss Respawned & Reset");
            }
            gameHud.setBoss(boss);
        } else {
            gameHud.setBoss(null);
        }

        // Re-initialize campfire if in FOREST
        if (currentWorld == WorldType.FOREST) {
            initForestEnvironment();
        } else if (currentWorld == WorldType.ICE) {
            campfire = new Campfire(-1000, -1000); // Hide campfire in ICE world
        }

        // Force camera update berdasarkan world
        if (currentWorld == WorldType.INFERNO) {
            camera.position.set(584, 584, 0);
        } else {
            // Camera akan follow player (handled in render loop)
            float targetX = player.position.x + (player.getWidth() / 2);
            float targetY = player.position.y + (player.getHeight() / 2);
            camera.position.set(targetX, targetY, 0);
        }

        isGameOver = false;
        isInventoryOpen = false;
        gamePaused = false;
        
        // Resume appropriate music after restart
        playMusic(currentWorld);
        
        System.out.println("Game restarted in world: " + currentWorld);
    }

    private void playMusic(WorldType worldType) {
        // Stop current music
        if (currentMusic != null && currentMusic.isPlaying()) {
            currentMusic.stop();
        }

        // Play appropriate music based on world
        if (worldType == WorldType.INFERNO) {
            currentMusic = GameAssetManager.getInstance().getInfernoMusic();
        } else {
            // FOREST and ICE use same music
            currentMusic = GameAssetManager.getInstance().getForestMusic();
        }

        if (currentMusic != null && !currentMusic.isPlaying()) {
            currentMusic.play();
            System.out.println("🎵 Playing music for world: " + worldType);
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        worldRenderer.dispose();
        debugRenderer.dispose();
        gameHud.dispose();
        inventoryUI.dispose();
        cookingMenu.dispose();
        tutorialPopup.dispose();
        loginUI.dispose();
        font.dispose();
        mapRenderer.dispose();
        GameAssetManager.getInstance().dispose();
    }

    private static class Renderable implements Comparable<Renderable> {
        float y;
        Runnable renderTask;

        Renderable(float y, Runnable renderTask) {
            this.y = y;
            this.renderTask = renderTask;
        }

        @Override
        public int compareTo(Renderable other) {
            return Float.compare(other.y, this.y);
        }
    }
}
