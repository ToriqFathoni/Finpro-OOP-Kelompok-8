package com.fernanda.finpro.factories;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.fernanda.finpro.entities.Monster;
import com.fernanda.finpro.entities.Orc;
import com.fernanda.finpro.singleton.GameAssetManager;

public class MonsterFactory {

    // --- ENUM TIPE MONSTER ---
    // Saat ini hanya ORC. Nanti tinggal tambah koma dan nama baru (misal: ORC, SLIME)
    public enum Type {
        ORC
    }

    // --- CONFIG AREA SPAWN ---
    private static final float MAP_WIDTH = 7040f;
    private static final float MAP_HEIGHT = 7040f;

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
        Vector2 pos = getRandomPositionInMap();
        return createMonster(Type.ORC, pos.x, pos.y);
    }

    // --- HELPER MATH ---
    private static Vector2 getRandomPositionInMap() {
        TiledMap map = GameAssetManager.getInstance().getMap();
        TiledMapTileLayer areaLuarLayer = (TiledMapTileLayer) map.getLayers().get("area_luar");

        float x, y;
        int attempts = 0;
        
        do {
            x = MathUtils.random(100f, MAP_WIDTH - 100f);
            y = MathUtils.random(100f, MAP_HEIGHT - 100f);
            
            int tileX = (int) (x / 32);
            int tileY = (int) (y / 32);
            
            // Check if tile exists in "area_luar"
            if (areaLuarLayer != null) {
                TiledMapTileLayer.Cell cell = areaLuarLayer.getCell(tileX, tileY);
                if (cell != null) {
                    return new Vector2(x, y);
                }
            }
            
            attempts++;
        } while (attempts < 50); // Try 50 times to find a valid spot

        // Fallback if no valid spot found (should be rare if map is mostly area_luar)
        return new Vector2(3500, 3500); 
    }
}
