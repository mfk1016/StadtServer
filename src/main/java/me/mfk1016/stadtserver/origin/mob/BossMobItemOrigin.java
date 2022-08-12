package me.mfk1016.stadtserver.origin.mob;

import com.google.gson.JsonObject;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import static me.mfk1016.stadtserver.util.Functions.stackEmpty;

public class BossMobItemOrigin extends BossMobOrigin {

    private final ItemStack loot;

    public BossMobItemOrigin(int chance, EntityType entityType, int minlevel, World.Environment environment, ItemStack loot) {
        super(chance, entityType, minlevel, environment);
        this.loot = loot;
        assert !stackEmpty(loot);
    }

    @Override
    public ItemStack applyOrigin() {
        return loot.clone();
    }

    public static BossMobItemOrigin fromJson(JsonObject object) {
        int chance = object.get("chance").getAsInt();
        EntityType entity = EntityType.valueOf(object.get("entity").getAsString());
        int level = object.get("minlevel").getAsInt();
        World.Environment env = World.Environment.valueOf(object.get("environment").getAsString());
        Material mat = Material.valueOf(object.get("material").getAsString());
        int amount = object.get("amount").getAsInt();
        ItemStack item = new ItemStack(mat, amount);
        return new BossMobItemOrigin(chance, entity, level, env, item);
    }
}
