package me.mfk1016.stadtserver.logic;

import com.destroystokyo.paper.event.inventory.PrepareResultEvent;
import me.mfk1016.stadtserver.enchantments.CustomEnchantment;
import me.mfk1016.stadtserver.enchantments.EnchantmentManager;
import me.mfk1016.stadtserver.enchantments.SmithingEnchantment;
import me.mfk1016.stadtserver.logic.sorting.PluginCategories;
import me.mfk1016.stadtserver.spells.CustomSpell;
import me.mfk1016.stadtserver.util.Keys;
import me.mfk1016.stadtserver.util.Pair;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.GrindstoneInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.persistence.PersistentDataContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static me.mfk1016.stadtserver.enchantments.EnchantmentManager.*;
import static me.mfk1016.stadtserver.util.Functions.stackEmpty;

public class AnvilLogic {

    private static final List<Pair<Enchantment, Function<Boolean, Integer>>> vanillaCosts = new ArrayList<>();

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

    public static void fixAnvilResult(PrepareAnvilEvent event) {
        AnvilInventory anvil = event.getInventory();
        ItemStack target = anvil.getFirstItem();
        ItemStack sacrifice = anvil.getSecondItem();

        if (stackEmpty(target))
            // No action without a target
            return;

        boolean isRenamed = false;
        boolean isRepaired = false;
        boolean isEnchanted = false;
        boolean isAncientTome = false;

        String newName = anvil.getRenameText();
        ItemMeta targetMeta = target.getItemMeta();
        if (targetMeta != null) {
            isRenamed = newName != null && !newName.isBlank();
        }
        if (targetMeta instanceof Repairable && targetMeta instanceof Damageable damageable && !stackEmpty(sacrifice)) {
            isRepaired = damageable.hasDamage() && AnvilLogic.isRepairMaterial(target, sacrifice.getType());
        }
        if (!stackEmpty(sacrifice) && !EnchantmentManager.getItemEnchantments(sacrifice).isEmpty()) {
            isEnchanted = sacrifice.getType() == target.getType() || sacrifice.getType() == Material.ENCHANTED_BOOK;
            isAncientTome = target.getType() != Material.ENCHANTED_BOOK && AncientTome.isAncientTome(sacrifice);
        }
        if (!isRenamed && !isRepaired && !isEnchanted)
            return;

        anvil.setRepairCost(0);
        event.setResult(target.clone());
        if (isEnchanted) {
            boolean validResult = doAnvilEnchant(event);
            if (!validResult && !isRepaired && !isRenamed) {
                event.setResult(null);
                return;
            }
        }
        if (isRepaired)
            doAnvilRepair(event);
        if (isRenamed)
            doAnvilRename(event);

        // Add base costs and increase anvil penalty
        int baseCost = 0;
        if (isEnchanted && !isAncientTome) {
            Repairable targetRepairData = (Repairable) target.getItemMeta();
            baseCost += targetRepairData.getRepairCost();
            Repairable sacrificeRepairData = (Repairable) Objects.requireNonNull(sacrifice.getItemMeta());
            baseCost += sacrificeRepairData.getRepairCost();
        } else if (isRepaired) {
            Repairable targetRepairData = (Repairable) target.getItemMeta();
            baseCost += targetRepairData.getRepairCost();
        }
        anvil.setRepairCost(anvil.getRepairCost() + baseCost);
        ItemStack result = Objects.requireNonNull(event.getResult());
        if (!isAncientTome && (isEnchanted || isRepaired))
            AnvilLogic.increaseAnvilUses(result, sacrifice);
        event.setResult(result);

        if (isRepaired && !isEnchanted)
            SmithingEnchantment.tryApplySmithingEnchantment(event);
        anvil.setMaximumRepairCost(100);
    }

