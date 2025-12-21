package com.fernanda.finpro.managers;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.fernanda.finpro.entities.Monster;
import com.fernanda.finpro.entities.Player;

import java.util.List;

public class CollisionManager {
    private Player player;
    private List<Monster> monsters;

    public CollisionManager(Player player, List<Monster> monsters) {
        this.player = player;
        this.monsters = monsters;
    }

    public void update(float dt) {
        Rectangle playerBody = player.getHitbox();

        for (Monster m : monsters) {
            if (m.isDead()) continue;

            // PLAYER MENYERANG MONSTER
            if (player.isHitboxActive()) {
                if (player.getAttackHitbox().overlaps(m.getBodyHitbox())) {
                    m.takeDamage(player.getDamage());

                    float distance = m.getKnockbackDistance();

                    Vector2 knockback = new Vector2(m.position).sub(player.position).nor().scl(distance);
                    
                    // Validasi knockback tidak tembus building
                    Vector2 newPos = new Vector2(m.position).add(knockback);
                    float centerX = newPos.x + (m.getBodyHitbox().width / 2);
                    float centerY = newPos.y + (m.getBodyHitbox().height / 2);
                    
                    // Cek apakah posisi baru tidak blocked
                    if (!m.isTileBlocked(centerX, centerY)) {
                        m.position.add(knockback);
                    }
                }
            }

            // MONSTER MENYERANG PLAYER
            Rectangle mAtkRect = m.getAttackHitbox();
            if (mAtkRect.width > 0) {
                if (mAtkRect.overlaps(playerBody)) {
                    player.takeDamage(m.getDamage());
                }
            }

            // TABRAKAN BADAN (Push back tanpa damage)
            if (playerBody.overlaps(m.getBodyHitbox())) {
                if (!player.isDodging()) {
                    Vector2 pushDirection = new Vector2(player.position).sub(m.position).nor();
                    float pushForce = 150f * dt;
                    player.position.mulAdd(pushDirection, pushForce);
                }
            }
        }
    }
}
