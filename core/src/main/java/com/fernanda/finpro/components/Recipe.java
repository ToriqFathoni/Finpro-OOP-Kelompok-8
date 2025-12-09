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
}
