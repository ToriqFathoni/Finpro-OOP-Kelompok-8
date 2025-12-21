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
    
    // Dynamic Layout Variables
    private float menuWidth, menuHeight, menuX, menuY;
    private float nameX, ingredientX, effectX;
    private float rowHeight;
    private float relativeScale;
    
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
        
        // Initialize layout with default values
        updateLayout(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }
    
    /**
     * Updates all layout calculations based on screen size.
     * Call this in show() and resize() to adapt to any screen resolution.
     */
    private void updateLayout(float screenWidth, float screenHeight) {
        // DYNAMIC MENU DIMENSIONS (90% width, 80% height, centered)
        menuWidth = screenWidth * 0.9f;
        menuHeight = screenHeight * 0.8f;
        menuX = (screenWidth - menuWidth) / 2;
        menuY = (screenHeight - menuHeight) / 2;
        
        // DYNAMIC FONT SCALING (Compact to fit all items)
        // Base scale 1.6 for readable but compact layout
        // Formula: (CurrentHeight / 1080) * BaseScale
        relativeScale = (screenHeight / 1080f) * 1.6f;
        
        // Update Row Height proportionally (8% of screen height for compact layout)
        rowHeight = screenHeight * 0.08f;
        
        // DYNAMIC COLUMN POSITIONS
        nameX = menuX + (menuWidth * 0.05f);     // 5% padding
        ingredientX = menuX + (menuWidth * 0.45f); // Start at 45% width
        effectX = menuX + (menuWidth * 0.75f);    // Start at 75% width
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
            
            // B. Use dynamic menu dimensions from updateLayout
            
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
            
            // Update Score
            if (currentPlayer != null) {
                int score = recipe.isLegendary() ? 10 : 5;
                currentPlayer.cookingScore += score;
            }

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
        
        // ===== A. USE DYNAMIC DIMENSIONS =====
        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();
        // menuWidth, menuHeight, menuX, menuY are now calculated in updateLayout()
        
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
        
        // ===== DEFINE SAFETY MARGINS (Hard Boundaries) =====
        float topBorder = menuY + menuHeight - 20;    // Absolute Top Limit
        float bottomBorder = menuY + 20;              // Absolute Bottom Limit
        float leftBorder = menuX + 20;
        float rightBorder = menuX + menuWidth - 20;
        
        // --- CLOSE BUTTON 'X' ---
        font.getData().setScale(relativeScale * 0.96f); // Scale based on screen
        font.setColor(Color.WHITE);
        layout.setText(font, "X");
        font.draw(batch, "X", 
                 closeButtonX + (closeButtonSize - layout.width) / 2,
                 closeButtonY + closeButtonSize / 2 + layout.height / 2);
        
        // Note: Hitbox calculations are in update() method for click detection
        
        // --- TITLE (Relative to Top Border) ---
        float headerY = topBorder - 40; // Start slightly below top
        font.getData().setScale(relativeScale * 1.22f); // Scale based on screen
        font.setColor(Color.ORANGE);
        String title = "=== COOKING MENU ===";
        layout.setText(font, title);
        font.draw(batch, title, menuX + (menuWidth - layout.width) / 2, headerY);
        
        // --- INSTRUCTIONS (Below Header) ---
        font.getData().setScale(relativeScale * 0.57f); // Scale based on screen
        font.setColor(Color.YELLOW);
        String instructions = "[UP/DOWN] Navigate  |  [ENTER] Cook  |  [C/ESC] Close";
        layout.setText(font, instructions);
        font.draw(batch, instructions, menuX + (menuWidth - layout.width) / 2, headerY - 50);
        
        // ===== E. COLUMN LAYOUT (UNIFIED BIG FONTS) =====
        // Use dynamic column positions from updateLayout
        float effectColumnWidth = menuWidth * 0.2f; // 20% of menu width
        
        // Starting Y position for recipe list (Lifted higher to use top space)
        float startY = menuY + menuHeight - (screenH * 0.15f); // Start higher up
        float currentY = startY;
        // rowHeight is now calculated dynamically in updateLayout
        
        // Draw column headers (BIG AND UNIFORM)
        font.getData().setScale(relativeScale * 0.87f); // Scale based on screen
        font.setColor(Color.GOLD);
        font.draw(batch, "RECIPE", nameX, currentY + 15);
        font.draw(batch, "INGREDIENTS", ingredientX, currentY + 15);
        font.draw(batch, "[ EFFECT ]", effectX, currentY + 15);
        
        currentY -= 10; // Extra space after headers
        
        // ===== F. RECIPE LIST WITH 3-COLUMN LAYOUT =====
        for (int i = 0; i < recipes.size(); i++) {
            Recipe recipe = recipes.get(i);
            boolean isConsumed = isRecipeConsumed(recipe);
            
            // CRITICAL: CHECK BOTTOM BOUNDARY - Stop drawing if we're too close to bottom
            if (currentY < bottomBorder + 40) {
                break; // Stop loop to prevent overflow
            }
            
            // Check if this is first legendary recipe
            if (i == 4) {
                currentY -= rowHeight * 0.3f; // Smaller gap BEFORE header (compact)
                
                // Check boundary again after moving down
                if (currentY < bottomBorder + 40) {
                    break; // Stop if header would overflow
                }
                
                font.getData().setScale(relativeScale * 0.87f); // Scale based on screen
                font.setColor(Color.GOLD);
                String separator = "=== LEGENDARY ARTIFACTS ===";
                layout.setText(font, separator);
                // PERFECTLY CENTER using GlyphLayout
                float separatorX = menuX + (menuWidth - layout.width) / 2;
                font.draw(batch, separator, separatorX, currentY);
                currentY -= rowHeight * 0.5f; // Smaller gap AFTER header (compact)
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
            font.getData().setScale(relativeScale * 0.91f); // UNIFIED BIG FONT with dynamic scaling
            
            // Selection indicator
            if (i == selectedIndex && !isConsumed) {
                font.setColor(Color.YELLOW);
                font.draw(batch, ">>", nameX - 40, currentY);
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
            font.draw(batch, recipeName, nameX, currentY);
            
            // Strikethrough for consumed
            if (isConsumed) {
                batch.end();
                sr.begin(ShapeRenderer.ShapeType.Filled);
                sr.setColor(Color.RED);
                sr.rect(nameX, currentY - layout.height / 2 - 1, layout.width, 2);
                sr.end();
                batch.begin();
            }
            
            // === COLUMN 2: INGREDIENTS (UNIFIED BIG FONT, CYAN) ===
            font.getData().setScale(relativeScale * 0.91f); // SAME SIZE AS NAME with dynamic scaling
            font.setColor(isConsumed ? Color.DARK_GRAY : Color.CYAN);
            String ingredients = recipe.getRequirementsString();
            font.draw(batch, ingredients, ingredientX, currentY);
            
            // Move to next row
            currentY -= rowHeight;
        }
        
        // ===== G. EFFECT DESCRIPTION (RIGHT PANEL - SELECTED ONLY) =====
        if (selectedIndex >= 0 && selectedIndex < recipes.size()) {
            Recipe selectedRecipe = recipes.get(selectedIndex);
            String description = selectedRecipe.getEffectDescription();
            
            font.getData().setScale(relativeScale * 0.78f); // Slightly smaller for long text to fit
            if (selectedRecipe.isLegendary()) {
                font.setColor(Color.GOLD);
            } else {
                font.setColor(Color.CYAN);
            }
            
            // Multi-line rendering with word wrap
            font.draw(batch, description, effectX, startY - 20, effectColumnWidth, Align.left, true);
        }
        
        // --- FEEDBACK MESSAGE ---
        if (feedbackTimer > 0) {
            font.getData().setScale(relativeScale); // Scale based on screen
            font.setColor(feedbackColor);
            layout.setText(font, feedbackMessage);
            font.draw(batch, feedbackMessage, menuX + (menuWidth - layout.width) / 2, menuY + 95);
        }
        
        // --- FOOTER HINT ---
        font.getData().setScale(relativeScale * 0.48f); // Scale based on screen
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
            updateLayout(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()); // Recalculate on show
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
        updateLayout(width, height); // Recalculate layout on resize
    }
    
    public void dispose() {
        font.dispose();
    }
}
