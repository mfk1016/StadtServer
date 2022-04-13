package me.mfk1016.stadtserver.brewing.barrel;

import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.brewing.PotionManager;
import me.mfk1016.stadtserver.util.Keys;
import me.mfk1016.stadtserver.util.Pair;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static me.mfk1016.stadtserver.util.Functions.undecoratedText;

public class BarrelManager {

    public static ItemStack createFluidBarrel(int amount) {
        ItemStack result = new ItemStack(Material.BARREL, amount);
        ItemMeta meta = result.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(Keys.IS_FLUID_BARREL, PersistentDataType.INTEGER, 1);
        meta.displayName(undecoratedText("Fluid Barrel"));
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
        }
        if (pdcBlock.has(Keys.BARREL_LIQUID_TYPE) && pdcBlock.has(Keys.BARREL_LIQUID_AMOUNT)) {
            String liquidType = pdcBlock.getOrDefault(Keys.BARREL_LIQUID_TYPE, PersistentDataType.STRING, "");
            pdcItem.set(Keys.BARREL_LIQUID_TYPE, PersistentDataType.STRING, liquidType);
            int liquidAmount = pdcBlock.getOrDefault(Keys.BARREL_LIQUID_AMOUNT, PersistentDataType.INTEGER, 0);
            pdcItem.set(Keys.BARREL_LIQUID_AMOUNT, PersistentDataType.INTEGER, liquidAmount);
            assert PotionManager.SPECIAL_POTION_TYPE.containsKey(liquidType);
            String entry = PotionManager.SPECIAL_POTION_TYPE.get(liquidType).name() + " (" + liquidAmount + ")";
            List<Component> newLore = Objects.requireNonNull(meta.hasLore() ? meta.lore() : new ArrayList<>());
            if (!newLore.contains(undecoratedText(entry)))
                newLore.add(undecoratedText(entry));
            meta.lore(newLore);
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

    public static void setFluidOfBarrel(Block block, String type, int amount) {
        assert isFluidBarrel(block);
        Barrel barrel = (Barrel) block.getState();
        PersistentDataContainer pdcBlock = barrel.getPersistentDataContainer();
        pdcBlock.set(Keys.BARREL_LIQUID_TYPE, PersistentDataType.STRING, type);
        pdcBlock.set(Keys.BARREL_LIQUID_AMOUNT, PersistentDataType.INTEGER, amount);
        barrel.update();
    }

}
