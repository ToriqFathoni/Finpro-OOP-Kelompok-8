package com.fernanda.finpro.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.fernanda.finpro.components.Inventory;
import com.fernanda.finpro.components.ItemType;
import com.fernanda.finpro.components.Recipe;
import com.fernanda.finpro.components.RecipeManager;

import java.util.List;
import java.util.Map;

public class CookingMenu {
    private boolean visible;
    private BitmapFont font;
    private int selectedIndex;
    private GlyphLayout layout; // For text width calculations
    
    // Feedback System
    private String feedbackMessage = "";
    private float feedbackTimer = 0;
    private Color feedbackColor = Color.WHITE;
    
    // Mouse Interaction
    private Rectangle closeButtonBounds;
    
    public CookingMenu() {
        this.visible = false;
        this.selectedIndex = 0;
        this.font = new BitmapFont();
        this.layout = new GlyphLayout();
        this.closeButtonBounds = new Rectangle(0, 0, 40, 40);
        
        // Prevent pixelation when scaling font
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    }
    
    public void update(Inventory playerInventory) {
        if (!visible) return;
        
        List<Recipe> recipes = RecipeManager.getInstance().getAllRecipes();
        
        // Update feedback timer
        if (feedbackTimer > 0) {
            feedbackTimer -= Gdx.graphics.getDeltaTime();
        }
        
        // Mouse Click Detection for Close Button
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            // Convert mouse Y (top-down) to OpenGL Y (bottom-up)
            int mouseX = Gdx.input.getX();
            int mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();
            
            if (closeButtonBounds.contains(mouseX, mouseY)) {
                visible = false;
                System.out.println("DEBUG: Cooking Menu closed via mouse click");
                return;
            }
        }
        
