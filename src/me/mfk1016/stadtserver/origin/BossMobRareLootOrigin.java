package me.mfk1016.stadtserver.origin;

import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.util.Keys;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static org.bukkit.World.Environment.*;
import static org.bukkit.entity.EntityType.*;

@SuppressWarnings("ClassCanBeRecord")
public class BossMobRareLootOrigin {

    private static final List<BossMobRareLootOrigin> ORIGINS = new ArrayList<>();

    static {
        // super rare -> 5%
        addOrigin(ENDERMAN, THE_END, 2, 5, new ItemStack(Material.ELYTRA));
        addOrigin(CREEPER, NORMAL, 2, 5, new ItemStack(Material.TOTEM_OF_UNDYING));
        addOrigin(WITHER_SKELETON, NETHER, 1, 5, new ItemStack(Material.NETHERITE_SCRAP, 2));
        addOrigin(ZOMBIFIED_PIGLIN, NETHER, 1, 5, new ItemStack(Material.NETHER_WART, 4));
        addOrigin(PIGLIN, NETHER, 1, 5, new ItemStack(Material.NETHER_WART, 4));

        // quite rare -> 10%
        addOrigin(WITCH, NORMAL, 1, 10, new ItemStack(Material.BREWING_STAND, 1));
        addOrigin(DROWNED, NORMAL, 2, 10, new ItemStack(Material.TRIDENT));
        addOrigin(ZOMBIE, NORMAL, 1, 10, new ItemStack(Material.IRON_BLOCK));
        addOrigin(ZOMBIE_VILLAGER, NORMAL, 1, 10, new ItemStack(Material.IRON_BLOCK, 2));
        addOrigin(PILLAGER, NORMAL, 1, 10, new ItemStack(Material.AMETHYST_SHARD, 4));
        addOrigin(VINDICATOR, NORMAL, 1, 10, new ItemStack(Material.AMETHYST_SHARD, 8));

        // rare -> 25%
        addOrigin(STRAY, NORMAL, 1, 25, new ItemStack(Material.BLUE_ICE, 8));
        addOrigin(BLAZE, NETHER, 1, 25, new ItemStack(Material.MAGMA_CREAM, 4));
        addOrigin(SKELETON, NORMAL, 1, 25, new ItemStack(Material.SPECTRAL_ARROW, 4));
        addOrigin(SKELETON, NETHER, 1, 25, new ItemStack(Material.SPECTRAL_ARROW, 8));
        addOrigin(PIGLIN, NETHER, 2, 25, new ItemStack(Material.GOLD_BLOCK));

        // common -> 50%
        addOrigin(SPIDER, NORMAL, 1, 50, new ItemStack(Material.COBWEB, 8));
        addOrigin(BLAZE, NETHER, 1, 50, new ItemStack(Material.BLAZE_POWDER, 8));
        addOrigin(HUSK, NORMAL, 1, 50, new ItemStack(Material.RED_SAND, 32));
        addOrigin(GUARDIAN, NORMAL, 1, 50, new ItemStack(Material.WET_SPONGE, 2));
        addOrigin(ZOMBIFIED_PIGLIN, NETHER, 1, 50, new ItemStack(Material.GOLD_INGOT, 2));

        // bonus -> 100%
        addOrigin(ENDERMAN, NORMAL, 1, 100, new ItemStack(Material.ENDER_PEARL));
        addOrigin(ENDERMAN, NETHER, 1, 100, new ItemStack(Material.ENDER_PEARL));
        addOrigin(ENDERMAN, THE_END, 1, 100, new ItemStack(Material.ENDER_PEARL));
        addOrigin(SHULKER, THE_END, 1, 100, new ItemStack(Material.SHULKER_SHELL));
        addOrigin(CREEPER, NORMAL, 2, 100, new ItemStack(Material.TNT, 2));
        addOrigin(WITHER_SKELETON, NETHER, 1, 100, new ItemStack(Material.WITHER_SKELETON_SKULL));

        // level 4
        addOrigin(EVOKER, NORMAL, 4, 100, new ItemStack(Material.SPAWNER));
        addOrigin(WITCH, NORMAL, 4, 100, new ItemStack(Material.DRAGON_BREATH, 4));
    }

    private final EntityType type;
    private final Environment environment;
    private final int levelReq;
    private final int chance;
    private final ItemStack item;

    private BossMobRareLootOrigin(EntityType type, Environment environment, int levelReq, int chance, ItemStack item) {
        this.type = type;
        this.environment = environment;
        this.levelReq = levelReq;
        this.chance = chance;
        this.item = item;
    }

    private static void addOrigin(EntityType t, Environment e, int levelReq, int chance, ItemStack i) {
        ORIGINS.add(new BossMobRareLootOrigin(t, e, levelReq, chance, i));
    }

    public static List<ItemStack> matchOrigins(Entity entity) {
        EntityType typ = entity.getType();
        Environment env = entity.getWorld().getEnvironment();
        int level = entity.getMetadata(Keys.IS_BOSS).get(0).asInt();
        List<ItemStack> result = new ArrayList<>();
        for (BossMobRareLootOrigin origin : ORIGINS) {
            if (origin.type != typ || origin.environment != env || level < origin.levelReq)
                continue;
            if (StadtServer.RANDOM.nextInt(100) < origin.chance * (level - origin.levelReq + 1))
                result.add(origin.item.clone());
        }
        return result;
    }
}
