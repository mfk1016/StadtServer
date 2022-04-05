package me.mfk1016.stadtserver.brewing.recipe;

import me.mfk1016.stadtserver.brewing.BrewingRecipe;
import me.mfk1016.stadtserver.brewing.PotionLibrary;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class CustomAmplifyRecipe extends BrewingRecipe {

    private static final Map<PotionEffect, PotionEffect> CUSTOM_MAP = new HashMap<>();

    static {
        CUSTOM_MAP.put(PotionLibrary.HASTE_1_1, PotionLibrary.HASTE_0_2);
        CUSTOM_MAP.put(PotionLibrary.FATIGUE_1_1, PotionLibrary.FATIGUE_0_2);
        CUSTOM_MAP.put(PotionLibrary.RESISTANCE_1_1, PotionLibrary.RESISTANCE_0_2);
        CUSTOM_MAP.put(PotionLibrary.LEVITATION_1_1, PotionLibrary.LEVITATION_0_2);
    }

    public CustomAmplifyRecipe() {
        super();
    }

    @Override
    public boolean isApplicable(@NotNull ItemStack input, Material ingredient) {
        return ingredient == Material.GLOWSTONE_DUST && PotionLibrary.matchCustomBrews(input, CUSTOM_MAP);
    }

    @Override
    public ItemStack brewPotion(@NotNull ItemStack input, Material ingredient) {
        return PotionLibrary.mapCustomBrew(input, CUSTOM_MAP);
    }
}
