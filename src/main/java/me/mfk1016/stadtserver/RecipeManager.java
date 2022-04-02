package me.mfk1016.stadtserver;

import me.mfk1016.stadtserver.brewing.BrewingRecipe;
import me.mfk1016.stadtserver.brewing.recipe.*;
import me.mfk1016.stadtserver.spells.SpellManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static me.mfk1016.stadtserver.util.Functions.stackEmpty;

public class RecipeManager {

    private static final List<BrewingRecipe> ALL_BREWING_RECIPES = new ArrayList<>();
    private static final int FURNACE = 0;
    private static final int SMOKER = 1;
    private static final int BLAST = 2;


    public static void registerRecipes() {
        for (SmeltingRecipes recipe : SmeltingRecipes.values()) {
            Bukkit.addRecipe(recipe.toRecipe());
        }
        for (SlabToBlockRecipes recipe : SlabToBlockRecipes.values()) {
            Bukkit.addRecipe(recipe.toRecipe());
        }
        for (StoneCutterRecipes recipe : StoneCutterRecipes.values()) {
            Bukkit.addRecipe(recipe.toRecipe());
        }
        for (SmithingRecipe wrenchRecipe : getWrenchRecipes()) {
            Bukkit.addRecipe(wrenchRecipe);
        }
        Bukkit.addRecipe(getMediumRecipe());

        SpellManager.ALL_SPELLS.forEach((spell) -> {
            for (Recipe recipe : spell.getRecipes())
                Bukkit.addRecipe(recipe);
        });

        // Clay block to clay ball
        var clayBlockToBall = new ShapelessRecipe(new NamespacedKey(StadtServer.getInstance(), "clay_block_to_ball"), new ItemStack(Material.CLAY_BALL, 4));
        clayBlockToBall.addIngredient(1, Material.CLAY);
        Bukkit.addRecipe(clayBlockToBall);

        if (ALL_BREWING_RECIPES.isEmpty()) {
            ALL_BREWING_RECIPES.add(new WarpedFungusRecipe());
            ALL_BREWING_RECIPES.add(new AmethystShardRecipe());

            ALL_BREWING_RECIPES.add(new StaminaRecipe());
            ALL_BREWING_RECIPES.add(new ShieldingRecipe());

            ALL_BREWING_RECIPES.add(new CustomAmplifyRecipe());
            ALL_BREWING_RECIPES.add(new CustomExtendRecipe());
            ALL_BREWING_RECIPES.add(new CustomCorruptRecipe());
            ALL_BREWING_RECIPES.add(new CustomSplashRecipe());
            ALL_BREWING_RECIPES.add(new CustomLingerRecipe());
            ALL_BREWING_RECIPES.add(new DyePotionRecipe());
        }
    }

    public static void unregisterBrewingRecipes() {
        ALL_BREWING_RECIPES.clear();
    }

    public static BrewingRecipe byID(String id) {
        for (var recipe : ALL_BREWING_RECIPES)
            if (recipe.getRecipeID().equals(id))
                return recipe;
        return null;
    }

    public static BrewingRecipe matchBrewingRecipe(BrewerInventory inventory, boolean weakMatch) {

        ItemStack ingredient = inventory.getIngredient();
        if (ingredient == null)
            return null;

        for (var recipe : ALL_BREWING_RECIPES) {
            boolean match = false;
            int matchCount = 0;
            for (int i = 0; i < 3; i++) {
                ItemStack input = inventory.getItem(i);
                if (stackEmpty(input)) {
                    matchCount++;
                } else if (recipe.isApplicable(input, ingredient.getType())) {
                    match = true;
                    matchCount++;
                }
            }
            if (match && (weakMatch || matchCount == 3)) {
                return recipe;
            }
        }
        return null;
    }

