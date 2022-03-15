package me.mfk1016.stadtserver.origin.enchantment;

import me.mfk1016.stadtserver.enchantments.CustomEnchantment;
import me.mfk1016.stadtserver.enchantments.EnchantmentManager;
import me.mfk1016.stadtserver.util.Pair;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

public class LootChestOrigin extends EnchantmentOrigin {

    private final World.Environment environment;

    public LootChestOrigin(CustomEnchantment enchantment, int chance, int[] levelChances, World.Environment env) {
        super(enchantment, chance, levelChances);
        environment = env;
    }

    public static List<Pair<LootChestOrigin, Integer>> matchOrigins(World world) {
        List<Pair<LootChestOrigin, Integer>> result = new ArrayList<>();
        for (EnchantmentOrigin elem : EnchantmentManager.ORIGINS) {
            if (elem instanceof LootChestOrigin origin) {
                if (origin.canUseOrigin(world)) {
                    int level = origin.getRandomLevel();
                    if (level > 0)
                        result.add(new Pair<>(origin, level));
                }
            }
        }
        return result;
    }

    private boolean canUseOrigin(World world) {
        return world.getEnvironment() == environment;
    }
}
