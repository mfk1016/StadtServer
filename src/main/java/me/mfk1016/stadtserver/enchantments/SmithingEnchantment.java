package me.mfk1016.stadtserver.enchantments;

import me.mfk1016.stadtserver.origin.enchantment.*;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/*
    Repair items with a maximum level cost
    - The level cost limit depends on the enchantment level (1: 25 ... 5: 5)
    - Works only for repairing; repairing and enchanting costs the normal amount of levels

    Repair netherite tools/armor with netherite scrap; tools require up to 2 pieces, armor pieces require up to 3
    - TO FIX: ignores the stack size of netherite scrap
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
                item.setItemMeta(bookMeta);
                EnchantmentManager.enchantItem(item, EnchantmentManager.SMITHING, bookTargetLevel);
            }
        } else {
            if (EnchantmentManager.isEnchantedWith(item, Enchantment.MENDING)) {
                item.removeEnchantment(Enchantment.MENDING);
                EnchantmentManager.enchantItem(item, EnchantmentManager.SMITHING, itemTargetLevel);
            }
        }
    }

    public static void tryApplySmithingEnchantment(PrepareAnvilEvent event) {
        // Assumes, that the anvil state (items, enchantments, costs) is correct and it is only repairing
        AnvilInventory anvil = event.getInventory();
        ItemStack result = Objects.requireNonNull(event.getResult());

        if (!EnchantmentManager.getItemEnchantments(result).containsKey(EnchantmentManager.SMITHING))
            return;

        // Prevent a repair cost overflow
        if (result.getItemMeta() instanceof Repairable repairMeta && repairMeta.getRepairCost() > 63) {
            repairMeta.setRepairCost(63);
            result.setItemMeta((ItemMeta) repairMeta);
        }

        // Adjust the repair cost
        int smithingLevel = EnchantmentManager.getItemEnchantments(result).get(EnchantmentManager.SMITHING);
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
        return true;
    }

    @Override
    public int getAnvilCost(ItemStack sacrifice, int level) {
        if (sacrifice.getType() == Material.ENCHANTED_BOOK) {
            return level;
        } else {
            return 2 * level;
        }
    }

    @Override
    public Set<EnchantmentOrigin> getOrigins() {
        Set<EnchantmentOrigin> result = new HashSet<>();

        // Librarian: 3% at villager level 1+ for Smithing I / II with 2/1 distribution
        // Tool/Weaponsmith: 7% at villager level 3+ for Smithing II / III / IV with 4/2/1 distribution
        int[] levelChancesLibrarian = {2, 1, 0, 0, 0};
        int[] levelChancesSmith = {0, 4, 2, 1, 0};
        int[] baseCosts = {10, 20, 30, 40, 0};
        result.add(new VillagerTradeOrigin(this, 7, levelChancesSmith, Villager.Profession.TOOLSMITH, 3, baseCosts));
        result.add(new VillagerTradeOrigin(this, 7, levelChancesSmith, Villager.Profession.WEAPONSMITH, 3, baseCosts));
        result.add(new VillagerTradeOrigin(this, 3, levelChancesLibrarian, Villager.Profession.LIBRARIAN, 1, baseCosts));

        // Piglin: 4% chance for Smithing III / IV with 2/1 distribution
        int[] levelChancesPiglin = {0, 0, 2, 1, 0};
        result.add(new PiglinTradeOrigin(this, 4, levelChancesPiglin));

        // Loot chest: 10%/20%/25% chance for Smithing IV in overworld/nether/end
        int[] levelChancesLoot = {0, 0, 0, 1, 0};
        result.add(new LootChestOrigin(this, 10, levelChancesLoot, World.Environment.NORMAL));
        result.add(new LootChestOrigin(this, 20, levelChancesLoot, World.Environment.NETHER));
        result.add(new LootChestOrigin(this, 25, levelChancesLoot, World.Environment.THE_END));

        // Boss Piglin Brute: 50% chance for Smithing V in nether
        int[] levelChancesBoss = {0, 0, 0, 0, 1};
        result.add(new BossMobBookOrigin(this, 50, levelChancesBoss, EntityType.PIGLIN_BRUTE, 1, World.Environment.NETHER));

        return result;
    }
}
