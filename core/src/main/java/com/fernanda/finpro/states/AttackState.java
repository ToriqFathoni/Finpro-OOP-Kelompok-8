package com.fernanda.finpro.states;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.fernanda.finpro.singleton.GameAssetManager;

public class AttackState implements PlayerState {
    private Animation<TextureRegion> attackAnimation;

    public AttackState() {
        Texture texture = GameAssetManager.getInstance().getTexture(GameAssetManager.SOLDIER_ATTACK);
        // Split 6 kolom
        TextureRegion[][] tmp = TextureRegion.split(texture, texture.getWidth() / 6, texture.getHeight());
        TextureRegion[] frames = new TextureRegion[6];
        for (int i = 0; i < 6; i++) frames[i] = tmp[0][i];

        attackAnimation = new Animation<>(0.1f, frames);
        attackAnimation.setPlayMode(Animation.PlayMode.NORMAL); // Tidak looping
    }

    @Override public void enter() {}
    @Override public void update(float dt) {}
    @Override public TextureRegion getCurrentFrame(float stateTime) {
        return attackAnimation.getKeyFrame(stateTime, false);
    }

    public boolean isFinished(float stateTime) {
        return attackAnimation.isAnimationFinished(stateTime);
    }
}
