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
import com.fernanda.finpro.entities.Boss; // <-- 1. IMPORT DITAMBAHKAN
import com.fernanda.finpro.entities.GroundItem;
import com.fernanda.finpro.entities.Monster;
import com.fernanda.finpro.entities.Player;
import com.fernanda.finpro.managers.CollisionManager;
import com.fernanda.finpro.managers.SpawnManager;
import com.fernanda.finpro.objects.Campfire;
import com.fernanda.finpro.singleton.GameAssetManager;
import com.fernanda.finpro.ui.GameHud;
import com.fernanda.finpro.ui.InventoryUI;
import com.fernanda.finpro.ui.CookingMenu;
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
    LoginUI loginUI;
    TiledMap map;
    OrthogonalTiledMapRenderer mapRenderer;
    List<Monster> monsters;
    List<GroundItem> groundItems;
    List<Renderable> renderQueue;

    Campfire campfire;
    TutorialPopup tutorialPopup;

    private boolean isInventoryOpen = false;
    private boolean gamePaused = false;
    private boolean isCookingMenuOpen = false;

    private static final float VIEWPORT_WIDTH = 800f;
    private static final float VIEWPORT_HEIGHT = 450f;

    ShapeRenderer worldRenderer;
    ShapeRenderer debugRenderer;
    boolean debugMode = true;
    BitmapFont font;

    private SpawnManager spawnManager;
    private CollisionManager collisionManager;
    private Vector2 playerSpawnPoint = new Vector2(100, 100);
    private boolean isGameOver = false;
    private WorldType currentWorld = WorldType.FOREST;

    @Override
    public void create() {
        batch = new SpriteBatch();
        worldRenderer = new ShapeRenderer();
        debugRenderer = new ShapeRenderer();
        font = new BitmapFont();
        font.getData().setScale(0.8f);

        GameAssetManager.getInstance().loadImages();
        GameAssetManager.getInstance().finishLoading();

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

        loginUI = new LoginUI(viewport, new LoginUI.LoginListener() {
            @Override
            public void onLoginSuccess(String username, Map<String, Integer> inventoryData) {
                player.inventory.clear();
                for (Map.Entry<String, Integer> entry : inventoryData.entrySet()) {
                    try {
                        ItemType type = ItemType.valueOf(entry.getKey());
                        player.inventory.addItem(type, entry.getValue());
                    } catch (IllegalArgumentException e) {
                        System.err.println("Unknown item type: " + entry.getKey());
                    }
                }
                System.out.println("Logged in as: " + username);
            }
        });

        player.stats.addListener(gameHud);
        gameHud.onHealthChanged(player.stats.getCurrentHealth(), player.stats.maxHealth);
        gameHud.onStaminaChanged(player.stats.getCurrentStamina(), player.stats.maxStamina);

        monsters = new ArrayList<>();
        groundItems = new ArrayList<>();
        renderQueue = new ArrayList<>();

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

        System.out.println("✅ DEBUG: Game Started Successfully!");
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        gameHud.resize(width, height);
        inventoryUI.resize(width, height);
    }

    @Override
    public void render() {
        if (loginUI.isVisible()) {
            Gdx.gl.glClearColor(0, 0, 0, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            loginUI.render();
            return;
        }

        float dt = Gdx.graphics.getDeltaTime();

        if (!isGameOver) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.H)) {
                tutorialPopup.toggle();
            }

            if (tutorialPopup.isVisible() && Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                tutorialPopup.hide();
            }

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
                    cookingMenu.update(player.inventory);
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
                            NetworkManager.getInstance().saveInventory(player);
                        }
                    }

                    Iterator<Monster> monsterIterator = monsters.iterator();
                    while (monsterIterator.hasNext()) {
                        Monster m = monsterIterator.next();
                        if (m.canBeRemoved()) {
                            ItemType drop = m.rollDrop();
                            if (drop != null) {
                                groundItems.add(new GroundItem(drop, m.position.x, m.position.y));
                            }
                            monsterIterator.remove();
                        }
                    }

                    spawnManager.update(dt);
                    
                    // --- 4. UPDATE BOSS ---
                    Boss boss = spawnManager.getBoss();
                    if (boss != null) {
                        boss.update(dt);
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
                }
            }
        } else {
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
        
        // --- 4. RENDER BOSS ---
        Boss bossDebug = spawnManager.getBoss();
        if (bossDebug != null) {
            // bossDebug.renderDebug(debugRenderer);
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

        if (tutorialPopup.isVisible()) {
            com.badlogic.gdx.math.Matrix4 uiMatrix = new com.badlogic.gdx.math.Matrix4();
            uiMatrix.setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            batch.setProjectionMatrix(uiMatrix);
            debugRenderer.setProjectionMatrix(uiMatrix);
            tutorialPopup.render(batch, debugRenderer);
        }

        if (isGameOver) {
            batch.setProjectionMatrix(camera.combined);
            batch.begin();
            font.setColor(Color.RED);
            font.draw(batch, "GAME OVER - Press R to Restart", player.position.x - 100, player.position.y + 50);
            batch.end();

            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
                restartGame();
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
        spawnManager.spawnBoss(); 
        
        gameHud.setBoss(spawnManager.getBoss());
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
        // spawnManager.reset() akan dipanggil dari restartGame() jika perlu
        // Kita tidak ingin me-reset spawn monster saat ganti map
        
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
        if (currentWorld == WorldType.ICE) {
            setPlayerSpawn("spawn_ice_player");
            player.reset(player.position.x, player.position.y);
        } else if (currentWorld == WorldType.INFERNO) {
            setPlayerSpawn("spawn_player_inferno");
            player.reset(player.position.x, player.position.y);
        } else {
            player.reset(playerSpawnPoint.x, playerSpawnPoint.y);
        }

        monsters.clear();
        groundItems.clear();
        spawnManager.reset(); // Ini akan memanggil despawnBoss() dan spawn monster awal jika perlu
        
        // Jika restart di Inferno, spawn ulang boss
        if (currentWorld == WorldType.INFERNO) {
            spawnManager.spawnBoss();
        }

        isGameOver = false;
        isInventoryOpen = false;
        gamePaused = false;
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