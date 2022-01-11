package me.mfk1016.stadtserver.logic.sorting;

import org.bukkit.Material;

public class FarmCategories {

    public static boolean isMagicBrew(Material material) {
        return switch (material) {
            case FERMENTED_SPIDER_EYE, GOLDEN_APPLE, GOLDEN_CARROT, GLISTERING_MELON_SLICE, SUGAR, ENCHANTED_GOLDEN_APPLE,
                    GLOW_BERRIES, RABBIT_FOOT, SPIDER_EYE, NETHER_WART, MAGMA_CREAM, DRAGON_BREATH,
                    GHAST_TEAR, TOTEM_OF_UNDYING, WITHER_SKELETON_SKULL -> true;
            default -> false;
        };
    }

    public static boolean isMobDrop(Material material) {
        return switch (material) {
            case ROTTEN_FLESH, BONE, BONE_MEAL, STRING, GUNPOWDER, BLAZE_ROD, BLAZE_POWDER, SLIME_BALL, SLIME_BLOCK,
                    PHANTOM_MEMBRANE, SHULKER_SHELL, DRAGON_EGG, DRAGON_HEAD, ZOMBIE_HEAD,
                    SKELETON_SKULL, CREEPER_HEAD, PLAYER_HEAD -> true;
            default -> false;
        };
    }


    public static boolean isFood(Material material) {
        return switch (material) {
            case COOKED_BEEF, COOKED_CHICKEN, COOKED_COD, COOKED_MUTTON, COOKED_PORKCHOP, COOKED_RABBIT, COOKED_SALMON,
                    COOKIE, BREAD, BAKED_POTATO, MUSHROOM_STEW, RABBIT_STEW, BEETROOT_SOUP, CAKE,
                    PUMPKIN_PIE, SUSPICIOUS_STEW -> true;
            default -> false;
        };
    }

    public static boolean isAnimalMeat(Material material) {
        return switch (material) {
            case BEEF, PORKCHOP, MUTTON, CHICKEN, RABBIT, COD, SALMON, PUFFERFISH, TROPICAL_FISH -> true;
            default -> false;
        };
    }

    public static boolean isHoney(Material material) {
        return switch (material) {
            case HONEY_BLOCK, HONEY_BOTTLE, HONEYCOMB, HONEYCOMB_BLOCK -> true;
            default -> false;
        };
    }

    public static boolean isAnimal(Material material) {
        return switch (material) {
            case LEATHER, FEATHER, EGG, RABBIT_HIDE, TURTLE_EGG, SCUTE, INK_SAC, GLOW_INK_SAC -> true;
            default -> false;
        };
    }


    public static boolean isSeedCrop(Material material) {
        return switch (material) {
            case WHEAT, CARROT, POTATO, BEETROOT, MELON_SLICE, MELON, PUMPKIN, APPLE -> true;
            default -> false;
        };
    }

    public static boolean isGrowPlant(Material material) {
        return switch (material) {
            case COCOA_BEANS, SUGAR_CANE, CACTUS, BAMBOO, KELP, SEA_PICKLE, SWEET_BERRIES,
                    CHORUS_FRUIT, CHORUS_FLOWER -> true;
            default -> false;
        };
    }

    public static boolean isSeed(Material material) {
        return switch (material) {
            case WHEAT_SEEDS, BEETROOT_SEEDS, MELON_SEEDS, PUMPKIN_SEEDS -> true;
            default -> false;
        };
    }

    public static boolean isPlant(Material material) {
        return switch (material) {
            case DANDELION, POPPY, ORANGE_TULIP, PINK_TULIP, RED_TULIP, WHITE_TULIP, ROSE_BUSH, WITHER_ROSE,
                    CORNFLOWER, BLUE_ORCHID, LILAC, LILY_OF_THE_VALLEY, ALLIUM, AZURE_BLUET, OXEYE_DAISY, SUNFLOWER,
                    PEONY, VINE, LILY_PAD, SEA_PICKLE, BIG_DRIPLEAF, SMALL_DRIPLEAF, BIG_DRIPLEAF_STEM,
                    GRASS, TALL_GRASS, SEAGRASS, TALL_SEAGRASS, MOSS_BLOCK, MOSS_CARPET, FERN, LARGE_FERN,
                    SPORE_BLOSSOM, CRIMSON_HYPHAE, WARPED_HYPHAE -> true;
            default -> false;
        };
    }


    public static boolean isSapling(Material material) {
        return switch (material) {
            case SPRUCE_SAPLING, ACACIA_SAPLING, BIRCH_SAPLING, OAK_SAPLING, JUNGLE_SAPLING, DARK_OAK_SAPLING,
                    AZALEA, FLOWERING_AZALEA, WARPED_FUNGUS, CRIMSON_FUNGUS, BROWN_MUSHROOM, RED_MUSHROOM -> true;
            default -> false;
        };
    }

    public static boolean isLeaves(Material material) {
        return switch (material) {
            case ACACIA_LEAVES, BIRCH_LEAVES, AZALEA_LEAVES, JUNGLE_LEAVES, OAK_LEAVES, SPRUCE_LEAVES, DARK_OAK_LEAVES,
                    FLOWERING_AZALEA_LEAVES, WARPED_WART_BLOCK, NETHER_WART_BLOCK, SHROOMLIGHT, MUSHROOM_STEM,
                    BROWN_MUSHROOM_BLOCK, RED_MUSHROOM_BLOCK -> true;
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

    public static boolean isPlanks(Material material) {
        return switch (material) {
            case ACACIA_PLANKS, BIRCH_PLANKS, DARK_OAK_PLANKS, JUNGLE_PLANKS,
                    OAK_PLANKS, SPRUCE_PLANKS, CRIMSON_PLANKS, WARPED_PLANKS -> true;
            default -> false;
        };
    }
}
