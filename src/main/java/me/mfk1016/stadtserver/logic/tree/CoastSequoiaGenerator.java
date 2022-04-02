package me.mfk1016.stadtserver.logic.tree;

import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.logic.sorting.PluginCategories;
import me.mfk1016.stadtserver.util.Pair;
import org.bukkit.Axis;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Orientable;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CoastSequoiaGenerator extends TreeGenerator {

    private static final int MIN_HEIGHT = 70;
    private static final int HEIGHT_RANGE = 40;
    private static final double TRUNK_FACTOR = 0.8D;

    private int trunkHeight;
    private int minBranch;

    public CoastSequoiaGenerator(Block nwBase) {
        super(nwBase, MIN_HEIGHT + StadtServer.RANDOM.nextInt(HEIGHT_RANGE), MIN_HEIGHT,
                Material.SPRUCE_LOG, Material.SPRUCE_LEAVES, Material.SPRUCE_SAPLING);
        checkSquare = 9;
    }

    @Override
    public void generateTree() {
        trunkHeight = (int) ((double) treeHeight * TRUNK_FACTOR);
        minBranch = (trunkHeight / 5) * 3;
        int branchLength = treeHeight - trunkHeight;
        // 3x3 Spruce Tree; height = highest log
        // trunk
        for (int y = 0; y < trunkHeight; y++) {
            for (int x = 0; x < 3; x++) {
                for (int z = 0; z < 3; z++) {
                    if (y == trunkHeight - 1) {
                        setWood(nwBase.getRelative(x, y, z), Axis.Y);
                        for (BlockFace face : BlockFace.values()) {
                            if (!face.isCartesian())
                                continue;
                            setLeaves(nwBase.getRelative(x, y, z).getRelative(face));
                            setLeaves(nwBase.getRelative(x, y - 1, z).getRelative(face));
                        }
                    } else {
                        setLog(nwBase.getRelative(x, y, z), Axis.Y);
                    }
                }
            }
        }

        // branches
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                List<Pair<Block, Branch>> branchBases = pickBranchBases();
                int currBranch = 1;
                for (var locbranch : branchBases) {
                    double yUp = Math.max(90D - ((currBranch - 1) * 5), 10D);
                    double yDown = yUp - 5;
                    int length = branchLength - StadtServer.RANDOM.nextInt(4) - (currBranch / 4);
                    Block last = randomBranchEnd(locbranch._1, locbranch._2, yDown, yUp, length, 0.8);
                    List<Pair<Block, Axis>> branchLogs = connectLogs(locbranch._1, last);
                    for (int i = 0; i < branchLogs.size(); i++) {
                        Block log = branchLogs.get(i)._1;
                        Axis axis = branchLogs.get(i)._2;
                        setWood(log, axis);
                        if (yUp > 15D)
                            setWood(log.getRelative(BlockFace.DOWN), axis);
                        if (i < (branchLogs.size() / 3) * 2)
                            continue;
                        for (BlockFace face : BlockFace.values()) {
                            if (!face.isCartesian())
                                continue;
                            setLeaves(log.getRelative(face));
                            if (yUp > 15D)
                                setLeaves(log.getRelative(face).getRelative(BlockFace.DOWN));
                            setLeaves(log.getRelative(face).getRelative(BlockFace.UP));
                            if (face != BlockFace.UP && face != BlockFace.DOWN && i != branchLogs.size() - 1) {
                                setLeaves(log.getRelative(face).getRelative(face));
                                if (yUp > 15D)
                                    setLeaves(log.getRelative(face).getRelative(face).getRelative(BlockFace.DOWN));
                            }
                        }
                    }
                    currBranch++;
                }
            }
        };
        runnable.runTaskLater(StadtServer.getInstance(), 1L);

        // roots
        for (var branch : Branch.cardinals()) {
            Block root = pickBranchRoot(branch, 1);
            int rootHeight = StadtServer.RANDOM.nextInt(3) + 2;
            Block log = root.getRelative(branch.toFace());
            if (!isRootTarget(log))
                continue;
            for (int i = 0; i < rootHeight; i++) {
                setWood(log.getRelative(0, i, 0), Axis.Y);
                setWood(log.getRelative(0, i - 2, 0).getRelative(branch.toFace()), Axis.Y);
            }
        }
    }

    // Calculate all branch bases and types from top to bottom
    private List<Pair<Block, Branch>> pickBranchBases() {
        List<Branch> branches = new ArrayList<>(Arrays.stream(Branch.values()).toList());
        Collections.shuffle(branches);
        branches.addAll(new ArrayList<>(branches));
        branches.addAll(new ArrayList<>(branches));
        List<Pair<Block, Branch>> result = new ArrayList<>();
        int currentHeight = trunkHeight;
        int runs = 0;
        for (Branch b : branches) {
            if (currentHeight < minBranch)
                break;
            Block loc0, loc1;
            loc0 = pickBranchRoot(b, currentHeight);
            loc1 = pickBranchRoot(b.invert(), currentHeight);
            result.add(new Pair<>(loc0, b));
            result.add(new Pair<>(loc1, b.invert()));
            if (runs > 3) {
                loc0 = pickBranchRoot(b.twoForward(), currentHeight);
                loc1 = pickBranchRoot(b.invert().twoForward(), currentHeight);
                result.add(new Pair<>(loc0, b.twoForward()));
                result.add(new Pair<>(loc1, b.invert().twoForward()));
                currentHeight -= 3;
            }
            runs++;
        }
        return result;
    }

    // pick a base inside the trunk for the branch for a given height
    private Block pickBranchRoot(Branch b, int height) {
        return switch (b) {
            case S -> nwBase.getRelative(1, height - 1, 2);
            case SE -> nwBase.getRelative(2, height - 1, 2);
            case E -> nwBase.getRelative(2, height - 1, 1);
            case NE -> nwBase.getRelative(2, height - 1, 0);
            case N -> nwBase.getRelative(1, height - 1, 0);
            case NW -> nwBase.getRelative(0, height - 1, 0);
            case W -> nwBase.getRelative(0, height - 1, 1);
            case SW -> nwBase.getRelative(0, height - 1, 2);
        };
    }

    protected void setWood(Block log, Axis axis) {
        if (!log.isEmpty() && !PluginCategories.isLeaves(log.getType()) && log.getType() != saplingType)
            return;
        log.setType(Material.SPRUCE_WOOD);
        if (log.getBlockData() instanceof Orientable o) {
            o.setAxis(axis);
            log.setBlockData(o);
        }
    }
}
