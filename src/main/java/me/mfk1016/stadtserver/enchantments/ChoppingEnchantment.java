package me.mfk1016.stadtserver.enchantments;

import me.mfk1016.stadtserver.EnchantmentManager;
import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.logic.sorting.FarmCategories;
import me.mfk1016.stadtserver.logic.TreeChopper;
import me.mfk1016.stadtserver.logic.sorting.ToolCategories;
import me.mfk1016.stadtserver.origin.enchantment.*;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/*
    Chop trees with one swing.

    - Designated for axes
    - Uses durability (based on the amount of chopped blocks and unbreaking level)
    - Is incompatible with efficiency
 */
public class ChoppingEnchantment extends CustomEnchantment {

    public ChoppingEnchantment(StadtServer plugin) {
        super(plugin, "Chopping", "chopping");
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public int getStartLevel() {
        return 1;
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TOOL;
    }

    @Override
    public boolean isCursed() {
        return false;
    }

    @Override
    public boolean conflictsWith(Enchantment other) {
        return other.equals(Enchantment.DIG_SPEED);
    }

    @Override
    public boolean canEnchantItem(@NotNull ItemStack item) {
        return ToolCategories.isAxe(item.getType());
    }

    @Override
    public int getAnvilCost(ItemStack sacrifice, int level) {
        if (sacrifice.getType() == Material.ENCHANTED_BOOK) {
            return 3;
        } else {
            return 6;
        }
    }

    @Override
    public Set<EnchantmentOrigin> getOrigins() {
        Set<EnchantmentOrigin> result = new HashSet<>();
        int[] levelChances = {1};

        // 10% chance for Chopping when getting an enchanted book
        result.add(new FishBookOrigin(this, 10, levelChances));

        // Librarian: 3% chance for Chopping at level 5
        // Toolsmith: 7% chance for Chopping at level 5
        int[] baseCosts = {30};
        result.add(new VillagerTradeOrigin(this, 3, levelChances, Villager.Profession.LIBRARIAN, 5, baseCosts));
        result.add(new VillagerTradeOrigin(this, 7, levelChances, Villager.Profession.TOOLSMITH, 5, baseCosts));

        // Boss Pillager/Vindicator: 50% chance for chopping
        result.add(new BossMobBookOrigin(this, 50, levelChances, EntityType.PILLAGER, 1, World.Environment.NORMAL));
        result.add(new BossMobBookOrigin(this, 50, levelChances, EntityType.VINDICATOR, 1, World.Environment.NORMAL));

        // Loot Chest: 5% Chance in the overworld
        result.add(new LootChestOrigin(this, 5, levelChances, World.Environment.NORMAL));

        return result;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.SPECTATOR)
            return;
        ItemStack axe = player.getInventory().getItemInMainHand();
        if (!EnchantmentManager.isEnchantedWith(axe, this, 1))
            return;

        // Calculate the logs to chop
        Block target = event.getBlock();
        if (!FarmCategories.isLog(target.getType()))
            return;
        TreeChopper tc = new TreeChopper(player, axe, target, plugin);
        tc.chopTree();
    }
}
