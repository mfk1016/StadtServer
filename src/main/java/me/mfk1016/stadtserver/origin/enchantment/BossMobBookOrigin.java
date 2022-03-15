package me.mfk1016.stadtserver.origin.enchantment;

import me.mfk1016.stadtserver.enchantments.CustomEnchantment;
import me.mfk1016.stadtserver.enchantments.EnchantmentManager;
import me.mfk1016.stadtserver.util.Keys;
import me.mfk1016.stadtserver.util.Pair;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BossMobBookOrigin extends EnchantmentOrigin {

    private final EntityType type;
    private final int levelReq;
    private final World.Environment environment;

    public BossMobBookOrigin(CustomEnchantment enchantment, int chance, int[] levelChances, EntityType type, int levelReq, World.Environment environment) {
        super(enchantment, chance, levelChances);
        this.type = type;
        this.levelReq = levelReq;
        this.environment = environment;
    }

    public static List<Pair<BossMobBookOrigin, Integer>> matchOrigins(Entity entity, World world) {
        List<Pair<BossMobBookOrigin, Integer>> result = new ArrayList<>();
        int level = entity.getMetadata(Keys.IS_BOSS).get(0).asInt();
        for (EnchantmentOrigin origin : EnchantmentManager.ORIGINS) {
            if (origin instanceof BossMobBookOrigin obj) {
                if (obj.type != entity.getType() || obj.environment != world.getEnvironment() || level < obj.levelReq)
                    continue;

                // 1 try per level above the requirement; take the highest
                int enchlevel = 0;
                int tries = level - obj.levelReq + 1;
                while (tries > 0) {
                    int newlevel = obj.getRandomLevel();
                    if (newlevel > enchlevel)
                        enchlevel = newlevel;
                    tries--;
                }

                if (enchlevel > 0)
                    result.add(new Pair<>(obj, enchlevel));
            }
        }
        return result;
    }

}
