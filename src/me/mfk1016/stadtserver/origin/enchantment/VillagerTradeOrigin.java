package me.mfk1016.stadtserver.origin.enchantment;

import me.mfk1016.stadtserver.EnchantmentManager;
import me.mfk1016.stadtserver.enchantments.CustomEnchantment;
import me.mfk1016.stadtserver.util.Pair;
import org.bukkit.entity.Villager;

import java.util.Random;

public class VillagerTradeOrigin extends EnchantmentOrigin {

    private final int[] baseCosts;
    private final int costDeviation;
    private final int villagerLevel;

    public VillagerTradeOrigin(CustomEnchantment enchantment, int chance, int[] levelChances, int villagerLevel, int[] baseCosts, int costDeviation) {
        super(enchantment, chance, levelChances);
        this.baseCosts = baseCosts;
        this.costDeviation = costDeviation;
        this.villagerLevel = villagerLevel;
    }

    public static Pair<VillagerTradeOrigin, Integer> matchOrigins(Villager villager) {
        for (EnchantmentOrigin elem : EnchantmentManager.ORIGINS) {
            if (elem instanceof VillagerTradeOrigin origin) {
                if (origin.canUseOrigin(villager)) {
                    int level = origin.getRandomLevel();
                    if (level > 0)
                        return new Pair<>(origin, level);
                }
            }
        }
        return null;
    }

    private boolean canUseOrigin(Villager villager) {
        return villager.getVillagerLevel() == villagerLevel;
    }

    public int getCostForLevel(Random random, int level) {
        if (level < 1 || baseCosts[level - 1] == 0)
            return 0;
        int baseCost = baseCosts[level - 1];
        int deviation = random.nextInt((costDeviation * 2) + 1) - costDeviation;
        return Math.max(1, Math.min(baseCost + deviation, 64));
    }
}
