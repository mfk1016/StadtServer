package me.mfk1016.stadtserver.listener;

import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.logic.sorting.PluginCategories;
import me.mfk1016.stadtserver.logic.tree.*;
import me.mfk1016.stadtserver.util.Pair;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.UUID;

public class TreeListener implements Listener {

    private static final HashSet<UUID> growingPlayers = new HashSet<>();

    @EventHandler(priority = EventPriority.NORMAL)
    public void onTreeGrow(StructureGrowEvent event) {
        Player player = event.getPlayer();
        Pair<Integer, Block> sizebase = getTreeShape(event.getLocation().getBlock());
        if (sizebase == null) // not grown from sapling
            return;
        TreeGenerator gen = matchGenerator(sizebase._2, sizebase._2.getType(), sizebase._1);
        if (gen == null)
            return;

        if (player != null) {
            if (growingPlayers.contains(player.getUniqueId())) {
                event.setCancelled(true);
                return;
            } else {
                growingPlayers.add(player.getUniqueId());
            }
        }
        if (gen.isEnoughSpace()) {
            BukkitRunnable runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    gen.generateTree();
                    if (player != null)
                        growingPlayers.remove(player.getUniqueId());
                }
            };
            event.setCancelled(true);
            runnable.runTaskLater(StadtServer.getInstance(), 1L);
        } else if (player != null)
            growingPlayers.remove(player.getUniqueId());
    }

    private static Pair<Integer, Block> getTreeShape(Block test) {
        if (!PluginCategories.isSapling(test.getType()))
            return null;
        Material sap = test.getType();
        Block nwBase = test;
        // Go to the north west edge
        int steps = 0;
        while (steps < 20) {
            if (nwBase.getRelative(BlockFace.NORTH).getType() == sap) {
                nwBase = nwBase.getRelative(BlockFace.NORTH);
                steps++;
            } else if (nwBase.getRelative(BlockFace.WEST).getType() == sap) {
                nwBase = nwBase.getRelative(BlockFace.WEST);
                steps++;
            } else {
                steps = 20;
            }
        }
        // Find out the tree size
        int size = 1;
        // target = size to check - 1 (aka relatives)
        for (int target = 1; target <= 4; target++) {
            for (int x = 0; x < target; x++) {
                if (nwBase.getRelative(x, 0, target).getType() != sap)
                    return new Pair<>(size, nwBase);
                if (nwBase.getRelative(target, 0, x).getType() != sap)
                    return new Pair<>(size, nwBase);
            }
            if (nwBase.getRelative(target, 0, target).getType() != sap)
                return new Pair<>(size, nwBase);
            size++;
        }
        return new Pair<>(size, nwBase);
    }

    private TreeGenerator matchGenerator(Block root, Material saplingType, int size) {
        switch (saplingType) {
            case OAK_SAPLING -> {
                if (size == 2)
                    return new GermanOakGenerator(root);
            }
            case BIRCH_SAPLING -> {
                if (size == 2)
                    if (StadtServer.RANDOM.nextInt(4) == 0)
                        return new SplitHimalayaBirchGenerator(root);
                    else
                        return new HimalayaBirchGenerator(root);
            }
            case DARK_OAK_SAPLING -> {
                if (size == 3)
                    return new ProperDarkOakGenerator(root);
            }
            case ACACIA_SAPLING -> {
                if (size == 2)
                    return new ProperAcaciaGenerator(root);
            }
            case JUNGLE_SAPLING -> {
                if (size == 3)
                    return new YellowMerantiGenerator(root);
            }
            case SPRUCE_SAPLING -> {
                if (size == 3)
                    return new CoastSequoiaGenerator(root);
            }
        }
        return null;
    }
}
