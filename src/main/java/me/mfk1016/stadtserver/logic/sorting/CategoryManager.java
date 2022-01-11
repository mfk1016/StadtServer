package me.mfk1016.stadtserver.logic.sorting;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class CategoryManager {

    private static ItemCategory root = new ItemCategory("", "", null);

    public static ItemCategory matchCategory(Material mat) {
        return root.matchCategory(mat);
    }

    public static int cmp(Material a, Material b) {
        return root.cmp(a, b);
    }

    private static void addCategory(@NotNull String key, @Nullable Predicate<Material> memberFunction) {
        root.addSubCategory(key, memberFunction);
    }

    private static Predicate<Material> singleMatch(Material material) {
        return mat -> mat == material;
    }

    private static Predicate<Material> nameMatch(String key) {
        return mat -> mat.name().contains(key);
    }

    public static void initialize() {
        root = new ItemCategory("", "", null);

        // Order is preserved
        addCategory("stick", singleMatch(Material.STICK));
        addCategory("torch", singleMatch(Material.TORCH));

        addCategory("tool.pickaxe", ToolCategories::isPickaxe);
        addCategory("tool.shovel", ToolCategories::isShovel);
        addCategory("tool.axe", ToolCategories::isAxe);
        addCategory("tool.weapon.sword", ToolCategories::isSword);
        addCategory("tool.weapon", ToolCategories::isMiscWeapon);
        addCategory("tool.hoe", ToolCategories::isHoe);
        addCategory("tool.misc", ToolCategories::isMiscTool);

        addCategory("tool.armor.helmet", ToolCategories::isHelmet);
        addCategory("tool.armor.chest", ToolCategories::isChestplate);
        addCategory("tool.armor.leggins", ToolCategories::isLeggins);
        addCategory("tool.armor.boots", ToolCategories::isBoots);
        addCategory("tool.armor", singleMatch(Material.ARMOR_STAND));

        addCategory("tool.fillmap", singleMatch(Material.FILLED_MAP));
        addCategory("tool.emptymap", singleMatch(Material.MAP));
        addCategory("tool.enchbook", singleMatch(Material.ENCHANTED_BOOK));
        addCategory("tool.bottle", singleMatch(Material.GLASS_BOTTLE));
        addCategory("tool.potion", ToolCategories::isPotion);

        addCategory("tool.stack.sign", nameMatch("SIGN"));
        addCategory("tool.stack.bucket", nameMatch("BUCKET"));
        addCategory("tool.stack.misc", ToolCategories::isMiscStackTool);
        addCategory("tool.stack", nameMatch("CANDLE"));
        addCategory("tool.projectile.arrow", ToolCategories::isArrow);
        addCategory("tool.projectile", ToolCategories::isProjectile);
        addCategory("tool", ToolCategories::isTool);


        addCategory("work.station", WorkCategories::isStation);
        addCategory("work.shulker", nameMatch("SHULKER_BOX"));
        addCategory("work.inventory", WorkCategories::isInventory);

        addCategory("work.vehicle.boat", nameMatch("BOAT"));
        addCategory("work.vehicle.minecart", nameMatch("MINECART"));
        addCategory("work.vehicle", WorkCategories::isMiscVehicle);

        addCategory("work.redstone.basic", WorkCategories::isRedstoneBasic);
        addCategory("work.redstone.piston", singleMatch(Material.PISTON));
        addCategory("work.redstone.sticky", singleMatch(Material.STICKY_PISTON));
        addCategory("work.redstone.button", nameMatch("BUTTON"));
        addCategory("work.redstone", nameMatch("PRESSURE_PLATE"));

        addCategory("work.trapdoor", nameMatch("TRAPDOOR"));
        addCategory("work.door", nameMatch("DOOR"));
        addCategory("work.dye", nameMatch("DYE"));
        addCategory("work.bed", nameMatch("BED"));
        addCategory("work.banner", nameMatch("BANNER"));

        addCategory("magic", FarmCategories::isMagicBrew);

        addCategory("mine.coal", nameMatch("COAL"));
        addCategory("mine.smelt", MineCategories::isSmeltable);
        addCategory("mine.netherite", nameMatch("NETHERITE"));
        addCategory("mine.diamond", nameMatch("DIAMOND"));
        addCategory("mine.gold", nameMatch("GOLD"));
        addCategory("mine.iron", nameMatch("IRON"));
        addCategory("mine.copper", nameMatch("COPPER"));
        addCategory("mine.quartz", nameMatch("QUARTZ"));
        addCategory("mine.glow", nameMatch("GLOWSTONE"));
        addCategory("mine.lapis", nameMatch("LAPIS"));
        addCategory("mine.amethyst", nameMatch("AMETHYST"));
        addCategory("mine", MineCategories::isMine);


        addCategory("farm.monster", FarmCategories::isMobDrop);

        addCategory("farm.food", FarmCategories::isFood);
        addCategory("farm.animal.meat", FarmCategories::isAnimalMeat);
        addCategory("farm.animal.honey", FarmCategories::isHoney);
        addCategory("farm.animal.wool", nameMatch("WOOL"));
        addCategory("farm.animal", FarmCategories::isAnimal);

        addCategory("farm.plant.crop", FarmCategories::isSeedCrop);
        addCategory("farm.plant.grow", FarmCategories::isGrowPlant);
        addCategory("farm.plant.seed", FarmCategories::isSeed);
        addCategory("farm.plant.coral", nameMatch("CORAL"));
        addCategory("farm.plant", FarmCategories::isPlant);

        addCategory("farm.tree.sapling", FarmCategories::isSapling);
        addCategory("farm.tree.leaves", FarmCategories::isLeaves);
        addCategory("farm.tree.dark_oak", nameMatch("DARK_OAK"));
        addCategory("farm.tree.oak", nameMatch("OAK"));
        addCategory("farm.tree.birch", nameMatch("BIRCH"));
        addCategory("farm.tree.spruce", nameMatch("SPRUCE"));
        addCategory("farm.tree.acacia", nameMatch("ACACIA"));
        addCategory("farm.tree.jungle", nameMatch("JUNGLE"));
        addCategory("farm.tree.crimson", nameMatch("CRIMSON"));
        addCategory("farm.tree", nameMatch("WARPED"));


        addCategory("build.furniture", BuildCategories::isFurniture);
        addCategory("build.carpet", nameMatch("CARPET"));

        addCategory("build.normal.cobble", nameMatch("COBBLESTONE"));
        addCategory("build.normal.stone", BuildCategories::isStone);
        addCategory("build.normal.deepslate", nameMatch("DEEPSLATE"));
        addCategory("build.normal.granite", nameMatch("GRANITE"));
        addCategory("build.normal.diorite", nameMatch("DIORITE"));
        addCategory("build.normal.andesite", nameMatch("ANDESITE"));
        addCategory("build.normal", BuildCategories::isNormalStone);

        addCategory("build.nether.rack", singleMatch(Material.NETHERRACK));
        addCategory("build.nether.brick", nameMatch("NETHER_BRICK"));
        addCategory("build.nether.quartz", nameMatch("QUARTZ"));
        addCategory("build.nether.blackstone", nameMatch("BLACKSTONE"));
        addCategory("build.nether", BuildCategories::isNetherStone);

        addCategory("build.end.stone", nameMatch("END_STONE"));
        addCategory("build.end.purpur", nameMatch("PURPUR"));

        addCategory("build.soil", BuildCategories::isSoil);
        addCategory("build.clay", BuildCategories::isClay);
        addCategory("build.concrete", nameMatch("CONCRETE"));
        addCategory("build.red_sandstone", nameMatch("RED_SANDSTONE"));
        addCategory("build.sandstone", nameMatch("SANDSTONE"));
        addCategory("build.glass", nameMatch("GLASS"));
        addCategory("build.prismarine.lantern", singleMatch(Material.SEA_LANTERN));
        addCategory("build.prismarine", nameMatch("PRISMARINE"));
    }
}
