package com.fernanda.finpro.world;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

public class WorldManager {
    public enum ZoneType {
        FOREST,
        ICE,
        FIRE
    }

    // --- SINKRONISASI RADIUS DENGAN MAIN.JAVA ---
    // Radius ini harus sama persis dengan yang ada di Main.java
    // agar warna background/logika zona cocok dengan gambar di layar.

    private static final float FIRE_RADIUS_LIMIT = 800f;   // 0 - 600: Api
    private static final float ICE_RADIUS_LIMIT = 2000f;   // 600 - 1500: Es
    // Di atas 1500 otomatis dianggap Hutan (sampai batas pulau di 3500)

    public ZoneType getZone(float x, float y) {
        float distanceFromCenter = Vector2.len(x, y);

        if (distanceFromCenter < FIRE_RADIUS_LIMIT) {
            return ZoneType.FIRE;
        } else if (distanceFromCenter < ICE_RADIUS_LIMIT) {
            return ZoneType.ICE;
        } else {
            return ZoneType.FOREST;
        }
    }

    public Color getZoneColor(ZoneType zone) {
        switch (zone) {
            case FIRE:
                return new Color(0.3f, 0.1f, 0.1f, 1f); // Merah Gelap
            case ICE:
                return new Color(0.1f, 0.3f, 0.4f, 1f); // Biru Dingin
            case FOREST:
            default:
                return new Color(0.1f, 0.25f, 0.1f, 1f); // Hijau Gelap
        }
    }
}
