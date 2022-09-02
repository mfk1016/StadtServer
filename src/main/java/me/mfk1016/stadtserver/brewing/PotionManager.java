package me.mfk1016.stadtserver.brewing;

import com.google.gson.*;
import me.mfk1016.stadtserver.StadtServer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import static org.bukkit.potion.PotionEffectType.*;


public class PotionManager {

    public static final HashMap<String, CustomPotionType> CUSTOM_POTION_TYPE = new HashMap<>();
    public static final HashMap<String, SpecialPotionType> SPECIAL_POTION_TYPE = new HashMap<>();

    public static boolean isValidPotionID(String id) {
        if (CUSTOM_POTION_TYPE.containsKey(id))
            return true;
        String[] elements = id.split(":", 2);
        String typeString = elements[0];
        String levelString = elements.length == 1 ? "1_1" : elements[1];
        try {
            PotionType type = PotionType.valueOf(typeString.toUpperCase(Locale.ENGLISH));
            return switch (levelString) {
                case "2_1" -> type.isExtendable();
                case "0_2" -> type.isUpgradeable();
                default -> levelString.equals("1_1");
            };
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static ItemStack brewPotion(String id, BottleType bottleType) {
        if (CUSTOM_POTION_TYPE.containsKey(id)) {
            CustomPotionType cpt = Objects.requireNonNull(CUSTOM_POTION_TYPE.get(id));
            return cpt.createPotion(bottleType);
        }
        String[] elements = id.split(":", 2);
        String typeString = elements[0];
        String levelString = elements.length == 1 ? "1_1" : elements[1];
        ItemStack result = bottleType.asItemStack();
        PotionMeta meta = (PotionMeta) result.getItemMeta();
        PotionType type = PotionType.valueOf(typeString.toUpperCase(Locale.ENGLISH));
        switch (levelString) {
            case "2_1" -> meta.setBasePotionData(new PotionData(type, true, false));
            case "0_2" -> meta.setBasePotionData(new PotionData(type, false, true));
            default -> meta.setBasePotionData(new PotionData(type, false, false));
        }
        result.setItemMeta(meta);
        return result;
    }

    public static ItemStack brewSpecialPotion(String id) {
        assert SPECIAL_POTION_TYPE.containsKey(id);
        SpecialPotionType spt = Objects.requireNonNull(SPECIAL_POTION_TYPE.get(id));
        return spt.createPotion();
    }

    public static Optional<CustomPotionType> matchCustomPotion(ItemStack input) {
        for (var potionType : CUSTOM_POTION_TYPE.values()) {
            if (potionType.isMatched(input))
                return Optional.of(potionType);
        }
        return Optional.empty();
    }

    public static Optional<SpecialPotionType> matchSpecialPotion(ItemStack input) {
        for (var potionType : SPECIAL_POTION_TYPE.values()) {
            if (potionType.isMatched(input))
                return Optional.of(potionType);
        }
        return Optional.empty();
    }

    /* --- JSON --- */
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void onPluginEnable() {
        InputStream configStream = StadtServer.getInstance().getResource("potions.json");
        try {
            JsonObject root = gson.fromJson(new InputStreamReader(Objects.requireNonNull(configStream)), JsonObject.class);
            JsonArray customs = root.getAsJsonArray("custom_types");
            for (JsonElement jsonElement : customs) {
                CustomPotionType toAdd = CustomPotionType.fromJson(jsonElement.getAsJsonObject());
                CUSTOM_POTION_TYPE.put(toAdd.id(), toAdd);
            }
            JsonArray specials = root.getAsJsonArray("special_types");
            for (JsonElement jsonElement : specials) {
                SpecialPotionType toAdd = SpecialPotionType.fromJson(jsonElement.getAsJsonObject());
                SPECIAL_POTION_TYPE.put(toAdd.id(), toAdd);
            }
        } catch (Exception e) {
            StadtServer.LOGGER.severe("origins.json loading error: origins not available");
            e.printStackTrace();
        }
    }

    public static final Map<String, PotionEffectType> POTION_EFFECT_TYPE = new HashMap<>();

    static {
        POTION_EFFECT_TYPE.put("absorption", ABSORPTION);
        POTION_EFFECT_TYPE.put("bad_omen", BAD_OMEN);
        POTION_EFFECT_TYPE.put("blindness", BLINDNESS);
        POTION_EFFECT_TYPE.put("conduit_power", CONDUIT_POWER);
        POTION_EFFECT_TYPE.put("nausea", CONFUSION);
        POTION_EFFECT_TYPE.put("resistance", DAMAGE_RESISTANCE);
        POTION_EFFECT_TYPE.put("dolphins_grace", DOLPHINS_GRACE);
        POTION_EFFECT_TYPE.put("haste", FAST_DIGGING);
        POTION_EFFECT_TYPE.put("fire_resistance", FIRE_RESISTANCE);
        POTION_EFFECT_TYPE.put("glowing", GLOWING);
        POTION_EFFECT_TYPE.put("harm", HARM);
        POTION_EFFECT_TYPE.put("heal", HEAL);
        POTION_EFFECT_TYPE.put("health_boost", HEALTH_BOOST);
        POTION_EFFECT_TYPE.put("heroism", HERO_OF_THE_VILLAGE);
        POTION_EFFECT_TYPE.put("hunger", HUNGER);
        POTION_EFFECT_TYPE.put("strength", INCREASE_DAMAGE);
        POTION_EFFECT_TYPE.put("invisibility", INVISIBILITY);
        POTION_EFFECT_TYPE.put("jump", JUMP);
        POTION_EFFECT_TYPE.put("levitation", LEVITATION);
        POTION_EFFECT_TYPE.put("luck", LUCK);
        POTION_EFFECT_TYPE.put("night_vision", NIGHT_VISION);
        POTION_EFFECT_TYPE.put("poison", POISON);
        POTION_EFFECT_TYPE.put("regeneration", REGENERATION);
        POTION_EFFECT_TYPE.put("saturation", SATURATION);
        POTION_EFFECT_TYPE.put("slowness", SLOW);
        POTION_EFFECT_TYPE.put("fatigue", SLOW_DIGGING);
        POTION_EFFECT_TYPE.put("slow_falling", SLOW_FALLING);
        POTION_EFFECT_TYPE.put("speed", SPEED);
        POTION_EFFECT_TYPE.put("unluck", UNLUCK);
        POTION_EFFECT_TYPE.put("water_breathing", WATER_BREATHING);
        POTION_EFFECT_TYPE.put("weakness", WEAKNESS);
        POTION_EFFECT_TYPE.put("wither", WITHER);
    }
}
