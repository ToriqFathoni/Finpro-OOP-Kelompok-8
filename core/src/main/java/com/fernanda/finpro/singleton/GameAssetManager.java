package com.fernanda.finpro.singleton;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;

public class GameAssetManager {
    private static GameAssetManager instance;
    public final AssetManager manager;

    public static final String SOLDIER_WALK = "Soldier-Walk.png";
    public static final String SOLDIER_IDLE = "Soldier-Idle.png";
    public static final String SOLDIER_ATTACK = "Soldier-Attack01.png";
    public static final String SOLDIER_HURT = "Soldier-Hurt.png";
    public static final String SOLDIER_DEATH = "Soldier-Death.png";

    public static final String ORC_IDLE = "Orc-Idle.png";
    public static final String ORC_WALK = "Orc-Walk.png";
    public static final String ORC_ATTACK = "Orc-Attack01.png";
    public static final String ORC_HURT = "Orc-Hurt.png";
    public static final String ORC_DEATH = "Orc-Death.png";
    public static final String ORC_SKULL = "orcskull.png";
    public static final String ORC_MEAT = "OrcMeat.png";

    public static final String ROASTED_MEAT = "roasted-meat.png";
    public static final String HUNTERS_STEW = "hunter-stew.png";
    public static final String BONE_BROTH = "bone-broth.png";
    public static final String YETI_SOUP = "yeti-soup.png";
    
    public static final String BERSERKERS_ELIXIR = "legend-1.png";
    public static final String HEART_OF_MOUNTAIN = "legend-2.png";
    public static final String GOD_SLAYER_ELIXIR = "legend-3.png";

    public static final String CAMPFIRE = "Campfire.png";

    public static final String NOTE = "note.png";

    public static final String WEREWOLF_IDLE = "Werewolf-Idle.png";
    public static final String WEREWOLF_ATTACK = "Werewolf-Attack.png";
    public static final String WEREWOLF_WALK = "Werewolf-Walk.png";
    public static final String WEREWOLF_DEATH = "Werewolf-Death.png";
    public static final String WEREWOLF_CLAW = "WerewolfClaw.png";

    public static final String YETI_IDLE = "Yeti-Idle.png";
    public static final String YETI_ATTACK = "Yeti-Attack.png";
    public static final String YETI_WALK = "Yeti-Walk.png";
    public static final String YETI_HEART = "YetiHeart.png";
    public static final String ETERNAL_ICE_SHARD = "ice-shard.png";

    public static final String BOSS_IDLE = "Boss-Idle.png";
    public static final String BOSS_ATTACK = "Boss-Attack.png";
    public static final String BOSS_HITBOX = "Boss-Hitbox.png";
    public static final String BOSS_RETRIEVE = "Boss-Retrieve.png";
    public static final String BOSS_CASTING = "Boss-Casting.png";
    public static final String BOSS_DEATH = "Boss-Death.png";
    public static final String FIREBALL = "Fireball.png";
    public static final String EXPLOSION = "Explosion.png";
    public static final String SHADOW = "Shadow.png";

    public static final String MINIBOSS_IDLE = "MiniBoss-Idle.png";
    public static final String MINIBOSS_WALK = "MiniBoss-Walk.png";
    public static final String MINIBOSS_ATTACK = "MiniBoss-Attack.png";

    public static final String MAP_TMX = "maps/green_world_fix.tmx";
    public static final String ICE_MAP_TMX = "maps/ice_world_fix.tmx";
    public static final String LAVA_MAP_TMX = "maps/inferno/lava_world_fix.tmx";

    public static final String LOBBY_MUSIC = "musics/lobby_music.ogg";
    public static final String FOREST_MUSIC = "musics/forest_music.mp3";
    public static final String INFERNO_MUSIC = "musics/inferno_music.ogg";

    public static final String SWORD_SLASH = "musics/sword.wav";
    public static final String MONSTER_HURT = "musics/monster-hurt.wav";
    public static final String METEOR_CRASH = "musics/meteor_crash.mp3";
    public static final String ICE_ATTACK = "musics/ice_attack.mp3";
    public static final String BOSS_SMASH = "musics/boss_smash.mp3";
    public static final String YETI_HIT = "musics/yeti_attack.mp3";
    public static final String WEREWOLF_SCRATCH = "musics/werewolf_attack.mp3";

    private Music lobbyMusic;
    private Music forestMusic;
    private Music infernoMusic;
    private Sound swordSlashSound;
    private Sound monsterHurtSound;
    private Sound meteorCrashSound;
    private Sound iceAttackSound;
    private Sound bossSmashSound;
    private Sound yetiHitSound;
    private Sound werewolfScratchSound;

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
        manager.load(YETI_WALK, Texture.class);
        manager.load(YETI_HEART, Texture.class);
        manager.load(ETERNAL_ICE_SHARD, Texture.class);

        // Boss
        manager.load(BOSS_IDLE, Texture.class);
        manager.load(BOSS_ATTACK, Texture.class);
        manager.load(BOSS_HITBOX, Texture.class);
        manager.load(BOSS_RETRIEVE, Texture.class);
        manager.load(BOSS_CASTING, Texture.class);
        manager.load(BOSS_DEATH, Texture.class);
        manager.load(FIREBALL, Texture.class);
        manager.load(EXPLOSION, Texture.class);
        manager.load(SHADOW, Texture.class);

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
        setFilter(YETI_WALK);
        setFilter(YETI_HEART);
        setFilter(ETERNAL_ICE_SHARD);

        setFilter(BOSS_IDLE);
        setFilter(BOSS_ATTACK);
        setFilter(BOSS_HITBOX);
        setFilter(BOSS_RETRIEVE);
        setFilter(BOSS_CASTING);
        setFilter(BOSS_DEATH);
        setFilter(FIREBALL);
        setFilter(EXPLOSION);
        setFilter(SHADOW);

