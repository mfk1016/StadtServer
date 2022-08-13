package me.mfk1016.stadtserver.origin.loot;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import me.mfk1016.stadtserver.StadtServer;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@RequiredArgsConstructor
public abstract class LootOrigin {

    public static final ArrayList<LootOrigin> ORIGINS = new ArrayList<>();

    public static List<LootOrigin> match(String type, Optional<ItemStack> target, World world) {
        Collections.shuffle(ORIGINS);
        List<LootOrigin> result = new ArrayList<>();
        for (LootOrigin origin : ORIGINS) {
            if (origin.isMatched(type, target, world))
                result.add(origin);
        }
        return result;
    }

    public static void initialize(JsonObject root, Gson gson) {
        ORIGINS.clear();
        JsonArray fish = root.getAsJsonArray("loot:fish");
        for (JsonElement jsonElement : fish) {
            ORIGINS.add(FishEnchantmentOrigin.fromJson(jsonElement.getAsJsonObject(), gson));
        }
        JsonArray piglin = root.getAsJsonArray("loot:piglin");
        for (JsonElement jsonElement : piglin) {
            ORIGINS.add(PiglinEnchantmentOrigin.fromJson(jsonElement.getAsJsonObject(), gson));
        }
        JsonArray chest = root.getAsJsonArray("loot:chest");
        for (JsonElement jsonElement : chest) {
            ORIGINS.add(ChestEnchantmentOrigin.fromJson(jsonElement.getAsJsonObject(), gson));
        }
    }

    private final String type;
    private final int chance;

    private boolean isMatched(String type, Optional<ItemStack> target, World world) {
        return this.type.equals(type)
                && isApplicable(target, world)
                && StadtServer.RANDOM.nextInt(100) < chance;
    }

    protected abstract boolean isApplicable(Optional<ItemStack> target, World world);

    public abstract ItemStack applyOrigin(Optional<ItemStack> target);
}
