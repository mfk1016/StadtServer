package me.mfk1016.stadtserver.brewing.recipe;

import me.mfk1016.stadtserver.brewing.BrewingRecipe;
import me.mfk1016.stadtserver.brewing.PotionLibrary;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.HashMap;
import java.util.Map;

public class CustomCorruptRecipe extends BrewingRecipe {

    private static final Map<PotionEffect, PotionEffect> CUSTOM_MAP = new HashMap<>();

    static {
        CUSTOM_MAP.put(PotionLibrary.HASTE_1_1, PotionLibrary.FATIGUE_1_1);
        CUSTOM_MAP.put(PotionLibrary.HASTE_2_1, PotionLibrary.FATIGUE_2_1);
        CUSTOM_MAP.put(PotionLibrary.HASTE_0_2, PotionLibrary.FATIGUE_0_2);
    }

    @Override
    public boolean isApplicable(ItemStack input, Material ingredient) {
        return ingredient == Material.FERMENTED_SPIDER_EYE && PotionLibrary.matchCustomBrews(input, CUSTOM_MAP);
    }

    @Override
    public ItemStack brewPotion(ItemStack input, Material ingredient) {
        return PotionLibrary.mapCustomBrew(input, CUSTOM_MAP);
    }
}
