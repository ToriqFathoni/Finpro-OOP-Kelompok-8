package com.fernanda.finpro.strategy;

import com.fernanda.finpro.entities.Boss;
import com.fernanda.finpro.entities.MeteorController;
import com.fernanda.finpro.entities.Player;


public class MeteorAttackStrategy implements AttackStrategy {
    
    private float castingTimer;
    private boolean isCasting;
    private boolean finished;
    private MeteorController meteorController;
    
    private static final float CASTING_DURATION = 0.9f; // 6 frames * 0.15f
    
    public MeteorAttackStrategy(MeteorController meteorController) {
        this.meteorController = meteorController;
        reset();
    }
    
    @Override
    public void execute(Boss boss, Player player, float dt) {
        if (isCasting) {
            castingTimer += dt;
            
            if (castingTimer >= CASTING_DURATION) {
                meteorController.startRain();
                isCasting = false;
                finished = true;
                System.out.println("Boss Casting Selesai -> Hujan Meteor Dimulai");
            }
        }
    }
    
    @Override
    public boolean isFinished() {
        return finished;
    }
    
    @Override
    public void reset() {
        this.castingTimer = 0;
        this.isCasting = true;
        this.finished = false;
    }
    
    @Override
    public String getName() {
        return "Meteor Rain";
    }
    
    public boolean isCasting() {
        return isCasting;
    }
    
    public float getCastingTimer() {
        return castingTimer;
    }
}
