package com.fernanda.finpro.managers;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.fernanda.finpro.entities.Monster;
import com.fernanda.finpro.entities.Orc;
import com.fernanda.finpro.entities.Werewolf;
import com.fernanda.finpro.factories.MonsterFactory;

import java.util.ArrayList;
import java.util.List;

public class SpawnManager {
    private List<Monster> monsterList;
    private List<SpawnRule> spawnRules;

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

        // Aturan Spawn: Max 10 Orc, Interval 15 detik
        spawnRules.add(new SpawnRule(MonsterFactory.Type.ORC, Orc.class, 10, 15.0f));

        // Aturan Spawn: Max 5 Werewolf, Interval 15 detik (Total 15 Monster)
        spawnRules.add(new SpawnRule(MonsterFactory.Type.WEREWOLF, Werewolf.class, 5, 15.0f));

        System.out.println("Spawn Manager Initialized with " + spawnRules.size() + " rules.");
        
        // Spawn awal langsung (agar tidak menunggu player)
        spawnInitialMonsters();
    }

    private void spawnInitialMonsters() {
        for (SpawnRule rule : spawnRules) {
            for (int i = 0; i < rule.maxCount; i++) {
                spawnMonster(rule.type);
            }
        }
    }

    public void update(float dt) {
        for (SpawnRule rule : spawnRules) {

            //  Cek Populasi Spesifik
            int currentCount = countMonsters(rule.classType);

            // Jika masih ada slot kosong
            if (currentCount < rule.maxCount) {

                // Jalankan Timer
                rule.timer += dt;

                if (rule.timer >= rule.spawnInterval) {
                    // SPAWN!
                    spawnMonster(rule.type);
                    rule.timer = 0;
                }
            }
        }
    }

    private void spawnMonster(MonsterFactory.Type type) {
        Vector2 pos = MonsterFactory.getRandomSpawnPoint();

        Monster m = MonsterFactory.createMonster(type, pos.x, pos.y);
        monsterList.add(m);

        System.out.println("Spawned: " + type);
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
        // Spawn ulang saat reset game
        spawnInitialMonsters();
        System.out.println("Spawn Manager Reset.");
    }
}
