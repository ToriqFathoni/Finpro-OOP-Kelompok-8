package com.fernanda.finpro.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.fernanda.finpro.singleton.GameAssetManager;

public class Decoration {
    public Vector2 position;
    private Texture texture;
    private float width, height;
    private Rectangle collisionBox;
    private boolean hasCollision;
    private float rotation = 0f;

    public Decoration(String textureName, float x, float y, float scale, boolean hasCollision) {
        this.position = new Vector2(x, y);
        this.texture = GameAssetManager.getInstance().getTexture(textureName);
        this.width = texture.getWidth() * scale;
        this.height = texture.getHeight() * scale;
        this.hasCollision = hasCollision;
        
        if (hasCollision) {
            if (textureName.contains("Fence")) {
                // Full collision for fence
                this.collisionBox = new Rectangle(x, y, width, height);
            } else {
                // Collision box at the bottom (trunk area)
                float colWidth = width * 0.3f;
                float colHeight = height * 0.2f;
                this.collisionBox = new Rectangle(x + (width - colWidth) / 2, y, colWidth, colHeight);
            }
        }
    }

    public Decoration(String textureName, float x, float y, float scale) {
        this(textureName, x, y, scale, true);
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
        if (hasCollision && collisionBox != null) {
            if (Math.abs(rotation - 90) < 0.1f || Math.abs(rotation - 270) < 0.1f) {
                // Rotate collision box 90 degrees around center
                float cx = position.x + width / 2;
                float cy = position.y + height / 2;
                
                // Swap dimensions (Original height becomes width, Original width becomes height)
                float newWidth = height; 
                float newHeight = width; 
                
                this.collisionBox.setSize(newWidth, newHeight);
                this.collisionBox.setPosition(cx - newWidth / 2, cy - newHeight / 2);
            }
        }
    }

    public void render(SpriteBatch batch) {
        // batch.draw(texture, x, y, originX, originY, width, height, scaleX, scaleY, rotation, srcX, srcY, srcWidth, srcHeight, flipX, flipY)
        batch.draw(texture, 
            position.x, position.y, 
            width / 2, height / 2, 
            width, height, 
            1, 1, 
            rotation, 
            0, 0, 
            texture.getWidth(), texture.getHeight(), 
            false, false);
    }
    
    public float getWidth() { return width; }
    public float getHeight() { return height; }
    public Rectangle getCollisionBox() { return collisionBox; }
    public boolean hasCollision() { return hasCollision; }
}
