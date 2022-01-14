package me.mfk1016.stadtserver.logic.tree;

import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.util.Pair;
import org.bukkit.Axis;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

public class SplittingBirchGenerator extends TreeGenerator {

    private static final int MIN_HEIGHT = 20;
    private static final int HEIGHT_RANGE = 10;
    private static final double TRUNK_FACTOR = 0.75D;

    private int trunkHeight;
    private int minBranch;

    // Contains the nwBase for every height
    private List<Block> mainBases = new ArrayList<>();
    private List<Block> offBases = new ArrayList<>();

    public SplittingBirchGenerator(Block nwBase) {
        super(nwBase, MIN_HEIGHT + StadtServer.RANDOM.nextInt(HEIGHT_RANGE), MIN_HEIGHT,
                Material.BIRCH_LOG, Material.BIRCH_LEAVES, Material.BIRCH_SAPLING);
        checkSquare = 6;
    }

    @Override
    public void generateTree() {
        trunkHeight = (int) ((double) treeHeight * TRUNK_FACTOR);
        minBranch = trunkHeight / 4;
        int branchLength = treeHeight - trunkHeight;
        // off branch = main.invert()
        Branch main = Branch.values()[StadtServer.RANDOM.nextInt(Branch.values().length)];
        // The main branch is located between 1/4 and 1/3 of the trunk height (rounded down)
        minBranch += StadtServer.RANDOM.nextInt((trunkHeight / 3) - minBranch);
        // Trunk before main branch
        for (int h = 0; h < minBranch; h++) {
            Block currBase = nwBase.getRelative(0, h, 0);
            setLog(currBase, Axis.Y);
            setLog(nwBase.getRelative(1, h, 0), Axis.Y);
            setLog(nwBase.getRelative(0, h, 1), Axis.Y);
            setLog(nwBase.getRelative(1, h, 1), Axis.Y);
            mainBases.add(currBase);
            offBases.add(currBase);
        }
        // Pick main and off branch
        Block mainBranchBase = mainBases.get(mainBases.size() - 1);
        Block mainLast = randomBranchEnd(mainBranchBase, main, 50D, 60D, 4);
        List<Pair<Block, Axis>> mainBranchBlocks = connectLogs(mainBranchBase, mainLast);
        for (var elem : mainBranchBlocks) {
            setLog(elem._1, Axis.Y);
            setLog(elem._1.getRelative(1, 0, 0), Axis.Y);
            setLog(elem._1.getRelative(0, 0, 1), Axis.Y);
            setLog(elem._1.getRelative(1, 0, 1), Axis.Y);
            if (!mainBases.contains(elem._1))
                mainBases.add(elem._1);
        }
        Block offLast = randomBranchEnd(mainBranchBase, main.invert(), 50D, 60D, 4);
        List<Pair<Block, Axis>> offBranchBlocks = connectLogs(mainBranchBase, offLast);
        for (var elem : offBranchBlocks) {
            setLog(elem._1, Axis.Y);
            setLog(elem._1.getRelative(1, 0, 0), Axis.Y);
            setLog(elem._1.getRelative(0, 0, 1), Axis.Y);
            setLog(elem._1.getRelative(1, 0, 1), Axis.Y);
            if (!offBases.contains(elem._1))
                offBases.add(elem._1);
        }
        // Now finish the trunk
        int toAdd = trunkHeight - (Math.max(mainLast.getY(), offLast.getY()) - nwBase.getY());
        for (int h = 1; h <= toAdd; h++) {
            Block currBaseM = mainLast.getRelative(0, h, 0);
            Block currBaseO = offLast.getRelative(0, h, 0);
            setLog(currBaseM, Axis.Y);
            setLog(currBaseO, Axis.Y);
            setLog(currBaseM.getRelative(1, 0, 0), Axis.Y);
            setLog(currBaseO.getRelative(1, 0, 0), Axis.Y);
            setLog(currBaseM.getRelative(0, 0, 1), Axis.Y);
            setLog(currBaseO.getRelative(0, 0, 1), Axis.Y);
            setLog(currBaseM.getRelative(1, 0, 1), Axis.Y);
            setLog(currBaseO.getRelative(1, 0, 1), Axis.Y);
            mainBases.add(currBaseM);
            offBases.add(currBaseO);
        }
    }
}
