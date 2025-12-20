package com.fernanda.finpro.components;

import java.util.ArrayList;
import java.util.List;

public class PlayerStats {
    // Config
    public float maxHealth;
    public float maxStamina;
    public float healthRegenRate;
    public float staminaRegenRate;

    // State
    private float currentHealth;
    private float currentStamina;
    public boolean isDead = false;

    // Observer Pattern
    private List<StatsListener> listeners = new ArrayList<>();

    public PlayerStats(float maxHealth, float maxStamina, float hpRegen, float stRegen) {
        this.maxHealth = maxHealth;
        this.maxStamina = maxStamina;
        this.healthRegenRate = hpRegen;
        this.staminaRegenRate = stRegen;

        this.currentHealth = maxHealth;
        this.currentStamina = maxStamina;
    }

    public void addListener(StatsListener listener) {
        listeners.add(listener);
    }

    public void reset() {
        this.currentHealth = maxHealth;
        this.currentStamina = maxStamina;
        this.isDead = false;
        notifyHealthChanged();
        notifyStaminaChanged();
    }

    public void update(float dt, float energyRegenBoost) {
        if (isDead) return;

        // NO HP AUTO-REGEN (Removed!)
        // Health regeneration is disabled per game design

        // Regen Stamina (Energy) with buff support
        if (currentStamina < maxStamina) {
            float totalRegen = staminaRegenRate + energyRegenBoost;
            currentStamina += totalRegen * dt;
            if (currentStamina > maxStamina) currentStamina = maxStamina;
            notifyStaminaChanged();
        }
    }
    
    // Method to upgrade max stats permanently (for legendary elixirs)
    public void upgradeMaxHealth(float newMaxHealth) {
        this.maxHealth = newMaxHealth;
        this.currentHealth = newMaxHealth; // Also restore to full
        System.out.println("[PERMANENT UPGRADE] Max HP set to " + (int)newMaxHealth);
        notifyHealthChanged();
    }
    
    public void upgradeMaxStamina(float newMaxStamina) {
        this.maxStamina = newMaxStamina;
        this.currentStamina = newMaxStamina; // Also restore to full
        System.out.println("[PERMANENT UPGRADE] Max Energy set to " + (int)newMaxStamina);
        notifyStaminaChanged();
    }

    public void takeDamage(float amount) {
        currentHealth -= amount;
        if (currentHealth <= 0) {
            currentHealth = 0;
            isDead = true;
            notifyDead();
            System.out.println("STATUS: Player Mati!");
        } else {
            System.out.println("STATUS: Kena Hit! HP: " + (int)currentHealth);
        }
        notifyHealthChanged();
    }
    
    public void heal(float amount) {
        if (isDead) return;
        currentHealth += amount;
        if (currentHealth > maxHealth) {
            currentHealth = maxHealth;
        }
        System.out.println("[HEAL] +" + (int)amount + " HP! Current: " + (int)currentHealth + "/" + (int)maxHealth);
        notifyHealthChanged();
    }
    
    public void restoreStamina(float amount) {
        if (isDead) return;
        currentStamina += amount;
        if (currentStamina > maxStamina) {
            currentStamina = maxStamina;
        }
        System.out.println("[STAMINA] +" + (int)amount + " Stamina! Current: " + (int)currentStamina + "/" + (int)maxStamina);
        notifyStaminaChanged();
    }

    public boolean useStamina(float amount) {
        if (currentStamina >= amount) {
            currentStamina -= amount;
            notifyStaminaChanged();
            return true;
        }
        return false;
    }

    // Notify Helpers
    private void notifyHealthChanged() {
        for (StatsListener listener : listeners) listener.onHealthChanged(currentHealth, maxHealth);
    }
    private void notifyStaminaChanged() {
        for (StatsListener listener : listeners) listener.onStaminaChanged(currentStamina, maxStamina);
    }
    private void notifyDead() {
        for (StatsListener listener : listeners) listener.onDead();
    }

    // Getters
    public float getCurrentHealth() { return currentHealth; }
    public float getCurrentStamina() { return currentStamina; }
}
