package me.mfk1016.stadtserver.origin.enchantment;

import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.enchantments.CustomEnchantment;

public abstract class EnchantmentOrigin {

    protected final CustomEnchantment enchantment;
    protected final int chance;
    protected final int[] levelChances;
    protected int levelChancesSum = 0;

    public EnchantmentOrigin(CustomEnchantment enchantment, int chance, int[] levelChances) {
        this.enchantment = enchantment;
        this.chance = chance;
        this.levelChances = levelChances;
        for (int levelChance : levelChances) {
            levelChancesSum += levelChance;
        }
    }

    public CustomEnchantment getEnchantment() {
        return enchantment;
    }

    public int getRandomLevel() {
        if (StadtServer.RANDOM.nextInt(100) < chance) {
            int subchance = StadtServer.RANDOM.nextInt(levelChancesSum);
            int prevPart = 0;
            for (int i = 0; i < levelChances.length; i++) {
                if (prevPart == levelChancesSum)
                    break;
                if (subchance < prevPart + levelChances[i])
                    return i + 1;
                prevPart += levelChances[i];
            }
        }
        return 0;
    }
}
