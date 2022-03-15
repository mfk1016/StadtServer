package me.mfk1016.stadtserver.origin.enchantment;

import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.enchantments.CustomEnchantment;
import me.mfk1016.stadtserver.enchantments.EnchantmentManager;
import me.mfk1016.stadtserver.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class PiglinTradeOrigin extends EnchantmentOrigin {

    public PiglinTradeOrigin(CustomEnchantment enchantment, int chance, int[] levelChances) {
        super(enchantment, chance, levelChances);
    }

    public static Pair<PiglinTradeOrigin, Integer> matchOrigins() {
        List<Pair<PiglinTradeOrigin, Integer>> results = new ArrayList<>();
        for (EnchantmentOrigin origin : EnchantmentManager.ORIGINS) {
            if (origin instanceof PiglinTradeOrigin obj) {
                int level = obj.getRandomLevel();
                if (level > 0)
                    results.add(new Pair<>(obj, level));
            }
        }
        if (results.isEmpty())
            return null;
        else
            return results.get(StadtServer.RANDOM.nextInt(results.size()));
    }

}
