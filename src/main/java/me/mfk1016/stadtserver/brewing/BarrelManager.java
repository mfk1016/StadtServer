package me.mfk1016.stadtserver.brewing;

import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.brewing.recipe.BarrelRecipe;
import me.mfk1016.stadtserver.util.Keys;
import me.mfk1016.stadtserver.util.Pair;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Optional;

import static me.mfk1016.stadtserver.util.Functions.stackEmpty;
import static me.mfk1016.stadtserver.util.Functions.undecoratedText;

public class BarrelManager {

    public static ItemStack createFluidBarrel(int amount) {
        ItemStack result = new ItemStack(Material.BARREL, amount);
        ItemMeta meta = result.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(Keys.IS_FLUID_BARREL, PersistentDataType.INTEGER, 1);
        meta.displayName(undecoratedText("Fluid Barrel").color(NamedTextColor.YELLOW));
        result.setItemMeta(meta);
        return result;
    }

    public static ShapedRecipe fluidBarrelRecipe() {
        NamespacedKey recipeKey = new NamespacedKey(StadtServer.getInstance(), "fluid_barrel_recipe");
        ShapedRecipe recipe = new ShapedRecipe(recipeKey, createFluidBarrel(1));
        recipe.shape("-I-", "CBC", "-C-");
        recipe.setIngredient('B', new RecipeChoice.ExactChoice(new ItemStack(Material.BARREL)));
        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('C', Material.HONEYCOMB);
        return recipe;
    }

    public static boolean isFluidBarrel(ItemStack item) {
        if (item.getType() != Material.BARREL)
            return false;
        return item.getItemMeta().getPersistentDataContainer().has(Keys.IS_FLUID_BARREL);
    }

    public static boolean isFluidBarrel(Block block) {
        if (block.getType() != Material.BARREL)
            return false;
        Barrel barrel = (Barrel) block.getState();
        return isFluidBarrel(barrel);
    }

    public static boolean isFluidBarrel(Barrel barrel) {
        return barrel.getPersistentDataContainer().has(Keys.IS_FLUID_BARREL);
    }

    public static void fluidBarrelToBlock(ItemStack item, Block block) {
        assert item.getType() == Material.BARREL && block.getType() == Material.BARREL;
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdcItem = meta.getPersistentDataContainer();
        Barrel barrel = (Barrel) block.getState();
        PersistentDataContainer pdcBlock = barrel.getPersistentDataContainer();
        if (pdcItem.has(Keys.IS_FLUID_BARREL)) {
            pdcBlock.set(Keys.IS_FLUID_BARREL, PersistentDataType.INTEGER, 1);
        }
        if (pdcItem.has(Keys.BARREL_LIQUID_TYPE)) {
            String liquidType = pdcItem.getOrDefault(Keys.BARREL_LIQUID_TYPE, PersistentDataType.STRING, "");
            pdcBlock.set(Keys.BARREL_LIQUID_TYPE, PersistentDataType.STRING, liquidType);
        }
        if (pdcItem.has(Keys.BARREL_LIQUID_AMOUNT)) {
            int liquidAmount = pdcItem.getOrDefault(Keys.BARREL_LIQUID_AMOUNT, PersistentDataType.INTEGER, 0);
            pdcBlock.set(Keys.BARREL_LIQUID_AMOUNT, PersistentDataType.INTEGER, liquidAmount);
        }
        barrel.update();
    }

    public static ItemStack fluidBarrelAsItem(Block block) {
        assert block.getType() == Material.BARREL;
        ItemStack result = createFluidBarrel(1);
        ItemMeta meta = result.getItemMeta();
        PersistentDataContainer pdcItem = meta.getPersistentDataContainer();
        Barrel barrel = (Barrel) block.getState();
        PersistentDataContainer pdcBlock = barrel.getPersistentDataContainer();
        if (pdcBlock.has(Keys.IS_FLUID_BARREL)) {
            pdcItem.set(Keys.IS_FLUID_BARREL, PersistentDataType.INTEGER, 1);
            meta.displayName(undecoratedText("Fluid Barrel").color(NamedTextColor.YELLOW));
        }
        if (pdcBlock.has(Keys.BARREL_LIQUID_TYPE) && pdcBlock.has(Keys.BARREL_LIQUID_AMOUNT)) {
            String liquidType = pdcBlock.getOrDefault(Keys.BARREL_LIQUID_TYPE, PersistentDataType.STRING, "");
            pdcItem.set(Keys.BARREL_LIQUID_TYPE, PersistentDataType.STRING, liquidType);
            int liquidAmount = pdcBlock.getOrDefault(Keys.BARREL_LIQUID_AMOUNT, PersistentDataType.INTEGER, 0);
            pdcItem.set(Keys.BARREL_LIQUID_AMOUNT, PersistentDataType.INTEGER, liquidAmount);
            assert PotionManager.SPECIAL_POTION_TYPE.containsKey(liquidType);
            String entry = PotionManager.SPECIAL_POTION_TYPE.get(liquidType).name() + " (" + liquidAmount + ")";
            meta.displayName(undecoratedText("Barrel of " + entry).color(NamedTextColor.AQUA));
        }
        result.setItemMeta(meta);
        return result;
    }

