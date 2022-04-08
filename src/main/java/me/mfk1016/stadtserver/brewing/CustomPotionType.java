package me.mfk1016.stadtserver.brewing;

import com.google.gson.JsonObject;
import me.mfk1016.stadtserver.logic.sorting.PluginCategories;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;

import static me.mfk1016.stadtserver.util.Functions.undecoratedText;

/*
    - amplifier 0 == level 1
    - duration in ticks; instant -> 1
    - ambient unwichtig

    Duration:

    0.5 - 1 - 2 - 3

    1:30 - 3:00 - 8:00 - 15:00
    1800 - 3600 - 9600 - 18000
    0:45 - 1:30 - 4:00 - 7:30
     900 - 1800 - 4800 - 9000
    0:22 - 0:45 - 2:00 - 3:45
     450 -  900 - 2400 - 4500
 */
public record CustomPotionType(String id, String name, PotionEffect... effects) {

    public TextComponent getPotionName(BottleType bottleType) {
        String finalName = switch (bottleType) {
            case NORMAL -> "Potion of " + name;
            case SPLASH -> "Splash Potion of " + name;
            case LINGER -> "Lingering Potion of " + name;
        };
        return undecoratedText(finalName);
    }

    public PotionMeta getPotionMeta() {
        PotionMeta potion = (PotionMeta) Bukkit.getItemFactory().getItemMeta(Material.POTION);
        potion.setBasePotionData(new PotionData(org.bukkit.potion.PotionType.THICK));
        for (var effect : effects)
            potion.addCustomEffect(effect, true);
        return potion;
    }

    public ItemStack createPotion(BottleType bottleType) {
        ItemStack result = bottleType.asItemStack();
        PotionMeta meta = getPotionMeta();
        if (!name.isEmpty())
            meta.displayName(getPotionName(bottleType));
        result.setItemMeta(meta);
        return result;
    }

    public boolean isMatched(ItemStack item) {
        if (!PluginCategories.isPotion(item.getType()))
            return false;
        return item.isSimilar(createPotion(BottleType.ofStack(item)));
    }


    public static CustomPotionType fromJson(JsonObject object) {
        String id = object.get("id").getAsString();
        String name = object.get("name").getAsString();
        ArrayList<PotionEffect> effects = new ArrayList<>();
        for (var effect : object.getAsJsonArray("effects")) {
            JsonObject effectObject = effect.getAsJsonObject();
            PotionEffectType type = PotionManager.POTION_EFFECT_TYPE.get(effectObject.get("type").getAsString());
            int amplifier = effectObject.get("level").getAsInt() - 1;
            int duration = effectObject.has("duration") ? effectObject.get("duration").getAsInt() : 1;
            assert type != null;
            effects.add(new PotionEffect(type, duration, amplifier));
        }
        return new CustomPotionType(id, name, effects.toArray(new PotionEffect[0]));
    }
}