        setFilter(MINIBOSS_WALK);
        setFilter(MINIBOSS_IDLE);
        setFilter(MINIBOSS_ATTACK);
    }

    private void setFilter(String fileName) {
        manager.get(fileName, Texture.class).setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    }

    public void loadMusic() {
        try {
            lobbyMusic = Gdx.audio.newMusic(Gdx.files.internal(LOBBY_MUSIC));
            lobbyMusic.setLooping(true);
            lobbyMusic.setVolume(0.4f);
            System.out.println("✅ Lobby music loaded");
        } catch (Exception e) {
            System.err.println("❌ Failed to load lobby music: " + e.getMessage());
        }

        try {
            forestMusic = Gdx.audio.newMusic(Gdx.files.internal(FOREST_MUSIC));
            forestMusic.setLooping(true);
            forestMusic.setVolume(0.4f);
            System.out.println("✅ Forest music loaded");
        } catch (Exception e) {
            System.err.println("❌ Failed to load forest music: " + e.getMessage());
        }

        try {
            infernoMusic = Gdx.audio.newMusic(Gdx.files.internal(INFERNO_MUSIC));
            infernoMusic.setLooping(true);
            infernoMusic.setVolume(0.5f);
            System.out.println("✅ Inferno music loaded");
        } catch (Exception e) {
            System.err.println("❌ Failed to load inferno music: " + e.getMessage());
        }
    }

    public void loadSounds() {
        try {
            swordSlashSound = Gdx.audio.newSound(Gdx.files.internal(SWORD_SLASH));
            System.out.println("✅ Sword slash sound loaded");
        } catch (Exception e) {
            System.err.println("❌ Failed to load sword slash sound: " + e.getMessage());
        }
        
        try {
            monsterHurtSound = Gdx.audio.newSound(Gdx.files.internal(MONSTER_HURT));
            System.out.println("✅ Monster hurt sound loaded");
        } catch (Exception e) {
            System.err.println("❌ Failed to load monster hurt sound: " + e.getMessage());
        }
        
        try {
            meteorCrashSound = Gdx.audio.newSound(Gdx.files.internal(METEOR_CRASH));
            System.out.println("✅ Meteor crash sound loaded");
        } catch (Exception e) {
            System.err.println("❌ Failed to load meteor crash sound: " + e.getMessage());
        }
        
        try {
            iceAttackSound = Gdx.audio.newSound(Gdx.files.internal(ICE_ATTACK));
            System.out.println("✅ Ice attack sound loaded");
        } catch (Exception e) {
            System.err.println("❌ Failed to load ice attack sound: " + e.getMessage());
        }
        
        try {
            bossSmashSound = Gdx.audio.newSound(Gdx.files.internal(BOSS_SMASH));
            System.out.println("✅ Boss smash sound loaded");
        } catch (Exception e) {
            System.err.println("❌ Failed to load boss smash sound: " + e.getMessage());
        }
        
        try {
            yetiHitSound = Gdx.audio.newSound(Gdx.files.internal(YETI_HIT));
            System.out.println("✅ Yeti hit sound loaded");
        } catch (Exception e) {
            System.err.println("❌ Failed to load yeti hit sound: " + e.getMessage());
        }
        
        try {
            werewolfScratchSound = Gdx.audio.newSound(Gdx.files.internal(WEREWOLF_SCRATCH));
            System.out.println("✅ Werewolf scratch sound loaded");
        } catch (Exception e) {
            System.err.println("❌ Failed to load werewolf scratch sound: " + e.getMessage());
        }
    }

    public void dispose() {
        manager.dispose();
        if (lobbyMusic != null) {
            lobbyMusic.stop();
            lobbyMusic.dispose();
        }
        if (forestMusic != null) {
            forestMusic.stop();
            forestMusic.dispose();
        }
        if (infernoMusic != null) {
            infernoMusic.stop();
            infernoMusic.dispose();
        }
        if (swordSlashSound != null) {
            swordSlashSound.dispose();
        }
        if (monsterHurtSound != null) {
            monsterHurtSound.dispose();
        }
        if (meteorCrashSound != null) {
            meteorCrashSound.dispose();
        }
        if (iceAttackSound != null) {
            iceAttackSound.dispose();
        }
        if (bossSmashSound != null) {
            bossSmashSound.dispose();
        }
        if (yetiHitSound != null) {
            yetiHitSound.dispose();
        }
        if (werewolfScratchSound != null) {
            werewolfScratchSound.dispose();
        }
    }

    public Texture getTexture(String name) { return manager.get(name, Texture.class); }
    public TiledMap getMap() { return manager.get(MAP_TMX, TiledMap.class); }
    public TiledMap getIceMap() { return manager.get(ICE_MAP_TMX, TiledMap.class); }
    public TiledMap getLavaMap() { return manager.get(LAVA_MAP_TMX, TiledMap.class); }
    public Music getLobbyMusic() { return lobbyMusic; }
    public Music getForestMusic() { return forestMusic; }
    public Music getInfernoMusic() { return infernoMusic; }
    public Sound getSwordSlashSound() { return swordSlashSound; }
    public Sound getMonsterHurtSound() { return monsterHurtSound; }
    public Sound getMeteorCrashSound() { return meteorCrashSound; }
    public Sound getIceAttackSound() { return iceAttackSound; }
    public Sound getBossSmashSound() { return bossSmashSound; }
    public Sound getYetiHitSound() { return yetiHitSound; }
    public Sound getWerewolfScratchSound() { return werewolfScratchSound; }
}
