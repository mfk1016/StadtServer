package me.mfk1016.stadtserver.logic.sorting;

import org.bukkit.Material;

public class BuildCategories {

    public static boolean isFurniture(Material material) {
        return switch (material) {
            case CHAIN, FLOWER_POT, PAINTING, JACK_O_LANTERN, LANTERN, SOUL_LANTERN, IRON_BARS -> true;
            default -> false;
        };
    }


    public static boolean isStone(Material material) {
        return switch (material) {
            case STONE, STONE_SLAB, STONE_STAIRS, SMOOTH_STONE, SMOOTH_STONE_SLAB, INFESTED_STONE,
                    STONE_BRICKS, STONE_BRICK_SLAB, STONE_BRICK_STAIRS, STONE_BRICK_WALL,
                    MOSSY_STONE_BRICKS, MOSSY_STONE_BRICK_SLAB, MOSSY_STONE_BRICK_STAIRS, CRACKED_STONE_BRICKS,
                    CHISELED_STONE_BRICKS, INFESTED_STONE_BRICKS, INFESTED_CHISELED_STONE_BRICKS,
                    INFESTED_CRACKED_STONE_BRICKS, INFESTED_MOSSY_STONE_BRICKS -> true;
            default -> false;
        };
    }

    public static boolean isNormalStone(Material material) {
        return switch (material) {
            case TUFF, CALCITE, MAGMA_BLOCK -> true;
            default -> false;
        };
    }


    public static boolean isNetherStone(Material material) {
        return switch (material) {
            case BASALT, POLISHED_BASALT, SMOOTH_BASALT -> true;
            default -> false;
        };
    }


    public static boolean isSoil(Material material) {
        return switch (material) {
            case DIRT, COARSE_DIRT, GRASS_BLOCK, PODZOL, MYCELIUM, GRAVEL, SAND, RED_SAND, SNOW_BLOCK, SNOWBALL, SNOW, POWDER_SNOW,
                    SOUL_SAND, SOUL_SOIL -> true;
            default -> false;
        };
    }

    public static boolean isClay(Material material) {
        String key = material.name();
        if (key.contains("TERRACOTTA"))
            return true;
        return switch (material) {
            case CLAY, CLAY_BALL, BRICK, BRICK_SLAB, BRICKS, BRICK_STAIRS, BRICK_WALL -> true;
            default -> false;
        };
    }
}
