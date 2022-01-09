package me.mfk1016.stadtserver.logic;

import org.bukkit.Material;

public class MaterialTypes {

    /* --- TOOLS --- */

    public static boolean isTool(Material material) {
        return isPickaxe(material) ||
                isShovel(material) ||
                isSword(material) ||
                isAxe(material) ||
                isHoe(material) ||
                switch (material) {
                    case BOW, CROSSBOW, FISHING_ROD, SHEARS, FLINT_AND_STEEL, LEAD, COMPASS, CLOCK -> true;
                    default -> false;
                };
    }

    public static boolean isPickaxe(Material material) {
        return switch (material) {
            case WOODEN_PICKAXE, STONE_PICKAXE, IRON_PICKAXE, GOLDEN_PICKAXE, DIAMOND_PICKAXE, NETHERITE_PICKAXE -> true;
            default -> false;
        };
    }

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

    /* --- ARMOR --- */

    public static boolean isArmor(Material material) {
        return isHelmet(material) ||
                isChestplate(material) ||
                isLeggins(material) ||
                isBoots(material) ||
                material == Material.SHIELD;
    }

    public static boolean isHelmet(Material material) {
        return switch (material) {
            case LEATHER_HELMET, CHAINMAIL_HELMET, IRON_HELMET,
                    GOLDEN_HELMET, DIAMOND_HELMET, NETHERITE_HELMET, TURTLE_HELMET -> true;
            default -> false;
        };
    }

    public static boolean isChestplate(Material material) {
        return switch (material) {
            case LEATHER_CHESTPLATE, CHAINMAIL_CHESTPLATE, IRON_CHESTPLATE,
                    GOLDEN_CHESTPLATE, DIAMOND_CHESTPLATE, NETHERITE_CHESTPLATE -> true;
            default -> false;
        };
    }

    public static boolean isLeggins(Material material) {
        return switch (material) {
            case LEATHER_LEGGINGS, CHAINMAIL_LEGGINGS, IRON_LEGGINGS,
                    GOLDEN_LEGGINGS, DIAMOND_LEGGINGS, NETHERITE_LEGGINGS -> true;
            default -> false;
        };
    }

    public static boolean isBoots(Material material) {
        return switch (material) {
            case LEATHER_BOOTS, CHAINMAIL_BOOTS, IRON_BOOTS,
                    GOLDEN_BOOTS, DIAMOND_BOOTS, NETHERITE_BOOTS -> true;
            default -> false;
        };
    }

    /* --- WOODEN STUFF --- */

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

    public static boolean isAnvilStone(Material material) {
        return switch (material) {
            case COBBLESTONE, COBBLED_DEEPSLATE, BLACKSTONE, GRANITE,
                    DIORITE, ANDESITE -> true;
            default -> false;
        };
    }
}
