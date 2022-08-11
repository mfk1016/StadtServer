package me.mfk1016.stadtserver.origin.mob;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.candlestore.CandleStoreManager;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public abstract class MobOrigin {

    public static final ArrayList<MobOrigin> ORIGINS = new ArrayList<>();

    public static List<MobOrigin> match(Entity entity) {
        List<MobOrigin> result = new ArrayList<>();
        for (MobOrigin origin : ORIGINS) {
            if (origin.isMatched(entity))
                result.add(origin);
        }
        return result;
    }

    public static void initialize(JsonObject root, Gson gson) {
        ORIGINS.clear();
        JsonArray enchantment = root.getAsJsonArray("mob:boss_enchantment");
        for (JsonElement jsonElement : enchantment) {
            ORIGINS.add(BossMobEnchantmentOrigin.fromJson(jsonElement.getAsJsonObject(), gson));
        }
        JsonArray loot = root.getAsJsonArray("mob:boss_loot");
        for (JsonElement jsonElement : loot) {
            ORIGINS.add(BossMobItemOrigin.fromJson(jsonElement.getAsJsonObject(), gson));
        }

        // Candle Tool: Witch Boss Level 4 (ritual), 25% chance
        BossMobItemOrigin candleToolOrigin = new BossMobItemOrigin(
                25, EntityType.WITCH, 4,
                World.Environment.NORMAL, CandleStoreManager.getCandleTool());
        ORIGINS.add(candleToolOrigin);
    }


    protected final int chance;
    protected final EntityType entityType;
    protected final World.Environment environment;

    public MobOrigin(int chance, EntityType entityType, World.Environment environment) {
        this.chance = chance;
        this.entityType = entityType;
        this.environment = environment;
    }

    protected boolean isMatched(Entity entity) {
        return entity.getType() == entityType
                && entity.getWorld().getEnvironment() == environment
                && isApplicable(entity)
                && StadtServer.RANDOM.nextInt(100) < chance;
    }

    protected abstract boolean isApplicable(Entity entity);

    public abstract ItemStack applyOrigin();
}
