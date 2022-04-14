package me.mfk1016.stadtserver.brewing.recipe;

import io.papermc.paper.potion.PotionMix;
import me.mfk1016.stadtserver.brewing.PotionManager;
import org.bukkit.Material;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

import java.util.Collection;
import java.util.List;

public class SpecialPotionRecipe extends AdvancedPotionRecipe {

    private final ItemStack inputItem;
    private final String outputID;

    public SpecialPotionRecipe(String recipeKey, Material ingredient, ItemStack inputItem, String outputID) {
        super(recipeKey, new RecipeChoice.MaterialChoice(ingredient));
        this.inputItem = inputItem;
        this.outputID = outputID;
    }

    @Override
    protected boolean isInputMatched(ItemStack stack) {
        return stack.equals(inputItem);
    }

    @Override
    public Collection<PotionMix> buildBaseRecipes() {
        RecipeChoice input = new RecipeChoice.ExactChoice(inputItem);
        ItemStack output = PotionManager.brewSpecialPotion(outputID);
        return List.of(new PotionMix(getRecipeKey(0), output, input, ingredient));
    }

    @Override
    public void applyAdvancedRecipe(BrewEvent event) {
    }
}
