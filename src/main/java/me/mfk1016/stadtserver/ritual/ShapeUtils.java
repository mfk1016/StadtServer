package me.mfk1016.stadtserver.ritual;

import org.bukkit.block.Block;

public class ShapeUtils {

    public static Block[] horizontalBlocks(Block center, int absX, int modY, int absZ) {
        return new Block[] {
                center.getRelative(-absX, modY, -absZ),
                center.getRelative(-absX, modY, absZ),
                center.getRelative(absX, modY, -absZ),
                center.getRelative(absX, modY, absZ)
        };
    }

    public static Block[] horizontalCornerBlocks(Block center, int absXZ, int modY) {
        return horizontalBlocks(center, absXZ, modY, absXZ);
    }

    public static Block[] horizontalLineBlocks(Block center, int absXZ, int modY) {
        return new Block[] {
                center.getRelative(-absXZ, modY, 0),
                center.getRelative(absXZ, modY, 0),
                center.getRelative(0, modY, -absXZ),
                center.getRelative(0, modY, absXZ)
        };
    }
}
