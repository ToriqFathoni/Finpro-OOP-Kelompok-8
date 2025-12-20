package com.fernanda.finpro.components;

public class BuffManager {
    // Temporary Buffs
    private float damageBoostTimer = 0f;
    private int damageBoostAmount = 0;
    
    private float energyRegenBoostTimer = 0f;
    private float energyRegenBoostAmount = 0f;
    
    public void update(float dt) {
        // Update damage boost timer
        if (damageBoostTimer > 0) {
            damageBoostTimer -= dt;
            if (damageBoostTimer <= 0) {
                damageBoostTimer = 0;
                damageBoostAmount = 0;
                System.out.println("[BUFF] Damage Boost expired!");
            }
        }
        
        // Update energy regen boost timer
        if (energyRegenBoostTimer > 0) {
            energyRegenBoostTimer -= dt;
            if (energyRegenBoostTimer <= 0) {
                energyRegenBoostTimer = 0;
                energyRegenBoostAmount = 0f;
                System.out.println("[BUFF] Energy Regen Boost expired!");
            }
        }
    }
    
    // Damage Boost
    public void applyDamageBoost(int amount, float duration) {
        this.damageBoostAmount = amount;
        this.damageBoostTimer = duration;
        System.out.println("[BUFF] Damage +" + amount + " for " + (int)duration + " seconds!");
    }
    
    public int getDamageBoost() {
        return damageBoostTimer > 0 ? damageBoostAmount : 0;
    }
    
    public float getDamageBoostTimeRemaining() {
        return damageBoostTimer;
    }
    
    // Energy Regen Boost
    public void applyEnergyRegenBoost(float amount, float duration) {
        this.energyRegenBoostAmount = amount;
        this.energyRegenBoostTimer = duration;
        System.out.println("[BUFF] Energy Regen +" + (int)amount + "/s for " + (int)duration + " seconds!");
    }
    
    public float getEnergyRegenBoost() {
        return energyRegenBoostTimer > 0 ? energyRegenBoostAmount : 0f;
    }
    
    public float getEnergyRegenBoostTimeRemaining() {
        return energyRegenBoostTimer;
    }
    
    // Check if any buff is active
    public boolean hasActiveBuff() {
        return damageBoostTimer > 0 || energyRegenBoostTimer > 0;
    }
}
