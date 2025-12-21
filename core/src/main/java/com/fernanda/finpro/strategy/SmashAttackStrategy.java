package com.fernanda.finpro.strategy;

import com.fernanda.finpro.entities.Boss;
import com.fernanda.finpro.entities.Player;

/**
 * Strategy Pattern - Smash Attack Implementation
 * Boss smashes hands down dealing massive damage in close range
 */
public class SmashAttackStrategy implements AttackStrategy {
    
    public enum Phase { WINDUP, HOLD, RECOVER }
    
    private Phase currentPhase;
    private float phaseTimer;
    private boolean hasDealtDamage;
    private boolean finished;
    
    private static final float WINDUP_DURATION = 0.6f;  // Attack animation
    private static final float HOLD_DURATION = 5.0f;     // Hands on ground
    private static final float RECOVER_DURATION = 0.6f;  // Retrieve animation
    private static final float IMPACT_DURATION = 0.25f;  // Damage window
    
    public SmashAttackStrategy() {
        reset();
    }
    
    @Override
    public void execute(Boss boss, Player player, float dt) {
        phaseTimer += dt;
        
        switch (currentPhase) {
            case WINDUP:
                if (phaseTimer >= WINDUP_DURATION) {
                    currentPhase = Phase.HOLD;
                    phaseTimer = 0;
                    hasDealtDamage = false;
                }
                break;
                
            case HOLD:
                // Deal damage only during impact window at the START of HOLD phase
                if (!hasDealtDamage && phaseTimer >= 0f && phaseTimer <= IMPACT_DURATION) {
                    boss.checkSmashCollision(player);
                    hasDealtDamage = true;
                }
                
                if (phaseTimer >= HOLD_DURATION) {
                    currentPhase = Phase.RECOVER;
                    phaseTimer = 0;
                }
                break;
                
            case RECOVER:
                if (phaseTimer >= RECOVER_DURATION) {
                    finished = true;
                }
                break;
        }
    }
    
    @Override
    public boolean isFinished() {
        return finished;
    }
    
    @Override
    public void reset() {
        this.currentPhase = Phase.WINDUP;
        this.phaseTimer = 0;
        this.hasDealtDamage = false;
        this.finished = false;
    }
    
    @Override
    public String getName() {
        return "Smash Attack";
    }
    
    public Phase getCurrentPhase() {
        return currentPhase;
    }
    
    public float getPhaseTimer() {
        return phaseTimer;
    }
    
    public boolean canBeHit() {
        // Boss can be hit when hands are on ground (HOLD phase) after impact
        return currentPhase == Phase.HOLD && phaseTimer > IMPACT_DURATION;
    }
}
