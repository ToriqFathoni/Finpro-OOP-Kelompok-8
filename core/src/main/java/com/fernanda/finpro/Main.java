package com.fernanda.finpro;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx. graphics.g2d.BitmapFont;
import com.badlogic. gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math. Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.fernanda.finpro.components.ItemType;
import com.fernanda.finpro.entities.Decoration;
import com.fernanda.finpro.entities. GroundItem;
import com.fernanda. finpro.entities.Monster;
import com.fernanda. finpro.entities. Orc;
import com.fernanda.finpro.entities. Player;
import com.fernanda.finpro.factories.MonsterFactory;
import com.fernanda.finpro.objects.Campfire;
import com.fernanda.finpro.objects.SignBoard;
import com.fernanda.finpro.singleton.GameAssetManager;
import com.fernanda.finpro.ui.GameHud;
import com.fernanda.finpro. ui. InventoryUI;
import com.fernanda.finpro.ui.CookingMenu;
import com.fernanda.finpro.world.WorldManager;

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
    WorldManager worldManager;
    List<Monster> monsters;
    List<GroundItem> groundItems;
    List<Decoration> decorations;
    List<Decoration> groundTiles;
    List<Renderable> renderQueue;

    // Hardcore Cooking & Tutorial System
    Campfire campfire;
    List<SignBoard> signBoards;

    private boolean isInventoryOpen = false;
    private boolean gamePaused = false;
    private boolean isCookingMenuOpen = false;

    private static final float VIEWPORT_WIDTH = 960f;
    private static final float VIEWPORT_HEIGHT = 540f;

    ShapeRenderer worldRenderer;
    ShapeRenderer debugRenderer;
    boolean debugMode = true;

    BitmapFont font;

    private static final float RADIUS_FIRE = 800f;
    private static final float RADIUS_ICE = 2000f;
    private static final float RADIUS_FOREST = 3500f;

    private Vector2 tempVector = new Vector2();

    private float spawnTimer = 0;
    private float nextSpawnDelay = 0;
    private int maxOrcCount;

    private boolean isGameOver = false;

    @Override
    public void create() {
        batch = new SpriteBatch();
        worldRenderer = new ShapeRenderer();
        debugRenderer = new ShapeRenderer();
        font = new BitmapFont();
        font.getData().setScale(1.5f);

        GameAssetManager.getInstance().loadImages();
        GameAssetManager. getInstance().finishLoading();

        player = new Player(2800, 0);

        camera = new OrthographicCamera();
        viewport = new FitViewport(VIEWPORT_WIDTH, VIEWPORT_HEIGHT, camera);

        gameHud = new GameHud();
        inventoryUI = new InventoryUI();
        cookingMenu = new CookingMenu();

        player.stats.addListener(gameHud);
        gameHud.onHealthChanged(player.stats.getCurrentHealth(), player.stats.maxHealth);
        gameHud.onStaminaChanged(player.stats. getCurrentStamina(), player.stats.maxStamina);

        worldManager = new WorldManager();

        monsters = new ArrayList<>();
        groundItems = new ArrayList<>();
        decorations = new ArrayList<>();
        groundTiles = new ArrayList<>();
        renderQueue = new ArrayList<>();

        // Generate Ground
        generateGround();

        // Generate Fences
        generateFences();
        // Generate Decorations
        
        // 1. Tree 1 Clusters (Big Trees) & Tree 2 (Side Trees)
        int numClusters = 30;
        for (int i = 0; i < numClusters; i++) {
            Vector2 clusterCenter = generateRandomPositionInGreenZone();
            
            // Spawn Tree 1 (Big) in cluster
            int treesInCluster = MathUtils.random(3, 7);
            for (int j = 0; j < treesInCluster; j++) {
                 float offsetX = MathUtils.random(-120, 120);
                 float offsetY = MathUtils.random(-120, 120);
                 float x = clusterCenter.x + offsetX;
                 float y = clusterCenter.y + offsetY;
                 
                 if (isPositionValid(x, y, 100f)) {
                    decorations.add(new Decoration(GameAssetManager.TREE_1, x, y, 4.0f));
                 }
            }

            // Spawn Tree 2 (Medium) near cluster
            int tree2Count = MathUtils.random(2, 4);
            for (int j = 0; j < tree2Count; j++) {
                 float offsetX = MathUtils.random(-200, 200);
                 float offsetY = MathUtils.random(-200, 200);
                 float x = clusterCenter.x + offsetX;
                 float y = clusterCenter.y + offsetY;

                 if (isPositionValid(x, y, 80f)) {
                    decorations.add(new Decoration(GameAssetManager.TREE_2, x, y, 3.0f));
                 }
            }
        }

        // 2. Tree 3 (Grass/Small) Scattered
        for (int i = 0; i < 150; i++) {
            Vector2 pos = generateRandomPositionInGreenZone();
            if (isPositionValid(pos.x, pos.y, 50f)) {
                decorations.add(new Decoration(GameAssetManager.TREE_3, pos.x, pos.y, 2.0f));
            }
        }

        // Initialize Hardcore Cooking & Tutorial System
        // Move Campfire right next to Player spawn for easy testing!
        campfire = new Campfire(2900, 50);  // Player spawns at (2800, 0)
        signBoards = new ArrayList<>();
        
        // SignBoard 1: Near Player Start (Tutorial)
        signBoards.add(new SignBoard(2750, 80, 
            "SURVIVAL TIP: Kill Monsters -> Gather Ingredients -> Cook at Fire!"));
        
        // SignBoard 2: Near Campfire (Recipe Hint)
        signBoards.add(new SignBoard(2850, 120, 
            "CHEF'S NOTE: The Legendary Burger requires Meat, Herbs, and Slime Gel!"));

        maxOrcCount = MathUtils.random(40, 80);
        System.out.println("Target Populasi Orc: " + maxOrcCount);
        nextSpawnDelay = 0.5f;
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
                handleInvisibleWall();
                handleDecorationCollision();

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
                if (monsters.size() < maxOrcCount) {
                    spawnTimer += dt;
                    if (spawnTimer >= nextSpawnDelay) {
                        monsters.add(MonsterFactory.createOrcInForest());
                        spawnTimer = 0;
                        nextSpawnDelay = MathUtils.random(0.2f, 1.2f);
                    }
                }

                // Monster AI & Collision
                for (Monster m : monsters) {
                    m.update(dt);
                    m. aiBehavior(dt, player);

                    if (m.isDead()) continue;

                    Rectangle playerBody = player.getHitbox();

                    if (player.isHitboxActive()) {
                        if (player.getAttackHitbox().overlaps(m.getBodyHitbox())) {
                            m.takeDamage(10);
                            Vector2 knockback = new Vector2(m.position).sub(player.position).nor().scl(10);
                            m.position.add(knockback);
                        }
                    }

                    Rectangle mAtkRect = m.getAttackHitbox();
                    if (mAtkRect.width > 0) {
                        if (mAtkRect.overlaps(playerBody)) {
                            player.takeDamage(m.getDamage());
                        }
                    }

                    if (playerBody.overlaps(m.getBodyHitbox())) {
                        player.takeDamage(5);
                        if (! player.isDodging()) {
                            Vector2 pushDirection = new Vector2(player.position).sub(m.position).nor();
                            float pushForce = 300f * dt;
                            player.position.mulAdd(pushDirection, pushForce);
                        }
                    }
                }
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
        float targetX = player.position.x + (player.getWidth() / 2);
        float targetY = player. position.y + (player.getHeight() / 2);
        camera.position.x = targetX;
        camera.position. y = targetY;
        camera.update();

        // RENDERING
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        worldRenderer.setProjectionMatrix(camera.combined);
        worldRenderer. begin(ShapeRenderer.ShapeType.Filled);
        worldRenderer.setColor(0.1f, 0.4f, 0.1f, 1f); worldRenderer.circle(0, 0, RADIUS_FOREST);
        worldRenderer.setColor(0.4f, 0.5f, 0.8f, 1f); worldRenderer.circle(0, 0, RADIUS_ICE);
        worldRenderer.setColor(0.6f, 0.2f, 0.2f, 1f); worldRenderer.circle(0, 0, RADIUS_FIRE);
        worldRenderer.end();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        worldRenderer.begin(ShapeRenderer. ShapeType.Line);
        worldRenderer.setColor(1f, 1f, 1f, 0.15f);

        float gridSize = 100f;
        float startX = camera.position.x - camera.viewportWidth / 2;
        float endX = camera.position.x + camera.viewportWidth / 2;
        float startY = camera.position.y - camera.viewportHeight / 2;
        float endY = camera.position. y + camera.viewportHeight / 2;

        startX = (float)Math.floor(startX / gridSize) * gridSize;
        startY = (float)Math.floor(startY / gridSize) * gridSize;

        for (float x = startX; x <= endX; x += gridSize) {
            worldRenderer.line(x, startY, x, endY);
        }
        for (float y = startY; y <= endY; y += gridSize) {
            worldRenderer.line(startX, y, endX, y);
        }

        worldRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // Render Sprites
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        
        // 0. Draw Ground Tiles (Background)
        for (Decoration tile : groundTiles) {
            // Simple culling
            if (tile.position.x >= camera.position.x - camera.viewportWidth && 
                tile.position.x <= camera.position.x + camera.viewportWidth &&
                tile.position.y >= camera.position.y - camera.viewportHeight &&
                tile.position.y <= camera.position.y + camera.viewportHeight) {
                tile.render(batch);
            }
        }

        // Prepare Render Queue
        renderQueue.clear();

        // Add Decorations
        for (Decoration d : decorations) {
            renderQueue.add(new Renderable(d.position.y, () -> d.render(batch)));
        }

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
            debugRenderer.setProjectionMatrix(uiMatrix);
            
            cookingMenu.render(batch, debugRenderer, player.inventory);
        }

        if (isGameOver) {
            batch.setProjectionMatrix(camera.combined);
            batch.begin();
            font.setColor(Color.RED);
            font.draw(batch, "GAME OVER - Press R to Restart", player.position.x - 200, player.position.y + 100);
            batch.end();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys. ESCAPE)) Gdx.app.exit();
    }

    private void generateGround() {
        float scale = 4.0f;
        float tileSize = 16f * scale; // 64
        int gridSize = (int) ((RADIUS_FOREST * 2) / tileSize) + 2;
        int[][] tileMap = new int[gridSize][gridSize]; // 0=Grass1, 2=Grass3
        
        // 1. Initialize with Grass 1 (0) - Java does this by default
        
        // 2. Generate Grass Patches (Rectangles with Grass 3)
        int numPatches = 15; 
        for (int i = 0; i < numPatches; i++) {
            int w = MathUtils.random(4, 8);
            int h = MathUtils.random(4, 8);
            int cx = MathUtils.random(5, gridSize - w - 5);
            int cy = MathUtils.random(5, gridSize - h - 5);
            
            for (int x = cx; x < cx + w; x++) {
                for (int y = cy; y < cy + h; y++) {
                    tileMap[x][y] = 2; // Grass 3
                }
            }
        }
        
        // 3. Create Decorations (Ground & Fences)
        float startX = -RADIUS_FOREST;
        float startY = -RADIUS_FOREST;
        
        for (int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {
                float worldX = startX + x * tileSize;
                float worldY = startY + y * tileSize;
                
                // Check if in forest zone
                float dist = Vector2.len(worldX, worldY);
                if (dist > RADIUS_ICE - 50 && dist < RADIUS_FOREST + 50) {
                    String asset;
                    if (tileMap[x][y] == 2) asset = GameAssetManager.GRASS_3;
                    else asset = GameAssetManager.GRASS_1;
                    
                    groundTiles.add(new Decoration(asset, worldX, worldY, scale, false));

                    // Add Fences around Grass 3
                    if (tileMap[x][y] == 2) {
                        // Check Top
                        if (y + 1 < gridSize && tileMap[x][y+1] != 2) {
                            Decoration fence = new Decoration(GameAssetManager.FENCE_1, worldX, worldY + tileSize/2, scale, true);
                            decorations.add(fence);
                        }
                        // Check Bottom
                        if (y - 1 >= 0 && tileMap[x][y-1] != 2) {
                            Decoration fence = new Decoration(GameAssetManager.FENCE_1, worldX, worldY - tileSize/2, scale, true);
                            decorations.add(fence);
                        }
                        // Check Left
                        if (x - 1 >= 0 && tileMap[x-1][y] != 2) {
                            Decoration fence = new Decoration(GameAssetManager.FENCE_1, worldX - tileSize/2, worldY, scale, true);
                            fence.setRotation(90);
                            decorations.add(fence);
                        }
                        // Check Right
                        if (x + 1 < gridSize && tileMap[x+1][y] != 2) {
                            Decoration fence = new Decoration(GameAssetManager.FENCE_1, worldX + tileSize/2, worldY, scale, true);
                            fence.setRotation(90);
                            decorations.add(fence);
                        }
                    }
                }
            }
        }
    }

    private void generateFences() {
        // Create some fence lines near the road or random spots
        for (int i = 0; i < 5; i++) {
            Vector2 startPos = generateRandomPositionInGreenZone();
            int length = MathUtils.random(5, 10);
            boolean horizontal = MathUtils.randomBoolean();
            
            for (int j = 0; j < length; j++) {
                float x = startPos.x + (horizontal ? j * 60 : 0);
                float y = startPos.y + (horizontal ? 0 : j * 40);
                
                if (isPositionValid(x, y, 50f)) {
                    decorations.add(new Decoration(GameAssetManager.FENCE_1, x, y, 2.0f));
                }
            }
        }
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

    private void handleDecorationCollision() {
        Rectangle playerRect = player.getHitbox();
        for (Decoration d : decorations) {
            if (!d.hasCollision()) continue;

            Rectangle treeRect = d.getCollisionBox();
            if (playerRect.overlaps(treeRect)) {
                // Calculate push direction (from tree center to player center)
                float treeCenterX = treeRect.x + treeRect.width / 2;
                float treeCenterY = treeRect.y + treeRect.height / 2;
                float playerCenterX = playerRect.x + playerRect.width / 2;
                float playerCenterY = playerRect.y + playerRect.height / 2;

                Vector2 pushDir = new Vector2(playerCenterX - treeCenterX, playerCenterY - treeCenterY).nor();
                
                // Push player out
                float pushSpeed = 2.0f; 
                player.position.add(pushDir.scl(pushSpeed));
            }
        }
    }

    private boolean isPositionValid(float x, float y, float minDistance) {
        for (Decoration d : decorations) {
            if (Vector2.dst(x, y, d.position.x, d.position.y) < minDistance) {
                return false;
            }
        }
        return true;
    }

    private void restartGame() {
        player.reset(2800, 0);
        monsters.clear();
        groundItems. clear();
        spawnTimer = 0;
        isGameOver = false;
        isInventoryOpen = false;
        gamePaused = false;
    }

    private Vector2 generateRandomPositionInGreenZone() {
        float angle = MathUtils.random(0f, 360f);
        float distance = MathUtils.random(RADIUS_ICE + 100, RADIUS_FOREST - 200); 
        float x = distance * MathUtils.cosDeg(angle);
        float y = distance * MathUtils.sinDeg(angle);
        return new Vector2(x, y);
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
