package me.mfk1016.stadtserver.origin.enchantment;

import me.mfk1016.stadtserver.enchantments.CustomEnchantment;
import me.mfk1016.stadtserver.enchantments.EnchantmentManager;
import me.mfk1016.stadtserver.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FishBookOrigin extends EnchantmentOrigin {

    public FishBookOrigin(CustomEnchantment enchantment, int chance, int[] levelChances) {
        super(enchantment, chance, levelChances);
    }

    public static List<Pair<FishBookOrigin, Integer>> matchOrigins() {
        List<Pair<FishBookOrigin, Integer>> result = new ArrayList<>();
        for (EnchantmentOrigin elem : EnchantmentManager.ORIGINS) {
            if (elem instanceof FishBookOrigin origin) {
                int level = origin.getRandomLevel();
                if (level > 0)
                    result.add(new Pair<>(origin, level));
            }
        }
        return result;
    }
}
