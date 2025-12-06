package com.fernanda.finpro.states;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.fernanda.finpro.singleton.GameAssetManager;

public class WalkState implements PlayerState {
    private Animation<TextureRegion> walkAnimation;

    public WalkState() {
        Texture texture = GameAssetManager.getInstance().getTexture(GameAssetManager.SOLDIER_WALK);

        // --- KONFIGURASI FRAME ---
        // Sesuai gambar Anda, ada 6 frame jalan dalam 1 baris
        int frameCols = 6;
        int frameRows = 1;

        // --- DIAGNOSA OTOMATIS (CEK UKURAN GAMBAR) ---
        // Kode ini akan memberi tahu Anda jika asetnya bermasalah matematika
        if (texture.getWidth() % frameCols != 0) {
            System.err.println("===============================================================");
            System.err.println(" [WARNING FATAL] MASALAH JITTER TERDETEKSI PADA ASET!");
            System.err.println("---------------------------------------------------------------");
            System.err.println(" File       : Soldier-Walk.png");
            System.err.println(" Lebar Asli : " + texture.getWidth() + " pixel");
            System.err.println(" Jumlah Col : " + frameCols);
            System.err.println(" Kalkulasi  : " + texture.getWidth() + " / " + frameCols + " = "
                + (texture.getWidth() / (float)frameCols));
            System.err.println("");
            System.err.println(" MASALAH: Lebar gambar TIDAK HABIS dibagi " + frameCols + ".");
            System.err.println("          Komputer membuang koma, menyebabkan pergeseran pixel.");
            System.err.println(" SOLUSI : Resize kanvas gambar di Photoshop/Editor agar lebarnya kelipatan 6.");
            System.err.println("          (Contoh lebar yang benar: 192, 288, 384, 600, dll)");
            System.err.println("===============================================================");
        }

        // --- LOGIKA SPLIT ---
        // texture.getWidth() / frameCols akan membulatkan ke bawah jika hasil koma.
        // Inilah penyebab gambar bergeser jika aset tidak pas.
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
