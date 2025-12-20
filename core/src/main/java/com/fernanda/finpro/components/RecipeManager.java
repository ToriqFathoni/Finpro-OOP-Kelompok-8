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
        allRecipes.clear(); // PURGE old dummy recipes
        
        // === FINAL 7 HARDCORE RECIPES ===
        
        // 1. Roasted Meat (Hardcore: 2 Meat + 1 Skull)
        Recipe roastedMeat = new Recipe("Roasted Meat", ItemType.ROASTED_MEAT);
        roastedMeat.addRequirement(ItemType.ORC_MEAT, 2);
        roastedMeat.addRequirement(ItemType.ORC_SKULL, 1);
        allRecipes.add(roastedMeat);

        // 2. Hunter's Stew (1 Meat + 1 Claw)
        Recipe huntersStew = new Recipe("Hunter's Stew", ItemType.HUNTERS_STEW);
        huntersStew.addRequirement(ItemType.ORC_MEAT, 1);
        huntersStew.addRequirement(ItemType.WEREWOLF_CLAW, 1);
        allRecipes.add(huntersStew);

        // 3. Bone Broth (2 Skulls)
        Recipe boneBroth = new Recipe("Bone Broth", ItemType.BONE_BROTH);
        boneBroth.addRequirement(ItemType.ORC_SKULL, 2);
        allRecipes.add(boneBroth);

        // 4. Yeti Soup (1 Heart + 1 Meat + 1 Claw)
        Recipe yetiSoup = new Recipe("Yeti Soup", ItemType.YETI_SOUP);
        yetiSoup.addRequirement(ItemType.YETI_HEART, 1);
        yetiSoup.addRequirement(ItemType.ORC_MEAT, 1);
        yetiSoup.addRequirement(ItemType.WEREWOLF_CLAW, 1);
        allRecipes.add(yetiSoup);

        // 5. Berserker's Elixir (5 Claws + 5 Skulls) - LEGENDARY
        Recipe berserkerElixir = new Recipe("Berserker's Elixir", ItemType.BERSERKERS_ELIXIR);
        berserkerElixir.addRequirement(ItemType.WEREWOLF_CLAW, 5);
        berserkerElixir.addRequirement(ItemType.ORC_SKULL, 5);
        allRecipes.add(berserkerElixir);

        // 6. Heart of Mountain (3 Hearts + 3 Meat) - LEGENDARY
        Recipe heartOfMountain = new Recipe("Heart of Mountain", ItemType.HEART_OF_MOUNTAIN);
        heartOfMountain.addRequirement(ItemType.YETI_HEART, 3);
        heartOfMountain.addRequirement(ItemType.ORC_MEAT, 3);
        allRecipes.add(heartOfMountain);

        // 7. GOD SLAYER (1 Shard + 5 Hearts + 5 Claws) - ULTIMATE
        Recipe godSlayer = new Recipe("GOD SLAYER ELIXIR", ItemType.GOD_SLAYER_ELIXIR);
        godSlayer.addRequirement(ItemType.ETERNAL_ICE_SHARD, 1);
        godSlayer.addRequirement(ItemType.YETI_HEART, 5);
        godSlayer.addRequirement(ItemType.WEREWOLF_CLAW, 5);
        allRecipes.add(godSlayer);
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
