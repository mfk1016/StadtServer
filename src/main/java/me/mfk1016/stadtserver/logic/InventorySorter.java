package me.mfk1016.stadtserver.logic;

import me.mfk1016.stadtserver.EnchantmentManager;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static me.mfk1016.stadtserver.util.Functions.stackEmpty;

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
        result = cmpLexical(a, b);
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
        boolean notExistsA = stackEmpty(a);
        boolean notExistsB = stackEmpty(b);
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
        if (MaterialTypes.isTool(a.getType()) && MaterialTypes.isTool(b.getType())) {
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

    private static int cmpLexical(ItemStack a, ItemStack b) {
        StringBuilder as = new StringBuilder(a.getType().name().toLowerCase());
        StringBuilder bs = new StringBuilder(b.getType().name().toLowerCase());
        boolean aCatFound = false;
        boolean bCatFound = false;
        if (as.toString().equals("repeater") || as.toString().equals("comparator")) {
            as.insert(0, "redstonez");
            aCatFound = true;
        }
        if (bs.toString().equals("repeater") || bs.toString().equals("comparator")) {
            bs.insert(0, "redstonez");
            bCatFound = true;
        }
        for (var cat : categories) {
            if (!aCatFound && as.toString().contains(cat)) {
                as.insert(0, cat);
                aCatFound = true;
            }
            if (!bCatFound && bs.toString().contains(cat)) {
                bs.insert(0, cat);
                bCatFound = true;
            }
            if (aCatFound && bCatFound)
                break;
        }
        return Integer.compare(as.toString().compareTo(bs.toString()), 0);
    }

    private static int cmpEnchantments(ItemStack a, ItemStack b) {

        // enchanted < not enchanted
        // more enchantments < less enchantments
        Map<Enchantment, Integer> enchantmentsA = EnchantmentManager.getItemEnchantments(a);
        Map<Enchantment, Integer> enchantmentsB = EnchantmentManager.getItemEnchantments(b);
        String nameA = "";
        String nameB = "";
        if (!enchantmentsA.isEmpty()) {
            nameA = enchantmentsA.keySet().stream().toList().get(0).getKey().getKey();
        }
        if (!enchantmentsB.isEmpty()) {
            nameB = enchantmentsB.keySet().stream().toList().get(0).getKey().getKey();
        }
        int nameCmp = Integer.compare(nameA.compareTo(nameB), 0);

        if (enchantmentsA.isEmpty() && enchantmentsB.isEmpty()) {
            return 0;
        } else if (enchantmentsA.isEmpty()) {
            return 1;
        } else if (enchantmentsB.isEmpty()) {
            return -1;
        } else if (nameCmp != 0) {
            return nameCmp;
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

    private static final List<String> categories = new ArrayList<>();

    static {
        categories.add("dark_oak");
        categories.add("oak");
        categories.add("birch");
        categories.add("spruce");
        categories.add("acacia");
        categories.add("jungle");
        categories.add("crimson");
        categories.add("nether_wart");
        categories.add("warped");
        categories.add("azalea");
        categories.add("dirt");
        categories.add("honey");
        categories.add("chorus");
        categories.add("carrot");
        categories.add("potato");
        categories.add("glowstone");
        categories.add("melon");
        categories.add("pumpkin");
        categories.add("slime");
        categories.add("piston");
        categories.add("rail");
        categories.add("minecart");

        categories.add("lingering_potion");
        categories.add("splash_potion");
        categories.add("potion");

        categories.add("dye");
        categories.add("bed");
        categories.add("banner");
        categories.add("candle");
        categories.add("carpet");
        categories.add("glass_pane");
        categories.add("glass");
        categories.add("wool");
        categories.add("shulker_box");
        categories.add("glazed_terracotta");
        categories.add("terracotta");
        categories.add("concrete");

        categories.add("netherite");
        categories.add("diamond");
        categories.add("gold");
        categories.add("iron");
        categories.add("copper");
        categories.add("amethyst");
        categories.add("lapis");
        categories.add("coal");
        categories.add("quartz");
        categories.add("granite");
        categories.add("diorite");
        categories.add("andesite");
        categories.add("redstone");
        categories.add("red_sandstone");
        categories.add("sandstone");
        categories.add("end_stone");
        categories.add("cobblestone");
        categories.add("basalt");
        categories.add("blackstone");
        categories.add("prismarine");
        categories.add("nether_brick");
        categories.add("stone_brick");
        categories.add("stone");
        categories.add("brick");
    }
}
