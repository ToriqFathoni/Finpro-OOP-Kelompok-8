package com.fernanda.finpro.ui;

import com.badlogic. gdx.Gdx;
import com.badlogic. gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic. gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx. graphics.g2d.BitmapFont;
import com.badlogic. gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com. badlogic.gdx.utils. viewport.Viewport;
import com.fernanda.finpro. components. Inventory;
import com.fernanda.finpro.components.ItemType;

import java.util.HashMap;
import java.util.Map;

public class InventoryUI {
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;
    private BitmapFont titleFont;
    private Viewport viewport;
    private OrthographicCamera camera;

    // Grid Configuration
    private static final int GRID_COLS = 5;
    private static final int GRID_ROWS = 4;
    private static final float SLOT_SIZE = 32f;
    private static final float SLOT_PADDING = 3f;

    // Panel sizing
    private static final float PANEL_WIDTH = 240f;
    private static final float PANEL_HEIGHT = 230f;
    private static final float HEADER_HEIGHT = 24f;
    private static final float INFO_BOX_HEIGHT = 30f;

    // Colors
    private static final Color HEADER_COLOR = new Color(0.2f, 0.5f, 0.3f, 1f);
    private static final Color BODY_COLOR = new Color(0.82f, 0.7f, 0.5f, 1f);
    private static final Color BORDER_COLOR = new Color(0.15f, 0.35f, 0.2f, 1f);

    private InventorySlot[][] slots;
    private int selectedCol = 0;
    private int selectedRow = 0;

    // Clickable buttons
    private Rectangle closeButtonRect;
    private Rectangle leftArrowRect;
    private Rectangle rightArrowRect;

    // Panel position (untuk button hitbox calculation)
    private float panelX;
    private float panelY;

    public InventoryUI() {
        this.shapeRenderer = new ShapeRenderer();
        this.batch = new SpriteBatch();

        this.font = new BitmapFont();
        this.font.getData().setScale(0.6f);

        this.titleFont = new BitmapFont();
        this.titleFont.getData().setScale(0.8f);

        camera = new OrthographicCamera();
        viewport = new FitViewport(480, 270, camera);

        // Calculate panel position once
        panelX = (480 - PANEL_WIDTH) / 2;
        panelY = (270 - PANEL_HEIGHT) / 2;

        // Initialize button hitboxes
        closeButtonRect = new Rectangle(panelX + PANEL_WIDTH - 25, panelY + PANEL_HEIGHT - HEADER_HEIGHT, 20, 20);
        leftArrowRect = new Rectangle(panelX + 5, panelY + PANEL_HEIGHT / 2 - 10, 15, 20);
        rightArrowRect = new Rectangle(panelX + PANEL_WIDTH - 20, panelY + PANEL_HEIGHT / 2 - 10, 15, 20);

        initializeSlots();
    }

