package me.mfk1016.stadtserver.origin.enchantment;

import me.mfk1016.stadtserver.enchantments.CustomEnchantment;
import me.mfk1016.stadtserver.enchantments.EnchantmentManager;

import java.util.HashMap;
import java.util.Map;

public class PiglinTradeOrigin extends EnchantmentOrigin {

    public PiglinTradeOrigin(CustomEnchantment enchantment, int chance, int[] levelChances) {
        super(enchantment, chance, levelChances);
    }

    public static Map<PiglinTradeOrigin, Integer> matchOrigins() {
        Map<PiglinTradeOrigin, Integer> result = new HashMap<>();
        for (EnchantmentOrigin origin : EnchantmentManager.ORIGINS) {
            if (origin instanceof PiglinTradeOrigin obj) {
                int level = obj.getRandomLevel();
                if (level > 0)
                    result.put(obj, level);
            }
        }
        return result;
    }

}
