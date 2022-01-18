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

public class ProperAcaciaGenerator extends TreeGenerator {

    private static final int MIN_HEIGHT = 12;
    private static final int HEIGHT_RANGE = 5;
    private static final double TRUNK_FACTOR = 0.2D;

    private int trunkHeight;

    public ProperAcaciaGenerator(Block nwBase) {
        super(nwBase, MIN_HEIGHT + StadtServer.RANDOM.nextInt(HEIGHT_RANGE), MIN_HEIGHT,
                Material.ACACIA_LOG, Material.ACACIA_LEAVES, Material.ACACIA_SAPLING);
        checkSquare = 6;
    }

    @Override
    public void generateTree() {
        trunkHeight = (int) ((double) treeHeight * TRUNK_FACTOR);
        int branchLength = treeHeight - trunkHeight;
        // 2x2 Acacia Tree; height = highest log
        // trunk
        for (int y = 0; y < trunkHeight; y++) {
            for (int x = 0; x < 2; x++) {
                for (int z = 0; z < 2; z++) {
                    setLog(nwBase.getRelative(x, y, z), Axis.Y);
                }
            }
        }
        // branches
        List<Pair<Block, Branch>> branchBases = pickBranchBases();
        int currBranch = 1;
        for (var locbranch : branchBases) {
            double yUp = Math.max(75D - ((currBranch - 1) * 2.5), 40D);
            double yDown = yUp - 2.5;
            int length = branchLength + StadtServer.RANDOM.nextInt(3) + (currBranch / 3);
            Block last = randomBranchEnd(locbranch._1, locbranch._2, yDown, yUp, length, 0.8);
            List<Pair<Block, Axis>> branchLogs = connectLogs(locbranch._1, last);
            for (Pair<Block, Axis> branchLog : branchLogs) {
                setLog(branchLog._1, Axis.Y);
                setLog(branchLog._1.getRelative(BlockFace.DOWN), Axis.Y);
                if (!branchLog._1.getRelative(0, -2, 0).getType().isOccluding()) {
                    branchLog._1.getRelative(BlockFace.DOWN).setType(Material.ACACIA_WOOD);
                }
                if (!branchLog._1.getRelative(0, 1, 0).getType().isOccluding()) {
                    branchLog._1.setType(Material.ACACIA_WOOD);
                }
            }
            addAcaciaLeaves(last);
            currBranch++;
        }
    }

    // Calculate all branch bases and types from top to bottom
    private List<Pair<Block, Branch>> pickBranchBases() {
        List<Branch> branches = new ArrayList<>(Arrays.stream(Branch.values()).toList());
        Collections.shuffle(branches);
        List<Pair<Block, Branch>> result = new ArrayList<>();
        int currentHeight = trunkHeight;
        for (Branch b : branches) {
            Block loc0 = pickBranchRoot(b, currentHeight);
            Block loc1 = pickBranchRoot(b.invert(), currentHeight - 1);
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

    private void addAcaciaLeaves(Block last) {
        for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
                if (Math.abs(x) + Math.abs(z) >= 7)
                    continue;
                setLeaves(last.getRelative(x, 0, z));
            }
        }
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                if (Math.abs(x) + Math.abs(z) >= 5)
                    continue;
                setLeaves(last.getRelative(x, 1, z));
            }
        }
    }
}
