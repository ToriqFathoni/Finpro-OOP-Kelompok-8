package com.fernanda.finpro.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
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
    private Viewport viewport;
    
    private boolean isVisible = true;
    private String statusMessage = "Enter Username:";
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
        this.font = new BitmapFont();
        this.font.getData().setScale(1.5f);
        
        // Set this class as the input processor to capture typing
        Gdx.input.setInputProcessor(this);
    }

    public void render() {
        if (!isVisible) return;

        // Draw semi-transparent background
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        
        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.9f);
        shapeRenderer.rect(0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        
        // Draw Input Box
        float boxWidth = 400;
        float boxHeight = 50;
        float boxX = (viewport.getWorldWidth() - boxWidth) / 2;
        float boxY = (viewport.getWorldHeight() - boxHeight) / 2;
        
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(boxX, boxY, boxWidth, boxHeight);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(boxX + 2, boxY + 2, boxWidth - 4, boxHeight - 4); // Border effect
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.rect(boxX + 4, boxY + 4, boxWidth - 8, boxHeight - 8); // Inner black box
        
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // Draw Text
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        
        // Status / Title
        GlyphLayout layout = new GlyphLayout(font, statusMessage);
        float textX = (viewport.getWorldWidth() - layout.width) / 2;
        float textY = boxY + boxHeight + 50;
        font.setColor(Color.WHITE);
        font.draw(batch, statusMessage, textX, textY);
        
        // Username Input
        String displayText = usernameBuffer.toString() + (System.currentTimeMillis() % 1000 > 500 ? "|" : ""); // Blinking cursor
        GlyphLayout inputLayout = new GlyphLayout(font, displayText);
        float inputX = (viewport.getWorldWidth() - inputLayout.width) / 2;
        float inputY = boxY + 35;
        font.setColor(Color.WHITE);
        font.draw(batch, displayText, inputX, inputY);
        
        // Instruction
        if (!isLoading) {
            String hint = "Type username and press ENTER";
            font.getData().setScale(1.0f);
            GlyphLayout hintLayout = new GlyphLayout(font, hint);
            font.setColor(Color.GRAY);
            font.draw(batch, hint, (viewport.getWorldWidth() - hintLayout.width) / 2, boxY - 20);
            font.getData().setScale(1.5f);
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
