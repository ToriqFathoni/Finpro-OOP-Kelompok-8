package com.fernanda.finpro.states;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.fernanda.finpro.singleton.GameAssetManager;

public class DodgeState implements PlayerState {
    private Animation<TextureRegion> dodgeAnimation;

    public DodgeState() {
        Texture texture = GameAssetManager.getInstance().getTexture(GameAssetManager.SOLDIER_IDLE);

        TextureRegion[][] tmp = TextureRegion.split(texture, texture.getWidth() / 6, texture.getHeight());
        TextureRegion[] frames = new TextureRegion[6];
        for (int i = 0; i < 6; i++) frames[i] = tmp[0][i];

        dodgeAnimation = new Animation<>(0.05f, frames);
        dodgeAnimation.setPlayMode(Animation.PlayMode.LOOP);
    }

    @Override
    public void enter() {
    }

    @Override
    public void update(float dt) {
    }

    @Override
    public TextureRegion getCurrentFrame(float stateTime) {
        return dodgeAnimation.getKeyFrame(stateTime, true);
    }
}
