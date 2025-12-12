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
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.fernanda.finpro.components.ItemType;
import com.fernanda.finpro.entities.GroundItem;
import com.fernanda.finpro.entities.Monster;
import com.fernanda.finpro.entities.Orc;
import com.fernanda.finpro.entities.Player;
import com.fernanda.finpro.factories.MonsterFactory;
import com.fernanda.finpro.managers.CollisionManager;
import com.fernanda.finpro.managers.SpawnManager;
import com.fernanda.finpro.objects.Campfire;
import com.fernanda.finpro.objects.SignBoard;
import com.fernanda.finpro.singleton.GameAssetManager;
import com.fernanda.finpro.ui.GameHud;
import com.fernanda.finpro.ui.InventoryUI;
import com.fernanda.finpro.ui.CookingMenu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Main extends ApplicationAdapter {
    SpriteBatch batch;
    Player player;
    OrthographicCamera camera;
    Viewport viewport;
    GameHud gameHud;
    InventoryUI inventoryUI;
    CookingMenu cookingMenu;

    TiledMap map;
    OrthogonalTiledMapRenderer mapRenderer;

    List<Monster> monsters;
    List<GroundItem> groundItems;
    List<Renderable> renderQueue;

    // Hardcore Cooking & Tutorial System
    Campfire campfire;
    List<SignBoard> signBoards;

    private boolean isInventoryOpen = false;
    private boolean gamePaused = false;
    private boolean isCookingMenuOpen = false;

    // Viewport diperkecil agar kamera lebih zoom-in ke player
    private static final float VIEWPORT_WIDTH = 800f;
    private static final float VIEWPORT_HEIGHT = 450f;

    ShapeRenderer worldRenderer;
    ShapeRenderer debugRenderer;
    boolean debugMode = true;

    BitmapFont font;

    private static final float RADIUS_FIRE = 800f;
    private static final float RADIUS_ICE = 2000f;
    private static final float RADIUS_FOREST = 3500f;

    private Vector2 tempVector = new Vector2();
    private SpawnManager spawnManager;
    private CollisionManager collisionManager;

    private Vector2 playerSpawnPoint = new Vector2(100, 100); // Default fallback

    private boolean isGameOver = false;
    private com.fernanda.finpro.enums.WorldType currentWorld = com.fernanda.finpro.enums.WorldType.FOREST;

    @Override
    public void create() {
        batch = new SpriteBatch();
        worldRenderer = new ShapeRenderer();
        debugRenderer = new ShapeRenderer();
        font = new BitmapFont();
        font.getData().setScale(0.8f); // Scale disesuaikan dengan viewport kecil

        GameAssetManager.getInstance().loadImages();
        GameAssetManager. getInstance().finishLoading();

        // Load Map
        map = GameAssetManager.getInstance().getMap();
        mapRenderer = new OrthogonalTiledMapRenderer(map, 1.0f);

        // Find Player Spawn Point
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

        // Spawn Player
        player = new Player(playerSpawnPoint.x, playerSpawnPoint.y);

        camera = new OrthographicCamera();
        viewport = new FitViewport(VIEWPORT_WIDTH, VIEWPORT_HEIGHT, camera);

        gameHud = new GameHud();
        inventoryUI = new InventoryUI();
        cookingMenu = new CookingMenu();

        player.stats.addListener(gameHud);
        gameHud.onHealthChanged(player.stats.getCurrentHealth(), player.stats.maxHealth);
        gameHud.onStaminaChanged(player.stats. getCurrentStamina(), player.stats.maxStamina);

        monsters = new ArrayList<>();
        groundItems = new ArrayList<>();
        renderQueue = new ArrayList<>();

        // Initialize Hardcore Cooking & Tutorial System
        // Find Campfire Position from Map Layer
        Vector2 campfirePos = new Vector2(playerSpawnPoint.x + 64, playerSpawnPoint.y); // Default
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
        signBoards = new ArrayList<>();

        // SignBoard 1: Survival Tips from Map Layer
        Vector2 survivalTipPos = new Vector2(playerSpawnPoint.x - 32, playerSpawnPoint.y); // Default
        MapLayer tipsLayer = map.getLayers().get("survival_tips");
        if (tipsLayer instanceof TiledMapTileLayer) {
            TiledMapTileLayer tl = (TiledMapTileLayer) tipsLayer;
            boolean found = false;
            for (int x = 0; x < tl.getWidth(); x++) {
                for (int y = 0; y < tl.getHeight(); y++) {
                    if (tl.getCell(x, y) != null) {
                        survivalTipPos.set(x * 16, y * 16);
                        found = true;
                        break;
                    }
                }
                if (found) break;
            }
        }
        signBoards.add(new SignBoard(survivalTipPos.x, survivalTipPos.y,
            "SURVIVAL TIP: Kill Monsters -> Gather Ingredients -> Cook at Fire!"));

        // SignBoard 2: Near Campfire (Recipe Hint)
        signBoards.add(new SignBoard(campfirePos.x + 40, campfirePos.y + 40,
            "CHEF'S NOTE: The Legendary Burger requires Meat, Herbs, and Slime Gel!"));

        monsters = new ArrayList<>();
        spawnManager = new SpawnManager(monsters);
        collisionManager = new CollisionManager(player, monsters);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        gameHud.resize(width, height);
        inventoryUI.resize(width, height);
    }

    @Override
    public void render() {
        float dt = Gdx.graphics.getDeltaTime();

        if (!isGameOver) {
            // Toggle Inventory
            if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
                if (!isCookingMenuOpen) { // Only toggle inventory if cooking menu is closed
                    isInventoryOpen = !isInventoryOpen;
                    gamePaused = isInventoryOpen;
                    System.out.println("Inventory:  " + (isInventoryOpen ?  "OPEN (PAUSED)" : "CLOSED"));
                }
            }

            // Toggle Cooking Menu (when near campfire)
            if (Gdx.input.isKeyJustPressed(Input.Keys.C)) {
                if (!isInventoryOpen && campfire.isPlayerNearby(player)) {
                    isCookingMenuOpen = !isCookingMenuOpen;
                    cookingMenu.setVisible(isCookingMenuOpen);
                    gamePaused = isCookingMenuOpen;
                }
            }

            // Update Cooking Menu
            if (isCookingMenuOpen) {
                cookingMenu.update(player.inventory);
                if (!cookingMenu.isVisible()) {
                    isCookingMenuOpen = false;
                    gamePaused = false;
                }
            }

            // Update game (ONLY if not paused)
            if (!gamePaused) {
                player.update(dt);
                handleMapCollision();

                // Keep player within map bounds
                player.position.x = MathUtils.clamp(player.position.x, 0, 1168 - player.getWidth());
                player.position.y = MathUtils.clamp(player.position.y, 0, 1168 - player.getHeight());

                // Update SignBoards (Campfire doesn't need update anymore)
                for (SignBoard sign : signBoards) {
                    sign.update(player);
                }

                // Item Pickup
                Iterator<GroundItem> itemIterator = groundItems.iterator();
                while (itemIterator. hasNext()) {
                    GroundItem item = itemIterator.next();
                    item.update(dt);

                    if (item.isActive() && item.getHitbox().overlaps(player.getHitbox())) {
                        player.inventory.addItem(item.getType(), 1);
                        itemIterator.remove();
                    }
                }

                // Monster Cleanup & Drop
                Iterator<Monster> monsterIterator = monsters.iterator();
                while (monsterIterator.hasNext()) {
                    Monster m = monsterIterator.next();
                    if (m.canBeRemoved()) {
                        if (m instanceof Orc) {
                            ItemType drop = ((Orc) m).rollDrop();
                            if (drop != null) {
                                groundItems.add(new GroundItem(drop, m.position.x, m.position.y));
                                System.out.println("[DROP] Orc dropped: " + drop.getDisplayName());
                            }
                        }
                        monsterIterator. remove();
                    }
                }

                // Spawn
                spawnManager.update(dt);

                for (Monster m : monsters) {
                    m.update(dt);
                    m.aiBehavior(dt, player);

                    handleEntityCollision(m.getBodyHitbox(), m.position, m.getBodyHitbox().width, m.getBodyHitbox().height);
                }
                collisionManager.update(dt);
            } else {
                // Handle inventory input & check if should close
                boolean shouldClose = inventoryUI.handleInput();
                if (shouldClose) {
                    isInventoryOpen = false;
                    gamePaused = false;
                    System.out.println("Inventory closed via UI button");
                }
            }

            // Death check
            if (player.stats. getCurrentHealth() <= 0) {
                if (player.isDeathAnimationFinished()) {
                    isGameOver = true;
                }
            }
        } else {
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
                restartGame();
            }
        }

        // Camera
        if (currentWorld == com.fernanda.finpro.enums.WorldType.INFERNO) {
            // Static Camera centered on map (1168x1168)
            camera.position.set(584, 584, 0);
        } else {
            float targetX = player.position.x + (player.getWidth() / 2);
            float targetY = player. position.y + (player.getHeight() / 2);
            
            // Clamp Camera to Map Bounds to avoid black bars
            float mapWidth = 1168f;
            float mapHeight = 1168f;
            
            float visibleWidth = camera.viewportWidth * camera.zoom;
            float visibleHeight = camera.viewportHeight * camera.zoom;
            
            float minX = visibleWidth / 2;
            float maxX = mapWidth - visibleWidth / 2;
            float minY = visibleHeight / 2;
            float maxY = mapHeight - visibleHeight / 2;
            
            // Clamp X
            if (mapWidth < visibleWidth) {
                camera.position.x = mapWidth / 2;
            } else {
                camera.position.x = MathUtils.clamp(targetX, minX, maxX);
            }
            
            // Clamp Y
            if (mapHeight < visibleHeight) {
                camera.position.y = mapHeight / 2;
            } else {
                camera.position.y = MathUtils.clamp(targetY, minY, maxY);
            }
        }
        camera.update();

        // RENDERING
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();

        // Render Map
        mapRenderer.setView(camera);
        mapRenderer.render();

        // Render Sprites
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Prepare Render Queue
        renderQueue.clear();

        // Add Monsters
        for (Monster m : monsters) {
            renderQueue.add(new Renderable(m.position.y, () -> m.render(batch)));
        }

        // Add Player
        renderQueue.add(new Renderable(player.position.y, () -> player.render(batch)));

        // Add Ground Items
        for (GroundItem item : groundItems) {
            renderQueue.add(new Renderable(item.getPosition().y, () -> item.render(batch)));
        }

        // Sort by Y (Descending)
        Collections.sort(renderQueue);

        // Draw Sorted Entities
        for (Renderable r : renderQueue) {
            r.renderTask.run();
        }

        player.render(batch);

        // Render SignBoard text (must be in batch)
        for (SignBoard sign : signBoards) {
            sign.renderText(batch);
        }

        batch.end();

        // Debug
        Gdx.gl.glEnable(GL20.GL_BLEND);
        debugRenderer.setProjectionMatrix(camera.combined);
        debugRenderer.begin(ShapeRenderer.ShapeType.Line);
        for (Monster m : monsters) {
            m.renderDebug(debugRenderer);
        }
        debugRenderer.end();

        // Render Campfire and SignBoards (Filled shapes)
        debugRenderer.begin(ShapeRenderer.ShapeType.Filled);
        campfire.render(debugRenderer);
        for (SignBoard sign : signBoards) {
            sign.render(debugRenderer);
        }
        debugRenderer.end();

        if (debugMode) {
            debugRenderer. begin(ShapeRenderer.ShapeType.Line);
            debugRenderer.setColor(Color.RED);
            Rectangle hitbox = player.getHitbox();
            debugRenderer.rect(hitbox.x, hitbox. y, hitbox.width, hitbox.height);
            if (player.isHitboxActive()) {
                debugRenderer.setColor(Color.YELLOW);
                debugRenderer.rect(player. getAttackHitbox().x, player.getAttackHitbox().y, player.getAttackHitbox().width, player.getAttackHitbox().height);
            }
            debugRenderer. end();
        }

        gameHud.render();

        if (isInventoryOpen) {
            inventoryUI.render(player.inventory);
        }

        // Render Cooking Menu (LAST - in UI/Screen Space)
        if (isCookingMenuOpen) {
            // Switch to UI/Screen coordinates (not world coordinates)
            com.badlogic.gdx.math.Matrix4 uiMatrix = new com.badlogic.gdx.math.Matrix4();
            uiMatrix.setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            batch.setProjectionMatrix(uiMatrix);
            worldRenderer.setProjectionMatrix(uiMatrix);

            cookingMenu.render(batch, worldRenderer, player.inventory);
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
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) Gdx.app.exit();
        }
    }

    private void handleMapCollision() {
        // Player Collision
        Rectangle playerRect = player.getHitbox();
        handleEntityCollision(playerRect, player.position, player.getWidth(), player.getHeight());

        // Check for World Transition
        checkWorldTransition(playerRect);
    }

    private void checkWorldTransition(Rectangle playerRect) {
        TiledMapTileLayer transitionLayer = null;
        
        if (currentWorld == com.fernanda.finpro.enums.WorldType.FOREST) {
            transitionLayer = (TiledMapTileLayer) map.getLayers().get("next_map");
            if (checkLayerCollision(playerRect, transitionLayer)) {
                switchToIceWorld();
            }
        } else if (currentWorld == com.fernanda.finpro.enums.WorldType.ICE) {
            // Check Back to Forest
            transitionLayer = (TiledMapTileLayer) map.getLayers().get("back_map_forest");
            if (checkLayerCollision(playerRect, transitionLayer)) {
                switchToForestWorld();
                return;
            }
            
            // Check Next to Inferno
            transitionLayer = (TiledMapTileLayer) map.getLayers().get("inferno_next_map");
            if (checkLayerCollision(playerRect, transitionLayer)) {
                switchToInfernoWorld();
            }
        } else if (currentWorld == com.fernanda.finpro.enums.WorldType.INFERNO) {
            // Check Back to Ice
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
        currentWorld = com.fernanda.finpro.enums.WorldType.ICE;
        spawnManager.setWorld(currentWorld);

        // Load Ice Map
        map = GameAssetManager.getInstance().getIceMap();
        mapRenderer.setMap(map);
        
        camera.zoom = 1.0f;

        // Find Ice Spawn Point
        setPlayerSpawn("spawn_ice_player");
        resetWorldState();
    }
    
    private void switchToIceWorldFromInferno() {
        System.out.println("Switching to Ice World (From Inferno)!");
        currentWorld = com.fernanda.finpro.enums.WorldType.ICE;
        spawnManager.setWorld(currentWorld);

        // Load Ice Map
        map = GameAssetManager.getInstance().getIceMap();
        mapRenderer.setMap(map);
        
        camera.zoom = 1.0f;

        // Find Back Spawn Point
        setPlayerSpawn("back_spawn_inferno");
        resetWorldState();
    }

    private void switchToForestWorld() {
        System.out.println("Switching to Forest World!");
        currentWorld = com.fernanda.finpro.enums.WorldType.FOREST;
        spawnManager.setWorld(currentWorld);

        // Load Forest Map
        map = GameAssetManager.getInstance().getMap();
        mapRenderer.setMap(map);
        
        camera.zoom = 1.0f;

        // Find Player Spawn Point
        setPlayerSpawn("spawn_player_back");
        resetWorldState();
    }
    
    private void switchToInfernoWorld() {
        System.out.println("Switching to Inferno World!");
        currentWorld = com.fernanda.finpro.enums.WorldType.INFERNO;
        spawnManager.setWorld(currentWorld);

        // Load Inferno Map
        map = GameAssetManager.getInstance().getLavaMap();
        mapRenderer.setMap(map);
        
        // Zoom Out Camera (Fit ~90% of map height)
        // Map Height 1168 * 0.9 = 1051. Viewport Height 450. Zoom ~2.3
        camera.zoom = 2.3f;

        // Find Inferno Spawn Point
        setPlayerSpawn("spawn_player_inferno");
        resetWorldState();
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
        // Clear Monsters and Items
        monsters.clear();
        groundItems.clear();
        spawnManager.reset();

        // Reset Environment Objects
        signBoards.clear();
        
        if (currentWorld == com.fernanda.finpro.enums.WorldType.FOREST) {
             // Re-initialize Campfire and SignBoards for Forest
             initForestEnvironment();
        } else {
             // Hide campfire in other worlds
             campfire = new Campfire(-1000, -1000); 
        }
    }
    
    private void initForestEnvironment() {
        // We need to re-run the logic to find campfire position
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

        // SignBoard 1: Survival Tips from Map Layer
        Vector2 survivalTipPos = new Vector2(player.position.x - 32, player.position.y); // Default
        MapLayer tipsLayer = map.getLayers().get("survival_tips");
        if (tipsLayer instanceof TiledMapTileLayer) {
            TiledMapTileLayer tl = (TiledMapTileLayer) tipsLayer;
            boolean found = false;
            for (int x = 0; x < tl.getWidth(); x++) {
                for (int y = 0; y < tl.getHeight(); y++) {
                    if (tl.getCell(x, y) != null) {
                        survivalTipPos.set(x * 16, y * 16);
                        found = true;
                        break;
                    }
                }
                if (found) break;
            }
        }
        signBoards.add(new SignBoard(survivalTipPos.x, survivalTipPos.y,
            "SURVIVAL TIP: Kill Monsters -> Gather Ingredients -> Cook at Fire!"));

        signBoards.add(new SignBoard(campfirePos.x + 40, campfirePos.y + 40,
            "CHEF'S NOTE: The Legendary Burger requires Meat, Herbs, and Slime Gel!"));
    }

    private void handleEntityCollision(Rectangle hitbox, Vector2 position, float width, float height) {
        // Check corners
        checkCollision(hitbox.x, hitbox.y, position, width, height);
        checkCollision(hitbox.x + hitbox.width, hitbox.y, position, width, height);
        checkCollision(hitbox.x, hitbox.y + hitbox.height, position, width, height);
        checkCollision(hitbox.x + hitbox.width, hitbox.y + hitbox.height, position, width, height);
    }

    private void checkCollision(float x, float y, Vector2 position, float width, float height) {
        if (isCellBlocked(x, y)) {
            // Find center of the tile
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

        if (tileX < 0 || tileX >= 73 || tileY < 0 || tileY >= 73) return true; // Block out of bounds

        // Layers to check for collision
        String[] collisionLayers;

        if (currentWorld == com.fernanda.finpro.enums.WorldType.ICE) {
            collisionLayers = new String[] { "ice_building" };
        } else if (currentWorld == com.fernanda.finpro.enums.WorldType.INFERNO) {
            collisionLayers = new String[] { "building_inferno", "lava_obstacle" };
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
        if (currentWorld == com.fernanda.finpro.enums.WorldType.ICE) {
            setPlayerSpawn("spawn_ice_player");
            player.reset(player.position.x, player.position.y); // Reset player stats & state at new position
        } else if (currentWorld == com.fernanda.finpro.enums.WorldType.INFERNO) {
            setPlayerSpawn("spawn_player_inferno");
            player.reset(player.position.x, player.position.y);
        } else {
            player.reset(playerSpawnPoint.x, playerSpawnPoint.y);
        }
        
        monsters.clear();
        groundItems.clear();
        spawnManager.reset();
        isGameOver = false;
        isInventoryOpen = false;
        gamePaused = false;
    }

    @Override
    public void dispose() {
        batch.dispose();
        worldRenderer. dispose();
        debugRenderer.dispose();
        gameHud.dispose();
        inventoryUI.dispose();
        cookingMenu.dispose();
        font.dispose();
        for (SignBoard sign : signBoards) {
            sign.dispose();
        }
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
            // Descending Y (Higher Y = Background = First)
            return Float.compare(other.y, this.y);
        }
    }
}
