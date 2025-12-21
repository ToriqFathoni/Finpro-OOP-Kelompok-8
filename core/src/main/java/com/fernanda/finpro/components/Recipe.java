package com.fernanda.finpro.components;

import java.util.HashMap;
import java.util.Map;

public class Recipe {
    private String recipeName;
    private Map<ItemType, Integer> requirements;
    private ItemType resultItem;

    public Recipe(String recipeName, ItemType resultItem) {
        this.recipeName = recipeName;
        this.resultItem = resultItem;
        this.requirements = new HashMap<>();
    }

    public void addRequirement(ItemType type, int amount) {
        requirements.put(type, amount);
    }

    public String getRecipeName() {
        return recipeName;
    }

    public Map<ItemType, Integer> getRequirements() {
        return requirements;
    }

    public ItemType getResultItem() {
        return resultItem;
    }

    public String getRequirementsString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<ItemType, Integer> entry : requirements.entrySet()) {
            sb.append(entry.getValue()).append("x ").append(entry.getKey().getDisplayName()).append(", ");
        }
        if (sb.length() > 2) {
            sb.setLength(sb.length() - 2);
        }
        return sb.toString();
    }
    
    /**
     * Check if this recipe is a legendary item (permanent upgrade)
     */
    public boolean isLegendary() {
        return resultItem == ItemType.BERSERKERS_ELIXIR ||
               resultItem == ItemType.HEART_OF_MOUNTAIN ||
               resultItem == ItemType.GOD_SLAYER_ELIXIR;
    }
    
    /**
     * Get buff/effect description for this recipe
     */
    public String getEffectDescription() {
        switch (resultItem) {
            case ROASTED_MEAT:
                return "Heals 40 HP instantly.\nBasic recovery food.";
            case HUNTERS_STEW:
                return "Heals 10 HP + Damage +5\nfor 60 seconds.\nGood for combat boost.";
            case BONE_BROTH:
                return "Heals 5 HP + Energy Regen\n+20/s for 10 seconds.\nStamina recovery.";
            case YETI_SOUP:
                return "Heals 75% of Max HP.\nPowerful healing.";
            case BERSERKERS_ELIXIR:
                return "[LEGENDARY]\nPERMANENT UPGRADE:\n+3 Damage\nMax HP = 100\nMax Energy = 120\n(One-time use)";
            case HEART_OF_MOUNTAIN:
                return "[LEGENDARY]\nPERMANENT UPGRADE:\n+3 Damage\nMax HP = 200\nMax Energy = 150\n(One-time use)";
            case GOD_SLAYER_ELIXIR:
                return "[LEGENDARY]\nPERMANENT UPGRADE:\n+5 Damage\nMax HP = 300\nMax Energy = 170\n(One-time use)";
            default:
                return "Raw ingredient.\nCannot be consumed.";
        }
    }
}
