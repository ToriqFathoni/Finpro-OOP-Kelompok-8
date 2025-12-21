package com.fernanda.finpro.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.fernanda.finpro.managers.NetworkManager;

import java.util.Map;

public class LoginUI implements InputProcessor {
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;
    private BitmapFont titleFont;
    private Viewport viewport;
    private Texture backgroundTexture;
    
    private boolean isVisible = true;
    private String statusMessage = "";
    private StringBuilder usernameBuffer = new StringBuilder();
    private boolean isLoading = false;

    private LoginListener listener;

    public interface LoginListener {
        void onLoginSuccess(String username, Map<String, Integer> inventoryData, boolean miniBossDefeated);
    }

    public LoginUI(Viewport viewport, LoginListener listener) {
        this.viewport = viewport;
        this.listener = listener;
        this.shapeRenderer = new ShapeRenderer();
        this.batch = new SpriteBatch();
        
        // High-resolution fonts with proper scaling
        this.font = new BitmapFont();
        this.font.getData().setScale(1.6f);
        this.font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        
        this.titleFont = new BitmapFont();
        this.titleFont.getData().setScale(2.8f);
        this.titleFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        
        // Load background image
        try {
            backgroundTexture = new Texture(Gdx.files.internal("login-page.png"));
            backgroundTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        } catch (Exception e) {
            System.err.println("Failed to load login-page.png: " + e.getMessage());
            backgroundTexture = null;
        }
        
        // Set this class as the input processor to capture typing
        Gdx.input.setInputProcessor(this);
    }

    public void render() {
        if (!isVisible) return;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        
        // Draw background image
        if (backgroundTexture != null) {
            batch.draw(backgroundTexture, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        } else {
            // Fallback: draw black background
            shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0.1f, 0.1f, 0.1f, 1f);
            shapeRenderer.rect(0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
            shapeRenderer.end();
        }
        
        // Draw Game Title - "Cooking Monsta"
        String gameTitle = "Cooking Monsta";
        titleFont.setColor(1f, 1f, 1f, 1f);
        GlyphLayout titleLayout = new GlyphLayout(titleFont, gameTitle);
        float titleX = (viewport.getWorldWidth() - titleLayout.width) / 2;
        float titleY = viewport.getWorldHeight() - 80;
        
        // Title shadow (stronger depth)
        titleFont.setColor(0, 0, 0, 0.9f);
        titleFont.draw(batch, gameTitle, titleX + 4, titleY - 4);
        titleFont.setColor(0.1f, 0.1f, 0.1f, 0.6f);
        titleFont.draw(batch, gameTitle, titleX + 2, titleY - 2);
        
        // Title main - vibrant cooking theme colors (orange-red like fire)
        titleFont.setColor(1f, 0.4f, 0.1f, 1f); // Bright orange
        titleFont.draw(batch, gameTitle, titleX, titleY);
        
        batch.end();
        
        // Draw Input Box
        float boxWidth = 500;
        float boxHeight = 60;
        float boxX = (viewport.getWorldWidth() - boxWidth) / 2;
        float boxY = (viewport.getWorldHeight() / 2) - 50;
        
        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Box shadow
        shapeRenderer.setColor(0, 0, 0, 0.7f);
        shapeRenderer.rect(boxX + 5, boxY - 5, boxWidth, boxHeight);
        
        // Box background
        shapeRenderer.setColor(0.2f, 0.2f, 0.25f, 0.95f);
        shapeRenderer.rect(boxX, boxY, boxWidth, boxHeight);
        
        // Box border
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(3);
        shapeRenderer.setColor(0.8f, 0.7f, 0.3f, 1f);
        shapeRenderer.rect(boxX, boxY, boxWidth, boxHeight);
        Gdx.gl.glLineWidth(1);
        shapeRenderer.end();
        
        // Draw Text
        batch.begin();
        
        // Username label
        String label = "USERNAME:";
        font.setColor(0.9f, 0.9f, 0.9f, 1f);
        GlyphLayout labelLayout = new GlyphLayout(font, label);
        font.draw(batch, label, boxX + 20, boxY + boxHeight + 40);
        
        // Username Input with blinking cursor
        String displayText = usernameBuffer.toString();
        if (System.currentTimeMillis() % 1000 > 500) {
            displayText += "_";
        }
        font.setColor(1f, 1f, 1f, 1f);
        font.draw(batch, displayText, boxX + 20, boxY + 40);
        
        // Instruction
        if (!isLoading) {
            String hint = "Press ENTER to Login";
            font.getData().setScale(1.2f);
            GlyphLayout hintLayout = new GlyphLayout(font, hint);
            font.setColor(0.7f, 0.7f, 0.7f, 1f);
            font.draw(batch, hint, (viewport.getWorldWidth() - hintLayout.width) / 2, boxY - 30);
            font.getData().setScale(1.6f);
        }
        
        // Status message (error/loading)
        if (!statusMessage.isEmpty()) {
            font.getData().setScale(1.4f);
            GlyphLayout statusLayout = new GlyphLayout(font, statusMessage);
            if (isLoading) {
                font.setColor(0.3f, 0.8f, 1f, 1f);
            } else {
                font.setColor(1f, 0.3f, 0.3f, 1f);
            }
            font.draw(batch, statusMessage, (viewport.getWorldWidth() - statusLayout.width) / 2, boxY - 70);
            font.getData().setScale(1.6f);
        }
        
        batch.end();
    }

    private void performLogin(String username) {
        isLoading = true;
        statusMessage = "Logging in...";
        
        NetworkManager.getInstance().login(username, new NetworkManager.LoginCallback() {
            @Override
            public void onSuccess(String username, Map<String, Integer> inventoryData, boolean miniBossDefeated) {
                isLoading = false;
                isVisible = false;
                Gdx.input.setInputProcessor(null); // Release input processor
                listener.onLoginSuccess(username, inventoryData, miniBossDefeated);
            }

            @Override
            public void onFailure(Throwable t) {
                isLoading = false;
                statusMessage = "Login Failed. Try again.";
                Gdx.app.error("LoginUI", "Login failed", t);
            }
        });
    }

    public boolean isVisible() {
        return isVisible;
    }
    
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
        titleFont.dispose();
        if (backgroundTexture != null) {
            backgroundTexture.dispose();
        }
    }

    // --- InputProcessor Implementation ---

    @Override
    public boolean keyDown(int keycode) {
        if (!isVisible || isLoading) return false;

        if (keycode == Input.Keys.ENTER) {
            String username = usernameBuffer.toString().trim();
            if (!username.isEmpty()) {
                performLogin(username);
            } else {
                statusMessage = "Username cannot be empty!";
            }
            return true;
        }
        
        if (keycode == Input.Keys.BACKSPACE) {
            if (usernameBuffer.length() > 0) {
                usernameBuffer.setLength(usernameBuffer.length() - 1);
            }
            return true;
        }
        
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        if (!isVisible || isLoading) return false;

        // Allow letters, numbers, and some symbols
        if (Character.isLetterOrDigit(character) || character == '_' || character == '-') {
            if (usernameBuffer.length() < 15) { // Max length
                usernameBuffer.append(character);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) { return false; }
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) { return false; }
    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }
    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) { return false; }
    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
    @Override
    public boolean mouseMoved(int screenX, int screenY) { return false; }
    @Override
    public boolean scrolled(float amountX, float amountY) { return false; }
}
