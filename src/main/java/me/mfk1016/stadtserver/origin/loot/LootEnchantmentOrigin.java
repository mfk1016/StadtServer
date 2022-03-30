package me.mfk1016.stadtserver.origin.loot;

import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.enchantments.CustomEnchantment;
import me.mfk1016.stadtserver.enchantments.EnchantmentManager;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Optional;

public abstract class LootEnchantmentOrigin extends LootOrigin {

    protected final CustomEnchantment enchantment;
    protected final int[] levelWeights;
    private final int levelWeightSum;

    public LootEnchantmentOrigin(String type, int chance, CustomEnchantment enchantment, int[] levelWeights) {
        super(type, chance);
        this.enchantment = enchantment;
        this.levelWeights = levelWeights;
        assert levelWeights.length == enchantment.getMaxLevel();
        levelWeightSum = Arrays.stream(levelWeights).sum();
        assert levelWeightSum > 0;
    }

    private int getEnchantmentLevel() {
        int target = StadtServer.RANDOM.nextInt(levelWeightSum);
        for (int i = 0; i < levelWeights.length; i++) {
            target -= levelWeights[i];
            if (target < 0)
                return i + 1;
        }
        return levelWeights.length;
    }

    @Override
    protected boolean isApplicable(Optional<ItemStack> target, World world) {
        return target.isEmpty() || EnchantmentManager.canEnchantItem(target.get(), enchantment);
    }

    @Override
    public ItemStack applyOrigin(Optional<ItemStack> target) {
        ItemStack result = target.isEmpty() ? new ItemStack(Material.ENCHANTED_BOOK) : target.get();
        EnchantmentManager.enchantItem(result, enchantment, getEnchantmentLevel());
        return result;
    }
}
