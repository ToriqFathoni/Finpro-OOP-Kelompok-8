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

    public void update(float dt) {
        if (isDead) return;

        // Regen HP
        if (currentHealth < maxHealth) {
            currentHealth += healthRegenRate * dt;
            if (currentHealth > maxHealth) currentHealth = maxHealth;
            notifyHealthChanged();
        }

        // Regen Stamina
        if (currentStamina < maxStamina) {
            currentStamina += staminaRegenRate * dt;
            if (currentStamina > maxStamina) currentStamina = maxStamina;
            notifyStaminaChanged();
        }
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
