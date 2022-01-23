package me.mfk1016.stadtserver.brewing;

import me.mfk1016.stadtserver.StadtServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static me.mfk1016.stadtserver.util.Functions.undecoratedText;
import static org.bukkit.potion.PotionEffectType.*;

/*
    - amplifier = 0 == level = 1
    - duration in ticks; instant -> 1
    - ambient unwichtig

    Duration:

    0.5 - 1 - 2 - 3

    1:30 - 3:00 - 8:00 - 15:00
    0:45 - 1:30 - 4:00 - 7:30
    0:22 - 0:45 - 2:00 - 3:45
 */


public class PotionLibrary {

    // Haste, Fatigue

    public static final PotionEffect HASTE_1_1 = effectDef(FAST_DIGGING, 3, 0, 1);
    public static final PotionEffect HASTE_2_1 = effectDef(FAST_DIGGING, 8, 0, 1);
    public static final PotionEffect HASTE_0_2 = effectDef(FAST_DIGGING, 1, 30, 2);

    public static final PotionEffect FATIGUE_1_1 = effectDef(SLOW_DIGGING, 1, 30, 1);
    public static final PotionEffect FATIGUE_2_1 = effectDef(SLOW_DIGGING, 4, 0, 1);
    public static final PotionEffect FATIGUE_0_2 = effectDef(SLOW_DIGGING, 0, 45, 4);

    // Resistance

    public static final PotionEffect RESISTANCE_1_1 = effectDef(DAMAGE_RESISTANCE, 1, 30, 1);
    public static final PotionEffect RESISTANCE_2_1 = effectDef(DAMAGE_RESISTANCE, 4, 0, 1);
    public static final PotionEffect RESISTANCE_0_2 = effectDef(DAMAGE_RESISTANCE, 0, 45, 2);

    // Levitation

    public static final PotionEffect LEVITATION_1_1 = effectDef(LEVITATION, 1, 30, 1);
    public static final PotionEffect LEVITATION_2_1 = effectDef(LEVITATION, 4, 0, 1);
    public static final PotionEffect LEVITATION_0_2 = effectDef(LEVITATION, 0, 45, 2);

    // Level 1 Duration 3 -> extended + warped fungus

    public static final PotionEffect FIRE_RESISTANCE_3_1 = effectDef(FIRE_RESISTANCE, 15, 0, 1);
    public static final PotionEffect INVISIBILITY_3_1 = effectDef(INVISIBILITY, 15, 0, 1);
    public static final PotionEffect JUMP_3_1 = effectDef(JUMP, 15, 0, 1);
    public static final PotionEffect NIGHT_VISION_3_1 = effectDef(NIGHT_VISION, 15, 0, 1);
    public static final PotionEffect SPEED_3_1 = effectDef(SPEED, 15, 0, 1);
    public static final PotionEffect STRENGTH_3_1 = effectDef(INCREASE_DAMAGE, 15, 0, 1);
    public static final PotionEffect WATER_BREATH_3_1 = effectDef(WATER_BREATHING, 15, 0, 1);
    public static final PotionEffect HASTE_3_1 = effectDef(FAST_DIGGING, 15, 0, 1);

    public static final PotionEffect SLOW_3_1 = effectDef(SLOW, 7, 30, 1);
    public static final PotionEffect SLOW_FALLING_3_1 = effectDef(SLOW_FALLING, 7, 30, 1);
    public static final PotionEffect WEAKNESS_3_1 = effectDef(WEAKNESS, 7, 30, 1);
    public static final PotionEffect FATIGUE_3_1 = effectDef(SLOW_DIGGING, 7, 30, 1);
    public static final PotionEffect RESISTANCE_3_1 = effectDef(DAMAGE_RESISTANCE, 7, 30, 1);
    public static final PotionEffect LEVITATION_3_1 = effectDef(LEVITATION, 7, 30, 1);

    public static final PotionEffect POISON_3_1 = effectDef(POISON, 3, 45, 1);
    public static final PotionEffect REGENERATION_3_1 = effectDef(REGENERATION, 3, 45, 1);

    // Level 2 Duration 1 -> amplified + warped fungus / extended + amethyst

