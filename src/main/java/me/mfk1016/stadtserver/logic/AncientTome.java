package me.mfk1016.stadtserver.logic;

import me.mfk1016.stadtserver.EnchantmentManager;
import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.util.Keys;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static me.mfk1016.stadtserver.util.Functions.stackEmpty;

public class AncientTome {

    private static final List<Enchantment> probabilities = new ArrayList<>();

    static {
        addEnchantment(Enchantment.ARROW_DAMAGE, 3);
        addEnchantment(Enchantment.ARROW_KNOCKBACK, 1);
        addEnchantment(Enchantment.DAMAGE_ALL, 3);
        addEnchantment(Enchantment.DAMAGE_ARTHROPODS, 2);
        addEnchantment(Enchantment.DAMAGE_UNDEAD, 3);
        addEnchantment(Enchantment.DIG_SPEED, 3);
        addEnchantment(Enchantment.DURABILITY, 5);
        addEnchantment(Enchantment.FIRE_ASPECT, 1);
        addEnchantment(Enchantment.IMPALING, 3);
        addEnchantment(Enchantment.KNOCKBACK, 3);
        addEnchantment(Enchantment.LOOT_BONUS_BLOCKS, 1);
        addEnchantment(Enchantment.LOOT_BONUS_MOBS, 1);
        addEnchantment(Enchantment.LOYALTY, 1);
        addEnchantment(Enchantment.LUCK, 1);
        addEnchantment(Enchantment.LURE, 3);
        addEnchantment(Enchantment.OXYGEN, 3);
        addEnchantment(Enchantment.PIERCING, 3);
        addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3);
        addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 3);
        addEnchantment(Enchantment.PROTECTION_FALL, 3);
        addEnchantment(Enchantment.PROTECTION_FIRE, 3);
        addEnchantment(Enchantment.PROTECTION_PROJECTILE, 3);
        addEnchantment(Enchantment.QUICK_CHARGE, 1);
        addEnchantment(Enchantment.RIPTIDE, 3);
        addEnchantment(Enchantment.SWEEPING_EDGE, 3);
        addEnchantment(Enchantment.THORNS, 1);

        addEnchantment(EnchantmentManager.SMITHING, 1);
        addEnchantment(EnchantmentManager.FARMING, 1);
        addEnchantment(EnchantmentManager.EAGLE_EYE, 2);
    }

    public static boolean isAncientTome(ItemStack stack) {
        if (stackEmpty(stack))
            return false;
        if (!(stack.getItemMeta() instanceof EnchantmentStorageMeta meta))
            return false;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        return pdc.getOrDefault(Keys.IS_ANCIENT_TOME, PersistentDataType.INTEGER, 0) == 1;
    }

    public static ItemStack createAncientTome(Enchantment enchantment) {
        ItemStack result = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) Objects.requireNonNull(result.getItemMeta());
        meta.addStoredEnchant(enchantment, enchantment.getMaxLevel(), true);
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(Keys.IS_ANCIENT_TOME, PersistentDataType.INTEGER, 1);
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Ancient Tome");
        result.setItemMeta(meta);
        return result;
    }

    public static ItemStack randomAncientTome() {
        int rolled = StadtServer.RANDOM.nextInt(probabilities.size());
        return createAncientTome(probabilities.get(rolled));
    }

    private static void addEnchantment(Enchantment enchantment, int weight) {
        for (int i = 0; i < weight; i++)
            probabilities.add(enchantment);
    }

}
