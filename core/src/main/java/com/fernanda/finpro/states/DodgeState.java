package com.fernanda.finpro.states;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.fernanda.finpro.singleton.GameAssetManager;

public class DodgeState implements PlayerState {
    private Animation<TextureRegion> dodgeAnimation;

    public DodgeState() {
        // --- SEMENTARA ---
        // Karena belum ada gambar khusus Dodge, kita pakai Idle.
        // Nanti jika ada gambar "Soldier-Roll.png", ganti bagian ini.
        Texture texture = GameAssetManager.getInstance().getTexture(GameAssetManager.SOLDIER_IDLE);

        // Split (sama seperti Idle)
        TextureRegion[][] tmp = TextureRegion.split(texture, texture.getWidth() / 6, texture.getHeight());
        TextureRegion[] frames = new TextureRegion[6];
        for (int i = 0; i < 6; i++) frames[i] = tmp[0][i];

        // Kita mainkan animasinya agak cepat (0.05f) agar terlihat hectic
        dodgeAnimation = new Animation<>(0.05f, frames);
        dodgeAnimation.setPlayMode(Animation.PlayMode.LOOP);
    }

    @Override
    public void enter() {
        // Bisa tambahkan sfx "woosh" disini nanti
    }

    @Override
    public void update(float dt) {
        // Logika gerakan fisik ada di Player.java
    }

    @Override
    public TextureRegion getCurrentFrame(float stateTime) {
        return dodgeAnimation.getKeyFrame(stateTime, true);
    }
}
