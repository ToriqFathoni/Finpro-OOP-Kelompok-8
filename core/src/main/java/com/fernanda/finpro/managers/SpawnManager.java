package com.fernanda.finpro.managers;

import com.badlogic.gdx.math.MathUtils;
import com.fernanda.finpro.entities.Monster;
import com.fernanda.finpro.factories.MonsterFactory;

import java.util.List;

public class SpawnManager {
    // Referensi ke list monster yang ada di Main
    private List<Monster> monsterList;

    // Config Spawn (Logika Timer & Jumlah)
    private float spawnTimer = 0;
    private float nextSpawnDelay = 0.5f;
    private int maxOrcCount;

    public SpawnManager(List<Monster> monsterList) {
        this.monsterList = monsterList;

        this.maxOrcCount = MathUtils.random(10, 15);
        System.out.println("Spawn Manager Active. Target: " + maxOrcCount + " Orcs.");
    }

    public void update(float dt) {
        // Cek apakah populasi masih kurang dari target
        if (monsterList.size() < maxOrcCount) {
            spawnTimer += dt;

            if (spawnTimer >= nextSpawnDelay) {
                // Panggil Factory untuk spawn orc di hutan
                monsterList.add(MonsterFactory.createForestMonster());

                // Reset timer & acak waktu spawn berikutnya
                spawnTimer = 0;
                nextSpawnDelay = MathUtils.random(2f, 5f);
            }
        }
    }

    // Method untuk reset saat Game Restart
    public void reset() {
        spawnTimer = 0;
        nextSpawnDelay = 0.5f;
        maxOrcCount = MathUtils.random(10, 15);
        System.out.println("Spawn Manager Reset. New Target: " + maxOrcCount);
    }
}
