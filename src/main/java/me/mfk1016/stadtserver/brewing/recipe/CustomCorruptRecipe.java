package me.mfk1016.stadtserver.brewing.recipe;

import me.mfk1016.stadtserver.brewing.BottleType;
import me.mfk1016.stadtserver.brewing.BrewingRecipe;
import me.mfk1016.stadtserver.brewing.PotionLibrary;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CustomCorruptRecipe extends BrewingRecipe {

    private static final Map<PotionEffect, PotionEffect> CUSTOM_MAP = new HashMap<>();

    static {
        CUSTOM_MAP.put(PotionLibrary.HASTE_1_1, PotionLibrary.FATIGUE_1_1);
        CUSTOM_MAP.put(PotionLibrary.HASTE_2_1, PotionLibrary.FATIGUE_2_1);
        CUSTOM_MAP.put(PotionLibrary.HASTE_0_2, PotionLibrary.FATIGUE_0_2);
    }

    public CustomCorruptRecipe() {
        super();
    }

    @Override
    public boolean isApplicable(@NotNull ItemStack input, Material ingredient) {
        if (ingredient != Material.FERMENTED_SPIDER_EYE)
            return false;

        if (PotionLibrary.matchCustomBrews(input, CUSTOM_MAP))
            return true;

        if (!(input.getItemMeta() instanceof PotionMeta potion))
            return false;

        return potion.getBasePotionData().getType() == PotionType.SLOW_FALLING;
    }

    @Override
    public ItemStack brewPotion(@NotNull ItemStack input, Material ingredient) {
        if (PotionLibrary.isCustomBrew(input))
            return PotionLibrary.mapCustomBrew(input, CUSTOM_MAP);

        PotionMeta potion = (PotionMeta) Objects.requireNonNull(input.getItemMeta());
        if (potion.getBasePotionData().isExtended())
            return PotionLibrary.buildCustomBrew(BottleType.ofStack(input), PotionLibrary.LEVITATION_2_1);
        else
            return PotionLibrary.buildCustomBrew(BottleType.ofStack(input), PotionLibrary.LEVITATION_1_1);
    }
}
