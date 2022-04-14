package me.mfk1016.stadtserver.brewing;

import com.google.gson.JsonObject;
import me.mfk1016.stadtserver.logic.sorting.PluginCategories;
import me.mfk1016.stadtserver.util.Keys;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;

import static me.mfk1016.stadtserver.util.Functions.stackEmpty;
import static me.mfk1016.stadtserver.util.Functions.undecoratedText;

public record SpecialPotionType(String id, String name, Color color, PotionEffect... effects) {

    public TextComponent getPotionName() {
        return undecoratedText(name);
    }

    public PotionMeta getPotionMeta() {
        PotionMeta potion = (PotionMeta) Bukkit.getItemFactory().getItemMeta(Material.POTION);
        potion.setBasePotionData(new PotionData(org.bukkit.potion.PotionType.THICK));
        potion.setColor(color);
        PersistentDataContainer pdc = potion.getPersistentDataContainer();
        pdc.set(Keys.SPECIAL_POTION_ID, PersistentDataType.STRING, id);
        return potion;
    }

    public ItemStack createPotion() {
        ItemStack result = BottleType.NORMAL.asItemStack();
        PotionMeta meta = getPotionMeta();
        if (!name.isEmpty())
            meta.displayName(getPotionName());
        result.setItemMeta(meta);
        return result;
    }

    public boolean isMatched(ItemStack item) {
        if (stackEmpty(item) || !PluginCategories.isPotion(item.getType()))
            return false;
        ItemMeta potion = item.getItemMeta();
        PersistentDataContainer pdc = potion.getPersistentDataContainer();
        return pdc.getOrDefault(Keys.SPECIAL_POTION_ID, PersistentDataType.STRING, "").equals(id);
    }


    public static SpecialPotionType fromJson(JsonObject object) {
        String id = object.get("id").getAsString();
        String name = object.get("name").getAsString();
        Color color = Color.fromRGB(Integer.parseInt(object.get("color").getAsString(), 16));
        ArrayList<PotionEffect> effects = new ArrayList<>();
        for (var effect : object.getAsJsonArray("effects")) {
            JsonObject effectObject = effect.getAsJsonObject();
            PotionEffectType type = PotionManager.POTION_EFFECT_TYPE.get(effectObject.get("type").getAsString());
            int amplifier = effectObject.get("level").getAsInt() - 1;
            int duration = effectObject.has("duration") ? effectObject.get("duration").getAsInt() : 1;
            assert type != null;
            effects.add(new PotionEffect(type, duration, amplifier));
        }
        return new SpecialPotionType(id, name, color, effects.toArray(new PotionEffect[0]));
    }

    public void onPlayerDrink(Player player) {
        PotionEffect currentNausea = player.getPotionEffect(PotionEffectType.CONFUSION);
        for (PotionEffect effect : effects) {
            if (effect.getType() == PotionEffectType.CONFUSION && currentNausea != null) {
                // Nausea: Sum of levels + duration max of given
                int newDuration = Math.max(currentNausea.getDuration(), effect.getDuration()) + 1200;
                int newLevel = currentNausea.getAmplifier() + effect.getAmplifier() + 1;
                player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, newDuration, newLevel));
            } else {
                player.addPotionEffect(effect);
            }
        }
    }
}
