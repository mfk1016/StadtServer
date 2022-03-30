package me.mfk1016.stadtserver.enchantments;

import me.mfk1016.stadtserver.logic.sorting.PluginCategories;
import me.mfk1016.stadtserver.logic.tree.TreeChopper;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/*
    Chop trees with one swing.

    - Designated for axes
    - Uses durability (based on the amount of chopped blocks and unbreaking level)
    - Is incompatible with efficiency
 */
public class ChoppingEnchantment extends CustomEnchantment {

    public ChoppingEnchantment() {
        super("Chopping", "chopping");
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
        return PluginCategories.isAxe(item.getType());
    }

    @Override
    public int getAnvilCost(ItemStack sacrifice, int level) {
        if (sacrifice.getType() == Material.ENCHANTED_BOOK) {
            return 3;
        } else {
            return 6;
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.SPECTATOR)
            return;
        if (player.getGameMode() != GameMode.CREATIVE && !player.isSneaking())
            return;
        ItemStack axe = player.getInventory().getItemInMainHand();
        if (!EnchantmentManager.isEnchantedWith(axe, this, 1))
            return;

        // Calculate the logs to chop
        Block target = event.getBlock();
        if (!PluginCategories.isLog(target.getType()))
            return;
        TreeChopper tc = new TreeChopper(player, axe, target);
        tc.chopTree();
    }
}
