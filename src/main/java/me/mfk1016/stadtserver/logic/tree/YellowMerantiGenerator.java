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

public class YellowMerantiGenerator extends TreeGenerator {

    private static final int MIN_HEIGHT = 60;
    private static final int HEIGHT_RANGE = 30;
    private static final double TRUNK_FACTOR = 0.75D;

    private int trunkHeight;
    private int minBranch;

    public YellowMerantiGenerator(Block nwBase) {
        super(nwBase, MIN_HEIGHT + StadtServer.RANDOM.nextInt(HEIGHT_RANGE), MIN_HEIGHT,
                Material.JUNGLE_LOG, Material.JUNGLE_LEAVES, Material.JUNGLE_SAPLING);
        checkSquare = 9;
    }

    @Override
    public void generateTree() {
        trunkHeight = (int) ((double) treeHeight * TRUNK_FACTOR);
        minBranch = (trunkHeight / 5) * 3;
        int branchLength = treeHeight - trunkHeight;
        // 3x3 Jungle Tree; height = highest log
        // trunk
        for (int y = 0; y < trunkHeight; y++) {
            for (int x = 0; x < 3; x++) {
                for (int z = 0; z < 3; z++) {
                    if (y == trunkHeight - 1)
                        setWood(nwBase.getRelative(x, y, z), Axis.Y);
                    else
                        setLog(nwBase.getRelative(x, y, z), Axis.Y);
                }
            }
        }

        // branches
        List<Pair<Block, Branch>> branchBases = pickBranchBases();
        int currBranch = 1;
        for (var locbranch : branchBases) {
            double yUp = Math.max(80D - ((currBranch - 1) * 3), 10D);
            double yDown = yUp - 3;
            int length = branchLength - StadtServer.RANDOM.nextInt(3) - (currBranch / 3);
            Block last = randomBranchEnd(locbranch._1, locbranch._2, yDown, yUp, length, 0.8);
            List<Pair<Block, Axis>> branchLogs = connectLogs(locbranch._1, last);
            for (Pair<Block, Axis> branchLog : branchLogs) {
                Block log = branchLog._1;
                Axis axis = branchLog._2;
                setWood(log, axis);
                setWood(log.getRelative(BlockFace.DOWN), axis);
            }
            addMeratiLeaves(last);
            currBranch++;
        }

        // roots
        for (var branch : Branch.cardinals()) {
            Block root = pickBranchRoot(branch, 1);
            int rootHeight = StadtServer.RANDOM.nextInt(3) + 2;
            Block log = root.getRelative(branch.toFace());
            if (!isRootTarget(log))
                continue;
            for (int i = 0; i < rootHeight; i++) {
                setRoot(log.getRelative(0, i, 0));
                setRoot(log.getRelative(0, i - 2, 0).getRelative(branch.toFace()));
            }
        }

        // trunk vines
        for (int y = 0; y < trunkHeight; y++) {
            for (int i = 0; i < 3; i++) {
                if (StadtServer.RANDOM.nextInt(3) == 0)
                    setVines(nwBase.getRelative(-1, y, i), BlockFace.EAST);
                if (StadtServer.RANDOM.nextInt(3) == 0)
                    setVines(nwBase.getRelative(3, y, i), BlockFace.WEST);
                if (StadtServer.RANDOM.nextInt(3) == 0)
                    setVines(nwBase.getRelative(i, y, -1), BlockFace.SOUTH);
                if (StadtServer.RANDOM.nextInt(3) == 0)
                    setVines(nwBase.getRelative(i, y, 3), BlockFace.NORTH);
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
        int currBranch = 1;
        for (Branch b : branches) {
            if (currentHeight < minBranch)
                break;
            Block loc0, loc1;
            loc0 = pickBranchRoot(b, currentHeight);
            if (currBranch > 6) {
                currentHeight -= StadtServer.RANDOM.nextInt(2);
            }
            loc1 = pickBranchRoot(b.invert(), currentHeight);
            currentHeight -= 2;
            result.add(new Pair<>(loc0, b));
            result.add(new Pair<>(loc1, b.invert()));
            currBranch += 2;
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
        log.setType(Material.JUNGLE_WOOD);
        if (log.getBlockData() instanceof Orientable o) {
            o.setAxis(axis);
            log.setBlockData(o);
        }
    }

    protected void setRoot(Block log) {
        if (!isRootTarget(log))
            return;
        log.setType(Material.JUNGLE_WOOD);
        if (log.getBlockData() instanceof Orientable o) {
            o.setAxis(Axis.Y);
            log.setBlockData(o);
        }
    }

    private void addMeratiLeaves(Block last) {
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                for (int x = 0; x <= 4; x++) {
                    for (int z = 0; z <= 4; z++) {
                        if (x <= 2 && z <= 2 && x + z < 3) {
                            setLeaves(last.getRelative(x, 2, z));
                            setLeaves(last.getRelative(x, -1, z));
                            setLeaves(last.getRelative(-x, 2, z));
                            setLeaves(last.getRelative(-x, -1, z));
                            setLeaves(last.getRelative(x, 2, -z));
                            setLeaves(last.getRelative(x, -1, -z));
                            setLeaves(last.getRelative(-x, 2, -z));
                            setLeaves(last.getRelative(-x, -1, -z));
                        }
                        if (x <= 3 && z <= 3 && x + z < 5) {
                            setLeaves(last.getRelative(x, 1, z));
                            setLeaves(last.getRelative(-x, 1, z));
                            setLeaves(last.getRelative(x, 1, -z));
                            setLeaves(last.getRelative(-x, 1, -z));
                        }
                        if (x + z < 7) {
                            setLeaves(last.getRelative(x, 0, z));
                            setLeaves(last.getRelative(-x, 0, z));
                            setLeaves(last.getRelative(x, 0, -z));
                            setLeaves(last.getRelative(-x, 0, -z));
                        }
                        // vines
                        if (StadtServer.RANDOM.nextInt(2) == 0 && x + z < 7) {
                            if (x == 4) {
                                setVines(last.getRelative(-5, 0, z), BlockFace.EAST);
                                setVines(last.getRelative(5, 0, z), BlockFace.WEST);
                            } else if (z == 4) {
                                setVines(last.getRelative(x, 0, -5), BlockFace.SOUTH);
                                setVines(last.getRelative(x, 0, 5), BlockFace.NORTH);
                            }
                        }
                    }
                }
            }
        };
        runnable.runTaskLater(StadtServer.getInstance(), 1L);
    }
}
