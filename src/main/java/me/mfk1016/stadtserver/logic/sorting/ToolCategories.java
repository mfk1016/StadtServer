package me.mfk1016.stadtserver.logic.sorting;

import org.bukkit.Material;

public class ToolCategories {

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

    public static boolean isMiscWeapon(Material material) {
        return switch (material) {
            case BOW, CROSSBOW, TRIDENT, SHIELD -> true;
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

    public static boolean isMiscTool(Material material) {
        return switch (material) {
            case FISHING_ROD, SHEARS, FLINT_AND_STEEL, COMPASS, CLOCK, SPYGLASS,
                    CARROT_ON_A_STICK, WARPED_FUNGUS_ON_A_STICK -> true;
            default -> false;
        };
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
                    GOLDEN_CHESTPLATE, DIAMOND_CHESTPLATE, NETHERITE_CHESTPLATE, ELYTRA -> true;
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

    public static boolean isPotion(Material material) {
        return switch (material) {
            case POTION, SPLASH_POTION, LINGERING_POTION -> true;
            default -> false;
        };
    }

    public static boolean isArrow(Material material) {
        return switch (material) {
            case ARROW, SPECTRAL_ARROW, TIPPED_ARROW -> true;
            default -> false;
        };
    }

    public static boolean isProjectile(Material material) {
        return switch (material) {
            case FIRE_CHARGE, ENDER_PEARL, ENDER_EYE -> true;
            default -> false;
        };
    }

    public static boolean isMiscStackTool(Material material) {
        return switch (material) {
            case SOUL_TORCH, END_ROD, LEAD, ITEM_FRAME, BOOK, LADDER,
                    KNOWLEDGE_BOOK, WRITABLE_BOOK, WRITTEN_BOOK, SCAFFOLDING, SPONGE, WET_SPONGE, TNT -> true;
            default -> false;
        };
    }

    public static boolean isTool(Material material) {
        return material.name().toLowerCase().contains("disc") || switch (material) {
                case DIAMOND_HORSE_ARMOR, GOLDEN_HORSE_ARMOR, IRON_HORSE_ARMOR, LEATHER_HORSE_ARMOR, SADDLE -> true;
                default -> false;
            };
    }
}
