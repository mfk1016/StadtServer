package me.mfk1016.stadtserver.origin.villager;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.mfk1016.stadtserver.StadtServer;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.MerchantRecipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

public abstract class VillagerOrigin {

    public static final ArrayList<VillagerOrigin> ORIGINS = new ArrayList<>();

    public static Optional<VillagerOrigin> match(Villager villager, MerchantRecipe recipe) {
        Collections.shuffle(ORIGINS);
        for (VillagerOrigin origin : ORIGINS) {
            if (origin.isMatched(villager, recipe))
                return Optional.of(origin);
        }
        return Optional.empty();
    }

    public static void initialize(JsonObject root, Gson gson) {
        ORIGINS.clear();
        JsonArray librarian = root.getAsJsonArray("villager:librarian");
        for (JsonElement jsonElement : librarian) {
            ORIGINS.addAll(LibrarianBookOrigin.fromJson(jsonElement.getAsJsonObject(), gson));
        }
        JsonArray worker = root.getAsJsonArray("villager:worker");
        for (JsonElement jsonElement : worker) {
            ORIGINS.addAll(WorkerEnchantmentOrigin.fromJson(jsonElement.getAsJsonObject(), gson));
        }
        JsonArray book = root.getAsJsonArray("villager:book");
        for (JsonElement jsonElement : book) {
            ORIGINS.addAll(VillagerBookOrigin.fromJson(jsonElement.getAsJsonObject(), gson));
        }
    }

    private final int chance;
    protected final int villagerLevel;

    public VillagerOrigin(int chance, int villagerLevel) {
        this.chance = chance;
        this.villagerLevel = villagerLevel;
    }

    private boolean isMatched(Villager villager, MerchantRecipe recipe) {
        return villager.getVillagerLevel() == villagerLevel
                && matchProfession(villager)
                && isApplicable(villager, recipe)
                && StadtServer.RANDOM.nextInt(100) < chance;
    }

    protected abstract boolean matchProfession(Villager villager);

    protected abstract boolean isApplicable(Villager villager, MerchantRecipe recipe);

    public abstract MerchantRecipe applyOrigin(Villager villager, MerchantRecipe oldRecipe);
}
