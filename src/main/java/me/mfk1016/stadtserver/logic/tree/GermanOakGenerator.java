package me.mfk1016.stadtserver.logic.tree;

import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.logic.sorting.PluginCategories;
import me.mfk1016.stadtserver.util.Pair;
import org.bukkit.Axis;
import org.bukkit.Material;
import org.bukkit.block.Beehive;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Bee;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GermanOakGenerator extends TreeGenerator {

    private static final int MIN_HEIGHT = 20;
    private static final int HEIGHT_RANGE = 10;
    private static final double TRUNK_FACTOR = 0.5D;

    private int trunkHeight;
    private int minBranch;

    public GermanOakGenerator(Block nwBase) {
        super(nwBase, MIN_HEIGHT + StadtServer.RANDOM.nextInt(HEIGHT_RANGE), MIN_HEIGHT,
                Material.OAK_LOG, Material.OAK_LEAVES, Material.OAK_SAPLING);
        checkSquare = 6;
    }

    @Override
    public void generateTree() {
        trunkHeight = (int) ((double) treeHeight * TRUNK_FACTOR);
        minBranch = Math.max(trunkHeight / 3, 6);
        int branchLength = treeHeight - trunkHeight;
        // 2x2 Oak Tree; height = highest log
        // trunk
        for (int y = 0; y < trunkHeight; y++) {
            for (int x = 0; x < 2; x++) {
                for (int z = 0; z < 2; z++) {
                    setLog(nwBase.getRelative(x, y, z), Axis.Y);
                    if (y == trunkHeight - 1)
                        for (BlockFace face : BlockFace.values()) {
                            if (!face.isCartesian())
                                continue;
                            setLeaves(nwBase.getRelative(x, y, z).getRelative(face));
                            setLeaves(nwBase.getRelative(x, y - 1, z).getRelative(face));
                        }
                }
            }
        }
        // branches
        List<Pair<Block, Branch>> branchBases = pickBranchBases();
        int currBranch = 1;
        for (var locbranch : branchBases) {
            double yUp = Math.max(80D - ((currBranch - 1) * 3), 0D);
            double yDown = Math.max(yUp - 3, 0D);
            int length = branchLength + StadtServer.RANDOM.nextInt(3) - (currBranch / 8);
            Block last = randomBranchEnd(locbranch._1, locbranch._2, yDown, yUp, length, 0.8);
            List<Pair<Block, Axis>> branchLogs = connectLogs(locbranch._1, last);
            for (int i = 0; i < branchLogs.size(); i++) {
                Block log = branchLogs.get(i)._1;
                Axis axis = branchLogs.get(i)._2;
                setLog(log, axis);
                if (!log.getRelative(BlockFace.DOWN).getType().isOccluding() && i != branchLogs.size() - 1)
                    log.setType(Material.OAK_WOOD);
                if (i < branchLogs.size() / 3)
                    continue;
                generateSphereLeaves(log, Math.min(i / 3, 5));
            }
            // bees to last branch ?
            if (currBranch == branchBases.size() && StadtServer.RANDOM.nextInt(20) == 0) {
                Block target = locbranch._1.getRelative(BlockFace.DOWN);
                for (Branch b : Branch.cardinals()) {
                    Block nest = target.getRelative(b.toFace());
                    if (!nest.getType().isOccluding()) {
                        nest.setType(Material.BEE_NEST);
                        Block spawn = nest.getRelative(BlockFace.DOWN);
                        Bee bee1 = (Bee) spawn.getWorld().spawnEntity(spawn.getLocation(), EntityType.BEE);
                        Bee bee2 = (Bee) spawn.getWorld().spawnEntity(spawn.getLocation(), EntityType.BEE);
                        Bee bee3 = (Bee) spawn.getWorld().spawnEntity(spawn.getLocation(), EntityType.BEE);
                        Beehive nestData = (Beehive) nest.getState();
                        nestData.addEntity(bee1);
                        nestData.addEntity(bee2);
                        nestData.addEntity(bee3);
                        nestData.update();
                        break;
                    }
                }
            }
            currBranch++;
        }
        // roots
        for (var branch : Branch.cardinals()) {
            Block root = pickBranchRoot(branch, 1);
            boolean high = StadtServer.RANDOM.nextInt(2) == 0;
            Block log = root.getRelative(branch.toFace());
            if (!isRootTarget(log))
                continue;
            log.setType(Material.OAK_WOOD);
            if (high)
                log.getRelative(BlockFace.UP).setType(Material.OAK_WOOD);
        }
    }

    // Calculate all branch bases and types from top to bottom
    private List<Pair<Block, Branch>> pickBranchBases() {
        List<Branch> branches = new ArrayList<>(Arrays.stream(Branch.values()).toList());
        Collections.shuffle(branches);
        branches.addAll(new ArrayList<>(branches));
        List<Pair<Block, Branch>> result = new ArrayList<>();
        int currentHeight = trunkHeight;
        for (Branch b : branches) {
            if (currentHeight < minBranch)
                break;
            Block loc0 = pickBranchRoot(b, currentHeight);
            Block loc1 = pickBranchRoot(b.invert(), currentHeight);
            currentHeight -= StadtServer.RANDOM.nextInt(2);
            result.add(new Pair<>(loc0, b));
            result.add(new Pair<>(loc1, b.invert()));
        }
        return result;
    }

    // pick a base inside the trunk for the branch for a given height
    private Block pickBranchRoot(Branch b, int height) {
        return switch (b) {
            case S -> nwBase.getRelative(StadtServer.RANDOM.nextInt(2), height - 1, 1);
            case SE -> nwBase.getRelative(1, height - 1, 1);
            case E -> nwBase.getRelative(1, height - 1, StadtServer.RANDOM.nextInt(2));
            case NE -> nwBase.getRelative(1, height - 1, 0);
            case N -> nwBase.getRelative(StadtServer.RANDOM.nextInt(2), height - 1, 0);
            case NW -> nwBase.getRelative(0, height - 1, 0);
            case W -> nwBase.getRelative(0, height - 1, StadtServer.RANDOM.nextInt(2));
            case SW -> nwBase.getRelative(0, height - 1, 1);
        };
    }
}