    private static List<SmithingRecipe> getWrenchRecipes() {
        List<SmithingRecipe> wrenchRecipes = new ArrayList<>();
        RecipeChoice wrenchAdditive = new RecipeChoice.ExactChoice(new ItemStack(Material.COPPER_INGOT));
        RecipeChoice trowelAdditive = new RecipeChoice.ExactChoice(new ItemStack(Material.GOLD_INGOT));
        for (Material shovelMaterial : Arrays.asList(Material.WOODEN_SHOVEL,
                Material.STONE_SHOVEL,
                Material.IRON_SHOVEL,
                Material.GOLDEN_SHOVEL,
                Material.DIAMOND_SHOVEL,
                Material.NETHERITE_SHOVEL)) {
            ItemStack shovel = new ItemStack(shovelMaterial);
            RecipeChoice base = new RecipeChoice.MaterialChoice(shovelMaterial);

            NamespacedKey wrenchRecipeKey = new NamespacedKey(StadtServer.getInstance(), "wrenchRecipe_" + shovelMaterial.name());
            wrenchRecipes.add(new SmithingRecipe(wrenchRecipeKey, shovel, base, wrenchAdditive));
            NamespacedKey trowelRecipeKey = new NamespacedKey(StadtServer.getInstance(), "trowelRecipe_" + shovelMaterial.name());
            wrenchRecipes.add(new SmithingRecipe(trowelRecipeKey, shovel, base, trowelAdditive));
        }
        return wrenchRecipes;
    }

    private static SmithingRecipe getMediumRecipe() {
        RecipeChoice base = new RecipeChoice.MaterialChoice(Material.values());
        RecipeChoice medium = new RecipeChoice.MaterialChoice(SpellManager.SPELL_MEDIUMS);
        NamespacedKey mediumRecipeKey = new NamespacedKey(StadtServer.getInstance(), "spellMediumRecipe");
        return new SmithingRecipe(mediumRecipeKey, new ItemStack(Material.EMERALD), base, medium);
    }

    private enum SmeltingRecipes {
        RED_SAND("red_sand", SMOKER, Material.RED_SAND, Material.SAND, 0.1F, 100),
        SANDSTONE("sandstone", BLAST, Material.SANDSTONE, Material.SAND, 0.1F, 200),
        RED_SANDSTONE("red_sandstone", BLAST, Material.RED_SANDSTONE, Material.RED_SAND, 0.1F, 200);

        private final String name;
        private final int type;
        private final Material result;
        private final Material source;
        private final float exp;
        private final int time;

        SmeltingRecipes(String name, int type, Material result, Material source, float exp, int time) {
            this.name = name;
            this.type = type;
            this.result = result;
            this.source = source;
            this.exp = exp;
            this.time = time;
        }

        public Recipe toRecipe() {
            ItemStack out = new ItemStack(result);
            return switch (type) {
                case FURNACE -> new FurnaceRecipe(new NamespacedKey(StadtServer.getInstance(), "smelt_" + name), out, source, exp, time);
                case SMOKER -> new SmokingRecipe(new NamespacedKey(StadtServer.getInstance(), "smoke_" + name), out, source, exp, time);
                case BLAST -> new BlastingRecipe(new NamespacedKey(StadtServer.getInstance(), "blast_" + name), out, source, exp, time);
                default -> null;
            };
        }
    }

    private enum SlabToBlockRecipes {
        OAK("oak", Material.OAK_SLAB, Material.OAK_PLANKS),
        BIRCH("birch", Material.BIRCH_SLAB, Material.BIRCH_PLANKS),
        SPRUCE("spruce", Material.SPRUCE_SLAB, Material.SPRUCE_PLANKS),
        DARK_OAK("dark_oak", Material.DARK_OAK_SLAB, Material.DARK_OAK_PLANKS),
        JUNGLE("jungle", Material.JUNGLE_SLAB, Material.JUNGLE_PLANKS),
        ACACIA("acacia", Material.ACACIA_SLAB, Material.ACACIA_PLANKS),
        CRIMSON("crimson", Material.CRIMSON_SLAB, Material.CRIMSON_PLANKS),
        WARPED("warped", Material.WARPED_SLAB, Material.WARPED_PLANKS),

