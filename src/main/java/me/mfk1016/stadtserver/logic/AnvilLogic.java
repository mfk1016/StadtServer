package me.mfk1016.stadtserver.logic;

import me.mfk1016.stadtserver.enchantments.CustomEnchantment;
import me.mfk1016.stadtserver.util.Pair;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static me.mfk1016.stadtserver.EnchantmentManager.*;

public class AnvilLogic {

    private static final List<Pair<Enchantment, Function<Boolean, Integer>>> vanillaCosts = new ArrayList<>();
    private static final org.bukkit.util.Vector TO_BLOCK_CENTER = new Vector(0.5, 0.1, 0.5);

    public static boolean doAnvilEnchant(PrepareAnvilEvent event) {
        AnvilInventory anvil = event.getInventory();
        ItemStack sacrifice = Objects.requireNonNull(anvil.getItem(1));
        ItemStack result = Objects.requireNonNull(event.getResult());
        int enchantCost = 0;
        boolean isAncientTome = result.getType() != Material.ENCHANTED_BOOK && AncientTome.isAncientTome(sacrifice);
        boolean hasResult = false;

        Map<Enchantment, Integer> enchMapSac = getItemEnchantments(sacrifice);
        for (var enchEntrySac : enchMapSac.entrySet()) {
            Enchantment sacEnch = enchEntrySac.getKey();
            int sacEnchLevel = enchEntrySac.getValue();

            // Ignore conflicting enchantments and add one level for every conflicting
            if (!sacEnch.canEnchantItem(result)) {
                continue;
            }
            boolean conflicts = false;
            for (var tarEnch : getItemEnchantments(result).keySet()) {
                if (tarEnch.equals(sacEnch))
                    continue;
                if (tarEnch.conflictsWith(sacEnch) || sacEnch.conflictsWith(tarEnch))
                    conflicts = true;
            }
            if (conflicts) {
                enchantCost++;
                continue;
            }

            // Add new enchantments
            if (!isEnchantedWith(result, sacEnch)) {
                enchantItem(result, sacEnch, sacEnchLevel);
                enchantCost += sacrificeAnvilCost(sacEnch, sacEnchLevel, sacrifice);
                hasResult = true;
                continue;
            }

            // Merge existing enchantments
            int tarEnchLevel = getItemEnchantments(result).getOrDefault(sacEnch, 0);
            int resEnchLevel = Math.max(tarEnchLevel, sacEnchLevel);
            if (tarEnchLevel == sacEnchLevel && (tarEnchLevel < sacEnch.getMaxLevel() || isAncientTome))
                resEnchLevel += 1;
            disenchantItem(result, sacEnch);
            enchantItem(result, sacEnch, resEnchLevel);
            enchantCost += sacrificeAnvilCost(sacEnch, resEnchLevel, sacrifice);
            if (resEnchLevel > tarEnchLevel)
                hasResult = true;
        }

        anvil.setRepairCost(anvil.getRepairCost() + enchantCost);
        return hasResult;
    }


    public static void doAnvilRepair(PrepareAnvilEvent event) {
        AnvilInventory anvil = event.getInventory();
        ItemStack sacrifice = Objects.requireNonNull(anvil.getItem(1));
        ItemStack result = Objects.requireNonNull(event.getResult());
        Damageable resultDamage = (Damageable) Objects.requireNonNull(result.getItemMeta());
        int toSub;
        int repairCost = anvil.getRepairCost();

        if (result.getType() == sacrifice.getType()) {
            // Repair by merging items -> add sacrifice damage + 12%
            Damageable sacrificeMeta = (Damageable) Objects.requireNonNull(sacrifice.getItemMeta());
            toSub = (int) ((result.getType().getMaxDurability() - sacrificeMeta.getDamage()) * 1.12);
            repairCost += 2;
        } else {
            // Calculate the amount of items needed for repairing
            int durabilityPerItem = (int) Math.ceil((double)result.getType().getMaxDurability() / 4D);
            int neededItems = (int) Math.ceil((double) resultDamage.getDamage() / (double) durabilityPerItem);
            int usedItems = Math.min(neededItems, sacrifice.getAmount());
            if (usedItems < sacrifice.getAmount() && !anvil.getViewers().isEmpty()) {
                ItemStack toDrop = new ItemStack(sacrifice.getType(), sacrifice.getAmount() - usedItems);
                Location targetLocation = Objects.requireNonNull(anvil.getLocation()).add(0, 1, 0).add(TO_BLOCK_CENTER);
                Item dropped = anvil.getViewers().get(0).getWorld().dropItem(targetLocation, toDrop);
                dropped.setVelocity(new Vector());
                sacrifice.setAmount(usedItems);
                anvil.setItem(1, sacrifice);
            }

            toSub = usedItems * durabilityPerItem;
            repairCost += usedItems;
        }

        anvil.setRepairCost(repairCost);
        resultDamage.setDamage(Math.max(0, resultDamage.getDamage() - toSub));
        result.setItemMeta(resultDamage);
        event.setResult(result);
    }


