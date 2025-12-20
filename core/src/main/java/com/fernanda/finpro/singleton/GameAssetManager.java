package com.fernanda.finpro.singleton;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;

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
    public static final String ORC_SKULL = "orcskull.png";
    public static final String ORC_MEAT = "OrcMeat.png";

    // Food Assets (Cooked Meals)
    public static final String ROASTED_MEAT = "roasted-meat.png";
    public static final String HUNTERS_STEW = "hunter-stew.png";
    public static final String BONE_BROTH = "bone-broth.png";
    public static final String YETI_SOUP = "yeti-soup.png";
    
    // Legendary Elixirs
    public static final String BERSERKERS_ELIXIR = "legend-1.png";
    public static final String HEART_OF_MOUNTAIN = "legend-2.png";
    public static final String GOD_SLAYER_ELIXIR = "legend-3.png";

    //Campfire Assets
    public static final String CAMPFIRE = "Campfire.png";

    // UI Assets
    public static final String NOTE = "note.png";

    // Werewolf Assets
    public static final String WEREWOLF_IDLE = "Werewolf-Idle.png";
    public static final String WEREWOLF_ATTACK = "Werewolf-Attack.png";
    public static final String WEREWOLF_WALK = "Werewolf-Walk.png";
    public static final String WEREWOLF_DEATH = "Werewolf-Death.png";
    public static final String WEREWOLF_CLAW = "WerewolfClaw.png";

    public static final String YETI_IDLE = "Yeti-Idle.png";
    public static final String YETI_ATTACK = "Yeti-Attack.png";
    public static final String YETI_HEART = "YetiHeart.png";

    public static final String BOSS_IDLE = "Boss-Idle.png";
    public static final String BOSS_ATTACK = "Boss-Attack.png";
    public static final String BOSS_HITBOX = "Boss-Hitbox.png";
    public static final String BOSS_RETRIEVE = "Boss-Retrieve.png";

    // MiniBoss Assets
    public static final String MINIBOSS_IDLE = "MiniBoss-Idle.png";
    public static final String MINIBOSS_WALK = "MiniBoss-Walk.png";
    public static final String MINIBOSS_ATTACK = "MiniBoss-Attack.png";

    // Map Assets
    public static final String MAP_TMX = "maps/green_world_fix.tmx";
    public static final String ICE_MAP_TMX = "maps/ice_world_fix.tmx";
    public static final String LAVA_MAP_TMX = "maps/inferno/lava_world_fix.tmx";

    private GameAssetManager() {
        manager = new AssetManager();
        manager.setLoader(TiledMap.class, new TmxMapLoader(new InternalFileHandleResolver()));
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
        manager.load(ORC_SKULL, Texture.class);
        manager.load(ORC_MEAT, Texture.class);

        // Food (Cooked Meals)
        manager.load(ROASTED_MEAT, Texture.class);
        manager.load(HUNTERS_STEW, Texture.class);
        manager.load(BONE_BROTH, Texture.class);
        manager.load(YETI_SOUP, Texture.class);
        
        // Legendary Elixirs
        manager.load(BERSERKERS_ELIXIR, Texture.class);
        manager.load(HEART_OF_MOUNTAIN, Texture.class);
        manager.load(GOD_SLAYER_ELIXIR, Texture.class);

        // Campfire
        manager.load(CAMPFIRE, Texture.class);

        // UI Assets
        manager.load(NOTE, Texture.class);

        // Werewolf
        manager.load(WEREWOLF_IDLE, Texture.class);
        manager.load(WEREWOLF_ATTACK, Texture.class);
        manager.load(WEREWOLF_WALK, Texture.class);
        manager.load(WEREWOLF_DEATH, Texture.class);
        manager.load(WEREWOLF_CLAW, Texture.class);

        // Yeti
        manager.load(YETI_IDLE, Texture.class);
        manager.load(YETI_ATTACK, Texture.class);
        manager.load(YETI_HEART, Texture.class);

        // Boss
        manager.load(BOSS_IDLE, Texture.class);
        manager.load(BOSS_ATTACK, Texture.class);
        manager.load(BOSS_HITBOX, Texture.class);
        manager.load(BOSS_RETRIEVE, Texture.class);

        // MiniBoss
        manager.load(MINIBOSS_IDLE, Texture.class);
        manager.load(MINIBOSS_WALK, Texture.class);
        manager.load(MINIBOSS_ATTACK, Texture.class);

        // Map
        manager.load(MAP_TMX, TiledMap.class);
        manager.load(ICE_MAP_TMX, TiledMap.class);
        manager.load(LAVA_MAP_TMX, TiledMap.class);
    }

    public void finishLoading() {
        manager.finishLoading();

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
        setFilter(ORC_SKULL);
        setFilter(ORC_MEAT);
        
        // Food Assets
        setFilter(ROASTED_MEAT);
        setFilter(HUNTERS_STEW);
        setFilter(BONE_BROTH);
        setFilter(YETI_SOUP);
        
        // Legendary Elixirs
        setFilter(BERSERKERS_ELIXIR);
        setFilter(HEART_OF_MOUNTAIN);
        setFilter(GOD_SLAYER_ELIXIR);

        setFilter(CAMPFIRE);
        setFilter(NOTE);

        setFilter(WEREWOLF_IDLE);
        setFilter(WEREWOLF_ATTACK);
        setFilter(WEREWOLF_WALK);
        setFilter(WEREWOLF_DEATH);
        setFilter(WEREWOLF_CLAW);

        setFilter(YETI_IDLE);
        setFilter(YETI_ATTACK);
        setFilter(YETI_HEART);

        setFilter(BOSS_IDLE);
        setFilter(BOSS_ATTACK);
        setFilter(BOSS_HITBOX);
        setFilter(BOSS_RETRIEVE);

        setFilter(MINIBOSS_WALK);
        setFilter(MINIBOSS_IDLE);
        setFilter(MINIBOSS_ATTACK);
    }

    private void setFilter(String fileName) {
        manager.get(fileName, Texture.class).setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    }

    public void dispose() { manager.dispose(); }
    public Texture getTexture(String name) { return manager.get(name, Texture.class); }
    public TiledMap getMap() { return manager.get(MAP_TMX, TiledMap.class); }
    public TiledMap getIceMap() { return manager.get(ICE_MAP_TMX, TiledMap.class); }
    public TiledMap getLavaMap() { return manager.get(LAVA_MAP_TMX, TiledMap.class); }
}
