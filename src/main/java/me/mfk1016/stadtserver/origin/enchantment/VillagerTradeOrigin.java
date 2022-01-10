package me.mfk1016.stadtserver.origin.enchantment;

import me.mfk1016.stadtserver.EnchantmentManager;
import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.enchantments.CustomEnchantment;
import me.mfk1016.stadtserver.util.Pair;
import org.bukkit.entity.Villager;

import java.util.ArrayList;
import java.util.List;

public class VillagerTradeOrigin extends EnchantmentOrigin {

    private final Villager.Profession profession;
    private final int minLevel;
    private final int[] baseCosts;

    public VillagerTradeOrigin(CustomEnchantment enchantment, int chance, int[] levelChances, Villager.Profession profession, int minLevel, int[] baseCosts) {
        super(enchantment, chance, levelChances);
        this.profession = profession;
        this.minLevel = minLevel;
        this.baseCosts = baseCosts;
    }

    public static Pair<VillagerTradeOrigin, Integer> matchOrigins(Villager villager) {
        List<Pair<VillagerTradeOrigin, Integer>> results = new ArrayList<>();
        for (EnchantmentOrigin elem : EnchantmentManager.ORIGINS) {
            if (elem instanceof VillagerTradeOrigin origin) {
                if (origin.isMatched(villager)) {
                    int level = origin.getRandomLevel();
                    if (level > 0)
                        results.add(new Pair<>(origin, level));
                }
            }
        }
        if (results.isEmpty())
            return null;
        else
            return results.get(StadtServer.RANDOM.nextInt(results.size()));
    }

    protected boolean isMatched(Villager villager) {
        return villager.getVillagerLevel() >= minLevel && villager.getProfession() == profession;
    }

    public int getCostForLevel(int level) {
        if (level < 1 || baseCosts[level - 1] == 0)
            return 0;
        int baseCost = baseCosts[level - 1];
        // deviation = base costs / 5 rounded down
        int costDeviation = baseCost / 5;
        int deviation = StadtServer.RANDOM.nextInt((costDeviation * 2) + 1) - costDeviation;
        return Math.max(1, Math.min(baseCost + deviation, 64));
    }
}
