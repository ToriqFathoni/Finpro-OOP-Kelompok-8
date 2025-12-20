package com.fernanda.finpro.components;

public enum ItemType {
    // --- INGREDIENTS (Not Consumable) ---
    ORC_MEAT("OrcMeat.png", "Orc Meat", "Raw meat. Needs cooking.", false, false),
    ORC_SKULL("orcskull.png", "Orc Skull", "Bone material for energy brews.", false, false),
    WEREWOLF_CLAW("WerewolfClaw.png", "Werewolf Claw", "Sharp claw from the forest.", false, false),
    YETI_HEART("YetiHeart.png", "Yeti Heart", "Cold heart from the snow peaks.", false, false),
    ETERNAL_ICE_SHARD("legend-3.png", "Eternal Ice Shard", "Rare drop from the Frost Boss.", false, false),

    // --- SURVIVAL MEALS (Consumable) ---
    ROASTED_MEAT("roasted-meat.png", "Roasted Meat", "Effect: Heals 50% Max HP.", true, false),
    HUNTERS_STEW("hunter-stew.png", "Hunter's Stew", "Effect: Damage +5 (60s).", true, false),
    BONE_BROTH("bone-broth.png", "Bone Broth", "Effect: Energy Regen +20/s (10s).", true, false),
    YETI_SOUP("yeti-soup.png", "Yeti Soup", "Effect: Heals 75% Max HP.", true, false),

    // --- LEGENDARY FEASTS (Consumable, Legendary) ---
    BERSERKERS_ELIXIR("legend-1.png", "Berserker's Elixir", "PERMANENT: Dmg+3, HP=100, Energy=120.", true, true),
    HEART_OF_MOUNTAIN("legend-2.png", "Heart of Mountain", "PERMANENT: Dmg+3, HP=200, Energy=150.", true, true),
    GOD_SLAYER_ELIXIR("legend-3.png", "God Slayer Elixir", "ULTIMATE: Dmg+5, HP=300, Energy=170.", true, true);

    // Fields
    public final String texturePath;
    public final String displayName;
    public final String description;
    public final boolean isConsumable;
    public final boolean isLegendary;

    ItemType(String path, String name, String desc, boolean consumable, boolean legendary) {
        this.texturePath = path;
        this.displayName = name;
        this.description = desc;
        this.isConsumable = consumable;
        this.isLegendary = legendary;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getTexturePath() {
        return texturePath;
    }

    public String getDescription() {
        return description;
    }

    // Legacy method for backward compatibility
    public String getIcon() {
        // Return emoji based on type for console output
        if (isLegendary) return "⚗️";
        if (isConsumable) return "🍖";
        return "💀";
    }
}
