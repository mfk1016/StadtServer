package me.mfk1016.stadtserver.logic.tree;

import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.logic.sorting.PluginCategories;
import me.mfk1016.stadtserver.util.Pair;
import org.bukkit.Axis;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.Orientable;
import org.bukkit.util.Vector;

import java.util.*;

public abstract class TreeGenerator {

    protected final Block nwBase;
    protected int treeHeight;
    protected final int minHeight;
    protected final Material logType;
    protected final Material leavesType;
    protected final Material saplingType;
    protected int checkSquare = 3;
    private final Set<Block> placedLeaves = new HashSet<>();

    public TreeGenerator(Block nwBase, int treeHeight, int minHeight, Material log, Material leaves, Material sapling) {
        this.nwBase = nwBase;
        this.treeHeight = treeHeight;
        this.minHeight = minHeight;
        this.logType = log;
        this.leavesType = leaves;
        this.saplingType = sapling;
    }

    public boolean isEnoughSpace() {
        if (nwBase.getLocation().getBlockY() + treeHeight > 310)
            return false;
        int validHeight = 1;
        // Get the north west corner of the check area
        Block corner = nwBase;
        if (checkSquare > 2) {
            int offset = (checkSquare - 1) / 2;
            corner = nwBase.getRelative(-offset, 0, -offset);
        }
        for (int y = 0; y < treeHeight; y++) {
            for (int z = 0; z < checkSquare; z++) {
                for (int x = 0; y < checkSquare; x++) {
                    Block current = corner.getRelative(x, y, z);
                    if (PluginCategories.isLog(current.getType()) || PluginCategories.isWood(current.getType()))
                        continue;
                    if (current.getType().isOccluding())
                        break;
                }
            }
            validHeight++;
        }
        if (validHeight < minHeight)
            return false;
        else if (validHeight == treeHeight)
            return true;
        treeHeight = validHeight;
        return true;
    }

    protected Block randomBranchEnd(Block base, Branch b, double angleDown, double angleUp, double distance, double spread) {
        Pair<Vector, Vector> quadEdges = b.getSphereQuadrant(angleDown, angleUp, spread);
        double offX = (quadEdges._2.getX() - quadEdges._1.getX()) * StadtServer.RANDOM.nextDouble();
        double offY = (quadEdges._2.getY() - quadEdges._1.getY()) * StadtServer.RANDOM.nextDouble();
        double offZ = (quadEdges._2.getZ() - quadEdges._1.getZ()) * StadtServer.RANDOM.nextDouble();
        Vector toEnd = quadEdges._1.add(new Vector(offX, offY, offZ));
        toEnd = toEnd.normalize().multiply(distance);
        Location resultLoc = base.getLocation().clone().add(toEnd);
        if (resultLoc.getBlockY() < -64 || resultLoc.getBlockY() > 319)
            return base;
        return resultLoc.getBlock();
    }

    // Returns Pairs of block, axis determining the complete branch.
    // The indexes of the base branch are index % size(base + additionalBases)
    protected List<Pair<Block, Axis>> connectLogs(Block base, Block last, Block... additionalBases) {
        List<Vector> additionalBaseOffsets = new ArrayList<>();
        for (var b : additionalBases) {
            additionalBaseOffsets.add(b.getLocation().toVector().subtract(base.getLocation().toVector()));
        }
        List<Pair<Block, Axis>> result = new ArrayList<>();
        Vector distance = base.getLocation().toVector().subtract(last.getLocation().toVector());
        if (distance.length() == 0)
            return result;

        Axis axis;
        double xstep = Math.signum(distance.getX());
        double ystep = Math.signum(distance.getY());
        double zstep = Math.signum(distance.getZ());
        if (Math.abs(distance.getY()) >= Math.abs(distance.getX()) && Math.abs(distance.getY()) >= Math.abs(distance.getZ())) {
            xstep *= (Math.abs(distance.getX()) / Math.abs(distance.getY()));
            zstep *= (Math.abs(distance.getZ()) / Math.abs(distance.getY()));
            axis = Axis.Y;
        } else if (Math.abs(distance.getX()) >= Math.abs(distance.getY()) && Math.abs(distance.getX()) >= Math.abs(distance.getZ())) {
            ystep *= (Math.abs(distance.getY()) / Math.abs(distance.getX()));
            zstep *= (Math.abs(distance.getZ()) / Math.abs(distance.getX()));
            axis = Axis.X;
        } else {
            xstep *= (Math.abs(distance.getX()) / Math.abs(distance.getZ()));
            ystep *= (Math.abs(distance.getY()) / Math.abs(distance.getZ()));
            axis = Axis.Z;
        }

        Vector step = new Vector(xstep, ystep, zstep);
        if (step.length() == 0)
            return result;
        Vector offset = new Vector();
        while (distance.length() > offset.length()) {
            Block current = last.getRelative(offset.getBlockX(), offset.getBlockY(), offset.getBlockZ());
            for (var baseOffset : additionalBaseOffsets) {
                Block add = current.getRelative(baseOffset.getBlockX(), baseOffset.getBlockY(), baseOffset.getBlockZ());
                result.add(new Pair<>(add, axis));
            }
            result.add(new Pair<>(current, axis));
            offset.add(step);
        }
        for (Block additionBase : additionalBases) {
            result.add(new Pair<>(additionBase, Axis.Y));
        }
        result.add(new Pair<>(base, Axis.Y));
        Collections.reverse(result);
        return result;
    }

