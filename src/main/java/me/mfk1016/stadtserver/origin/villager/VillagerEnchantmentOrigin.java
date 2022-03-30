package me.mfk1016.stadtserver.origin.villager;

import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.enchantments.CustomEnchantment;

import java.util.Arrays;

public abstract class VillagerEnchantmentOrigin extends VillagerOrigin {

    protected final CustomEnchantment enchantment;
    protected final int[] levelWeights;
    private final int levelWeightSum;

    public VillagerEnchantmentOrigin(int chance, int villagerLevel, CustomEnchantment enchantment, int[] levelWeights) {
        super(chance, villagerLevel);
        this.enchantment = enchantment;
        this.levelWeights = levelWeights;
        assert levelWeights.length == enchantment.getMaxLevel();
        levelWeightSum = Arrays.stream(levelWeights).sum();
        assert levelWeightSum > 0;
    }

    protected int getEnchantmentLevel() {
        int target = StadtServer.RANDOM.nextInt(levelWeightSum);
        for (int i = 0; i < levelWeights.length; i++) {
            target -= levelWeights[i];
            if (target < 0)
                return i + 1;
        }
        return levelWeights.length;
    }
}