        // Navigation
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            selectedIndex--;
            if (selectedIndex < 0) selectedIndex = recipes.size() - 1;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            selectedIndex++;
            if (selectedIndex >= recipes.size()) selectedIndex = 0;
        }
        
        // Cook selected recipe
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            attemptCook(recipes.get(selectedIndex), playerInventory);
        }
        
        // Close menu with ESC only (C is handled by Main.java)
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            visible = false;
        }
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
        
        // ===== A. CALCULATE DYNAMIC DIMENSIONS =====
        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();
        
        // Make menu box take 70% width and 60% height
        float menuWidth = screenW * 0.7f;
        float menuHeight = screenH * 0.6f;
        
        // Calculate centered position
        float menuX = (screenW - menuWidth) / 2;
        float menuY = (screenH - menuHeight) / 2;
        
        // Update close button bounds
        float closeButtonSize = 40;
        float closeButtonX = menuX + menuWidth - closeButtonSize - 10;
        float closeButtonY = menuY + menuHeight - closeButtonSize - 10;
        closeButtonBounds.set(closeButtonX, closeButtonY, closeButtonSize, closeButtonSize);
        
        // ===== B. DRAW FULL SCREEN DIMMER (Background Overlay) =====
        Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA, com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA);
        
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0, 0, 0, 0.75f); // Darker dim (75% opacity)
        sr.rect(0, 0, screenW, screenH); // Cover entire screen
        
        // ===== C. DRAW MENU BOX (The Window) =====
        sr.setColor(0.15f, 0.1f, 0.08f, 0.98f); // Dark brown/gray
        sr.rect(menuX, menuY, menuWidth, menuHeight);
        
        // Close Button (RED square)
        sr.setColor(0.8f, 0.1f, 0.1f, 1f); // Dark red
        sr.rect(closeButtonX, closeButtonY, closeButtonSize, closeButtonSize);
        
        sr.end();
        
        // ===== D. DRAW BORDER (Gold/Orange frame) =====
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(Color.ORANGE);
        Gdx.gl.glLineWidth(4);
        sr.rect(menuX, menuY, menuWidth, menuHeight);
        
        // Inner border for depth effect
        sr.setColor(Color.GOLD);
        Gdx.gl.glLineWidth(2);
        sr.rect(menuX + 8, menuY + 8, menuWidth - 16, menuHeight - 16);
        sr.end();
        
        Gdx.gl.glLineWidth(1); // Reset line width
        Gdx.gl.glDisable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
        
        // ===== E. DRAW TEXT (Scaled & Centered) =====
        batch.begin();
        
        // --- CLOSE BUTTON 'X' ---
        font.getData().setScale(2.2f);
        font.setColor(Color.WHITE);
        layout.setText(font, "X");
        font.draw(batch, "X", 
                 closeButtonX + (closeButtonSize - layout.width) / 2,
                 closeButtonY + closeButtonSize / 2 + layout.height / 2);
        
        // --- TITLE (Large, Centered) ---
        font.getData().setScale(2.5f);
        font.setColor(Color.ORANGE);
        String title = "=== COOKING MENU ===";
        layout.setText(font, title);
        float titleX = menuX + (menuWidth - layout.width) / 2;
        float titleY = menuY + menuHeight - 40; // 40px padding from top
        font.draw(batch, title, titleX, titleY);
        
        // --- INSTRUCTIONS (Below title) ---
        font.getData().setScale(1.3f);
        font.setColor(Color.YELLOW);
        String instructions = "[UP/DOWN] Navigate  |  [ENTER] Cook  |  [C/ESC] Close  |  Click [X]";
        layout.setText(font, instructions);
        float instructX = menuX + (menuWidth - layout.width) / 2;
        float instructY = titleY - 55;
        font.draw(batch, instructions, instructX, instructY);
        
        // --- RECIPE LIST ---
        font.getData().setScale(1.8f); // BIGGER RECIPE TEXT
        float listStartY = instructY - 70;
        float lineHeight = 50f; // Space between each recipe
        
        for (int i = 0; i < recipes.size(); i++) {
            Recipe recipe = recipes.get(i);
            float itemY = listStartY - (i * lineHeight);
            
            // Check if player can cook this recipe
            boolean canCook = true;
            for (Map.Entry<ItemType, Integer> req : recipe.getRequirements().entrySet()) {
                if (!playerInventory.hasItem(req.getKey(), req.getValue())) {
                    canCook = false;
                    break;
                }
            }
            
            // Draw selection indicator
            if (i == selectedIndex) {
                font.setColor(Color.YELLOW);
                font.draw(batch, ">>", menuX + 30, itemY);
            }
            
            // Draw recipe name (color-coded by availability)
            if (canCook) {
                font.setColor(Color.GREEN); // Can cook = GREEN
            } else {
                font.setColor(Color.GRAY); // Cannot cook = GRAY
            }
            font.draw(batch, (i + 1) + ". " + recipe.getRecipeName(), menuX + 70, itemY);
            
            // Draw requirements (smaller text)
            font.getData().setScale(1.1f);
            font.setColor(Color.LIGHT_GRAY);
            String reqText = "Needs: " + recipe.getRequirementsString();
            font.draw(batch, reqText, menuX + menuWidth * 0.45f, itemY);
            font.getData().setScale(1.8f); // Reset to list scale
        }
        
        // --- FEEDBACK MESSAGE (if active) ---
        if (feedbackTimer > 0) {
            font.getData().setScale(2.2f);
            font.setColor(feedbackColor);
            layout.setText(font, feedbackMessage);
            float feedbackX = menuX + (menuWidth - layout.width) / 2;
            float feedbackY = menuY + 80;
            font.draw(batch, feedbackMessage, feedbackX, feedbackY);
        }
        
        // --- FOOTER HINT ---
        font.getData().setScale(1.2f);
        font.setColor(Color.CYAN);
        String hint = "Tip: Green = Ready to cook, Gray = Missing ingredients";
        layout.setText(font, hint);
        float hintX = menuX + (menuWidth - layout.width) / 2;
        float hintY = menuY + 35;
        font.draw(batch, hint, hintX, hintY);
        
        batch.end();
        
        // ===== F. RESET FONT SCALE (CRITICAL!) =====
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
    
    public void dispose() {
        font.dispose();
    }
}
