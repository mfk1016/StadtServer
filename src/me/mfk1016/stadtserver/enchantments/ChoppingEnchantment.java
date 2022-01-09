package me.mfk1016.stadtserver.enchantments;

import me.mfk1016.stadtserver.EnchantmentManager;
import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.logic.MaterialTypes;
import me.mfk1016.stadtserver.logic.TreeChopper;
import me.mfk1016.stadtserver.origin.enchantment.*;
import me.mfk1016.stadtserver.origin.enchantment.VillagerTradeOrigin;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

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

    // extends stuff

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public int getStartLevel() {
        return 1;
    }

    @Override
    public EnchantmentTarget getItemTarget() {
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
    public boolean canEnchantItem(ItemStack item) {
        return item != null && MaterialTypes.isAxe(item.getType());
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

        // 5%/8% chance for Chopping at villager level 4/5
        int[] baseCosts = {30};
        result.add(new VillagerTradeOrigin(this, 5, levelChances, 4, baseCosts, 5));
        result.add(new VillagerTradeOrigin(this, 8, levelChances, 5, baseCosts, 5));

        // Boss Pillager/Vindicator: 25%/50% chance for chopping
        result.add(new BossMobBookOrigin(this, 25, levelChances, EntityType.PILLAGER, 1, World.Environment.NORMAL));
        result.add(new BossMobBookOrigin(this, 50, levelChances, EntityType.VINDICATOR, 1, World.Environment.NORMAL));

        // Loot Chest: 5% Chance in the overworld
        result.add(new LootChestOrigin(this, 5, levelChances, World.Environment.NORMAL));

        return result;
    }

    /* --- LISTENERS --- */

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
        if (!MaterialTypes.isLog(target.getType()))
            return;
        TreeChopper tc = new TreeChopper(player, axe, target, plugin);
        tc.chopTree();
    }
}
