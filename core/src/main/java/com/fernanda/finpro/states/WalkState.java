package com.fernanda.finpro.states;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.fernanda.finpro.singleton.GameAssetManager;

public class WalkState implements PlayerState {
    private Animation<TextureRegion> walkAnimation;

    public WalkState() {
        Texture texture = GameAssetManager.getInstance().getTexture(GameAssetManager.SOLDIER_WALK);
        int frameCols = 8;
        int frameRows = 1;

        int perFrameWidth = texture.getWidth() / frameCols;
        int perFrameHeight = texture.getHeight() / frameRows;

        TextureRegion[][] tmp = TextureRegion.split(texture, perFrameWidth, perFrameHeight);

        TextureRegion[] frames = new TextureRegion[frameCols * frameRows];
        int index = 0;
        for (int i = 0; i < frameRows; i++) {
            for (int j = 0; j < frameCols; j++) {
                frames[index++] = tmp[i][j];
            }
        }

        walkAnimation = new Animation<>(0.1f, frames);
        walkAnimation.setPlayMode(Animation.PlayMode.LOOP);
    }

    @Override
    public void enter() {
        // Tidak ada logika khusus saat mulai jalan
    }

    @Override
    public void update(float dt) {
        // WalkState tidak mengurus posisi fisik, hanya animasi.
        // Gerakan fisik diurus oleh Player.java (velocity)
    }

    @Override
    public TextureRegion getCurrentFrame(float stateTime) {
        return walkAnimation.getKeyFrame(stateTime, true);
    }
}
