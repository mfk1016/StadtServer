package me.mfk1016.stadtserver.logic;

import org.bukkit.Material;

public class MaterialTypes {

    // TOOLS
    public static boolean isTool(Material material) {
        String key = material.name().toLowerCase();
        if (key.contains("sign") || key.contains("disc")) {
            return true;
        }
        return isPickaxe(material) ||
                isShovel(material) ||
                isSword(material) ||
                isAxe(material) ||
                isHoe(material) ||
                switch (material) {
                    case BOW, CROSSBOW, FISHING_ROD, SHEARS, FLINT_AND_STEEL, COMPASS, CLOCK, SPYGLASS, TRIDENT -> true;
                    default -> false;
                };
    }

    public static boolean isStackTool(Material material) {
        String key = material.name().toLowerCase();
        if (key.contains("candle")) {
            return true;
        }
        return isProjectile(material) || switch (material) {
            case TORCH, SOUL_TORCH, LANTERN, SOUL_LANTERN, SEA_PICKLE, END_ROD, JACK_O_LANTERN, LEAD,
                    BUCKET, WATER_BUCKET, LAVA_BUCKET, PAINTING, ITEM_FRAME, MAP, FILLED_MAP, BOOK, ENCHANTED_BOOK,
                    KNOWLEDGE_BOOK, WRITABLE_BOOK, WRITTEN_BOOK -> true;
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

    public static boolean isProjectile(Material material) {
        if (material.name().toLowerCase().contains("arrow")) {
            return true;
        }
        return switch (material) {
            case FIREWORK_ROCKET, FIREWORK_STAR, FIRE_CHARGE, SNOWBALL, EGG, ENDER_PEARL, ENDER_EYE -> true;
            default -> false;
        };
    }

    // ARMOR
    public static boolean isArmor(Material material) {
        return isHelmet(material) ||
                isChestplate(material) ||
                isLeggins(material) ||
                isBoots(material) ||
                isMount(material) ||
                material == Material.SHIELD ||
                material == Material.ELYTRA;
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

    public static boolean isMount(Material material) {
        return switch (material) {
            case DIAMOND_HORSE_ARMOR, GOLDEN_HORSE_ARMOR, IRON_HORSE_ARMOR, LEATHER_HORSE_ARMOR, SADDLE, CARROT_ON_A_STICK,
                    WARPED_FUNGUS_ON_A_STICK -> true;
            default -> false;
        };
    }

    // SPECIAL
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

    // REDSTONE
    public static boolean isRedstone(Material material) {
        String key = material.name().toLowerCase();
        if (key.contains("button") || key.contains("pressure_plate"))
            return true;
        return switch (material) {
            case REDSTONE, REDSTONE_LAMP, REDSTONE_BLOCK, REDSTONE_TORCH, LEVER, PISTON, STICKY_PISTON, HOPPER,
                    TRAPPED_CHEST, CHEST, BARREL, NOTE_BLOCK, BELL, SCULK_SENSOR, REPEATER, COMPARATOR, COMMAND_BLOCK,
                    TRIPWIRE_HOOK, TARGET, DISPENSER, DROPPER, OBSERVER, DAYLIGHT_DETECTOR, LIGHTNING_ROD -> true;
            default -> false;
        };
    }

    public static boolean isRail(Material material) {
        return switch (material) {
            case RAIL, POWERED_RAIL, ACTIVATOR_RAIL, DETECTOR_RAIL -> true;
            default -> false;
        };
    }

    // VEHICLE / INVENTORY / FURNITURE
    public static boolean isVehicle(Material material) {
        return switch (material) {
            case BIRCH_BOAT, ACACIA_BOAT, DARK_OAK_BOAT, JUNGLE_BOAT, OAK_BOAT, SPRUCE_BOAT, MINECART,
                    CHEST_MINECART, FURNACE_MINECART, HOPPER_MINECART, TNT_MINECART, COMMAND_BLOCK_MINECART -> true;
            default -> false;
        };
    }

    public static boolean isInventory(Material material) {
        if (material.name().toLowerCase().contains("shulker_box")) {
            return true;
        }
        return switch (material) {
            case CRAFTING_TABLE, FURNACE, BLAST_FURNACE, SMOKER, SMITHING_TABLE, CARTOGRAPHY_TABLE, FLETCHING_TABLE,
                    GRINDSTONE, ENCHANTING_TABLE, BREWING_STAND, ANVIL, LOOM, CHEST, TRAPPED_CHEST, ENDER_CHEST,
                    BARREL, HOPPER, COMPOSTER, CAULDRON, LAVA_CAULDRON, DISPENSER, DROPPER, LECTERN, STONECUTTER,
                    JUKEBOX, CAMPFIRE, SOUL_CAMPFIRE, BEACON -> true;
            default -> false;
        };
    }

    public static boolean isFurniture(Material material) {
        String key = material.name().toLowerCase();
        if (key.contains("bed") || key.contains("carpet") || key.contains("banner") || key.contains("door")) {
            return true;
        }
        return switch (material) {
            case BOOKSHELF, LADDER, CHAIN, FLOWER_POT, SCAFFOLDING, ARMOR_STAND -> true;
            default -> false;
        };
    }

    // DROPS
    public static boolean isFarmingResult(Material material) {
        return switch (material) {
            case WHEAT, WHEAT_SEEDS, CARROT, POTATO, BEETROOT, BEETROOT_SEEDS, NETHER_WART, COCOA_BEANS,
                    MELON_SEEDS, MELON_SLICE, MELON, PUMPKIN_SEEDS, PUMPKIN, SUGAR_CANE, CACTUS, BAMBOO, KELP,
                    SWEET_BERRIES, APPLE, GOLDEN_APPLE, ENCHANTED_GOLDEN_APPLE, GOLDEN_CARROT, GLISTERING_MELON_SLICE,
                    CHORUS_FRUIT, CHORUS_FLOWER, GLOW_BERRIES -> true;
            default -> false;
        };
    }

    public static boolean isAnimalResult(Material material) {
        String key = material.name().toLowerCase();
        if (key.contains("wool")) {
            return true;
        }
        return switch (material) {
            case BEEF, LEATHER, PORKCHOP, HONEY_BOTTLE, HONEYCOMB, HONEY_BLOCK, HONEYCOMB_BLOCK,
                    MUTTON, CHICKEN, FEATHER, RABBIT, RABBIT_FOOT, RABBIT_HIDE, TURTLE_EGG, SCUTE, COD, SALMON, PUFFERFISH,
                    TROPICAL_FISH, INK_SAC, GLOW_INK_SAC -> true;
            default -> false;
        };
    }

    public static boolean isMonsterResult(Material material) {
        return switch (material) {
            case ROTTEN_FLESH, BONE, BONE_MEAL, STRING, SPIDER_EYE, GUNPOWDER, BLAZE_ROD, BLAZE_POWDER, SLIME_BALL, SLIME_BLOCK,
                    MAGMA_CREAM, PHANTOM_MEMBRANE, SHULKER_SHELL, DRAGON_EGG, DRAGON_BREATH, DRAGON_HEAD, GHAST_TEAR,
                    WITHER_SKELETON_SKULL, TOTEM_OF_UNDYING, ZOMBIE_HEAD, SKELETON_SKULL, CREEPER_HEAD, PLAYER_HEAD,
                    FERMENTED_SPIDER_EYE -> true;
            default -> false;
        };
    }

    public static boolean isChoppingResult(Material material) {
        String key = material.name().toLowerCase();
        if (key.contains("sapling") || key.contains("log") || key.contains("leaves")) {
            return true;
        }
        return switch (material) {
            case BROWN_MUSHROOM, RED_MUSHROOM, VINE, CRIMSON_STEM, WARPED_STEM, STRIPPED_CRIMSON_STEM, STRIPPED_WARPED_STEM,
                    CRIMSON_FUNGUS, WARPED_FUNGUS, CRIMSON_HYPHAE, WARPED_HYPHAE, MOSS_BLOCK, MOSS_CARPET, AZALEA,
                    FLOWERING_AZALEA, NETHER_WART_BLOCK, WARPED_WART_BLOCK, SHROOMLIGHT -> true;
            default -> false;
        };
    }
}
