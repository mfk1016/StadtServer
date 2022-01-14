package me.mfk1016.stadtserver.logic.tree;

import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.util.Pair;
import org.bukkit.Axis;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LinearBirchGenerator extends TreeGenerator {

    // 2x2 Birch Tree; height = highest log
    private int branchLength;
    private int trunkHeight;
    private int minBranch;

    public LinearBirchGenerator(Block nwBase, int height) {
        super(nwBase, height, 20, Material.BIRCH_LOG, Material.BIRCH_LEAVES, Material.BIRCH_SAPLING);
        checkSquare = 6;
    }

    public void generateTree() {
        trunkHeight = (int) ((double) treeHeight / 4D * 3D);
        minBranch = trunkHeight / 4;
        branchLength = treeHeight - trunkHeight;
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
            double yUp = Math.max(80D - ((currBranch - 1) * 2), 45D);
            double yDown = yUp - 2;
            int length = branchLength + StadtServer.RANDOM.nextInt(3);
            Block last = randomBranchEnd(locbranch._1, locbranch._2, yDown, yUp, length);
            List<Pair<Block, Axis>> branchLogs = connectLogs(locbranch._1, last);
            for (int i = 0; i < branchLogs.size(); i++) {
                Block log = branchLogs.get(i)._1;
                Axis axis = branchLogs.get(i)._2;
                setLog(log, axis);
                for (BlockFace face : BlockFace.values()) {
                    if (face == BlockFace.DOWN) {
                        if (!log.getRelative(face).getType().isOccluding())
                            log.setType(Material.BIRCH_WOOD);
                        continue;
                    }
                    if (!face.isCartesian() || i < branchLogs.size() / 2)
                        continue;
                    setLeaves(log.getRelative(face));
                    setLeaves(log.getRelative(face).getRelative(BlockFace.UP));
                }
            }
            currBranch++;
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
            currentHeight -= StadtServer.RANDOM.nextInt(2);
            Block loc1 = pickBranchRoot(b.invert(), currentHeight);
            currentHeight -= 1;
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
