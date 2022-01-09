package me.mfk1016.stadtserver.origin.enchantment;

import me.mfk1016.stadtserver.EnchantmentManager;
import me.mfk1016.stadtserver.enchantments.CustomEnchantment;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;

public class LootChestOrigin extends EnchantmentOrigin {

    private final World.Environment environment;

    public LootChestOrigin(CustomEnchantment enchantment, int chance, int[] levelChances, World.Environment env) {
        super(enchantment, chance, levelChances);
        environment = env;
    }

    public static Map<LootChestOrigin, Integer> matchOrigins(World world) {
        Map<LootChestOrigin, Integer> result = new HashMap<>();
        for (EnchantmentOrigin elem : EnchantmentManager.ORIGINS) {
            if (elem instanceof LootChestOrigin origin) {
                if (origin.canUseOrigin(world)) {
                    int level = origin.getRandomLevel();
                    if (level > 0)
                        result.put(origin, level);
                }
            }
        }
        return result;
    }

    private boolean canUseOrigin(World world) {
        return world.getEnvironment() == environment;
    }
}