    private void initializeSlots() {
        slots = new InventorySlot[GRID_ROWS][GRID_COLS];

        // Grid positioning:  CENTER horizontally, START BELOW header with proper margin
        float gridWidth = GRID_COLS * (SLOT_SIZE + SLOT_PADDING) - SLOT_PADDING;
        float startX = panelX + (PANEL_WIDTH - gridWidth) / 2;

        // FIXED: Start Y calculation - mulai dari bawah header dengan margin yang cukup
        float startY = panelY + PANEL_HEIGHT - HEADER_HEIGHT - SLOT_SIZE - 8; // 8px margin dari header

        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                float x = startX + col * (SLOT_SIZE + SLOT_PADDING);
                float y = startY - row * (SLOT_SIZE + SLOT_PADDING);
                slots[row][col] = new InventorySlot(x, y);
            }
        }
    }

    public void render(Inventory inventory) {
        viewport.apply();
        camera.update();

        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        // 1. Overlay
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.75f);
        shapeRenderer. rect(0, 0, 480, 270);
        shapeRenderer.end();

        // 2. Panel body
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(BODY_COLOR);
        shapeRenderer.rect(panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT);
        shapeRenderer.end();

        // 3. Header
        shapeRenderer.begin(ShapeRenderer.ShapeType. Filled);
        shapeRenderer.setColor(HEADER_COLOR);
        shapeRenderer.rect(panelX, panelY + PANEL_HEIGHT - HEADER_HEIGHT, PANEL_WIDTH, HEADER_HEIGHT);
        shapeRenderer. end();

        // 4. Border
        Gdx.gl.glLineWidth(3);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(BORDER_COLOR);
        shapeRenderer.rect(panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT);
        shapeRenderer.end();
        Gdx.gl.glLineWidth(1);

        // 5. Render slots
        syncSlotsWithInventory(inventory);

        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                boolean isSelected = (row == selectedRow && col == selectedCol);
                slots[row][col]. render(batch, shapeRenderer, font, isSelected);
            }
        }
        
        // 5.5. Info box untuk item yang dipilih
        InventorySlot selectedSlot = slots[selectedRow][selectedCol];
        if (!selectedSlot.isEmpty()) {
            // Background info box
            float infoBoxY = panelY + 18;
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0.15f, 0.35f, 0.2f, 0.9f); // Dark green semi-transparent
            shapeRenderer.rect(panelX + 8, infoBoxY, PANEL_WIDTH - 16, INFO_BOX_HEIGHT);
            shapeRenderer.end();
            
            // Border info box
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(0.85f, 0.75f, 0.5f, 1f); // Light border
            Gdx.gl.glLineWidth(1.5f);
            shapeRenderer.rect(panelX + 8, infoBoxY, PANEL_WIDTH - 16, INFO_BOX_HEIGHT);
            shapeRenderer.end();
            Gdx.gl.glLineWidth(1);
        }

        // 6. UI Elements
        batch.begin();

        // Title
        titleFont.setColor(Color.WHITE);
        titleFont.draw(batch, "INVENTORY", panelX + 12, panelY + PANEL_HEIGHT - 6);

        // Close button (X) - with hover effect
        boolean hoverClose = isMouseOver(closeButtonRect);
        titleFont.setColor(hoverClose ? Color.RED : Color.WHITE);
        titleFont.draw(batch, "X", panelX + PANEL_WIDTH - 20, panelY + PANEL_HEIGHT - 6);

        // Navigation arrows - with hover effect
        font.getData().setScale(1.2f);

        boolean hoverLeft = isMouseOver(leftArrowRect);
        font.setColor(hoverLeft ?  Color. YELLOW : Color.WHITE);
        font.draw(batch, "<", panelX + 8, panelY + PANEL_HEIGHT / 2 + 5);

        boolean hoverRight = isMouseOver(rightArrowRect);
        font.setColor(hoverRight ?  Color.YELLOW : Color.WHITE);
        font.draw(batch, ">", panelX + PANEL_WIDTH - 18, panelY + PANEL_HEIGHT / 2 + 5);

        font.getData().setScale(0.6f);

        // Item Info Display
        if (!selectedSlot.isEmpty()) {
            ItemType selectedItem = selectedSlot.getItemType();
            int itemCount = selectedSlot.getCount();
            
            // Item name dengan font yang lebih besar dan jelas
            titleFont.setColor(Color.WHITE);
            titleFont.getData().setScale(0.65f);
            titleFont.draw(batch, selectedItem.getDisplayName(), panelX + 14, panelY + 40);
            
            // Item count
            font.setColor(Color.LIGHT_GRAY);
            font.getData().setScale(0.5f);
            font.draw(batch, "Quantity: " + itemCount, panelX + 14, panelY + 27);
            font.getData().setScale(0.6f);
            titleFont.getData().setScale(0.8f);
        }
        
        batch.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void syncSlotsWithInventory(Inventory inventory) {
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                slots[row][col].clear();
            }
        }

        HashMap<ItemType, Integer> items = inventory.getItems();
        int index = 0;

        for (Map.Entry<ItemType, Integer> entry : items.entrySet()) {
            if (index >= GRID_COLS * GRID_ROWS) break;

            int row = index / GRID_COLS;
            int col = index % GRID_COLS;

            slots[row][col].addItem(entry.getKey(), entry.getValue());
            index++;
        }
    }

    /**
     * Handle input (keyboard + mouse clicks).
     * Return true if inventory should close.
     */
    public boolean handleInput() {
        // Keyboard navigation
        if (Gdx. input.isKeyJustPressed(Input.Keys.LEFT)) {
            selectedCol = Math.max(0, selectedCol - 1);
        }
        if (Gdx. input.isKeyJustPressed(Input.Keys.RIGHT)) {
            selectedCol = Math.min(GRID_COLS - 1, selectedCol + 1);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            selectedRow = Math. max(0, selectedRow - 1);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            selectedRow = Math.min(GRID_ROWS - 1, selectedRow + 1);
        }

        // Mouse clicks
        if (Gdx. input.justTouched()) {
            float mouseX = getMouseX();
            float mouseY = getMouseY();

            // Close button clicked
            if (closeButtonRect.contains(mouseX, mouseY)) {
                System.out.println("[UI] Close button clicked!");
                return true; // Signal to close inventory
            }

            // Left arrow clicked
            if (leftArrowRect.contains(mouseX, mouseY)) {
                System. out.println("[UI] Left arrow clicked!");
                // TODO: Implement page navigation
            }

            // Right arrow clicked
            if (rightArrowRect.contains(mouseX, mouseY)) {
                System.out.println("[UI] Right arrow clicked!");
                // TODO: Implement page navigation
            }
        }

        return false; // Don't close inventory
    }

    /**
     * Check if mouse is hovering over a rectangle.
     */
    private boolean isMouseOver(Rectangle rect) {
        float mouseX = getMouseX();
        float mouseY = getMouseY();
        return rect.contains(mouseX, mouseY);
    }

    /**
     * Get mouse X position in world coordinates.
     */
    private float getMouseX() {
        return Gdx.input.getX() * (viewport.getWorldWidth() / Gdx.graphics.getWidth());
    }

    /**
     * Get mouse Y position in world coordinates (flipped Y-axis).
     */
    private float getMouseY() {
        float screenY = Gdx.input.getY();
        float worldY = Gdx.graphics. getHeight() - screenY; // Flip Y
        return worldY * (viewport. getWorldHeight() / Gdx.graphics.getHeight());
    }

    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
        titleFont.dispose();
    }
}
