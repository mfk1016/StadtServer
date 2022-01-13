package me.mfk1016.stadtserver.listener;

import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.logic.tree.BigBirchGenerator;
import org.bukkit.Material;
import org.bukkit.TreeType;
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
        Block block = event.getLocation().getBlock();
        if (event.getSpecies() == TreeType.BIRCH) {
            // Check that 2x2 saplings are placed
            Block nwBase = null;
            Material sap = Material.BIRCH_SAPLING;
            if (block.getType() != sap)
                return;
            Block N, S, W, E;
            N = block.getRelative(BlockFace.NORTH);
            S = block.getRelative(BlockFace.SOUTH);
            W = block.getRelative(BlockFace.WEST);
            E = block.getRelative(BlockFace.EAST);
            if (N.getType() == sap && W.getType() == sap && W.getRelative(BlockFace.NORTH).getType() == sap) {
                nwBase = W.getRelative(BlockFace.NORTH);
            }
            if (N.getType() == sap && E.getType() == sap && E.getRelative(BlockFace.NORTH).getType() == sap) {
                nwBase = N;
            }
            if (S.getType() == sap && W.getType() == sap && W.getRelative(BlockFace.SOUTH).getType() == sap) {
                nwBase = W;
            }
            if (S.getType() == sap && E.getType() == sap && E.getRelative(BlockFace.SOUTH).getType() == sap) {
                nwBase = block;
            }
            if (nwBase == null)
                return;
            int height = 20 + StadtServer.RANDOM.nextInt(15);
            BigBirchGenerator gen = new BigBirchGenerator(nwBase, height);
            if (gen.isEnoughSpace()) {
                gen.generateTree();
                event.setCancelled(true);
            }
        }
    }
}
