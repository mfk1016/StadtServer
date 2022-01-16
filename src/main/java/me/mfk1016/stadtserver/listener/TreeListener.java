package me.mfk1016.stadtserver.listener;

import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.logic.sorting.PluginCategories;
import me.mfk1016.stadtserver.logic.tree.TreeGenerator;
import me.mfk1016.stadtserver.util.Pair;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.world.StructureGrowEvent;

public class TreeListener extends BasicListener {

    public TreeListener(StadtServer plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onTreeGrow(StructureGrowEvent event) {
        Pair<Integer, Block> sizebase = getTreeShape(event.getLocation().getBlock());
        if (sizebase == null)
            return;
        int size = sizebase._1;
        Block nwBase = sizebase._2;
        Material sapling = nwBase.getType();
        TreeGenerator gen = TreeGenerator.matchGenerator(nwBase, sapling, size, event.isFromBonemeal());
        if (gen != null && gen.isEnoughSpace()) {
            gen.generateTree();
            event.setCancelled(true);
        }
    }

    private Pair<Integer, Block> getTreeShape(Block test) {
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
}