    public static final PotionEffect JUMP_1_2 = effectDef(JUMP, 3, 0, 2);
    public static final PotionEffect SPEED_1_2 = effectDef(SPEED, 3, 0, 2);
    public static final PotionEffect STRENGTH_1_2 = effectDef(INCREASE_DAMAGE, 3, 0, 2);
    public static final PotionEffect HASTE_1_2 = effectDef(FAST_DIGGING, 3, 0, 2);

    public static final PotionEffect SLOW_1_2 = effectDef(SLOW, 1, 30, 4);
    public static final PotionEffect FATIGUE_1_2 = effectDef(SLOW_DIGGING, 1, 30, 4);
    public static final PotionEffect RESISTANCE_1_2 = effectDef(DAMAGE_RESISTANCE, 1, 30, 2);
    public static final PotionEffect LEVITATION_1_2 = effectDef(LEVITATION, 1, 30, 2);

    public static final PotionEffect POISON_1_2 = effectDef(POISON, 0, 45, 2);
    public static final PotionEffect REGENERATION_1_2 = effectDef(REGENERATION, 0, 45, 2);

    // Level 3 Duration 0.5 -> amplified + amethyst

    public static final PotionEffect JUMP_0_3 = effectDef(JUMP, 1, 30, 3);
    public static final PotionEffect SPEED_0_3 = effectDef(SPEED, 1, 30, 3);
    public static final PotionEffect STRENGTH_0_3 = effectDef(INCREASE_DAMAGE, 1, 30, 3);
    public static final PotionEffect HASTE_0_3 = effectDef(FAST_DIGGING, 1, 30, 3);

    public static final PotionEffect SLOW_0_3 = effectDef(SLOW, 0, 45, 6);
    public static final PotionEffect FATIGUE_0_3 = effectDef(SLOW_DIGGING, 0, 45, 6);
    public static final PotionEffect RESISTANCE_0_3 = effectDef(DAMAGE_RESISTANCE, 0, 45, 3);
    public static final PotionEffect LEVITATION_0_3 = effectDef(LEVITATION, 0, 45, 3);

    public static final PotionEffect POISON_0_3 = effectDef(POISON, 0, 22, 3);
    public static final PotionEffect REGENERATION_0_3 = effectDef(REGENERATION, 0, 22, 3);

    public static final PotionEffect HEAL_3 = effectDefInstant(HEAL, 3);
    public static final PotionEffect HARM_3 = effectDefInstant(HARM, 3);


    private static final Map<PotionEffectType, String> potionNames = new HashMap<>();

    static {
        potionNames.put(ABSORPTION, "Absorption");
        potionNames.put(BAD_OMEN, "Bad Omen");
        potionNames.put(BLINDNESS, "Blindness");
        potionNames.put(CONDUIT_POWER, "Conduit Power");
        potionNames.put(CONFUSION, "Nausea");
        potionNames.put(DAMAGE_RESISTANCE, "Shielding");
        potionNames.put(DOLPHINS_GRACE, "Dolphins Grace");
        potionNames.put(FAST_DIGGING, "Stamina");
        potionNames.put(FIRE_RESISTANCE, "Fire Resistance");
        potionNames.put(GLOWING, "Glowing");
        potionNames.put(HARM, "Harming");
        potionNames.put(HEAL, "Healing");
        potionNames.put(HEALTH_BOOST, "Health Boost");
        potionNames.put(HERO_OF_THE_VILLAGE, "Heroism");
        potionNames.put(HUNGER, "Hunger");
        potionNames.put(INCREASE_DAMAGE, "Strength");
        potionNames.put(INVISIBILITY, "Invisibility");
        potionNames.put(JUMP, "Leaping");
        potionNames.put(LEVITATION, "Heavenly Height");
        potionNames.put(LUCK, "Luck");
        potionNames.put(NIGHT_VISION, "Night Vision");
        potionNames.put(POISON, "Poison");
        potionNames.put(REGENERATION, "Regeneration");
        potionNames.put(SATURATION, "Saturation");
        potionNames.put(SLOW, "Slowness");
        potionNames.put(SLOW_DIGGING, "Mining Fatigue");
        potionNames.put(SLOW_FALLING, "Slow Falling");
        potionNames.put(SPEED, "Swiftness");
        potionNames.put(UNLUCK, "Unluck");
        potionNames.put(WATER_BREATHING, "Water Breathing");
        potionNames.put(WEAKNESS, "Weakness");
        potionNames.put(WITHER, "Wither");
    }

