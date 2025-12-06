package com.fernanda.finpro.components;

public interface StatsListener {
    // Method ini akan dipanggil otomatis saat HP berubah
    void onHealthChanged(float currentHp, float maxHp);

    // Method ini dipanggil saat Stamina berubah
    void onStaminaChanged(float currentStamina, float maxStamina);

    // Method ini dipanggil saat player mati
    void onDead();
}
