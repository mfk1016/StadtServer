package me.mfk1016.stadtserver.ritual;

import org.bukkit.block.Block;

import java.util.Arrays;

public class ShapeUtils {

    public static Block[] horizontalBlocks(Block center, int absX, int modY, int absZ) {
        return new Block[] {
                center.getRelative(-absX, modY, -absZ),
                center.getRelative(-absX, modY, absZ),
                center.getRelative(absX, modY, -absZ),
                center.getRelative(absX, modY, absZ)
        };
    }

    public static Block[] horizontalBlockOctagon(Block center, int absX, int modY, int absZ) {
        Block[] blocks1 = horizontalBlocks(center, absX, modY, absZ);
        Block[] blocks2 = horizontalBlocks(center, absZ, modY, absX);
        Block[] result = Arrays.copyOf(blocks1, blocks1.length + blocks2.length);
        System.arraycopy(blocks2, 0, result, blocks1.length, blocks2.length);
        return result;
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
