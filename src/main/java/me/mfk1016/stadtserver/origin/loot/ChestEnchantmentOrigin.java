package me.mfk1016.stadtserver.origin.loot;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.mfk1016.stadtserver.enchantments.CustomEnchantment;
import me.mfk1016.stadtserver.enchantments.EnchantmentManager;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class ChestEnchantmentOrigin extends LootEnchantmentOrigin {

    public static final String TYPE = "enchantment.chest";

    private final World.Environment[] environment;

    public ChestEnchantmentOrigin(int chance, CustomEnchantment enchantment, int[] levelWeights, World.Environment[] environment) {
        super(TYPE, chance, enchantment, levelWeights);
        this.environment = environment;
    }

    @Override
    protected boolean isApplicable(Optional<ItemStack> target, World world) {
        for (World.Environment env : environment)
            if (world.getEnvironment() == env)
                return super.isApplicable(target, world);
        return false;
    }

    public static ChestEnchantmentOrigin fromJson(JsonObject object, Gson gson) {
        int chance = object.get("chance").getAsInt();
        String enchantment = object.get("enchantment").getAsString();
        int[] weights = gson.fromJson(object.getAsJsonArray("weights"), int[].class);
        String[] envs = gson.fromJson(object.getAsJsonArray("environment"), String[].class);
        CustomEnchantment realEnchantment = null;
        for (CustomEnchantment ench : EnchantmentManager.ALL_ENCHANTMENTS) {
            if (ench.getKey().getKey().equals(enchantment))
                realEnchantment = ench;
        }
        assert realEnchantment != null;
        World.Environment[] env = new World.Environment[envs.length];
        for (int i = 0; i < env.length; i++)
            env[i] = World.Environment.valueOf(envs[i]);
        return new ChestEnchantmentOrigin(chance, realEnchantment, weights, env);
    }
}
