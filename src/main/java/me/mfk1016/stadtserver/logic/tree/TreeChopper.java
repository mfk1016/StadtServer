package me.mfk1016.stadtserver.logic.tree;

import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.util.Keys;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;

public class TreeChopper {

    private final Plugin plugin;
    private final ItemStack axe;

    private final HashSet<Block> checkedBlocks = new HashSet<>();
    private final LinkedList<Block> blocksToCheck = new LinkedList<>();
    private final LinkedList<Block> treeLogs = new LinkedList<>();
    private final Material logType;
    private final int maxLogs;
    private final boolean survivalMode;
    private Iterator<Block> tmpIterator;
    private int chopSpeed = 2;

    public TreeChopper(Player player, ItemStack axe, Block init, StadtServer plugin) {
        this.plugin = plugin;
        this.axe = axe;
        blocksToCheck.add(init);
        logType = init.getType();
        maxLogs = Math.min(Math.abs(plugin.getConfig().getInt(Keys.CONFIG_MAX_LOGS)), 10000);
        survivalMode = player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE;
    }

    public void chopTree() {

        // Gather all log blocks
        collectLogs();

        // Get the unbreaking level of the axe
        int unbreakingFactor = 1;
        if (axe.getEnchantments().containsKey(Enchantment.DURABILITY)) {
            unbreakingFactor += axe.getEnchantmentLevel(Enchantment.DURABILITY);
        }

        // Damage the axe
        if (survivalMode) {
            Damageable itemdmg = Objects.requireNonNull((Damageable) axe.getItemMeta());
            int durRemain = axe.getType().getMaxDurability() - itemdmg.getDamage();
            int durNeeded = treeLogs.size() / unbreakingFactor;
            if (durRemain <= 10 || durNeeded > 2 * durRemain) {
                treeLogs.clear();
            } else {
                itemdmg.setDamage(Math.min(itemdmg.getDamage() + durNeeded, axe.getType().getMaxDurability() - 5));
                axe.setItemMeta(itemdmg);
            }
        }

        // Perform the chopping
        performChop();
    }

    private void collectLogs() {

        int validLeavesFound = 0;
        boolean blocked = false;
        while (blocksToCheck.size() > 0 && treeLogs.size() < (maxLogs + 10) && !blocked) {
            Block current = blocksToCheck.getFirst();
            for (Block relative : getUpwardRelatives(current)) {
                if (relative != null && !checkedBlocks.contains(relative)) {
                    if (isLog(relative) || isWood(relative)) {
                        treeLogs.addLast(relative);
                        blocksToCheck.addLast(relative);
                    } else if (validLeavesFound < 5 && isLeaves(relative) && relative.getBlockData() instanceof Leaves leaves) {
                        if (!leaves.isPersistent())
                            validLeavesFound++;
                    } else if (relative.getType() == Material.BOOKSHELF) {
                        blocked = true;
                    }
                    checkedBlocks.add(relative);
                }
            }
            blocksToCheck.removeFirst();
        }

        if (validLeavesFound < 5 || blocked || treeLogs.size() > maxLogs)
            treeLogs.clear();

        blocksToCheck.clear();
        checkedBlocks.clear();
        tmpIterator = treeLogs.iterator();
        if (treeLogs.size() > maxLogs / 2)
            chopSpeed = 4;
    }

    private void performChop() {
        // Chop the wood (delayed)
        BukkitRunnable runner = new BukkitRunnable() {
            @Override
            public void run() {
                if (treeLogs.size() == 0)
                    this.cancel();
                int broken = 0;
                while (tmpIterator.hasNext() && broken < chopSpeed) {
                    if (survivalMode)
                        tmpIterator.next().breakNaturally(axe);
                    else
                        tmpIterator.next().setType(Material.AIR);
                    broken++;
                    tmpIterator.remove();
                }
            }
        };
        runner.runTaskTimer(plugin, 1, 1);
    }

    private Block[] getUpwardRelatives(Block block) {
        Block[] result = new Block[17];
        int pos = 0;
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                for (int y = 0; y <= 1; y++) {
                    if (x == 0 && y == 0 && z == 0)
                        continue;
                    result[pos] = block.getRelative(x, y, z);
                    pos++;
                }
            }
        }
        return result;
    }

    private boolean isLog(Block block) {
        return block.getType() == logType;
    }

    private boolean isLeaves(Block block) {
        Material toCheck = block.getType();
        return switch (logType) {
            case ACACIA_LOG -> toCheck == Material.ACACIA_LEAVES;
            case BIRCH_LOG -> toCheck == Material.BIRCH_LEAVES;
            case CRIMSON_STEM -> toCheck == Material.NETHER_WART_BLOCK;
            case WARPED_STEM -> toCheck == Material.WARPED_WART_BLOCK;
            case DARK_OAK_LOG -> toCheck == Material.DARK_OAK_LEAVES;
            case OAK_LOG -> toCheck == Material.OAK_LEAVES || toCheck == Material.AZALEA_LEAVES;
            case JUNGLE_LOG -> toCheck == Material.JUNGLE_LEAVES;
            case SPRUCE_LOG -> toCheck == Material.SPRUCE_LEAVES;
            default -> toCheck == Material.DIAMOND_BLOCK;
        };
    }

    private boolean isWood(Block block) {
        return switch (logType) {
            case ACACIA_LOG -> block.getType() == Material.ACACIA_WOOD;
            case BIRCH_LOG -> block.getType() == Material.BIRCH_WOOD;
            case CRIMSON_STEM -> block.getType() == Material.CRIMSON_HYPHAE;
            case WARPED_STEM -> block.getType() == Material.WARPED_HYPHAE;
            case DARK_OAK_LOG -> block.getType() == Material.DARK_OAK_WOOD;
            case OAK_LOG -> block.getType() == Material.OAK_WOOD;
            case JUNGLE_LOG -> block.getType() == Material.JUNGLE_WOOD;
            case SPRUCE_LOG -> block.getType() == Material.SPRUCE_WOOD;
            default -> false;
        };
    }
}
