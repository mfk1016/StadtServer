package me.mfk1016.stadtserver.brewing.recipe;

import me.mfk1016.stadtserver.brewing.BottleType;
import me.mfk1016.stadtserver.brewing.BrewingRecipe;
import me.mfk1016.stadtserver.brewing.PotionLibrary;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class WarpedFungusRecipe extends BrewingRecipe {

    private static final int FAIL_CHANCE = 10;

    private static final Map<PotionEffect, PotionEffect> CUSTOM_MAP = new HashMap<>();

    static {
        CUSTOM_MAP.put(PotionLibrary.HASTE_2_1, PotionLibrary.HASTE_3_1);
        CUSTOM_MAP.put(PotionLibrary.HASTE_0_2, PotionLibrary.HASTE_1_2);
        CUSTOM_MAP.put(PotionLibrary.FATIGUE_2_1, PotionLibrary.FATIGUE_3_1);
        CUSTOM_MAP.put(PotionLibrary.FATIGUE_0_2, PotionLibrary.FATIGUE_1_2);
        CUSTOM_MAP.put(PotionLibrary.RESISTANCE_2_1, PotionLibrary.RESISTANCE_3_1);
        CUSTOM_MAP.put(PotionLibrary.RESISTANCE_0_2, PotionLibrary.RESISTANCE_1_2);
    }

    @Override
    public boolean isApplicable(ItemStack input, Material ingredient) {
        if (ingredient != Material.WARPED_FUNGUS)
            return false;

        if (PotionLibrary.matchCustomBrews(input, CUSTOM_MAP))
            return true;

        if (!(input.getItemMeta() instanceof PotionMeta potion))
            return false;

        if (!potion.getBasePotionData().isExtended() && !potion.getBasePotionData().isUpgraded())
            return false;

        return switch (potion.getBasePotionData().getType()) {
            case NIGHT_VISION, INVISIBILITY, JUMP, FIRE_RESISTANCE, SPEED, SLOWNESS, WATER_BREATHING,
                    POISON, REGEN, STRENGTH, WEAKNESS, SLOW_FALLING, TURTLE_MASTER -> true;
            default -> false;
        };
    }

    @Override
    public ItemStack brewPotion(ItemStack input, Material ingredient) {
        if (PotionLibrary.brewFailed(FAIL_CHANCE))
            return PotionLibrary.failedBrew(BottleType.fromItemStack(input));
        if (PotionLibrary.isCustomBrew(input))
            return PotionLibrary.mapCustomBrew(input, CUSTOM_MAP);

        PotionMeta potion = (PotionMeta) Objects.requireNonNull(input.getItemMeta());
        boolean amplified = potion.getBasePotionData().isUpgraded();
        if (potion.getBasePotionData().getType() == PotionType.TURTLE_MASTER)
            return brewTurtleMaster(input, amplified);
        PotionEffect targetDefinition = switch (potion.getBasePotionData().getType()) {
            case NIGHT_VISION -> PotionLibrary.NIGHT_VISION_3_1;
            case INVISIBILITY -> PotionLibrary.INVISIBILITY_3_1;
            case JUMP -> amplified ? PotionLibrary.JUMP_1_2 : PotionLibrary.JUMP_3_1;
            case FIRE_RESISTANCE -> PotionLibrary.FIRE_RESISTANCE_3_1;
            case SPEED -> amplified ? PotionLibrary.SPEED_1_2 : PotionLibrary.SPEED_3_1;
            case SLOWNESS -> amplified ? PotionLibrary.SLOW_1_2 : PotionLibrary.SLOW_3_1;
            case WATER_BREATHING -> PotionLibrary.WATER_BREATH_3_1;
            case POISON -> amplified ? PotionLibrary.POISON_1_2 : PotionLibrary.POISON_3_1;
            case REGEN -> amplified ? PotionLibrary.REGENERATION_1_2 : PotionLibrary.REGENERATION_3_1;
            case STRENGTH -> amplified ? PotionLibrary.STRENGTH_1_2 : PotionLibrary.STRENGTH_3_1;
            case WEAKNESS -> PotionLibrary.WEAKNESS_3_1;
            case SLOW_FALLING -> PotionLibrary.SLOW_FALLING_3_1;
            default -> null;
        };
        return PotionLibrary.buildCustomBrew(BottleType.fromItemStack(input), targetDefinition);
    }

    private ItemStack brewTurtleMaster(ItemStack input, boolean amplified) {
        PotionEffect slowness = new PotionEffect(PotionEffectType.SLOW, amplified ? 800 : 1600, amplified ? 5 : 3);
        PotionEffect resistance = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, amplified ? 800 : 1600, amplified ? 3 : 2);
        return PotionLibrary.buildCustomBrew(BottleType.fromItemStack(input), "the Turtle Master", slowness, resistance);
    }
}
