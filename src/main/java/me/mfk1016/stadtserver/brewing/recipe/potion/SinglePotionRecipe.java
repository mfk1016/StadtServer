package me.mfk1016.stadtserver.brewing.recipe.potion;

import io.papermc.paper.potion.PotionMix;
import me.mfk1016.stadtserver.brewing.BottleType;
import me.mfk1016.stadtserver.brewing.PotionManager;
import me.mfk1016.stadtserver.brewing.recipe.AdvancedPotionRecipe;
import org.bukkit.Material;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SinglePotionRecipe extends AdvancedPotionRecipe {

    private final String inputID;
    private final String outputID;

    public SinglePotionRecipe(String recipeKey, Material ingredient, String input, String output) {
        super(recipeKey, new RecipeChoice.MaterialChoice(ingredient));
        this.inputID = input;
        this.outputID = output;
    }

    @Override
    protected boolean isInputMatched(ItemStack stack) {
        return stack.isSimilar(PotionManager.brewPotion(inputID, BottleType.ofStack(stack)));
    }

    @Override
    public Collection<PotionMix> buildBaseRecipes() {
        List<PotionMix> results = new ArrayList<>();
        for (int i = 0; i < BottleType.values().length; i++) {
            BottleType bottleType = BottleType.values()[i];
            RecipeChoice input = new RecipeChoice.ExactChoice(PotionManager.brewPotion(inputID, bottleType));
            ItemStack output = PotionManager.brewPotion(outputID, bottleType);
            results.add(new PotionMix(getRecipeKey(i), output, input, ingredient));
        }
        return results;
    }

    @Override
    public void applyAdvancedRecipe(BrewEvent event) {
    }
}
