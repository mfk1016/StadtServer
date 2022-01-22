package me.mfk1016.stadtserver.logic.sorting;

import org.bukkit.Material;

public class PluginCategories {

    // For internal use

    public static boolean isShovel(Material material) {
        return switch (material) {
            case WOODEN_SHOVEL, STONE_SHOVEL, IRON_SHOVEL, GOLDEN_SHOVEL, DIAMOND_SHOVEL, NETHERITE_SHOVEL -> true;
            default -> false;
        };
    }

    public static boolean isSword(Material material) {
        return switch (material) {
            case WOODEN_SWORD, STONE_SWORD, IRON_SWORD, GOLDEN_SWORD, DIAMOND_SWORD, NETHERITE_SWORD -> true;
            default -> false;
        };
    }

    public static boolean isAxe(Material material) {
        return switch (material) {
            case WOODEN_AXE, STONE_AXE, IRON_AXE, GOLDEN_AXE, DIAMOND_AXE, NETHERITE_AXE -> true;
            default -> false;
        };
    }

    public static boolean isHoe(Material material) {
        return switch (material) {
            case WOODEN_HOE, STONE_HOE, IRON_HOE, GOLDEN_HOE, DIAMOND_HOE, NETHERITE_HOE -> true;
            default -> false;
        };
    }

    public static boolean isLog(Material material) {
        return switch (material) {
            case ACACIA_LOG, BIRCH_LOG, DARK_OAK_LOG, JUNGLE_LOG,
                    OAK_LOG, SPRUCE_LOG, CRIMSON_STEM, WARPED_STEM -> true;
            default -> false;
        };
    }

    public static boolean isWood(Material material) {
        return switch (material) {
            case ACACIA_WOOD, BIRCH_WOOD, DARK_OAK_WOOD, JUNGLE_WOOD,
                    OAK_WOOD, SPRUCE_WOOD, CRIMSON_HYPHAE, WARPED_HYPHAE, SHROOMLIGHT -> true;
            default -> false;
        };
    }

    public static boolean isPlanks(Material material) {
        return switch (material) {
            case ACACIA_PLANKS, BIRCH_PLANKS, DARK_OAK_PLANKS, JUNGLE_PLANKS,
                    OAK_PLANKS, SPRUCE_PLANKS, CRIMSON_PLANKS, WARPED_PLANKS -> true;
            default -> false;
        };
    }

    public static boolean isLeaves(Material material) {
        return switch (material) {
            case BIRCH_LEAVES, ACACIA_LEAVES, AZALEA_LEAVES, JUNGLE_LEAVES, OAK_LEAVES, SPRUCE_LEAVES,
                    DARK_OAK_LEAVES, FLOWERING_AZALEA_LEAVES, NETHER_WART_BLOCK, WARPED_WART_BLOCK -> true;
            default -> false;
        };
    }

    public static boolean isSapling(Material material) {
        return switch (material) {
            case BIRCH_SAPLING, ACACIA_SAPLING, OAK_SAPLING, JUNGLE_SAPLING, DARK_OAK_SAPLING, SPRUCE_SAPLING,
                    WARPED_FUNGUS, CRIMSON_FUNGUS, RED_MUSHROOM, BROWN_MUSHROOM -> true;
            default -> false;
        };
    }

    public static boolean isMushroomBlock(Material material) {
        return switch (material) {
            case NETHER_WART_BLOCK, WARPED_WART_BLOCK, SHROOMLIGHT, MUSHROOM_STEM,
                    BROWN_MUSHROOM_BLOCK, RED_MUSHROOM_BLOCK -> true;
            default -> false;
        };
    }
}
