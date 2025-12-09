package com. fernanda.finpro.components;

public enum ItemType {
    ORC_SKULL("Orc Skull", "💀"),      // NEW: Item dari Orc
    MEAT("Boar Meat", "🥩"),
    SLIME_GEL("Slime Gel", "💧"),
    HERB("Healing Herb", "🌿"),
    POTION("Health Potion", "🧪");

    private final String displayName;
    private final String icon;

    ItemType(String displayName, String icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }
}
