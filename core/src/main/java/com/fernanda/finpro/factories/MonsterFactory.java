package com.fernanda.finpro.factories;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.fernanda.finpro.entities.Monster;
import com.fernanda.finpro.entities.Orc;
import com.fernanda.finpro.entities.Werewolf;
import com.fernanda.finpro.singleton.GameAssetManager;

import java.util.ArrayList;
import java.util.List;

public class MonsterFactory {

    public enum Type {
        ORC,
        WEREWOLF
    }

    // --- CONFIG AREA SPAWN ---
    private static final float MAP_WIDTH = 1168f;
    private static final float MAP_HEIGHT = 1168f;

    /**
     * Method utama pembuatan monster.
     * Logic ini scalable: cukup tambah case baru jika ada monster baru.
     */
    public static Monster createMonster(Type type, float x, float y) {
        switch (type) {
            case ORC:
                return new Orc(x, y);

            case WEREWOLF:
                return new Werewolf(x, y);

            default:
                throw new IllegalArgumentException("Tipe Monster belum terdaftar: " + type);
        }
    }

    /**
     * Method khusus untuk spawn Orc di area Hutan.
     * Mengatur posisi acak dalam lingkaran, lalu memanggil createMonster.
     */
    public static Monster createForestMonster() {
        Vector2 pos = getRandomSpawnPoint();
        if (MathUtils.randomBoolean()) {
            return createMonster(Type.ORC, pos.x, pos.y);
        } else {
            return createMonster(Type.WEREWOLF, pos.x, pos.y);
        }
    }

    // --- HELPER MATH ---
    private static Vector2 getRandomSpawnPoint() {
        TiledMap map = GameAssetManager.getInstance().getMap();
        MapLayer layer = map.getLayers().get("spawn_monster");

        List<Vector2> spawnTiles = new ArrayList<>();

        if (layer instanceof TiledMapTileLayer) {
            TiledMapTileLayer spawnLayer = (TiledMapTileLayer) layer;
            for (int x = 0; x < spawnLayer.getWidth(); x++) {
                for (int y = 0; y < spawnLayer.getHeight(); y++) {
                    if (spawnLayer.getCell(x, y) != null) {
                        spawnTiles.add(new Vector2(x * 16, y * 16));
                    }
                }
            }
        }

        if (!spawnTiles.isEmpty()) {
            Vector2 tilePos = spawnTiles.get(MathUtils.random(0, spawnTiles.size() - 1));
            return tilePos.add(MathUtils.random(0, 16), MathUtils.random(0, 16));
        }

        // Fallback if no spawn points found
        return new Vector2(500, 500);
    }
}
