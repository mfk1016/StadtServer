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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public abstract class TreeGenerator {

    protected final Block nwBase;
    protected final int minHeight;
    protected final Material logType;
    protected final Material woodType;
    protected final Material leavesType;
    protected final Material saplingType;
    private final Set<Block> placedLeaves = new HashSet<>();
    protected int treeHeight;
    protected int checkSquare = 3;

    public TreeGenerator(Block nwBase, int treeHeight, int minHeight, Material log, Material wood, Material leaves, Material sapling) {
        this.nwBase = nwBase;
        this.treeHeight = treeHeight;
        this.minHeight = minHeight;
        this.logType = log;
        this.woodType = wood;
        this.leavesType = leaves;
        this.saplingType = sapling;
    }

    /* --- TEST --- */

    public boolean isEnoughSpace() {
        if (nwBase.getLocation().getBlockY() + treeHeight > 310)
            return false;
        int validHeight = 1;
        // Get the north-west corner of the check area
        Block corner = nwBase;
        if (checkSquare > 2) {
            int offset = (checkSquare - 1) / 2;
            corner = nwBase.getRelative(-offset, 0, -offset);
        }
        for (int y = 0; y < treeHeight; y++) {
            for (int z = 0; z < checkSquare; z++) {
                for (int x = 0; x < checkSquare; x++) {
                    Material current = corner.getRelative(x, y, z).getType();
                    if (PluginCategories.isLog(current) || PluginCategories.isWood(current))
                        continue;
                    if (current.isOccluding())
                        break;
                }
            }
            validHeight++;
        }
        if (validHeight < minHeight)
            return false;
        treeHeight = validHeight;
        return true;
    }


    /* --- BRANCH + LOGS/WOOD --- */

    protected static Block randomBranchEnd(Block base, Branch b, double angleDown, double angleUp, double distance, double spread) {
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
    protected static List<Pair<Block, Axis>> connectLogs(Block base, Block last, Block... additionalBases) {
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
        double distance_length = distance.length();
        while (distance_length > offset.length()) {
            Block current = last.getRelative(offset.getBlockX(), offset.getBlockY(), offset.getBlockZ());
            for (var baseOffset : additionalBaseOffsets) {
                Block add = current.getRelative(baseOffset.getBlockX(), baseOffset.getBlockY(), baseOffset.getBlockZ());
                result.add(new Pair<>(add, axis));
            }
            if (!current.getLocation().equals(base.getLocation()))
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
        if (!log.isEmpty() && !PluginCategories.isLeaves(log.getType()) &&
                log.getType() != saplingType && log.getType() != woodType)
            return;
        log.setType(logType);
        if (log.getBlockData() instanceof Orientable o) {
            o.setAxis(axis);
            log.setBlockData(o);
        }
    }

    protected void setWood(Block log, Axis axis, boolean replaceLog) {
        if (!log.isEmpty() && !PluginCategories.isLeaves(log.getType()) &&
                log.getType() != saplingType && !(log.getType() == logType && replaceLog))
            return;
        log.setType(woodType);
        if (log.getBlockData() instanceof Orientable o) {
            o.setAxis(axis);
            log.setBlockData(o);
        }
    }

    protected void setRoot(Block log) {
        if (!isRootTarget(log))
            return;
        log.setType(woodType);
        if (log.getBlockData() instanceof Orientable o) {
            o.setAxis(Axis.Y);
            log.setBlockData(o);
        }
    }

    protected void generateSphereLeaves(Block center, int radius) {
        assert radius > 0;
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                for (int x = -radius; x <= radius; x++)
                    for (int y = -radius; y <= radius; y++)
                        for (int z = -radius; z <= radius; z++) {
                            double currDist = Math.sqrt(x * x + y * y + z * z);
                            if (currDist < (double) radius) {
                                setLeaves(center.getRelative(x, y, z));
                            }
                        }
            }
        };
        runnable.runTaskLater(StadtServer.getInstance(), 1L);
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

    protected boolean isRootTarget(Block block) {
        return block.isEmpty()
                || PluginCategories.isLeaves(block.getType())
                || PluginCategories.isSapling(block.getType())
                || switch (block.getType()) {
            case DIRT, GRASS_BLOCK, PODZOL, MYCELIUM, ROOTED_DIRT,
                    GRASS, TALL_GRASS, WATER, VINE, SAND, GRAVEL,
                    RED_SAND, NETHERRACK, CRIMSON_NYLIUM, WARPED_NYLIUM -> true;
            default -> false;
        };
    }

    public abstract void generateTree();
}
