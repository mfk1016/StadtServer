package me.mfk1016.stadtserver.origin.trade;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.enchantments.CustomEnchantment;
import me.mfk1016.stadtserver.enchantments.EnchantmentManager;
import org.bukkit.Material;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;

import java.util.ArrayList;
import java.util.List;

public class VillagerBookOrigin extends VillagerEnchantmentOrigin {

    private final Villager.Profession profession;
    private final int[] levelCosts;
    private final int[] levelDeviation;

    public VillagerBookOrigin(int chance, Villager.Profession profession, int villagerLevel, CustomEnchantment enchantment, int[] levelWeights, int[] levelCosts, int[] levelDeviation) {
        super(chance, villagerLevel, enchantment, levelWeights);
        this.profession = profession;
        this.levelCosts = levelCosts;
        this.levelDeviation = levelDeviation;
        assert levelCosts.length == levelWeights.length;
        assert levelDeviation.length == levelWeights.length;
    }

    @Override
    protected boolean matchProfession(Villager villager) {
        return villager.getProfession() == profession;
    }

    @Override
    protected boolean isApplicable(Merchant merchant, MerchantRecipe recipe) {
        return true;
    }

    @Override
    public MerchantRecipe applyOrigin(Merchant merchant, MerchantRecipe oldRecipe) {
        ItemStack newBook = new ItemStack(Material.ENCHANTED_BOOK);
        int enchLevel = getEnchantmentLevel();
        EnchantmentManager.enchantItem(newBook, enchantment, enchLevel);
        int maxUses = 6 + (StadtServer.RANDOM.nextInt(3) * 3);
        MerchantRecipe result = new MerchantRecipe(newBook, 0, maxUses, true,
                25, 0.2f);
        int emeraldCost = Math.min(levelCosts[enchLevel - 1] + StadtServer.RANDOM.nextInt(levelDeviation[enchLevel - 1]), 64);
        result.setIngredients(List.of(new ItemStack(Material.EMERALD, emeraldCost), new ItemStack(Material.BOOK)));
        return result;
    }

    public static List<VillagerBookOrigin> fromJson(JsonObject object, Gson gson) {
        int chance = object.get("chance").getAsInt();
        Villager.Profession profession = Villager.Profession.valueOf(object.get("profession").getAsString());
        int[] levels = gson.fromJson(object.getAsJsonArray("level"), int[].class);
        String enchantment = object.get("enchantment").getAsString();
        int[] weights = gson.fromJson(object.getAsJsonArray("weights"), int[].class);
        int[] costs = gson.fromJson(object.getAsJsonArray("costs"), int[].class);
        int[] devs = gson.fromJson(object.getAsJsonArray("devs"), int[].class);
        CustomEnchantment realEnchantment = null;
        for (CustomEnchantment ench : EnchantmentManager.ALL_ENCHANTMENTS) {
            if (ench.getKey().getKey().equals(enchantment))
                realEnchantment = ench;
        }
        assert realEnchantment != null;
        List<VillagerBookOrigin> result = new ArrayList<>();
        for (int l : levels) {
            result.add(new VillagerBookOrigin(chance, profession, l, realEnchantment, weights, costs, devs));
        }
        return result;
    }
}
