package me.mfk1016.stadtserver.logic;

import me.mfk1016.stadtserver.EnchantmentManager;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.Arrays;
import java.util.Map;

public class InventorySorter {

    public static void sortInventory(Inventory inventory) {
        ItemStack[] contents = inventory.getContents();
        mergeStacks(contents);
        Arrays.sort(contents, InventorySorter::cmp);
        inventory.setContents(contents);
    }

    public static void sortDoubleChestInventory(Inventory left, Inventory right) {
        ItemStack[] leftContents = left.getContents();
        ItemStack[] rightContents = right.getContents();
        ItemStack[] completeContents = Arrays.copyOf(leftContents, leftContents.length + rightContents.length);
        System.arraycopy(rightContents, 0, completeContents, leftContents.length, rightContents.length);
        mergeStacks(completeContents);
        Arrays.sort(completeContents, InventorySorter::cmp);
        System.arraycopy(completeContents, 0, leftContents, 0, leftContents.length);
        System.arraycopy(completeContents, leftContents.length, rightContents, 0, rightContents.length);
        left.setContents(leftContents);
        right.setContents(rightContents);
    }

    // Merge similar stacks together as good as possible
    private static void mergeStacks(ItemStack[] contents) {
        for (int i = 0; i < contents.length; i++) {
            // Ignore empty slots
            if (contents[i] == null || contents[i].getType() == Material.AIR)
                continue;
            // Ignore maxed slots
            if (contents[i].getMaxStackSize() == contents[i].getAmount())
                continue;

            int missing = contents[i].getMaxStackSize() - contents[i].getAmount();
            for (int ti = i + 1; ti < contents.length; ti++) {
                // Ignore incompatible slots
                if (!contents[i].isSimilar(contents[ti]))
                    continue;
                // Ignore compatible maxed slots
                if (contents[ti].getMaxStackSize() == contents[ti].getAmount())
                    continue;

                if (contents[ti].getAmount() > missing) {
                    contents[i].setAmount(contents[i].getMaxStackSize());
                    contents[ti].setAmount(contents[ti].getAmount() - missing);
                    break;
                } else {
                    contents[i].setAmount(contents[i].getAmount() + contents[ti].getAmount());
                    missing -= contents[ti].getAmount();
                    contents[ti] = null;
                }
            }
        }
    }

    private static int cmp(ItemStack a, ItemStack b) {

        // Existence
        int result = cmpExistence(a, b);
        if (result != 0 || a == null) return result;

        // Type
        result = cmpType(a, b);
        if (result != 0) return result;

        // Lexical
        result = Integer.compare(a.getType().name().compareTo(b.getType().name()), 0);
        if (result != 0) return result;

        // Amount
        result = Integer.compare(b.getAmount(), a.getAmount());
        if (result != 0) return result;

        // Enchantments
        result = cmpEnchantments(a, b);
        if (result != 0) return result;

        // Damage
        result = cmpDamage(a, b);
        return result;
    }

    private static int cmpExistence(ItemStack a, ItemStack b) {

        boolean notExistsA = a == null || a.getType() == Material.AIR;
        boolean notExistsB = b == null || b.getType() == Material.AIR;

        // null / air > alles andere
        if (notExistsA && notExistsB) {
            return 0;
        } else if (notExistsA) {
            return 1;
        } else if (notExistsB) {
            return -1;
        } else {
            return 0;
        }
    }

    private static int cmpType(ItemStack a, ItemStack b) {

        if (a.getType().isBlock() && b.getType().isBlock()) {
            return 0;
        } else if (a.getType().isBlock()) {
            return -1;
        } else if (b.getType().isBlock()) {
            return 1;

        } else if (MaterialTypes.isTool(a.getType()) && MaterialTypes.isTool(b.getType())) {
            return 0;
        } else if (MaterialTypes.isTool(a.getType())) {
            return -1;
        } else if (MaterialTypes.isTool(b.getType())) {
            return 1;

        } else if (MaterialTypes.isArmor(a.getType()) && MaterialTypes.isArmor(b.getType())) {
            return 0;
        } else if (MaterialTypes.isArmor(a.getType())) {
            return -1;
        } else if (MaterialTypes.isArmor(b.getType())) {
            return 1;

        } else if (a.getType().isEdible() && b.getType().isEdible()) {
            return 0;
        } else if (a.getType().isEdible()) {
            return -1;
        } else if (b.getType().isEdible()) {
            return 1;

        } else {
            return 0;
        }
    }

    private static int cmpEnchantments(ItemStack a, ItemStack b) {

        // enchanted < not enchanted
        // more enchantments < less enchantments
        Map<Enchantment, Integer> enchantmentsA = EnchantmentManager.getItemEnchantments(a);
        Map<Enchantment, Integer> enchantmentsB = EnchantmentManager.getItemEnchantments(b);

        if (enchantmentsA.isEmpty() && enchantmentsB.isEmpty()) {
            return 0;
        } else if (enchantmentsA.isEmpty()) {
            return 1;
        } else if (enchantmentsB.isEmpty()) {
            return -1;
        } else {
            return Integer.compare(enchantmentsB.size(), enchantmentsA.size());
        }
    }

    private static int cmpDamage(ItemStack a, ItemStack b) {

        boolean hasDamageA = a.hasItemMeta() && a.getItemMeta() instanceof Damageable;
        boolean hasDamageB = b.hasItemMeta() && b.getItemMeta() instanceof Damageable;

        if (hasDamageA && hasDamageB) {
            Damageable dmgA = (Damageable) a.getItemMeta();
            Damageable dmgB = (Damageable) b.getItemMeta();
            return Integer.compare(dmgA.getDamage(), dmgB.getDamage());
        } else {
            return 0;
        }
    }
}
