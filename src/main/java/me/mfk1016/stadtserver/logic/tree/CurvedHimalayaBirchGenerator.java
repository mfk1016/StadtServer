package me.mfk1016.stadtserver.logic.tree;

import me.mfk1016.stadtserver.StadtServer;
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

public class CurvedHimalayaBirchGenerator extends TreeGenerator {

    private static final int MIN_HEIGHT = 30;
    private static final int HEIGHT_RANGE = 15;
    private static final double TRUNK_FACTOR = 0.75D;

    private int trunkHeight;
    private int minBranch;
    private Branch trunkBranch;
    private final List<Block> trunkBases = new ArrayList<>();
    private final List<Block> offTrunkBases = new ArrayList<>();

    public CurvedHimalayaBirchGenerator(Block nwBase) {
        super(nwBase, MIN_HEIGHT + StadtServer.RANDOM.nextInt(HEIGHT_RANGE), MIN_HEIGHT,
                Material.BIRCH_LOG, Material.BIRCH_LEAVES, Material.BIRCH_SAPLING);
        checkSquare = 6;
    }

    @Override
    public void generateTree() {
        trunkHeight = (int) ((double) treeHeight * TRUNK_FACTOR);
        int trunkSectionLength = trunkHeight / 3;
        trunkBranch = Branch.cardinals()[StadtServer.RANDOM.nextInt(4)];

        // first part: 90 degree
        Block firstTrunkLast = nwBase.getRelative(0, trunkSectionLength, 0);
        Block[] firstAdditionalBases = {
                nwBase.getRelative(1, 0, 0),
                nwBase.getRelative(0, 0, 1),
                nwBase.getRelative(1, 0, 1)
        };
        List<Pair<Block, Axis>> trunk = connectLogs(nwBase, firstTrunkLast, firstAdditionalBases);
        List<Pair<Block, Axis>> offTrunk = new ArrayList<>(trunk);
        // second part: 70 degree
        Block secondTrunkLast = randomBranchEnd(firstTrunkLast, trunkBranch, 60D, 60D, trunkSectionLength, 0);
        Block secondOffTrunkLast = randomBranchEnd(firstTrunkLast, trunkBranch.invert(), 60D, 60D, trunkSectionLength, 0);
        Block[] secondAdditionalBases = {
                firstTrunkLast.getRelative(1, 0, 0),
                firstTrunkLast.getRelative(0, 0, 1),
                firstTrunkLast.getRelative(1, 0, 1)
        };
        List<Pair<Block, Axis>> secondTrunk = connectLogs(firstTrunkLast, secondTrunkLast, secondAdditionalBases);
        trunk.addAll(secondTrunk);
        List<Pair<Block, Axis>> secondOffTrunk = connectLogs(firstTrunkLast, secondOffTrunkLast, secondAdditionalBases);
        offTrunk.addAll(secondOffTrunk);
        // third part 80 degree opposite
        Block thirdTrunkLast = randomBranchEnd(secondTrunkLast, trunkBranch.invert(), 80D, 80D, trunkSectionLength, 0);
        Block thirdOffTrunkLast = randomBranchEnd(secondOffTrunkLast, trunkBranch, 80D, 80D, trunkSectionLength, 0);
        Block[] thirdAdditionalBases = {
                secondTrunkLast.getRelative(1, 0, 0),
                secondTrunkLast.getRelative(0, 0, 1),
                secondTrunkLast.getRelative(1, 0, 1)
        };
        Block[] thirdAdditionalOffBases = {
                secondOffTrunkLast.getRelative(1, 0, 0),
                secondOffTrunkLast.getRelative(0, 0, 1),
                secondOffTrunkLast.getRelative(1, 0, 1)
        };
        List<Pair<Block, Axis>> thirdTrunk = connectLogs(secondTrunkLast, thirdTrunkLast, thirdAdditionalBases);
        trunk.addAll(thirdTrunk);
        List<Pair<Block, Axis>> thirdOffTrunk = connectLogs(secondOffTrunkLast, thirdOffTrunkLast, thirdAdditionalOffBases);
        offTrunk.addAll(thirdOffTrunk);

        for (int i = 0; i < trunk.size(); i++) {
            Block currLog = trunk.get(i)._1;
            setLog(currLog, trunk.get(i)._2);
            if (!currLog.getRelative(BlockFace.DOWN).getType().isOccluding())
                currLog.setType(Material.BIRCH_WOOD);
            if (i % 4 == 0) {
                trunkBases.add(trunk.get(i)._1);
            }
        }
        for (int i = 0; i < offTrunk.size(); i++) {
            Block currLog = offTrunk.get(i)._1;
            setLog(currLog, offTrunk.get(i)._2);
            if (!currLog.getRelative(BlockFace.DOWN).getType().isOccluding())
                currLog.setType(Material.BIRCH_WOOD);
            if (i % 4 == 0) {
                offTrunkBases.add(offTrunk.get(i)._1);
            }
        }

        trunkHeight = trunkBases.size();
        minBranch = trunkHeight / 4;

        // trunk top leaves
        for (int x = 0; x < 2; x++) {
            for (int z = 0; z < 2; z++) {
                Block log = trunkBases.get(trunkBases.size() - 1).getRelative(x, 0, z);
                for (BlockFace face : BlockFace.values()) {
                    if (!face.isCartesian())
                        continue;
                    setLeaves(log.getRelative(face));
                }
            }
        }

        // branches
        int branchLength = treeHeight - trunkHeight;
        List<Pair<Block, Branch>> branchBases = pickBranchBases();
        int currBranch = 1;
        for (var locbranch : branchBases) {
            double yUp = Math.max(70D - ((currBranch - 1)), 35D);
            double yDown = yUp - 1;
            int length = branchLength + StadtServer.RANDOM.nextInt(3);
            Block last = randomBranchEnd(locbranch._1, locbranch._2, yDown, yUp, length, 0.8);
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
    }

    // Calculate all branch bases and types from top to bottom
    private List<Pair<Block, Branch>> pickBranchBases() {
        List<Branch> branches = new ArrayList<>(Arrays.stream(Branch.values()).toList());
        Collections.shuffle(branches);
        branches.addAll(new ArrayList<>(branches));
        branches.addAll(new ArrayList<>(branches));
        List<Pair<Block, Branch>> result = new ArrayList<>();
        int currentHeight = Math.max(trunkBases.size(), offTrunkBases.size());
        for (Branch b : branches) {
            if (currentHeight < minBranch)
                break;
            Block loc0 = pickBranchRoot(b, currentHeight);
            Block loc1 = pickBranchRoot(b.invert(), currentHeight);
            currentHeight -= StadtServer.RANDOM.nextInt(3);
            result.add(new Pair<>(loc0, b));
            result.add(new Pair<>(loc1, b.invert()));
        }
        return result;
    }

    // pick a base inside the trunk for the branch for a given height
    private Block pickBranchRoot(Branch b, int height) {
        List<Block> bases;
        if (height > trunkBases.size()) {
            bases = offTrunkBases;
        } else if (height > offTrunkBases.size()) {
            bases = trunkBases;
        } else if (b == trunkBranch) {
            bases = trunkBases;
        } else if (b == trunkBranch.invert()) {
            bases = offTrunkBases;
        } else {
            bases = StadtServer.RANDOM.nextBoolean() ? trunkBases : offTrunkBases;
        }

        return switch (b) {
            case S -> bases.get(height - 1).getRelative(StadtServer.RANDOM.nextInt(2), 0, 1);
            case SE -> bases.get(height - 1).getRelative(1, 0, 1);
            case E -> bases.get(height - 1).getRelative(1, 0, StadtServer.RANDOM.nextInt(2));
            case NE -> bases.get(height - 1).getRelative(1, 0, 0);
            case N -> bases.get(height - 1).getRelative(StadtServer.RANDOM.nextInt(2), 0, 0);
            case NW -> bases.get(height - 1).getRelative(0, 0, 0);
            case W -> bases.get(height - 1).getRelative(0, 0, StadtServer.RANDOM.nextInt(2));
            case SW -> bases.get(height - 1).getRelative(0, 0, 1);
        };
    }
}
