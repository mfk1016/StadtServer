package me.mfk1016.stadtserver.origin.mob;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.enchantments.CustomEnchantment;
import me.mfk1016.stadtserver.enchantments.EnchantmentManager;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class BossMobEnchantmentOrigin extends BossMobOrigin {

    private final CustomEnchantment enchantment;
    private final int[] levelWeights;
    private final int levelWeightSum;

    public BossMobEnchantmentOrigin(int chance, EntityType entityType, int minlevel, World.Environment environment, CustomEnchantment enchantment, int[] levelWeights) {
        super(chance, entityType, minlevel, environment);
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
    public ItemStack applyOrigin() {
        ItemStack result = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantmentManager.enchantItem(result, enchantment, getEnchantmentLevel());
        return result;
    }

    public static BossMobEnchantmentOrigin fromJson(JsonObject object, Gson gson) {
        int chance = object.get("chance").getAsInt();
        EntityType entity = EntityType.valueOf(object.get("entity").getAsString());
        int level = object.get("minlevel").getAsInt();
        String enchantment = object.get("enchantment").getAsString();
        int[] weights = gson.fromJson(object.getAsJsonArray("weights"), int[].class);
        World.Environment env = World.Environment.valueOf(object.get("environment").getAsString());
        CustomEnchantment realEnchantment = null;
        for (CustomEnchantment ench : EnchantmentManager.ALL_ENCHANTMENTS) {
            if (ench.getKey().getKey().equals(enchantment))
                realEnchantment = ench;
        }
        assert realEnchantment != null;
        return new BossMobEnchantmentOrigin(chance, entity, level, env, realEnchantment, weights);
    }
}
