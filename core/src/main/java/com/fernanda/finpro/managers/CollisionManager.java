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
                    m.takeDamage(10);

                    // Efek Knockback ke Monster
                    Vector2 knockback = new Vector2(m.position).sub(player.position).nor().scl(10);
                    m.position.add(knockback);
                }
            }

            // MONSTER MENYERANG PLAYER
            Rectangle mAtkRect = m.getAttackHitbox();
            if (mAtkRect.width > 0) {
                if (mAtkRect.overlaps(playerBody)) {
                    player.takeDamage(m.getDamage());
                }
            }

            // TABRAKAN BADAN
            if (playerBody.overlaps(m.getBodyHitbox())) {
                player.takeDamage(5);

                if (!player.isDodging()) {
                    Vector2 pushDirection = new Vector2(player.position).sub(m.position).nor();
                    float pushForce = 150f * dt;
                    player.position.mulAdd(pushDirection, pushForce);
                }
            }
        }
    }
}
