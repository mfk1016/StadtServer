package me.mfk1016.stadtserver.enchantments;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/*
    Repair items with a limited level cost
    - The level cost limit depends on the enchantment level (1: 25 ... 5: 5, (ancient) 6: 3)
    - Works only for repairing; repairing and enchanting costs the normal amount of levels
    - Completely replaces Mending
 */
public class SmithingEnchantment extends CustomEnchantment {

    public SmithingEnchantment() {
        super("Smithing", "smithing");
    }

    public static int repairCostLimit(int enchantmentLevel) {
        if (enchantmentLevel > 5)
            return 3;
        return 25 - ((enchantmentLevel - 1) * 5);
    }

    public static void replaceMending(ItemStack item, int itemTargetLevel, int bookTargetLevel) {
        if (item == null)
            return;
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof EnchantmentStorageMeta bookMeta) {
            if (bookMeta.hasStoredEnchant(Enchantment.MENDING)) {
                bookMeta.removeStoredEnchant(Enchantment.MENDING);
                bookMeta.addStoredEnchant(EnchantmentManager.SMITHING, bookTargetLevel, true);
                item.setItemMeta(bookMeta);
            }
        } else {
            if (EnchantmentManager.isEnchantedWith(item, Enchantment.MENDING)) {
                item.removeEnchantment(Enchantment.MENDING);
                item.addUnsafeEnchantment(EnchantmentManager.SMITHING, itemTargetLevel);
            }
        }
    }

    public static void tryApplySmithingEnchantment(PrepareAnvilEvent event) {
        // Assumes, that the anvil state (items, enchantments, costs) is correct, and it is only repairing
        AnvilInventory anvil = event.getInventory();
        ItemStack result = Objects.requireNonNull(event.getResult());

        int smithingLevel = result.getEnchantmentLevel(EnchantmentManager.SMITHING);
        if (smithingLevel == 0)
            return;

        // Prevent a repair cost overflow
        if (result.getItemMeta() instanceof Repairable repairMeta && repairMeta.getRepairCost() > 63) {
            repairMeta.setRepairCost(63);
            result.setItemMeta(repairMeta);
        }

        // Adjust the repair cost
        int newRepairCost = Math.min(anvil.getRepairCost(), repairCostLimit(smithingLevel));
        anvil.setRepairCost(newRepairCost);
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    public int getStartLevel() {
        return 1;
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.BREAKABLE;
    }

    @Override
    public boolean isCursed() {
        return false;
    }

    @Override
    public boolean conflictsWith(Enchantment other) {
        return other.equals(Enchantment.MENDING);
    }

    @Override
    public boolean canEnchantItem(@NotNull ItemStack item) {
        return EnchantmentTarget.BREAKABLE.includes(item);
    }

    @Override
    public int getAnvilCost(ItemStack sacrifice, int level) {
        if (sacrifice.getType() == Material.ENCHANTED_BOOK) {
            return level;
        } else {
            return 2 * level;
        }
    }
}
