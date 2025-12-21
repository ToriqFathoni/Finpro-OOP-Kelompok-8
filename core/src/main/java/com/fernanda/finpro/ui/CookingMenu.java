package com.fernanda.finpro.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.fernanda.finpro.components.Inventory;
import com.fernanda.finpro.components.ItemType;
import com.fernanda.finpro.components.Recipe;
import com.fernanda.finpro.components.RecipeManager;
import com.fernanda.finpro.entities.Player;

import java.util.List;
import java.util.Map;

public class CookingMenu {
    private boolean visible;
    private BitmapFont font;
    private int selectedIndex;
    private GlyphLayout layout; // For text width calculations
    private Player currentPlayer; // Track player for consumed legendary checking
    
    // Feedback System
    private String feedbackMessage = "";
    private float feedbackTimer = 0;
    private Color feedbackColor = Color.WHITE;
    
    // Mouse Interaction
    private Rectangle closeButtonBounds;
    private Viewport viewport;
    private OrthographicCamera camera;
    
    public CookingMenu() {
        this.visible = false;
        this.selectedIndex = 0;
        this.font = new BitmapFont();
        this.layout = new GlyphLayout();
        this.closeButtonBounds = new Rectangle(0, 0, 40, 40);
        
        // Create viewport for coordinate conversion
        this.camera = new OrthographicCamera();
        this.viewport = new ScreenViewport(camera);
        
        // Prevent pixelation when scaling font
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    }
    
    public void update(Inventory playerInventory, Player player) {
        if (!visible) return;
        
        this.currentPlayer = player;
        List<Recipe> recipes = RecipeManager.getInstance().getAllRecipes();
        
        // Update feedback timer
        if (feedbackTimer > 0) {
            feedbackTimer -= Gdx.graphics.getDeltaTime();
        }
        
        // Mouse Click Detection for Close Button (GIANT FORGIVING HITBOX)
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            // A. Get raw screen coordinates and convert to world space
            Vector3 touchPoint = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            viewport.unproject(touchPoint); // Convert to world coordinates
            
            // B. Calculate menu dimensions
            float screenW = Gdx.graphics.getWidth();
            float screenH = Gdx.graphics.getHeight();
            float menuWidth = screenW * 0.85f;
            float menuHeight = screenH * 0.75f;
            float menuX = (screenW - menuWidth) / 2;
            float menuY = (screenH - menuHeight) / 2;
            
            // C. Define The VISUAL Button (Where the Red X is)
            float btnX = menuX + menuWidth - 50;
            float btnY = menuY + menuHeight - 50;
            
            // D. Define The GIANT CLICK AREA (120x120 pixels total)
            float hitPadding = 40; // HUGE padding on all sides
            float hitBoxX = btnX - hitPadding;
            float hitBoxY = btnY - hitPadding;
            float hitBoxW = 40 + (hitPadding * 2); // 120px total
            float hitBoxH = 40 + (hitPadding * 2); // 120px total
            
            // E. Check collision with giant hitbox
            if (touchPoint.x >= hitBoxX && touchPoint.x <= hitBoxX + hitBoxW &&
                touchPoint.y >= hitBoxY && touchPoint.y <= hitBoxY + hitBoxH) {
                visible = false;
                return;
            }
        }
        
