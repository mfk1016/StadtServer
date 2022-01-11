package me.mfk1016.stadtserver.logic.sorting;

import org.bukkit.Material;

public class MineCategories {

    public static boolean isSmeltable(Material material) {
        return switch (material) {
            case COAL_ORE, COPPER_ORE, DEEPSLATE_COAL_ORE, DEEPSLATE_COPPER_ORE, DEEPSLATE_DIAMOND_ORE, DIAMOND_ORE,
                    DEEPSLATE_EMERALD_ORE, DEEPSLATE_GOLD_ORE, DEEPSLATE_IRON_ORE, DEEPSLATE_LAPIS_ORE, GOLD_ORE,
                    DEEPSLATE_REDSTONE_ORE, EMERALD_ORE, IRON_ORE, LAPIS_ORE, REDSTONE_ORE, NETHER_GOLD_ORE,
                    NETHER_QUARTZ_ORE, RAW_COPPER, RAW_GOLD, RAW_IRON, ANCIENT_DEBRIS -> true;
            default -> false;
        };
    }

    public static boolean isMine(Material material) {
        return switch (material) {
            case OBSIDIAN, CRYING_OBSIDIAN, MAGMA_BLOCK, POINTED_DRIPSTONE -> true;
            default -> false;
        };
    }
}
