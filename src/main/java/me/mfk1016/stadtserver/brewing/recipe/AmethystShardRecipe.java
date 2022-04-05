package me.mfk1016.stadtserver.brewing.recipe;

import me.mfk1016.stadtserver.brewing.BottleType;
import me.mfk1016.stadtserver.brewing.BrewingRecipe;
import me.mfk1016.stadtserver.brewing.PotionLibrary;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AmethystShardRecipe extends BrewingRecipe {

    private static final int FAIL_CHANCE = 20;

    private static final Map<PotionEffect, PotionEffect> CUSTOM_MAP = new HashMap<>();

    static {
        CUSTOM_MAP.put(PotionLibrary.HASTE_2_1, PotionLibrary.HASTE_1_2);
        CUSTOM_MAP.put(PotionLibrary.HASTE_0_2, PotionLibrary.HASTE_0_3);
        CUSTOM_MAP.put(PotionLibrary.FATIGUE_2_1, PotionLibrary.FATIGUE_1_2);
        CUSTOM_MAP.put(PotionLibrary.FATIGUE_0_2, PotionLibrary.FATIGUE_0_3);
        CUSTOM_MAP.put(PotionLibrary.RESISTANCE_2_1, PotionLibrary.RESISTANCE_1_2);
        CUSTOM_MAP.put(PotionLibrary.RESISTANCE_0_2, PotionLibrary.RESISTANCE_0_3);
        CUSTOM_MAP.put(PotionLibrary.LEVITATION_2_1, PotionLibrary.LEVITATION_1_2);
        CUSTOM_MAP.put(PotionLibrary.LEVITATION_0_2, PotionLibrary.LEVITATION_0_3);
    }

    public AmethystShardRecipe() {
        super();
    }

    @Override
    public boolean isApplicable(@NotNull ItemStack input, Material ingredient) {
        if (ingredient != Material.AMETHYST_SHARD)
            return false;

        if (PotionLibrary.matchCustomBrews(input, CUSTOM_MAP))
            return true;

        if (!(input.getItemMeta() instanceof PotionMeta potion))
            return false;

        if (!potion.getBasePotionData().isExtended() && !potion.getBasePotionData().isUpgraded())
            return false;

        return switch (potion.getBasePotionData().getType()) {
            case INSTANT_HEAL, INSTANT_DAMAGE, JUMP, SPEED, SLOWNESS,
                    POISON, REGEN, STRENGTH -> true;
            default -> false;
        };
    }

    @Override
    public ItemStack brewPotion(@NotNull ItemStack input, Material ingredient) {
        if (PotionLibrary.brewFailed(FAIL_CHANCE))
            return PotionLibrary.failedBrew(BottleType.ofStack(input));
        if (PotionLibrary.isCustomBrew(input))
            return PotionLibrary.mapCustomBrew(input, CUSTOM_MAP);

        PotionMeta potion = (PotionMeta) Objects.requireNonNull(input.getItemMeta());
        boolean extended = potion.getBasePotionData().isExtended();
        PotionEffect targetDefinition = switch (potion.getBasePotionData().getType()) {
            case INSTANT_HEAL -> PotionLibrary.HEAL_3;
            case INSTANT_DAMAGE -> PotionLibrary.HARM_3;
            case JUMP -> extended ? PotionLibrary.JUMP_1_2 : PotionLibrary.JUMP_0_3;
            case SPEED -> extended ? PotionLibrary.SPEED_1_2 : PotionLibrary.SPEED_0_3;
            case SLOWNESS -> extended ? PotionLibrary.SLOW_1_2 : PotionLibrary.SLOW_0_3;
            case POISON -> extended ? PotionLibrary.POISON_1_2 : PotionLibrary.POISON_0_3;
            case REGEN -> extended ? PotionLibrary.REGENERATION_1_2 : PotionLibrary.REGENERATION_0_3;
            case STRENGTH -> extended ? PotionLibrary.STRENGTH_1_2 : PotionLibrary.STRENGTH_0_3;
            default -> null;
        };
        return PotionLibrary.buildCustomBrew(BottleType.ofStack(input), targetDefinition);
    }
}
