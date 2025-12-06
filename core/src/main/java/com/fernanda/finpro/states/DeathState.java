package com.fernanda.finpro.states;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.fernanda.finpro.singleton.GameAssetManager;

public class DeathState implements PlayerState {
    private Animation<TextureRegion> deathAnimation;

    public DeathState() {
        Texture texture = GameAssetManager.getInstance().getTexture(GameAssetManager.SOLDIER_DEATH);

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

        deathAnimation = new Animation<>(0.15f, frames);
        deathAnimation.setPlayMode(Animation.PlayMode.NORMAL);
    }

    @Override
    public void enter() {
        System.out.println("Player telah mati.");
    }

    @Override
    public void update(float dt) {
        // Tidak ada update, player mati
    }

    @Override
    public TextureRegion getCurrentFrame(float stateTime) {
        // Parameter kedua 'false' artinya tidak looping
        return deathAnimation.getKeyFrame(stateTime, false);
    }
}
