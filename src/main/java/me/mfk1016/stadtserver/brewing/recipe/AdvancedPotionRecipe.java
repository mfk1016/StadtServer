package me.mfk1016.stadtserver.brewing.recipe;

import io.papermc.paper.potion.PotionMix;
import me.mfk1016.stadtserver.StadtServer;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

import java.util.Collection;

import static me.mfk1016.stadtserver.util.Functions.stackEmpty;

public abstract class AdvancedPotionRecipe {

    private static final String RECIPE_KEY_PREFIX = "brewing_";

    private final String recipeKey;
    protected final RecipeChoice ingredient;

    public AdvancedPotionRecipe(String recipeKey, RecipeChoice ingredient) {
        this.recipeKey = recipeKey;
        this.ingredient = ingredient;
    }

    protected NamespacedKey getRecipeKey(int index) {
        return new NamespacedKey(StadtServer.getInstance(), RECIPE_KEY_PREFIX + recipeKey + "_" + index);
    }

    protected abstract boolean isInputMatched(ItemStack stack);

    public boolean isIngredientMatched(BrewEvent event) {
        ItemStack usedIngredient = event.getContents().getIngredient();
        return !stackEmpty(usedIngredient) && ingredient.test(usedIngredient);
    }

    // The ingredient must be in every recipe
    public abstract Collection<PotionMix> buildBaseRecipes();

    // Optional behaviour after the brewing is finished
    public abstract void applyAdvancedRecipe(BrewEvent event);

}
