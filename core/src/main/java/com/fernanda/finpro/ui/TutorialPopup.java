package com.fernanda.finpro.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Align;

public class TutorialPopup {
    private Texture backgroundTexture;
    private BitmapFont font;
    private boolean isVisible = true;
    private GlyphLayout layout;

    public TutorialPopup() {
        try {
            backgroundTexture = new Texture("note.png");
            System.out.println("✅ SUCCESS: note.png loaded successfully!");
        } catch (Exception e) {
            System.err.println("❌ ERROR: note.png missing. Using fallback gray rectangle.");
            backgroundTexture = null;
        }
        font = new BitmapFont();
        layout = new GlyphLayout();
        // CRITICAL FIX: Set Linear Filter for smooth text when scaled up
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    }

    // SAFELY HANDLE BATCH STATE
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        if (!isVisible) return;

        // 1. SAFETY CHECK: If batch is currently drawing, END it so we can use ShapeRenderer
        boolean batchWasDrawing = batch.isDrawing();
        if (batchWasDrawing) {
            batch.end();
        }

        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();

        // --- DRAW DIMMED BACKGROUND (ShapeRenderer) ---
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.6f); // Dark overlay
        shapeRenderer.rect(0, 0, screenW, screenH);
        
        // Draw fallback gray rectangle if texture is missing
        if (backgroundTexture == null) {
            float scrollWidth = screenW * 0.6f;
            float scrollHeight = scrollWidth * 1.3f;
            float scrollX = (screenW - scrollWidth) / 2f;
            float scrollY = (screenH - scrollHeight) / 2f;
            shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 0.95f);
            shapeRenderer.rect(scrollX, scrollY, scrollWidth, scrollHeight);
        }
        
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // --- DRAW SCROLL & TEXT (SpriteBatch) ---
        batch.begin(); // RESTART BATCH

        // Calculate Dimensions
        float scrollWidth = screenW * 0.6f; 
        float scrollHeight = (backgroundTexture != null) ? 
            scrollWidth * ((float)backgroundTexture.getHeight() / backgroundTexture.getWidth()) : 
            scrollWidth * 1.3f; // Fallback ratio
            
        float scrollX = (screenW - scrollWidth) / 2f;
        float scrollY = (screenH - scrollHeight) / 2f;

        // Draw Texture (if available)
        if (backgroundTexture != null) {
            batch.draw(backgroundTexture, scrollX, scrollY, scrollWidth, scrollHeight);
        }

        // Draw Text Content
        renderText(batch, screenH, scrollX, scrollY, scrollWidth, scrollHeight);

        batch.end(); // CLOSE BATCH AFTER DRAWING
    }

    private void renderText(SpriteBatch batch, float screenH, float x, float y, float w, float h) {
        float baseScale = screenH / 720f;
        
        // Margins
        float paddingTop = h * 0.19f;  
        float paddingX = w * 0.24f;    
        
        float textTargetWidth = w - (paddingX * 2); 
        float textCenterX = x + paddingX;
        
        // Start Y Position
        float currentY = y + h - paddingTop;

        // --- 1. HEADER (Keep this position, it's perfect) ---
        font.getData().setScale(baseScale * 1.4f); 
        font.setColor(Color.BLACK);
        
        layout.setText(font, "DEAR WARRIOR,", Color.BLACK, textTargetWidth, Align.center, true);
        font.draw(batch, layout, textCenterX, currentY);
        
        // --- 2. INCREASE GAP (CRITICAL FIX) ---
        // Previously 35, Increased to 60 to push body text out of the shadow area
        currentY -= layout.height + (60 * baseScale); 

        // --- 3. STORY ---
        font.getData().setScale(baseScale * 1.0f); 
        font.setColor(Color.DARK_GRAY);

        String part1 = "You are the CHOSEN ONE summoned to save this world.\n" +
                       "Your ultimate goal is to defeat the FINAL BOSS.";
        
        layout.setText(font, part1, Color.DARK_GRAY, textTargetWidth, Align.center, true);
        font.draw(batch, layout, textCenterX, currentY);
        
        currentY -= layout.height + (20 * baseScale); 

        String part2 = "However, you are weak. You must hunt, gather, and consume LEGENDARY MEALS. " +
                       "Only these meals grant the PERMANENT STRENGTH needed for victory.";
        
        layout.setText(font, part2, Color.DARK_GRAY, textTargetWidth, Align.center, true);
        font.draw(batch, layout, textCenterX, currentY);
        
        // Compact the lower gaps slightly to ensure footer fits
        currentY -= layout.height + (45 * baseScale); 

        // --- 4. CONTROLS ---
        font.getData().setScale(baseScale * 0.9f);
        String controls = "[CONTROLS]\n" +
                          "WASD: Move  |  CLICK: Attack\n" +
                          "TAB: Inventory  |  C: Cook\n" +
                          "H: Help Menu";
                          
        layout.setText(font, controls, Color.BLACK, textTargetWidth, Align.center, true);
        font.draw(batch, layout, textCenterX, currentY);

        // --- 5. FOOTER ---
        currentY -= layout.height + (35 * baseScale); 
        font.getData().setScale(baseScale * 0.8f);
        layout.setText(font, "[ Press SPACE or H to Begin ]", Color.FIREBRICK, textTargetWidth, Align.center, true);
        font.draw(batch, layout, textCenterX, currentY);
        
        // Reset Scale
        font.getData().setScale(1.0f);
    }
    
    public void toggle() {
        isVisible = !isVisible;
    }
    
    public void show() {
        isVisible = true;
    }
    
    public void hide() {
        isVisible = false;
    }
    
    public boolean isVisible() {
        return isVisible;
    }
    
    public void dispose() {
        font.dispose();
        if (backgroundTexture != null) {
            backgroundTexture.dispose();
        }
    }
}