    public static boolean isCustomBrew(ItemStack input) {
        if (!(input.getItemMeta() instanceof PotionMeta potion))
            return false;
        return potion.getBasePotionData().getType() == PotionType.THICK && !potion.getCustomEffects().isEmpty();
    }

    private static boolean matchCustomBrew(ItemStack input, PotionEffect definition) {
        if (!isCustomBrew(input))
            return false;

        PotionMeta potion = (PotionMeta) Objects.requireNonNull(input.getItemMeta());
        if (potion.getCustomEffects().size() != 1)
            return false;
        PotionEffect effect = Objects.requireNonNull(potion.getCustomEffects().get(0));
        return effect.getType().equals(definition.getType()) &&
                effect.getDuration() == definition.getDuration() &&
                effect.getAmplifier() == definition.getAmplifier();
    }

    public static boolean matchCustomBrews(ItemStack input, Map<PotionEffect, PotionEffect> mapping) {
        for (var def : mapping.keySet()) {
            if (matchCustomBrew(input, def))
                return true;
        }
        return false;
    }

    public static ItemStack buildCustomBrew(BottleType bottleType, PotionEffect definition) {
        ItemStack result = bottleType.asItemStack();
        PotionMeta potion = (PotionMeta) Objects.requireNonNull(result.getItemMeta());
        potion.setBasePotionData(new PotionData(PotionType.THICK));
        potion.addCustomEffect(definition, true);
        String finalName = switch (bottleType) {
            case NORMAL -> "Potion of " + potionNames.get(definition.getType());
            case SPLASH -> "Splash Potion of " + potionNames.get(definition.getType());
            case LINGER -> "Lingering Potion of " + potionNames.get(definition.getType());
        };
        potion.displayName(undecoratedText(finalName));
        result.setItemMeta(potion);
        return result;
    }

    public static ItemStack buildCustomBrew(BottleType bottleType, String name, PotionEffect... definitions) {
        ItemStack result = bottleType.asItemStack();
        PotionMeta potion = (PotionMeta) Objects.requireNonNull(result.getItemMeta());
        potion.setBasePotionData(new PotionData(PotionType.THICK));
        for (var definition : definitions)
            potion.addCustomEffect(definition, true);
        String finalName = switch (bottleType) {
            case NORMAL -> "Potion of " + name;
            case SPLASH -> "Splash Potion of " + name;
            case LINGER -> "Lingering Potion of " + name;
        };
        potion.displayName(undecoratedText(finalName));
        result.setItemMeta(potion);
        return result;
    }

    public static ItemStack mapCustomBrew(ItemStack input, Map<PotionEffect, PotionEffect> mapping) {
        for (var mapEntry : mapping.entrySet()) {
            if (matchCustomBrew(input, mapEntry.getKey()))
                return buildCustomBrew(BottleType.ofStack(input), mapEntry.getValue());
        }
        return failedBrew(BottleType.ofStack(input));
    }

    public static boolean brewFailed(int failChance) {
        return StadtServer.RANDOM.nextInt(100) < failChance;
    }

    public static ItemStack failedBrew(BottleType bottleType) {
        ItemStack result = bottleType.asItemStack();
        PotionMeta potion = (PotionMeta) Objects.requireNonNull(result.getItemMeta());
        potion.setBasePotionData(new PotionData(PotionType.THICK));
        result.setItemMeta(potion);
        return result;
    }

    private static PotionEffect effectDef(PotionEffectType type, int min, int sec, int level) {
        return new PotionEffect(type, (min * 60 + sec) * 20, level - 1);
    }

    private static PotionEffect effectDefInstant(PotionEffectType type, int level) {
        return new PotionEffect(type, 1, level - 1);
    }
}
