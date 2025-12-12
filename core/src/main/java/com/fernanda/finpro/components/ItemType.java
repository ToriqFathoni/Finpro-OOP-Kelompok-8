package com. fernanda.finpro.components;

public enum ItemType {
    // Raw Materials
    ORC_SKULL("Orc Skull", "💀"),
    ORC_MEAT("Orc Meat", "🥩"),
    WEREWOLF_CLAW("Werewolf Claw", "🦴"),
    YETI_HEART("Yeti Heart", "🦷"),
    RAW_MEAT("Raw Meat", "🥩"),
    SLIME_GEL("Slime Gel", "💧"),
    HERB("Healing Herb", "🌿"),
    POTION("Health Potion", "🧪"),

    // Crafted Foods
    ROASTED_MEAT("Roasted Meat", "🍖"),
    HERBAL_TEA("Herbal Tea", "🍵"),
    SPICY_SKEWER("Spicy Skewer", "🍢"),
    FOREST_SOUP("Forest Soup", "🍲"),
    SLIME_JELLY("Slime Jelly", "🟢"),
    GOURMET_BURGER("Gourmet Burger", "🍔"),

    // Special Crafted Items
    SKULL_ELIXIR("Skull Elixir", "⚗️");

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