    public static void doAnvilRename(PrepareAnvilEvent event) {
        AnvilInventory anvil = event.getInventory();
        ItemStack result = Objects.requireNonNull(event.getResult());

        String newName = anvil.getRenameText();
        ItemMeta resultMeta = Objects.requireNonNull(result.getItemMeta());
        resultMeta.setDisplayName(newName);
        result.setItemMeta(resultMeta);

        anvil.setRepairCost(anvil.getRepairCost() + 1);
        event.setResult(result);
    }

    public static void increaseAnvilUses(ItemStack item) {
        Repairable meta = (Repairable) Objects.requireNonNull(item.getItemMeta());
        int penalty = meta.getRepairCost() + 1;
        meta.setRepairCost((penalty * 2) - 1);
        item.setItemMeta((ItemMeta) meta);
    }

    public static int sacrificeAnvilCost(Enchantment enchantment, int level, ItemStack sacrifice) {
        if (AncientTome.isAncientTome(sacrifice))
            return 30;
        if (enchantment instanceof CustomEnchantment customEnchantment)
            return customEnchantment.getAnvilCost(sacrifice, level);
        boolean isBook = sacrifice.getType() == Material.ENCHANTED_BOOK;
        for (Pair<Enchantment, Function<Boolean, Integer>> p : vanillaCosts) {
            if (enchantment.equals(p._1)) {
                return p._2.apply(isBook) * level;
            }
        }
        return 0;
    }

    /* --- REPAIR MATERIAL --- */

    public static boolean isRepairMaterial(ItemStack item, Material repair) {
        if (item.getType() == repair)
            return true;
        return switch (item.getType()) {
            case NETHERITE_AXE, NETHERITE_HOE, NETHERITE_PICKAXE, NETHERITE_SHOVEL, NETHERITE_SWORD,
                    NETHERITE_BOOTS, NETHERITE_CHESTPLATE, NETHERITE_HELMET, NETHERITE_LEGGINGS -> repair == Material.NETHERITE_SCRAP;
            case DIAMOND_AXE, DIAMOND_HOE, DIAMOND_PICKAXE, DIAMOND_SHOVEL, DIAMOND_SWORD,
                    DIAMOND_BOOTS, DIAMOND_CHESTPLATE, DIAMOND_HELMET, DIAMOND_LEGGINGS -> repair == Material.DIAMOND;
            case GOLDEN_AXE, GOLDEN_HOE, GOLDEN_PICKAXE, GOLDEN_SHOVEL, GOLDEN_SWORD,
                    GOLDEN_BOOTS, GOLDEN_CHESTPLATE, GOLDEN_LEGGINGS, GOLDEN_HELMET -> repair == Material.GOLD_INGOT;
            case IRON_AXE, IRON_HOE, IRON_PICKAXE, IRON_SHOVEL, IRON_SWORD, SHEARS,
                    IRON_BOOTS, IRON_CHESTPLATE, IRON_LEGGINGS, IRON_HELMET,
                    CHAINMAIL_BOOTS, CHAINMAIL_CHESTPLATE, CHAINMAIL_HELMET, CHAINMAIL_LEGGINGS -> repair == Material.IRON_INGOT;
            case STONE_AXE, STONE_HOE, STONE_PICKAXE, STONE_SHOVEL, STONE_SWORD -> MaterialTypes.isAnvilStone(repair);
            case LEATHER_BOOTS, LEATHER_CHESTPLATE, LEATHER_LEGGINGS, LEATHER_HELMET -> repair == Material.LEATHER;
            case WOODEN_AXE, WOODEN_HOE, WOODEN_PICKAXE, WOODEN_SHOVEL, WOODEN_SWORD, SHIELD -> MaterialTypes.isPlanks(repair);
            case TURTLE_HELMET -> repair == Material.SCUTE;
            case ELYTRA -> repair == Material.PHANTOM_MEMBRANE;
            case TRIDENT -> repair == Material.NAUTILUS_SHELL;
            default -> false;
        };
    }

