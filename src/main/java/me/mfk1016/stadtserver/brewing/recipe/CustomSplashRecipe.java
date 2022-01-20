package me.mfk1016.stadtserver.brewing.recipe;

import me.mfk1016.stadtserver.brewing.BottleType;
import me.mfk1016.stadtserver.brewing.BrewingRecipe;
import me.mfk1016.stadtserver.brewing.PotionLibrary;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;

import java.util.Objects;

public class CustomSplashRecipe extends BrewingRecipe {

    public CustomSplashRecipe() {
        super("custom_splash");
    }

    @Override
    public boolean isApplicable(ItemStack input, Material ingredient) {

        if (ingredient != Material.GUNPOWDER)
            return false;

        return input.getType() == Material.POTION && PotionLibrary.isCustomBrew(input);
    }

    @Override
    public ItemStack brewPotion(ItemStack input, Material ingredient) {
        PotionMeta potion = (PotionMeta) Objects.requireNonNull(input.getItemMeta());
        ItemStack result = BottleType.SPLASH.asItemStack();
        result.setItemMeta(potion.clone());
        return result;
    }
}
