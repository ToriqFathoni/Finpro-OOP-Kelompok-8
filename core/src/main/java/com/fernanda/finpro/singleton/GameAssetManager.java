package com.fernanda.finpro.singleton;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;

public class GameAssetManager {
    private static GameAssetManager instance;
    public final AssetManager manager;

    // Player Assets
    public static final String SOLDIER_WALK = "Soldier-Walk.png";
    public static final String SOLDIER_IDLE = "Soldier-Idle.png";
    public static final String SOLDIER_ATTACK = "Soldier-Attack01.png";
    public static final String SOLDIER_HURT = "Soldier-Hurt.png";
    public static final String SOLDIER_DEATH = "Soldier-Death.png";

    // Orc Assets
    public static final String ORC_IDLE = "Orc-Idle.png";
    public static final String ORC_WALK = "Orc-Walk.png";
    public static final String ORC_ATTACK = "Orc-Attack01.png";
    public static final String ORC_HURT = "Orc-Hurt.png";
    public static final String ORC_DEATH = "Orc-Death.png";

    private GameAssetManager() {
        manager = new AssetManager();
    }

    public static GameAssetManager getInstance() {
        if (instance == null) instance = new GameAssetManager();
        return instance;
    }

    public void loadImages() {
        // Player
        manager.load(SOLDIER_WALK, Texture.class);
        manager.load(SOLDIER_IDLE, Texture.class);
        manager.load(SOLDIER_ATTACK, Texture.class);
        manager.load(SOLDIER_HURT, Texture.class);
        manager.load(SOLDIER_DEATH, Texture.class);

        // Orc
        manager.load(ORC_IDLE, Texture.class);
        manager.load(ORC_WALK, Texture.class);
        manager.load(ORC_ATTACK, Texture.class);
        manager.load(ORC_HURT, Texture.class);
        manager.load(ORC_DEATH, Texture.class);
    }

    public void finishLoading() {
        manager.finishLoading();

        // Filter Nearest untuk Pixel Art agar tajam
        setFilter(SOLDIER_WALK);
        setFilter(SOLDIER_IDLE);
        setFilter(SOLDIER_ATTACK);
        setFilter(SOLDIER_HURT);
        setFilter(SOLDIER_DEATH);

        setFilter(ORC_IDLE);
        setFilter(ORC_WALK);
        setFilter(ORC_ATTACK);
        setFilter(ORC_HURT);
        setFilter(ORC_DEATH);
    }

    private void setFilter(String fileName) {
        manager.get(fileName, Texture.class).setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    }

    public void dispose() { manager.dispose(); }
    public Texture getTexture(String name) { return manager.get(name, Texture.class); }
}