        COBBLE("cobble", Material.COBBLESTONE_SLAB, Material.COBBLESTONE),
        SANDSTONE("sandstone", Material.SANDSTONE_SLAB, Material.SANDSTONE),
        RED_SANDSTONE("red_sandstone", Material.RED_SANDSTONE_SLAB, Material.RED_SANDSTONE),
        MOSSY_COBBLE("mossy_cobble", Material.MOSSY_COBBLESTONE_SLAB, Material.MOSSY_COBBLESTONE),
        DEEPSLATE_COBBLE("deepslate_cobble", Material.COBBLED_DEEPSLATE_SLAB, Material.COBBLED_DEEPSLATE),
        PRISMARINE("prismarine", Material.PRISMARINE_SLAB, Material.PRISMARINE),

        STONE("stone", Material.STONE_SLAB, Material.STONE),
        GRANITE("granite", Material.GRANITE_SLAB, Material.GRANITE),
        DIORITE("diorite", Material.DIORITE_SLAB, Material.DIORITE),
        ANDESITE("andesite", Material.ANDESITE_SLAB, Material.ANDESITE),
        BLACKSTONE("blackstone", Material.BLACKSTONE_SLAB, Material.BLACKSTONE),
        DEEPSLATE_TILE("deepslate_tile", Material.DEEPSLATE_TILE_SLAB, Material.DEEPSLATE_TILES),
        QUARTZ("quartz", Material.QUARTZ_SLAB, Material.QUARTZ_BLOCK),

        STONE_BRICKS("stone_bricks", Material.STONE_BRICK_SLAB, Material.STONE_BRICKS),
        BRICKS("bricks", Material.BRICK_SLAB, Material.BRICKS),
        BLACKSTONE_BRICKS("blackstone_bricks", Material.POLISHED_BLACKSTONE_BRICK_SLAB, Material.POLISHED_BLACKSTONE_BRICKS),
        ENDSTONE_BRICKS("endstone_bricks", Material.END_STONE_BRICK_SLAB, Material.END_STONE_BRICKS),
        MOSSY_STONE_BRICKS("mossy_stone_bricks", Material.MOSSY_STONE_BRICK_SLAB, Material.MOSSY_STONE_BRICKS),
        CUT_SANDSTONE("cut_sandstone", Material.CUT_SANDSTONE_SLAB, Material.CUT_SANDSTONE),
        CUT_RED_SANDSTONE("cut_red_sandstone", Material.CUT_RED_SANDSTONE_SLAB, Material.CUT_RED_SANDSTONE),
        PURPUR("purpur", Material.PURPUR_SLAB, Material.PURPUR_BLOCK),
        DEEPSLATE_BRICKS("deepslate_bricks", Material.DEEPSLATE_BRICK_SLAB, Material.DEEPSLATE_BRICKS),
        NETHER_BRICKS("nether_brick", Material.NETHER_BRICK_SLAB, Material.NETHER_BRICKS),
        RED_NETHER_BRICK("red_nether_brick", Material.RED_NETHER_BRICK_SLAB, Material.RED_NETHER_BRICKS),
        PRISMARINE_BRICKS("prismarine_bricks", Material.PRISMARINE_BRICK_SLAB, Material.PRISMARINE_BRICKS),
        DARK_PRISMARINE("dark_prismarine", Material.DARK_PRISMARINE_SLAB, Material.DARK_PRISMARINE),

        SMOOTH_STONE("smooth_stone", Material.SMOOTH_STONE_SLAB, Material.SMOOTH_STONE),
        POLISHED_GRANITE("polished_granite", Material.POLISHED_GRANITE_SLAB, Material.POLISHED_GRANITE),
        POLISHED_DIORITE("polished_diorite", Material.POLISHED_DIORITE_SLAB, Material.POLISHED_DIORITE),
        POLISHED_ANDESITE("polished_andesite", Material.POLISHED_ANDESITE_SLAB, Material.POLISHED_ANDESITE),
        POLISHED_BLACKSTONE("polished_blackstone", Material.POLISHED_BLACKSTONE_SLAB, Material.POLISHED_BLACKSTONE),
        SMOOTH_SANDSTONE("smooth_sandstone", Material.SMOOTH_SANDSTONE_SLAB, Material.SMOOTH_SANDSTONE),
        SMOOTH_RED_SANDSTONE("smooth_red_sandstone", Material.SMOOTH_RED_SANDSTONE_SLAB, Material.SMOOTH_RED_SANDSTONE),
        POLISHED_DEEPSLATE("polished_deepslate", Material.POLISHED_DEEPSLATE_SLAB, Material.POLISHED_DEEPSLATE),
        SMOOTH_QUARTZ("smooth_quartz", Material.SMOOTH_QUARTZ_SLAB, Material.SMOOTH_QUARTZ),

