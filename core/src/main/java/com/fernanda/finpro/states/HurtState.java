package com.fernanda.finpro.states;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.fernanda.finpro.singleton.GameAssetManager;

public class HurtState implements PlayerState{
    private Animation<TextureRegion> hurtAnimation;
    private float animationDuration;

    public HurtState() {
        Texture texture = GameAssetManager.getInstance().getTexture(GameAssetManager.SOLDIER_HURT);

        int frameCols = 4;
        int frameRows = 1;

        TextureRegion[][] tmp = TextureRegion.split(texture,
            texture.getWidth() / frameCols,
            texture.getHeight() / frameRows);

        TextureRegion[] frames = new TextureRegion[frameCols * frameRows];
        int index = 0;
        for (int i = 0; i < frameRows; i++) {
            for (int j = 0; j < frameCols; j++) {
                frames[index++] = tmp[i][j];
            }
        }

        hurtAnimation = new Animation<>(0.1f, frames);
        hurtAnimation.setPlayMode(Animation.PlayMode.NORMAL);
        animationDuration = hurtAnimation.getAnimationDuration();
    }

    @Override
    public void enter() {
    }

    @Override
    public void update(float dt) {
        // Player biasanya diam sebentar saat kesakitan
    }

    public boolean isFinished(float stateTime) {
        return stateTime >= animationDuration;
    }

    @Override
    public TextureRegion getCurrentFrame(float stateTime) {
        return hurtAnimation.getKeyFrame(stateTime, false);
    }
}
