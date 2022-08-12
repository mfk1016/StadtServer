package me.mfk1016.stadtserver.brewing.recipe;

import io.papermc.paper.potion.PotionMix;
import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.brewing.BottleType;
import me.mfk1016.stadtserver.brewing.PotionManager;
import me.mfk1016.stadtserver.util.Pair;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static me.mfk1016.stadtserver.util.Functions.stackEmpty;

public abstract class AdvancedPotionRecipe {

    private static final String RECIPE_KEY_PREFIX = "brewing_";

    private final List<Pair<Integer, String>> randomOutput = new ArrayList<>();

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

    protected void addRandomOutput(int chance, String id) {
        randomOutput.add(new Pair<>(chance, id));
    }

    protected Optional<String> testRandomOutput() {
        int hit = StadtServer.RANDOM.nextInt(100);
        int test = 0;
        for (Pair<Integer, String> elem : randomOutput) {
            test += elem._1;
            if (hit < test)
                return Optional.of(elem._2);
        }
        return Optional.empty();
    }

    public void applyRandomOutput(BrewEvent event) {
        BrewerInventory inventory = event.getContents();
        for (int i = 0; i < 3; i++) {
            ItemStack input = inventory.getItem(i);
            if (stackEmpty(input) || !isInputMatched(input))
                continue;
            Optional<String> randomID = testRandomOutput();
            if (randomID.isEmpty())
                continue;
            BottleType targetBottle = BottleType.ofStack(event.getResults().get(i));
            ItemStack randomResult = PotionManager.brewPotion(randomID.get(), targetBottle);
            event.getResults().set(i, randomResult);
        }
    }

    public boolean isIngredientMatched(BrewEvent event) {
        ItemStack usedIngredient = event.getContents().getIngredient();
        return !stackEmpty(usedIngredient) && ingredient.test(usedIngredient);
    }

    // The ingredient must be in every recipe
    public abstract Collection<PotionMix> buildBaseRecipes();

    // Optional behaviour after the brewing is finished
    @SuppressWarnings({"EmptyMethod", "unused"})
    public abstract void applyAdvancedRecipe(BrewEvent event);
}
