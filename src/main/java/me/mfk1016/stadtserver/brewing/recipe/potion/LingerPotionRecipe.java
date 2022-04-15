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

public class LingerPotionRecipe extends AdvancedPotionRecipe {

    public LingerPotionRecipe() {
        super("linger", new RecipeChoice.MaterialChoice(Material.DRAGON_BREATH));
    }

    @Override
    protected boolean isInputMatched(ItemStack stack) {
        return stack.getType() == Material.SPLASH_POTION;
    }

    @Override
    public Collection<PotionMix> buildBaseRecipes() {
        int keyID = 0;
        List<PotionMix> recipes = new ArrayList<>();
        for (var potionType : PotionManager.CUSTOM_POTION_TYPE.values()) {
            ItemStack result = PotionManager.brewPotion(potionType.id(), BottleType.LINGER);
            RecipeChoice input = new RecipeChoice.ExactChoice(PotionManager.brewPotion(potionType.id(), BottleType.SPLASH));
            recipes.add(new PotionMix(getRecipeKey(keyID), result, input, ingredient));
            keyID++;
        }
        return recipes;
    }

    @Override
    public void applyAdvancedRecipe(BrewEvent event) {
    }
}