        CUT_COPPER("cut_copper", Material.CUT_COPPER_SLAB, Material.CUT_COPPER),
        EXPOSED_CUT_COPPER("e_cut_copper", Material.EXPOSED_CUT_COPPER_SLAB, Material.EXPOSED_CUT_COPPER),
        WEATHERED_CUT_COPPER("w_cut_copper", Material.WEATHERED_CUT_COPPER_SLAB, Material.WEATHERED_CUT_COPPER),
        OXIDIZED_CUT_COPPER("o_cut_copper", Material.OXIDIZED_CUT_COPPER_SLAB, Material.OXIDIZED_CUT_COPPER),
        WAXED_CUT_COPPER("wx_cut_copper", Material.WAXED_CUT_COPPER_SLAB, Material.WAXED_CUT_COPPER),
        WAXED_EXPOSED_CUT_COPPER("wx_e_cut_copper", Material.WAXED_EXPOSED_CUT_COPPER_SLAB, Material.WAXED_EXPOSED_CUT_COPPER),
        WAXED_WEATHERED_CUT_COPPER("wx_w_cut_copper", Material.WAXED_WEATHERED_CUT_COPPER_SLAB, Material.WAXED_WEATHERED_CUT_COPPER),
        WAXED_OXIDIZED_CUT_COPPER("wx_o_cut_copper", Material.WAXED_OXIDIZED_CUT_COPPER_SLAB, Material.WAXED_OXIDIZED_CUT_COPPER);

        private final String name;
        private final Material slab;
        private final Material block;

        SlabToBlockRecipes(String name, Material slab, Material block) {
            this.name = name + "_slab_to_plank";
            this.slab = slab;
            this.block = block;
        }

        public ShapedRecipe toRecipe() {
            ItemStack result = new ItemStack(block);
            ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(StadtServer.getInstance(), name), result);
            recipe.shape("S", "S");
            recipe.setIngredient('S', slab);
            return recipe;
        }
    }

    private enum StoneCutterRecipes {
        OAK_STAIRS("oak_stairs", Material.OAK_PLANKS, new ItemStack(Material.OAK_STAIRS)),
        BIRCH_STAIRS("birch_stairs", Material.BIRCH_PLANKS, new ItemStack(Material.BIRCH_STAIRS)),
        SPRUCE_STAIRS("spruce_stairs", Material.SPRUCE_PLANKS, new ItemStack(Material.SPRUCE_STAIRS)),
        DARK_OAK_STAIRS("dark_oak_stairs", Material.DARK_OAK_PLANKS, new ItemStack(Material.DARK_OAK_STAIRS)),
        JUNGLE_OAK_STAIRS("jungle_stairs", Material.JUNGLE_PLANKS, new ItemStack(Material.JUNGLE_STAIRS)),
        ACACIA_OAK_STAIRS("acacia_stairs", Material.ACACIA_PLANKS, new ItemStack(Material.ACACIA_STAIRS)),
        CRIMSON_OAK_STAIRS("crimson_stairs", Material.CRIMSON_PLANKS, new ItemStack(Material.CRIMSON_STAIRS)),
        WARPED_OAK_STAIRS("warped_stairs", Material.WARPED_PLANKS, new ItemStack(Material.WARPED_STAIRS));

        private final String name;
        private final Material input;
        private final ItemStack result;

        StoneCutterRecipes(String name, Material input, ItemStack result) {
            this.name = "stone_cut_" + name;
            this.input = input;
            this.result = result;
        }

        public StonecuttingRecipe toRecipe() {
            return new StonecuttingRecipe(new NamespacedKey(StadtServer.getInstance(), name), result, input);
        }
    }
}
