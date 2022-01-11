package me.mfk1016.stadtserver.logic.sorting;

import org.bukkit.Material;

public class WorkCategories {

    public static boolean isMiscVehicle(Material material) {
        return switch (material) {
            case RAIL, POWERED_RAIL, ACTIVATOR_RAIL, DETECTOR_RAIL -> true;
            default -> false;
        };
    }

    public static boolean isRedstoneBasic(Material material) {
        return switch (material) {
            case REDSTONE, REDSTONE_BLOCK, REDSTONE_TORCH, LEVER, REDSTONE_LAMP,
                    NOTE_BLOCK, BELL, SCULK_SENSOR, REPEATER, COMPARATOR, COMMAND_BLOCK,
                    TRIPWIRE_HOOK, TARGET, DISPENSER, DROPPER, OBSERVER, DAYLIGHT_DETECTOR, LIGHTNING_ROD -> true;
            default -> false;
        };
    }

    public static boolean isStation(Material material) {
        return switch (material) {
            case CRAFTING_TABLE, FURNACE, BLAST_FURNACE, SMOKER, SMITHING_TABLE, CARTOGRAPHY_TABLE, FLETCHING_TABLE,
                    GRINDSTONE, ENCHANTING_TABLE, BREWING_STAND, ANVIL, LOOM, COMPOSTER, CAULDRON, LAVA_CAULDRON, LECTERN,
                    STONECUTTER, JUKEBOX, CAMPFIRE, SOUL_CAMPFIRE, BEACON, LODESTONE, CONDUIT, BOOKSHELF -> true;
            default -> false;
        };
    }

    public static boolean isInventory(Material material) {
        return switch (material) {
            case CHEST, TRAPPED_CHEST, ENDER_CHEST, BARREL, HOPPER -> true;
            default -> false;
        };
    }
}
