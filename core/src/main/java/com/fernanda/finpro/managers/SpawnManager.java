package com.fernanda.finpro.managers;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.fernanda.finpro.entities.Boss; // <-- IMPORT DITAMBAHKAN
import com.fernanda.finpro.entities.MiniBoss;
import com.fernanda.finpro.entities.Monster;
import com.fernanda.finpro.entities.Orc;
import com.fernanda.finpro.entities.Werewolf;
import com.fernanda.finpro.entities.Yeti;
import com.fernanda.finpro.factories.MonsterFactory;
import com.fernanda.finpro.enums.WorldType;
import com.fernanda.finpro.singleton.GameAssetManager;

import java.util.ArrayList;
import java.util.List;

public class SpawnManager {
    private List<Monster> monsterList;
    private List<SpawnRule> spawnRules;
    private Boss boss; 
    private WorldType currentWorld = WorldType.FOREST;

    private static class SpawnRule {
        MonsterFactory.Type type;
        Class<? extends Monster> classType;
        int maxCount;
        float spawnInterval;
        float timer;

        public SpawnRule(MonsterFactory.Type type, Class<? extends Monster> classType, int maxCount, float interval) {
            this.type = type;
            this.classType = classType;
            this.maxCount = maxCount;
            this.spawnInterval = interval;
            this.timer = 0;
        }
    }

    public SpawnManager(List<Monster> monsterList) {
        this.monsterList = monsterList;
        this.spawnRules = new ArrayList<>();
        this.boss = null; 

        // Aturan Spawn: Max 10 Orc, Interval 15 detik
        spawnRules.add(new SpawnRule(MonsterFactory.Type.ORC, Orc.class, 10, 15.0f));
        // Aturan Spawn: Max 5 Werewolf, Interval 15 detik
        spawnRules.add(new SpawnRule(MonsterFactory.Type.WEREWOLF, Werewolf.class, 5, 15.0f));
        // Aturan Spawn: Max 15 Yeti, Interval 15 detik
        spawnRules.add(new SpawnRule(MonsterFactory.Type.YETI, Yeti.class, 15, 15.0f));
        // Aturan Spawn: Max 1 MiniBoss, Interval 30 detik (hanya 1 di map)
        spawnRules.add(new SpawnRule(MonsterFactory.Type.MINI_BOSS, MiniBoss.class, 1, 30.0f));

        System.out.println("Spawn Manager Initialized with " + spawnRules.size() + " rules.");

        spawnInitialMonsters();
    }

    private void spawnInitialMonsters() {
        // Jangan spawn monster jika di world INFERNO
        if (currentWorld == WorldType.INFERNO) return;

        for (SpawnRule rule : spawnRules) {
            // Hanya spawn monster yang sesuai dengan dunianya
            if ((rule.type == MonsterFactory.Type.YETI && currentWorld == WorldType.ICE) ||
                (rule.type == MonsterFactory.Type.MINI_BOSS && currentWorld == WorldType.ICE) ||
                ((rule.type == MonsterFactory.Type.ORC || rule.type == MonsterFactory.Type.WEREWOLF) && currentWorld == WorldType.FOREST)) {
                for (int i = 0; i < rule.maxCount; i++) {
                    spawnMonster(rule.type);
                }
            }
        }
    }

    public void update(float dt) {
        // Jika ada boss, jangan spawn monster biasa
        if (boss != null) {
            return;
        }

        for (SpawnRule rule : spawnRules) {
            int currentCount = countMonsters(rule.classType);
            if (currentCount < rule.maxCount) {
                rule.timer += dt;
                if (rule.timer >= rule.spawnInterval) {
                    spawnMonster(rule.type);
                    rule.timer = 0;
                }
            }
        }
    }

    public void setWorld(WorldType world) {
        this.currentWorld = world;
    }

    private void spawnMonster(MonsterFactory.Type type) {
        Vector2 pos;
        
        if (type == MonsterFactory.Type.MINI_BOSS) {
            if (currentWorld != WorldType.ICE) return;
            // Spawn di area kanan atas (800-1100)
            float x = MathUtils.random(800f, 1100f);
            float y = MathUtils.random(800f, 1100f);
            pos = new Vector2(x, y);
        } else if (type == MonsterFactory.Type.YETI) {
            if (currentWorld != WorldType.ICE) return; 
            pos = MonsterFactory.getRandomSpawnPoint(
                GameAssetManager.getInstance().getIceMap(), 
                new String[]{"ice_monster_spawn_1", "ice_monster_spawn_2", "ice_monster_spawn_3"}
            );
        } else { // Orc & Werewolf
            if (currentWorld != WorldType.FOREST) return;
            pos = MonsterFactory.getRandomSpawnPoint(
                GameAssetManager.getInstance().getMap(), 
                new String[]{"spawn_monster_1", "spawn_monster_2", "spawn_monster_3"}
            );
        }

        if (pos != null) {
            Monster m = MonsterFactory.createMonster(type, pos.x, pos.y);
            monsterList.add(m);
            System.out.println("Spawned: " + type);
        }
    }

    public void spawnBoss() {
        if (this.boss == null) {
            Vector2 pos = null;
            
            if (currentWorld == WorldType.INFERNO) {
                 pos = MonsterFactory.getRandomSpawnPoint(
                    GameAssetManager.getInstance().getLavaMap(), 
                    new String[]{"boss_infernospawn"}
                );
            }
            
            if (pos != null) {
                this.boss = new Boss(pos.x, pos.y);
                System.out.println("BOSS HAS SPAWNED at " + pos.x + ", " + pos.y);
            } else {
                System.err.println("FAILED TO SPAWN BOSS: Spawn point not found!");
            }
        }
    }

    public void despawnBoss() {
        if (this.boss != null) {
            this.boss = null;
            System.out.println("Boss has been despawned.");
        }
    }

    public Boss getBoss() {
        return this.boss;
    }

    private int countMonsters(Class<?> type) {
        int count = 0;
        for (Monster m : monsterList) {
            if (type.isInstance(m)) {
                count++;
            }
        }
        return count;
    }

    public void reset() {
        for (SpawnRule rule : spawnRules) {
            rule.timer = 0;
        }
        despawnBoss();
        spawnInitialMonsters();
        System.out.println("Spawn Manager Reset.");
    }
}