        // Navigation (skip consumed legendaries)
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            do {
                selectedIndex--;
                if (selectedIndex < 0) selectedIndex = recipes.size() - 1;
            } while (isRecipeConsumed(recipes.get(selectedIndex)));
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            do {
                selectedIndex++;
                if (selectedIndex >= recipes.size()) selectedIndex = 0;
            } while (isRecipeConsumed(recipes.get(selectedIndex)));
        }
        
        // Cook selected recipe (prevent cooking consumed legendaries)
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            Recipe selected = recipes.get(selectedIndex);
            if (!isRecipeConsumed(selected)) {
                attemptCook(selected, playerInventory);
            } else {
                feedbackMessage = "ALREADY CONSUMED!";
                feedbackColor = Color.ORANGE;
                feedbackTimer = 1.5f;
            }
        }
        
        // Close menu with ESC only (C is handled by Main.java)
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            visible = false;
        }
    }
    
    /**
     * Check if a legendary recipe has already been consumed
     */
    private boolean isRecipeConsumed(Recipe recipe) {
        if (currentPlayer == null) return false;
        return recipe.isLegendary() && currentPlayer.hasConsumedLegendary(recipe.getResultItem());
    }
    
    private void attemptCook(Recipe recipe, Inventory inventory) {
        boolean canCook = true;
        
        // Check if player has ALL required ingredients
        for (Map.Entry<ItemType, Integer> requirement : recipe.getRequirements().entrySet()) {
            if (!inventory.hasItem(requirement.getKey(), requirement.getValue())) {
                canCook = false;
                break;
            }
        }
        
        if (canCook) {
            // Remove all ingredients
            for (Map.Entry<ItemType, Integer> requirement : recipe.getRequirements().entrySet()) {
                inventory.removeItem(requirement.getKey(), requirement.getValue());
            }
            
            // Add result
            inventory.addItem(recipe.getResultItem(), 1);
            
            // Set SUCCESS feedback
            feedbackMessage = "SUCCESS! COOKED " + recipe.getRecipeName().toUpperCase();
            feedbackColor = Color.GREEN;
            feedbackTimer = 2.5f;
            
            System.out.println("🔥 [COOKING MENU] SUCCESS! Cooked: " + recipe.getRecipeName());
            System.out.println("   Used: " + recipe.getRequirementsString());
        } else {
            // Set FAIL feedback
            feedbackMessage = "NOT ENOUGH INGREDIENTS!";
            feedbackColor = Color.SCARLET;
            feedbackTimer = 2.0f;
            
            System.out.println("❌ [COOKING MENU] Missing Ingredients for: " + recipe.getRecipeName());
        }
    }
    
    public void render(SpriteBatch batch, ShapeRenderer sr, Inventory playerInventory) {
        if (!visible) return;
        
        List<Recipe> recipes = RecipeManager.getInstance().getAllRecipes();
        
        // ===== A. CALCULATE DIMENSIONS =====
        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();
        float menuWidth = screenW * 0.85f;
        float menuHeight = screenH * 0.75f;
        float menuX = (screenW - menuWidth) / 2;
        float menuY = (screenH - menuHeight) / 2;
        
        // Close button position
        float closeButtonSize = 40;
        float closeButtonX = menuX + menuWidth - closeButtonSize - 10;
        float closeButtonY = menuY + menuHeight - closeButtonSize - 10;
        
        // ===== B. DRAW BACKGROUND =====
        Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA, com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA);
        
        sr.begin(ShapeRenderer.ShapeType.Filled);
        // Screen dimmer
        sr.setColor(0, 0, 0, 0.75f);
        sr.rect(0, 0, screenW, screenH);
        // Menu box
        sr.setColor(0.15f, 0.1f, 0.08f, 0.98f);
        sr.rect(menuX, menuY, menuWidth, menuHeight);
        // Close button
        sr.setColor(0.8f, 0.1f, 0.1f, 1f);
        sr.rect(closeButtonX, closeButtonY, closeButtonSize, closeButtonSize);
        sr.end();
        
        // ===== C. DRAW BORDERS =====
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(Color.ORANGE);
        Gdx.gl.glLineWidth(4);
        sr.rect(menuX, menuY, menuWidth, menuHeight);
        sr.setColor(Color.GOLD);
        Gdx.gl.glLineWidth(2);
        sr.rect(menuX + 8, menuY + 8, menuWidth - 16, menuHeight - 16);
        sr.end();
        Gdx.gl.glLineWidth(1);
        Gdx.gl.glDisable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
        
        // ===== D. DRAW TEXT CONTENT =====
        batch.begin();
        
        // --- CLOSE BUTTON 'X' ---
        font.getData().setScale(2.2f);
        font.setColor(Color.WHITE);
        layout.setText(font, "X");
        font.draw(batch, "X", 
                 closeButtonX + (closeButtonSize - layout.width) / 2,
                 closeButtonY + closeButtonSize / 2 + layout.height / 2);
        
        // Note: Hitbox calculations are in update() method for click detection
        
        // --- TITLE ---
        font.getData().setScale(2.8f);
        font.setColor(Color.ORANGE);
        String title = "=== COOKING MENU ===";
        layout.setText(font, title);
        font.draw(batch, title, menuX + (menuWidth - layout.width) / 2, menuY + menuHeight - 35);
        
        // --- INSTRUCTIONS ---
        font.getData().setScale(1.3f);
        font.setColor(Color.YELLOW);
        String instructions = "[UP/DOWN] Navigate  |  [ENTER] Cook  |  [C/ESC] Close";
        layout.setText(font, instructions);
        font.draw(batch, instructions, menuX + (menuWidth - layout.width) / 2, menuY + menuHeight - 75);
        
        // ===== E. COLUMN LAYOUT (UNIFIED BIG FONTS) =====
        // Define column positions
        float nameColumnX = menuX + 80;                       // Left column
        float ingredientColumnX = menuX + (menuWidth / 2) - 150; // Center column
        float effectColumnX = menuX + menuWidth - 400;        // Right column
        float effectColumnWidth = 380;
        
        // Starting Y position for recipe list
        float startY = menuY + menuHeight - 140;
        float currentY = startY;
        float rowHeight = 85; // Large spacing for big text
        
        // Draw column headers (BIG AND UNIFORM)
        font.getData().setScale(2.0f);
        font.setColor(Color.GOLD);
        font.draw(batch, "RECIPE", nameColumnX, currentY + 15);
        font.draw(batch, "INGREDIENTS", ingredientColumnX, currentY + 15);
        font.draw(batch, "[ EFFECT ]", effectColumnX, currentY + 15);
        
        currentY -= 10; // Extra space after headers
        
        // ===== F. RECIPE LIST WITH 3-COLUMN LAYOUT =====
        for (int i = 0; i < recipes.size(); i++) {
            Recipe recipe = recipes.get(i);
            boolean isConsumed = isRecipeConsumed(recipe);
            
            // Check if this is first legendary recipe
            if (i == 4) {
                currentY -= 40; // Extra padding before separator
                font.getData().setScale(2.0f);
                font.setColor(Color.GOLD);
                String separator = "=== LEGENDARY ARTIFACTS ===";
                layout.setText(font, separator);
                // PERFECTLY CENTER using GlyphLayout
                float separatorX = menuX + (menuWidth - layout.width) / 2;
                font.draw(batch, separator, separatorX, currentY);
                currentY -= 60; // Extra padding after separator
            }
            
            // Check if player can cook
            boolean canCook = true;
            for (Map.Entry<ItemType, Integer> req : recipe.getRequirements().entrySet()) {
                if (!playerInventory.hasItem(req.getKey(), req.getValue())) {
                    canCook = false;
                    break;
                }
            }
            
            // === COLUMN 1: RECIPE NAME ===
            font.getData().setScale(2.1f); // UNIFIED BIG FONT
            
            // Selection indicator
            if (i == selectedIndex && !isConsumed) {
                font.setColor(Color.YELLOW);
                font.draw(batch, ">>", nameColumnX - 40, currentY);
            }
            
            // Recipe name with color coding
            String recipeName = (i + 1) + ". " + recipe.getRecipeName();
            if (isConsumed) {
                font.setColor(Color.DARK_GRAY);
            } else if (canCook) {
                font.setColor(Color.GREEN);
            } else {
                font.setColor(Color.GRAY);
            }
            
            layout.setText(font, recipeName);
            font.draw(batch, recipeName, nameColumnX, currentY);
            
            // Strikethrough for consumed
            if (isConsumed) {
                batch.end();
                sr.begin(ShapeRenderer.ShapeType.Filled);
                sr.setColor(Color.RED);
                sr.rect(nameColumnX, currentY - layout.height / 2 - 1, layout.width, 2);
                sr.end();
                batch.begin();
            }
            
            // === COLUMN 2: INGREDIENTS (UNIFIED BIG FONT, CYAN) ===
            font.getData().setScale(2.1f); // SAME SIZE AS NAME
            font.setColor(isConsumed ? Color.DARK_GRAY : Color.CYAN);
            String ingredients = recipe.getRequirementsString();
            font.draw(batch, ingredients, ingredientColumnX, currentY);
            
            // Move to next row
            currentY -= rowHeight;
        }
        
        // ===== G. EFFECT DESCRIPTION (RIGHT PANEL - SELECTED ONLY) =====
        if (selectedIndex >= 0 && selectedIndex < recipes.size()) {
            Recipe selectedRecipe = recipes.get(selectedIndex);
            String description = selectedRecipe.getEffectDescription();
            
            font.getData().setScale(1.8f); // Slightly smaller for long text to fit
            if (selectedRecipe.isLegendary()) {
                font.setColor(Color.GOLD);
            } else {
                font.setColor(Color.CYAN);
            }
            
            // Multi-line rendering with word wrap
            font.draw(batch, description, effectColumnX, startY - 20, effectColumnWidth, Align.left, true);
        }
        
        // --- FEEDBACK MESSAGE ---
        if (feedbackTimer > 0) {
            font.getData().setScale(2.3f);
            font.setColor(feedbackColor);
            layout.setText(font, feedbackMessage);
            font.draw(batch, feedbackMessage, menuX + (menuWidth - layout.width) / 2, menuY + 95);
        }
        
        // --- FOOTER HINT ---
        font.getData().setScale(1.1f);
        font.setColor(Color.CYAN);
        String hint = "Green=Ready | Gray=Missing | Strikethrough=Consumed";
        layout.setText(font, hint);
        font.draw(batch, hint, menuX + (menuWidth - layout.width) / 2, menuY + 40);
        
        batch.end();
        
        // Reset font scale
        font.getData().setScale(1.0f);
    }
    
    public void setVisible(boolean visible) {
        this.visible = visible;
        if (visible) {
            selectedIndex = 0; // Reset selection when opening
            System.out.println("DEBUG: Cooking Menu OPENED");
        } else {
            System.out.println("DEBUG: Cooking Menu CLOSED");
        }
    }
    
    public boolean isVisible() {
        return visible;
    }
    
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }
    
    public void dispose() {
        font.dispose();
    }
}