    public static Optional<Pair<String, Integer>> getFluidOfBarrel(Block block) {
        assert isFluidBarrel(block);
        Barrel barrel = (Barrel) block.getState();
        PersistentDataContainer pdcBlock = barrel.getPersistentDataContainer();
        if (pdcBlock.has(Keys.BARREL_LIQUID_TYPE) && pdcBlock.has(Keys.BARREL_LIQUID_AMOUNT)) {
            String id = pdcBlock.getOrDefault(Keys.BARREL_LIQUID_TYPE, PersistentDataType.STRING, "");
            int amount = pdcBlock.getOrDefault(Keys.BARREL_LIQUID_AMOUNT, PersistentDataType.INTEGER, 0);
            return Optional.of(new Pair<>(id, amount));
        } else {
            return Optional.empty();
        }
    }

    // amount = 0: clear barrel fluid
    public static void setFluidOfBarrel(Block block, String type, int amount) {
        assert isFluidBarrel(block);
        Barrel barrel = (Barrel) block.getState();
        PersistentDataContainer pdcBlock = barrel.getPersistentDataContainer();
        if (amount > 0) {
            pdcBlock.set(Keys.BARREL_LIQUID_TYPE, PersistentDataType.STRING, type);
            pdcBlock.set(Keys.BARREL_LIQUID_AMOUNT, PersistentDataType.INTEGER, amount);
        } else {
            pdcBlock.remove(Keys.BARREL_LIQUID_TYPE);
            pdcBlock.remove(Keys.BARREL_LIQUID_AMOUNT);
        }
        barrel.update();
    }

    public static void startBarrelBrewing(Block block) {
        Chunk chunk = block.getChunk();
        World world = chunk.getWorld();
        new BukkitRunnable() {
            private int cycles = 1;

            @Override
            public void run() {
                if (!chunk.isLoaded())
                    return;
                if (!BarrelManager.isFluidBarrel(block)) {
                    this.cancel();
                    return;
                }
                if (cycles == 10) {
                    world.playSound(block.getLocation(), Sound.BLOCK_BREWING_STAND_BREW, 1f, 1f);
                    BarrelManager.finalizeBarrelBrewing(block);
                    this.cancel();
                } else {
                    world.playSound(block.getLocation(), Sound.BLOCK_LAVA_AMBIENT, 1f, 1.5f);
                    cycles++;
                }
            }
        }.runTaskTimer(StadtServer.getInstance(), 40, 40);
    }

    public static void finalizeBarrelBrewing(Block block) {
        assert isFluidBarrel(block);
        Barrel barrel = (Barrel) block.getState();
        Inventory barrelInventory = barrel.getInventory();
        Optional<BarrelRecipe> recipe = PotionRecipeManager.matchBarrelRecipe(barrelInventory);
        String id = StadtServer.RANDOM.nextInt(2) == 0 ? "oettinger" : "sternburg";
        if (recipe.isPresent())
            id = recipe.get().getResultID();
        barrelInventory.clear();
        setFluidOfBarrel(block, id, 20);
    }

    public static String getFluidBarrelInfo(Block block) {
        Optional<Pair<String, Integer>> content = getFluidOfBarrel(block);
        if (content.isPresent()) {
            SpecialPotionType type = PotionManager.SPECIAL_POTION_TYPE.get(content.get()._1);
            int amount = content.get()._2;
            return type.name() + " (" + amount + ")";
        }
        Barrel barrel = (Barrel) block.getState();
        Inventory barrelInventory = barrel.getInventory();
        for (int i = 0; i < 5; i++) {
            if (!stackEmpty(barrelInventory.getItem(i)))
                continue;
            return switch (i) {
                case 0 -> "The barrel is empty. Add water to begin brewing";
                case 1 -> "Add water to proceed brewing";
                case 2 -> "Add 3 wheat to proceed brewing";
                case 3 -> "Add yeast to proceed brewing";
                default -> "Add an additive to start the brewing process";
            };
        }
        return "Brewing not finished yet";
    }

}
