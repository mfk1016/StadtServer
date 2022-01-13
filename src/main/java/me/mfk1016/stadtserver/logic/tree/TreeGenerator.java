package me.mfk1016.stadtserver.logic.tree;

import me.mfk1016.stadtserver.logic.sorting.PluginCategories;
import me.mfk1016.stadtserver.util.Pair;
import org.bukkit.Axis;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Orientable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class TreeGenerator {

    protected final Block nwBase;
    protected int treeHeight;
    protected final int minHeight;
    protected final Material logType;
    protected final Material leavesType;
    protected final Material saplingType;
    protected int checkSquare = 3;

    public TreeGenerator(Block nwBase, int treeHeight, int minHeight, Material log, Material leaves, Material sapling) {
        this.nwBase = nwBase;
        this.treeHeight = treeHeight;
        this.minHeight = minHeight;
        this.logType = log;
        this.leavesType = leaves;
        this.saplingType = sapling;
    }

    public boolean isEnoughSpace() {
        int validHeight = 1;
        // Get the north west corner of the check area
        Block corner = nwBase;
        if (checkSquare > 2) {
            int offset = (checkSquare - 1) / 2;
            corner = nwBase.getRelative(-offset, 0, -offset);
        }
        for (int x = 0; x < checkSquare; x++) {
            for (int z = 0; z < checkSquare; z++) {
                for (int y = 0; y < treeHeight; y++) {
                    Block current = corner.getRelative(x, y, z);
                    if (PluginCategories.isLog(current.getType()) || PluginCategories.isWood(current.getType()))
                        continue;
                    if (current.getType().isOccluding())
                        break;
                }
                validHeight++;
            }
        }
        if (validHeight < minHeight)
            return false;
        else if (validHeight == treeHeight)
            return true;
        treeHeight = validHeight;
        return true;
    }

    protected List<Pair<Block, Axis>> connectLogs(Block base, Block last) {
        List<Pair<Block, Axis>> result = new ArrayList<>();
        Vector distance = base.getLocation().toVector().subtract(last.getLocation().toVector());
        assert distance.length() > 0;
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
        Vector offset = new Vector();
        while (distance.length() > offset.length()) {
            Block current = last.getRelative(offset.getBlockX(), offset.getBlockY(), offset.getBlockZ());
            result.add(new Pair<>(current, axis));
            offset.add(new Vector(xstep, ystep, zstep));
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

    protected void setLeaves(Block leaves) {
        if (!leaves.isEmpty())
            return;
        leaves.setType(leavesType);
    }
}
