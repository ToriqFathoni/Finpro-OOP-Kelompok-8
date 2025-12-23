package com.fernanda.finpro.strategy;

import com.fernanda.finpro.entities.Boss;
import com.fernanda.finpro.entities.Player;

public interface AttackStrategy {
    void execute(Boss boss, Player player, float dt);

    boolean isFinished();

    void reset();

    String getName();
}
