package com.fernanda.finpro.factories;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.fernanda.finpro.entities.Monster;
import com.fernanda.finpro.entities.Orc;

public class MonsterFactory {

    // --- ENUM TIPE MONSTER ---
    // Saat ini hanya ORC. Nanti tinggal tambah koma dan nama baru (misal: ORC, SLIME)
    public enum Type {
        ORC
    }

    // --- CONFIG AREA SPAWN ---
    private static final float FOREST_RADIUS = 3000f;
    private static final float MIN_SPAWN_DIST = 400f;

    /**
     * Method utama pembuatan monster.
     * Logic ini scalable: cukup tambah case baru jika ada monster baru.
     */
    public static Monster createMonster(Type type, float x, float y) {
        switch (type) {
            case ORC:
                return new Orc(x, y);

            default:
                throw new IllegalArgumentException("Tipe Monster belum terdaftar: " + type);
        }
    }

    /**
     * Method khusus untuk spawn Orc di area Hutan.
     * Mengatur posisi acak dalam lingkaran, lalu memanggil createMonster.
     */
    public static Monster createOrcInForest() {
        Vector2 pos = getRandomPositionInCircle(MIN_SPAWN_DIST, FOREST_RADIUS);
        return createMonster(Type.ORC, pos.x, pos.y);
    }

    // --- HELPER MATH ---
    private static Vector2 getRandomPositionInCircle(float minRadius, float maxRadius) {
        float angle = MathUtils.random(0f, 360f);
        float distance = MathUtils.random(minRadius, maxRadius);

        float x = distance * MathUtils.cosDeg(angle);
        float y = distance * MathUtils.sinDeg(angle);

        return new Vector2(x, y);
    }
}
