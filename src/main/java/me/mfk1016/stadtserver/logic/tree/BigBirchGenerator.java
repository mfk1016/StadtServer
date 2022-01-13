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

public class BigBirchGenerator extends TreeGenerator {

    // 2x2 Birch Tree; height = highest log
    private int branchLength;
    private int trunkHeight;
    private int minBranch;

    public BigBirchGenerator(Block nwBase, int height) {
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
            Block last;
            if (currBranch == 1) {
                int lastHeight = branchLength + StadtServer.RANDOM.nextInt(4);
                last = locbranch._1.getRelative(StadtServer.RANDOM.nextInt(3) - 1, lastHeight, StadtServer.RANDOM.nextInt(3) - 1);
            } else {
                int slot = pickBranchLast(locbranch._2);
                int lastHeight = (locbranch._1.getY() - nwBase.getY()) + branchLength + StadtServer.RANDOM.nextInt(4);
                last = branchSlotToLoc(locbranch._2, slot, lastHeight);
            }
            List<Pair<Block, Axis>> branchLogs = connectLogs(locbranch._1, last);
            for (int i = 0; i < branchLogs.size(); i++) {
                Block log = branchLogs.get(i)._1;
                Axis axis = branchLogs.get(i)._2;
                setLog(log, axis);
                for (BlockFace face : BlockFace.values()) {
                    if (face == BlockFace.DOWN) {
                        if (log.getRelative(face).getType() != Material.BIRCH_LOG)
                            log.setType(Material.BIRCH_WOOD);
                    }
                    if (!face.isCartesian() || face == BlockFace.DOWN || i < branchLogs.size() / 2)
                        continue;
                    setLeaves(log.getRelative(face));
                    setLeaves(log.getRelative(face).getRelative(BlockFace.UP));
                }
            }
            currBranch++;
        }
        StadtServer.LOGGER.info("branch");
    }

    // Calculate the location for a branch slot for a given height
    private Block branchSlotToLoc(Branch b, int slot, int height) {
        assert slot > 0 && slot < 9;
        Block base = switch (b) {
            case S -> nwBase.getRelative(0, height - 1, 3);
            case SE -> nwBase.getRelative(3, height - 1, 3);
            case E -> nwBase.getRelative(3, height - 1, 1);
            case NE -> nwBase.getRelative(3, height - 1, -2);
            case N -> nwBase.getRelative(1, height - 1, -2);
            case NW -> nwBase.getRelative(-2, height - 1, -2);
            case W -> nwBase.getRelative(-2, height - 1, 0);
            case SW -> nwBase.getRelative(-2, height - 1, 3);
        };
        int aOffset = switch (b) {
            case S, E, N, W -> (slot % 2) + 1;
            default -> (slot % 3) + 1;
        };
        int bOffset = slot / 3;
        return switch (b) {
            case S, SE -> base.getRelative(aOffset, 0, bOffset);
            case E, NE -> base.getRelative(bOffset, 0, -aOffset);
            case N, NW -> base.getRelative(-aOffset, 0, -bOffset);
            case W, SW -> base.getRelative(-bOffset, 0, aOffset);
        };
    }

    // Pick 2 slots for a given branch; the first determines the branch angle, the second is the target
    private Integer pickBranchLast(Branch b) {
        switch (b) {
            case S, E, N, W -> {
                int first = StadtServer.RANDOM.nextInt(4) + 1;
                int second = StadtServer.RANDOM.nextInt(2) + 3;
                if (first > 2) second += 2;
                return second;
            }
            default -> {
                return StadtServer.RANDOM.nextInt(2) * 2 + 2;
            }
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
            Block loc1 = pickBranchRoot(b.opp(), currentHeight);
            currentHeight -= 1;
            result.add(new Pair<>(loc0, b));
            result.add(new Pair<>(loc1, b.opp()));
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