    static {
        vanillaCosts.add(new Pair<>(Enchantment.ARROW_DAMAGE, (isBook) -> (1)));
        vanillaCosts.add(new Pair<>(Enchantment.ARROW_FIRE, (isBook) -> (isBook ? 2 : 4)));
        vanillaCosts.add(new Pair<>(Enchantment.ARROW_INFINITE, (isBook) -> (isBook ? 4 : 8)));
        vanillaCosts.add(new Pair<>(Enchantment.ARROW_KNOCKBACK, (isBook) -> (isBook ? 2 : 4)));
        vanillaCosts.add(new Pair<>(Enchantment.BINDING_CURSE, (isBook) -> (isBook ? 4 : 8)));
        vanillaCosts.add(new Pair<>(Enchantment.CHANNELING, (isBook) -> (isBook ? 4 : 8)));
        vanillaCosts.add(new Pair<>(Enchantment.DAMAGE_ALL, (isBook) -> (1)));
        vanillaCosts.add(new Pair<>(Enchantment.DAMAGE_ARTHROPODS, (isBook) -> (isBook ? 1 : 2)));
        vanillaCosts.add(new Pair<>(Enchantment.DAMAGE_UNDEAD, (isBook) -> (isBook ? 1 : 2)));
        vanillaCosts.add(new Pair<>(Enchantment.DEPTH_STRIDER, (isBook) -> (isBook ? 2 : 4)));
        vanillaCosts.add(new Pair<>(Enchantment.DIG_SPEED, (isBook) -> (1)));
        vanillaCosts.add(new Pair<>(Enchantment.DURABILITY, (isBook) -> (isBook ? 1 : 2)));
        vanillaCosts.add(new Pair<>(Enchantment.FIRE_ASPECT, (isBook) -> (isBook ? 2 : 4)));
        vanillaCosts.add(new Pair<>(Enchantment.FROST_WALKER, (isBook) -> (isBook ? 2 : 4)));
        vanillaCosts.add(new Pair<>(Enchantment.IMPALING, (isBook) -> (isBook ? 2 : 4)));
        vanillaCosts.add(new Pair<>(Enchantment.KNOCKBACK, (isBook) -> (isBook ? 1 : 2)));
        vanillaCosts.add(new Pair<>(Enchantment.LOOT_BONUS_BLOCKS, (isBook) -> (isBook ? 2 : 4)));
        vanillaCosts.add(new Pair<>(Enchantment.LOOT_BONUS_MOBS, (isBook) -> (isBook ? 2 : 4)));
        vanillaCosts.add(new Pair<>(Enchantment.LOYALTY, (isBook) -> (1)));
        vanillaCosts.add(new Pair<>(Enchantment.LUCK, (isBook) -> (isBook ? 2 : 4)));
        vanillaCosts.add(new Pair<>(Enchantment.LURE, (isBook) -> (isBook ? 2 : 4)));
        vanillaCosts.add(new Pair<>(Enchantment.MENDING, (isBook) -> (isBook ? 2 : 4)));
        vanillaCosts.add(new Pair<>(Enchantment.MULTISHOT, (isBook) -> (isBook ? 2 : 4)));
        vanillaCosts.add(new Pair<>(Enchantment.OXYGEN, (isBook) -> (isBook ? 2 : 4)));
        vanillaCosts.add(new Pair<>(Enchantment.PIERCING, (isBook) -> (1)));
        vanillaCosts.add(new Pair<>(Enchantment.PROTECTION_ENVIRONMENTAL, (isBook) -> (1)));
        vanillaCosts.add(new Pair<>(Enchantment.PROTECTION_EXPLOSIONS, (isBook) -> (isBook ? 2 : 4)));
        vanillaCosts.add(new Pair<>(Enchantment.PROTECTION_FALL, (isBook) -> (isBook ? 1 : 2)));
        vanillaCosts.add(new Pair<>(Enchantment.PROTECTION_FIRE, (isBook) -> (isBook ? 1 : 2)));
        vanillaCosts.add(new Pair<>(Enchantment.PROTECTION_PROJECTILE, (isBook) -> (isBook ? 1 : 2)));
        vanillaCosts.add(new Pair<>(Enchantment.QUICK_CHARGE, (isBook) -> (isBook ? 1 : 2)));
        vanillaCosts.add(new Pair<>(Enchantment.RIPTIDE, (isBook) -> (isBook ? 2 : 4)));
        vanillaCosts.add(new Pair<>(Enchantment.SILK_TOUCH, (isBook) -> (isBook ? 4 : 8)));
        vanillaCosts.add(new Pair<>(Enchantment.SOUL_SPEED, (isBook) -> (isBook ? 4 : 8)));
        vanillaCosts.add(new Pair<>(Enchantment.SWEEPING_EDGE, (isBook) -> (isBook ? 2 : 4)));
        vanillaCosts.add(new Pair<>(Enchantment.THORNS, (isBook) -> (isBook ? 4 : 8)));
        vanillaCosts.add(new Pair<>(Enchantment.VANISHING_CURSE, (isBook) -> (isBook ? 4 : 8)));
        vanillaCosts.add(new Pair<>(Enchantment.WATER_WORKER, (isBook) -> (isBook ? 2 : 4)));
    }
}
