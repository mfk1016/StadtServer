package me.mfk1016.stadtserver.brewing.recipe;

import io.papermc.paper.potion.PotionMix;
import me.mfk1016.stadtserver.brewing.BottleType;
import me.mfk1016.stadtserver.brewing.PotionManager;
import org.bukkit.Material;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

import java.util.*;

public class MultiPotionRecipe extends AdvancedPotionRecipe {

    protected final Map<String, String> recipeMap = new HashMap<>();

    public MultiPotionRecipe(String recipeKey, Material ingredient) {
        super(recipeKey, new RecipeChoice.MaterialChoice(ingredient));
    }

    @Override
    protected boolean isInputMatched(ItemStack stack) {
        for (var inputID : recipeMap.keySet())
            if (stack.isSimilar(PotionManager.brewPotion(inputID, BottleType.ofStack(stack))))
                return true;
        return false;
    }

    @Override
    public Collection<PotionMix> buildBaseRecipes() {
        List<PotionMix> results = new ArrayList<>();
        for (int i = 0; i < BottleType.values().length; i++) {
            int j = 0;
            for (var entry : recipeMap.entrySet()) {
                BottleType bottleType = BottleType.values()[i];
                RecipeChoice input = new RecipeChoice.ExactChoice(PotionManager.brewPotion(entry.getKey(), bottleType));
                ItemStack output = PotionManager.brewPotion(entry.getValue(), bottleType);
                results.add(new PotionMix(getRecipeKey((i * recipeMap.size()) + j), output, input, ingredient));
                j++;
            }
        }
        return results;
    }

    @Override
    public void applyAdvancedRecipe(BrewEvent event) {
    }

    /* --- Vanilla additions --- */

    public static MultiPotionRecipe customExtend() {
        var recipe = new MultiPotionRecipe("custom_extend", Material.REDSTONE);
        recipe.recipeMap.put("haste:1_1", "haste:2_1");
        recipe.recipeMap.put("fatigue:1_1", "fatigue:2_1");
        recipe.recipeMap.put("resistance:1_1", "resistance:2_1");
        recipe.recipeMap.put("levitation:1_1", "levitation:2_1");
        return recipe;
    }

    public static MultiPotionRecipe customAmplify() {
        var recipe = new MultiPotionRecipe("custom_amplify", Material.GLOWSTONE_DUST);
        recipe.recipeMap.put("haste:1_1", "haste:0_2");
        recipe.recipeMap.put("fatigue:1_1", "fatigue:0_2");
        recipe.recipeMap.put("resistance:1_1", "resistance:0_2");
        recipe.recipeMap.put("levitation:1_1", "levitation:0_2");
        return recipe;
    }

    public static MultiPotionRecipe customCorrupt() {
        var recipe = new MultiPotionRecipe("custom_corrupt", Material.FERMENTED_SPIDER_EYE);
        recipe.recipeMap.put("haste:1_1", "fatigue:1_1");
        recipe.recipeMap.put("haste:2_1", "fatigue:2_1");
        recipe.recipeMap.put("haste:0_2", "fatigue:0_2");
        recipe.recipeMap.put("haste:3_1", "fatigue:3_1");
        recipe.recipeMap.put("haste:1_2", "fatigue:1_2");
        recipe.recipeMap.put("haste:0_3", "fatigue:0_3");
        recipe.recipeMap.put("slow_falling:1_1", "levitation:1_1");
        recipe.recipeMap.put("slow_falling:2_1", "levitation:2_1");
        recipe.recipeMap.put("slow_falling:3_1", "levitation:3_1");
        recipe.addRandomOutput(3, "bad_omen:0_4");
        return recipe;
    }
}
