package me.mfk1016.stadtserver.origin.trade;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import me.mfk1016.stadtserver.StadtServer;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

@RequiredArgsConstructor
public abstract class MerchantOrigin {

    public static final ArrayList<MerchantOrigin> ORIGINS = new ArrayList<>();

    public static Optional<MerchantOrigin> match(Merchant merchant, MerchantRecipe recipe) {
        Collections.shuffle(ORIGINS);
        for (MerchantOrigin origin : ORIGINS) {
            if (origin.isMatched(merchant, recipe))
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
        JsonArray spell = root.getAsJsonArray("wandering_trader:spell");
        for (JsonElement jsonElement : spell) {
            ORIGINS.add(WanderingTraderSpellOrigin.fromJson(jsonElement.getAsJsonObject(), gson));
        }
    }

    private final int chance;

    protected boolean isMatched(Merchant merchant, MerchantRecipe recipe) {
        return isApplicable(merchant, recipe)
                && StadtServer.RANDOM.nextInt(100) < chance;
    }

    protected abstract boolean isApplicable(Merchant merchant, MerchantRecipe recipe);

    public abstract MerchantRecipe applyOrigin(Merchant merchant, MerchantRecipe oldRecipe);
}
