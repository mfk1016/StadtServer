package me.mfk1016.stadtserver.brewing.recipe;

import io.papermc.paper.potion.PotionMix;
import me.mfk1016.stadtserver.brewing.BottleType;
import me.mfk1016.stadtserver.brewing.PotionManager;
import org.bukkit.Material;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SplashPotionRecipe extends AdvancedPotionRecipe {

    public SplashPotionRecipe() { super("splash", new RecipeChoice.MaterialChoice(Material.GUNPOWDER)); }

    @Override
    protected boolean isInputMatched(ItemStack stack) {
        return stack.getType() == Material.POTION;
    }

    @Override
    public Collection<PotionMix> buildBaseRecipes() {
        int keyID = 0;
        List<PotionMix> recipes = new ArrayList<>();
        for (var potionType : PotionManager.CUSTOM_POTION_TYPE.values()) {
            ItemStack result = PotionManager.brewPotion(potionType.id(), BottleType.SPLASH);
            RecipeChoice input = new RecipeChoice.ExactChoice(PotionManager.brewPotion(potionType.id(), BottleType.NORMAL));
            recipes.add(new PotionMix(getRecipeKey(keyID), result, input, ingredient));
            keyID++;
        }
        return recipes;
    }

    @Override
    public void applyAdvancedRecipe(BrewEvent event) {}
}
