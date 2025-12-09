package com.fernanda.finpro.components;

import java.util.ArrayList;
import java.util.List;

public class RecipeManager {
    private static RecipeManager instance;
    private List<Recipe> allRecipes;

    private RecipeManager() {
        allRecipes = new ArrayList<>();
        initializeRecipes();
    }

    public static RecipeManager getInstance() {
        if (instance == null) {
            instance = new RecipeManager();
        }
        return instance;
    }

    private void initializeRecipes() {
        // 1. "Roasted Meat" - Basic Survival Food
        Recipe roastedMeat = new Recipe("Roasted Meat", ItemType.ROASTED_MEAT);
        roastedMeat.addRequirement(ItemType.RAW_MEAT, 2);
        allRecipes.add(roastedMeat);

        // 2. "Herbal Tea" - Stamina Boost
        Recipe herbalTea = new Recipe("Herbal Tea", ItemType.HERBAL_TEA);
        herbalTea.addRequirement(ItemType.HERB, 3);
        allRecipes.add(herbalTea);

        // 3. "Spicy Skewer" - Attack Buff (Requires 2 Monster Types!)
        Recipe spicySkewer = new Recipe("Spicy Skewer", ItemType.SPICY_SKEWER);
        spicySkewer.addRequirement(ItemType.RAW_MEAT, 2);
        spicySkewer.addRequirement(ItemType.SLIME_GEL, 1);
        allRecipes.add(spicySkewer);

        // 4. "Forest Soup" - Regeneration (Complete Balanced Meal)
        Recipe forestSoup = new Recipe("Forest Soup", ItemType.FOREST_SOUP);
        forestSoup.addRequirement(ItemType.RAW_MEAT, 2);
        forestSoup.addRequirement(ItemType.HERB, 2);
        allRecipes.add(forestSoup);

        // 5. "Slime Jelly" - Defense
        Recipe slimeJelly = new Recipe("Slime Jelly", ItemType.SLIME_JELLY);
        slimeJelly.addRequirement(ItemType.SLIME_GEL, 4);
        allRecipes.add(slimeJelly);

        // 6. "GOURMET BURGER" - The Boss Key! (Ultimate Endgame Food)
        Recipe gourmetBurger = new Recipe("GOURMET BURGER", ItemType.GOURMET_BURGER);
        gourmetBurger.addRequirement(ItemType.RAW_MEAT, 5);
        gourmetBurger.addRequirement(ItemType.HERB, 3);
        gourmetBurger.addRequirement(ItemType.SLIME_GEL, 2);
        allRecipes.add(gourmetBurger);

        // 7. "Skull Elixir" - TEST RECIPE (Unique Orc-only craft)
        Recipe skullElixir = new Recipe("Skull Elixir", ItemType.SKULL_ELIXIR);
        skullElixir.addRequirement(ItemType.ORC_SKULL, 2);
        allRecipes.add(skullElixir);
    }

    public List<Recipe> getAllRecipes() {
        return allRecipes;
    }

    public Recipe findRecipeByResult(ItemType resultItem) {
        for (Recipe recipe : allRecipes) {
            if (recipe.getResultItem() == resultItem) {
                return recipe;
            }
        }
        return null;
    }
}