    public static boolean doAnvilEnchant(PrepareAnvilEvent event) {
        AnvilInventory anvil = event.getInventory();
        ItemStack sacrifice = Objects.requireNonNull(anvil.getSecondItem());
        ItemStack result = Objects.requireNonNull(event.getResult());
        int enchantCost = 0;
        boolean isAncientTome = result.getType() != Material.ENCHANTED_BOOK && AncientTome.isAncientTome(sacrifice);
        boolean hasResult = false;

        Map<Enchantment, Integer> enchMapSac = getItemEnchantments(sacrifice);
        for (var enchEntrySac : enchMapSac.entrySet()) {
            Enchantment sacEnch = enchEntrySac.getKey();
            int sacEnchLevel = enchEntrySac.getValue();

            // Ignore spells
            if (sacEnch instanceof CustomSpell)
                continue;

            // Ignore conflicting enchantments and add one level for every conflicting
            if (!sacEnch.canEnchantItem(result) && (result.getType() != Material.ENCHANTED_BOOK)) {
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

            // Ancient tome: only valid if the item is enchanted with the same enchantment + level
            if (isAncientTome) {
                int tarEnchLevel = getItemEnchantments(result).getOrDefault(sacEnch, 0);
                if (tarEnchLevel == sacEnch.getMaxLevel()) {
                    disenchantItem(result, sacEnch);
                    enchantItem(result, sacEnch, tarEnchLevel + 1);
                    enchantCost += sacrificeAnvilCost(sacEnch, tarEnchLevel + 1, sacrifice);
                    hasResult = true;
                }
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
            if (tarEnchLevel == sacEnchLevel && tarEnchLevel < sacEnch.getMaxLevel())
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
        ItemStack sacrifice = Objects.requireNonNull(anvil.getSecondItem());
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
            int durabilityPerItem = (int) Math.ceil((double) result.getType().getMaxDurability() / 4D);
            // double repair for netherite scrap
            if (sacrifice.getType() == Material.NETHERITE_SCRAP)
                durabilityPerItem *= 2;
            int neededItems = (int) Math.ceil((double) resultDamage.getDamage() / (double) durabilityPerItem);
            int usedItems = Math.min(neededItems, sacrifice.getAmount());
            if (!anvil.getViewers().isEmpty())
                anvil.setRepairCostAmount(usedItems);
            toSub = usedItems * durabilityPerItem;
            repairCost += usedItems;
        }

        anvil.setRepairCost(repairCost);
        resultDamage.setDamage(Math.max(0, resultDamage.getDamage() - toSub));

        // Remove "Repaired by Ritual" tag
        PersistentDataContainer pdc = resultDamage.getPersistentDataContainer();
        if (pdc.has(Keys.IS_RITUAL_REPAIRED)) {
            pdc.remove(Keys.IS_RITUAL_REPAIRED);
        }

        result.setItemMeta(resultDamage);
        EnchantmentManager.updateItemLore(result);
        event.setResult(result);
    }

    public static void doAnvilRename(PrepareAnvilEvent event) {
        AnvilInventory anvil = event.getInventory();
        ItemStack result = Objects.requireNonNull(event.getResult());

        String newName = anvil.getRenameText() == null ? "" : anvil.getRenameText();
        ItemMeta resultMeta = Objects.requireNonNull(result.getItemMeta());
        resultMeta.displayName(Component.text(newName));
        result.setItemMeta(resultMeta);

        anvil.setRepairCost(anvil.getRepairCost() + 1);
        event.setResult(result);
    }

    public static void increaseAnvilUses(ItemStack result, ItemStack sacrifice) {
        Repairable metaResult = (Repairable) Objects.requireNonNull(result.getItemMeta());
        Repairable metaSacrifice = (Repairable) Objects.requireNonNull(sacrifice.getItemMeta());
        int penalty = Math.max(metaResult.getRepairCost(), metaSacrifice.getRepairCost()) + 1;
        metaResult.setRepairCost((penalty * 2) - 1);
        result.setItemMeta(metaResult);
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

    public static boolean isRepairMaterial(ItemStack item, Material repair) {
        if (item.getType() == repair)
            return true;
        return switch (item.getType()) {
            case NETHERITE_AXE, NETHERITE_HOE, NETHERITE_PICKAXE, NETHERITE_SHOVEL, NETHERITE_SWORD,
                    NETHERITE_BOOTS, NETHERITE_CHESTPLATE, NETHERITE_HELMET, NETHERITE_LEGGINGS ->
                    repair == Material.NETHERITE_SCRAP;
            case DIAMOND_AXE, DIAMOND_HOE, DIAMOND_PICKAXE, DIAMOND_SHOVEL, DIAMOND_SWORD,
                    DIAMOND_BOOTS, DIAMOND_CHESTPLATE, DIAMOND_HELMET, DIAMOND_LEGGINGS -> repair == Material.DIAMOND;
            case GOLDEN_AXE, GOLDEN_HOE, GOLDEN_PICKAXE, GOLDEN_SHOVEL, GOLDEN_SWORD,
                    GOLDEN_BOOTS, GOLDEN_CHESTPLATE, GOLDEN_LEGGINGS, GOLDEN_HELMET -> repair == Material.GOLD_INGOT;
            case IRON_AXE, IRON_HOE, IRON_PICKAXE, IRON_SHOVEL, IRON_SWORD, SHEARS,
                    IRON_BOOTS, IRON_CHESTPLATE, IRON_LEGGINGS, IRON_HELMET,
                    CHAINMAIL_BOOTS, CHAINMAIL_CHESTPLATE, CHAINMAIL_HELMET, CHAINMAIL_LEGGINGS ->
                    repair == Material.IRON_INGOT;
            case STONE_AXE, STONE_HOE, STONE_PICKAXE, STONE_SHOVEL, STONE_SWORD -> switch (repair) {
                case COBBLESTONE, COBBLED_DEEPSLATE, BLACKSTONE, GRANITE,
                        DIORITE, ANDESITE -> true;
                default -> false;
            };
            case LEATHER_BOOTS, LEATHER_CHESTPLATE, LEATHER_LEGGINGS, LEATHER_HELMET -> repair == Material.LEATHER;
            case WOODEN_AXE, WOODEN_HOE, WOODEN_PICKAXE, WOODEN_SHOVEL, WOODEN_SWORD, SHIELD ->
                    PluginCategories.isPlanks(repair);
            case TURTLE_HELMET -> repair == Material.SCUTE;
            case ELYTRA -> repair == Material.PHANTOM_MEMBRANE;
            case TRIDENT -> repair == Material.NAUTILUS_SHELL;
            default -> false;
        };
    }

    /* --- GRINDSTONE SUPPORT --- */

    public static void fixGrindstoneResult(PrepareResultEvent event) {
        if (!(event.getInventory() instanceof GrindstoneInventory))
            return;
        if (event.getResult() == null)
            return;

        // The result is technically correct, but there may be lore left
        ItemStack result = event.getResult();
        ItemMeta meta = result.getItemMeta();
        if (meta == null || !meta.hasLore())
            return;
        meta.lore(null);
        result.setItemMeta(meta);
        event.setResult(result);
    }
}
