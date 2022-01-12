package me.mfk1016.stadtserver.logic.sorting;

import org.bukkit.Material;

public class FarmCategories {

    public static boolean isLog(Material material) {
        return switch (material) {
            case ACACIA_LOG, BIRCH_LOG, DARK_OAK_LOG, JUNGLE_LOG,
                    OAK_LOG, SPRUCE_LOG, CRIMSON_STEM, WARPED_STEM -> true;
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
}
