package com.fernanda.finpro.strategy;

import com.fernanda.finpro.entities.Boss;
import com.fernanda.finpro.entities.Player;

/**
 * Strategy Pattern - Attack Strategy Interface
 * Allows Boss to switch between different attack behaviors dynamically
 */
public interface AttackStrategy {
    /**
     * Execute the attack
     * @param boss The boss performing the attack
     * @param player The target player
     * @param dt Delta time
     */
    void execute(Boss boss, Player player, float dt);

    /**
     * Check if the attack is finished
     */
    boolean isFinished();

    /**
     * Reset the strategy for reuse
     */
    void reset();

    /**
     * Get the name of this attack (for debugging)
     */
    String getName();
}
