package me.mfk1016.stadtserver.origin.enchantment;

import me.mfk1016.stadtserver.EnchantmentManager;
import me.mfk1016.stadtserver.enchantments.CustomEnchantment;

import java.util.HashMap;
import java.util.Map;

public class FishBookOrigin extends EnchantmentOrigin {

    public FishBookOrigin(CustomEnchantment enchantment, int chance, int[] levelChances) {
        super(enchantment, chance, levelChances);
    }

    public static Map<FishBookOrigin, Integer> matchOrigins() {
        Map<FishBookOrigin, Integer> result = new HashMap<>();
        for (EnchantmentOrigin elem : EnchantmentManager.ORIGINS) {
            if (elem instanceof FishBookOrigin origin) {
                int level = origin.getRandomLevel();
                if (level > 0)
                    result.put(origin, level);
            }
        }
        return result;
    }
}
