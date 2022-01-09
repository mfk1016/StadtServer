package me.mfk1016.stadtserver.brewing.recipe;

import me.mfk1016.stadtserver.brewing.BottleType;
import me.mfk1016.stadtserver.brewing.BrewingRecipe;
import me.mfk1016.stadtserver.brewing.PotionLibrary;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;

import java.util.Objects;

public class CustomLingerRecipe extends BrewingRecipe {

    @Override
    public boolean isApplicable(ItemStack input, Material ingredient) {

        if (ingredient != Material.DRAGON_BREATH)
            return false;

        return input.getType() == Material.SPLASH_POTION && PotionLibrary.isCustomBrew(input);
    }

    @Override
    public ItemStack brewPotion(ItemStack input, Material ingredient) {
        PotionMeta potion = (PotionMeta) Objects.requireNonNull(input.getItemMeta()).clone();
        ItemStack result = BottleType.LINGER.asItemStack();
        result.setItemMeta(potion);
        return result;
    }
}
