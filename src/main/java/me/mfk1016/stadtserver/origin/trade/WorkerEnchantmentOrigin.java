package me.mfk1016.stadtserver.origin.trade;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.mfk1016.stadtserver.enchantments.CustomEnchantment;
import me.mfk1016.stadtserver.enchantments.EnchantmentManager;
import org.bukkit.Material;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;

import java.util.ArrayList;
import java.util.List;

import static me.mfk1016.stadtserver.util.Functions.copyRecipe;

public class WorkerEnchantmentOrigin extends VillagerEnchantmentOrigin {

    private final int[] addedCosts;

    public WorkerEnchantmentOrigin(int chance, int villagerLevel, CustomEnchantment enchantment, int[] levelWeights, int[] addedCosts) {
        super(chance, villagerLevel, enchantment, levelWeights);
        this.addedCosts = addedCosts;
        assert levelWeights.length == addedCosts.length;
    }

    @Override
    protected boolean matchProfession(Villager villager) {
        return switch (villager.getProfession()) {
            case ARMORER, TOOLSMITH, WEAPONSMITH, FLETCHER, FISHERMAN -> true;
            default -> false;
        };
    }

    @Override
    protected boolean isApplicable(Merchant merchant, MerchantRecipe recipe) {
        return EnchantmentManager.canEnchantItem(recipe.getResult(), enchantment);
    }

    @Override
    public MerchantRecipe applyOrigin(Merchant merchant, MerchantRecipe oldRecipe) {
        ItemStack newItem = oldRecipe.getResult();
        int enchLevel = getEnchantmentLevel();
        EnchantmentManager.enchantItem(newItem, enchantment, enchLevel);
        MerchantRecipe result = copyRecipe(oldRecipe, newItem);
        int cost = oldRecipe.getIngredients().get(0).getAmount() + addedCosts[enchLevel - 1];
        result.setIngredients(List.of(new ItemStack(Material.EMERALD, cost)));
        return result;
    }

    public static List<WorkerEnchantmentOrigin> fromJson(JsonObject object, Gson gson) {
        int chance = object.get("chance").getAsInt();
        int[] levels = gson.fromJson(object.getAsJsonArray("level"), int[].class);
        String enchantment = object.get("enchantment").getAsString();
        int[] weights = gson.fromJson(object.getAsJsonArray("weights"), int[].class);
        int[] added = gson.fromJson(object.getAsJsonArray("added"), int[].class);
        CustomEnchantment realEnchantment = null;
        for (CustomEnchantment ench : EnchantmentManager.ALL_ENCHANTMENTS) {
            if (ench.getKey().getKey().equals(enchantment))
                realEnchantment = ench;
        }
        assert realEnchantment != null;
        List<WorkerEnchantmentOrigin> result = new ArrayList<>();
        for (int l : levels) {
            result.add(new WorkerEnchantmentOrigin(chance, l, realEnchantment, weights, added));
        }
        return result;
    }
}
