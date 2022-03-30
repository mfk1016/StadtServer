package me.mfk1016.stadtserver.origin.loot;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.mfk1016.stadtserver.enchantments.CustomEnchantment;
import me.mfk1016.stadtserver.enchantments.EnchantmentManager;

public class FishEnchantmentOrigin extends LootEnchantmentOrigin {

    public static final String TYPE = "enchantment.fish";

    public FishEnchantmentOrigin(int chance, CustomEnchantment enchantment, int[] levelWeights) {
        super(TYPE, chance, enchantment, levelWeights);
    }

    public static FishEnchantmentOrigin fromJson(JsonObject object, Gson gson) {
        int chance = object.get("chance").getAsInt();
        String enchantment = object.get("enchantment").getAsString();
        int[] weights = gson.fromJson(object.getAsJsonArray("weights"), int[].class);
        CustomEnchantment realEnchantment = null;
        for (CustomEnchantment ench : EnchantmentManager.ALL_ENCHANTMENTS) {
            if (ench.getKey().getKey().equals(enchantment))
                realEnchantment = ench;
        }
        assert realEnchantment != null;
        return new FishEnchantmentOrigin(chance, realEnchantment, weights);
    }
}