    protected void setLog(Block log, Axis axis) {
        if (!log.isEmpty() && !PluginCategories.isLeaves(log.getType()) && log.getType() != saplingType)
            return;
        log.setType(logType);
        if (log.getBlockData() instanceof Orientable o) {
            o.setAxis(axis);
            log.setBlockData(o);
        }
    }

    protected void generateSphereLeaves(Block center, int radius) {
        for (int x = 0; x <= radius; x++)
            for (int y = 0; y <= radius; y++)
                for (int z = 0; z <= radius; z++) {
                    double currDist = Math.sqrt(x * x + y * y + z * z);
                    if (currDist <= radius) {
                        int random = currDist == radius ? 2 : 1;
                        if (StadtServer.RANDOM.nextInt(random) == 0)
                            setLeaves(center.getRelative(x, y, z));
                        if (StadtServer.RANDOM.nextInt(random) == 0)
                            setLeaves(center.getRelative(-x, y, z));
                        if (StadtServer.RANDOM.nextInt(random) == 0)
                            setLeaves(center.getRelative(x, -y, z));
                        if (StadtServer.RANDOM.nextInt(random) == 0)
                            setLeaves(center.getRelative(x, y, -z));
                        if (StadtServer.RANDOM.nextInt(random) == 0)
                            setLeaves(center.getRelative(-x, -y, z));
                        if (StadtServer.RANDOM.nextInt(random) == 0)
                            setLeaves(center.getRelative(-x, y, -z));
                        if (StadtServer.RANDOM.nextInt(random) == 0)
                            setLeaves(center.getRelative(x, -y, -z));
                        if (StadtServer.RANDOM.nextInt(random) == 0)
                            setLeaves(center.getRelative(-x, -y, -z));
                    }
                }
    }

    protected void setLeaves(Block leaves) {
        if (!leaves.isEmpty() || placedLeaves.contains(leaves))
            return;
        placedLeaves.add(leaves);
        leaves.setType(leavesType);
    }

    protected void setVines(Block target, BlockFace face) {
        if (!target.isEmpty() || placedLeaves.contains(target))
            return;
        target.setType(Material.VINE);
        MultipleFacing vine = (MultipleFacing) Material.VINE.createBlockData();
        vine.setFace(face, true);
        target.setBlockData(vine);
    }

    public abstract void generateTree();

    public static TreeGenerator matchGenerator(Block root, Material saplingType, int size) {
        switch (saplingType) {
            case OAK_SAPLING -> {
                if (size == 2)
                    return new GermanOakGenerator(root);
            }
            case BIRCH_SAPLING -> {
                if (size == 2)
                    if (StadtServer.RANDOM.nextInt(4) == 0)
                        return new SplitHimalayaBirchGenerator(root);
                    else
                        return new HimalayaBirchGenerator(root);
            }
            case DARK_OAK_SAPLING -> {
                if (size == 3)
                    return new ProperDarkOakGenerator(root);
            }
            case ACACIA_SAPLING -> {
                if (size == 2)
                    return new ProperAcaciaGenerator(root);
            }
            case JUNGLE_SAPLING -> {
                if (size == 3)
                    return new YellowMerantiGenerator(root);
            }
            case SPRUCE_SAPLING -> {
                if (size == 3)
                    return new CoastSequoiaGenerator(root);
            }
        }
        return null;
    }
}
