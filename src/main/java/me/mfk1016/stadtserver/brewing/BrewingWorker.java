package me.mfk1016.stadtserver.brewing;

import me.mfk1016.stadtserver.StadtServer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BrewingStand;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static me.mfk1016.stadtserver.util.Functions.stackEmpty;

public class BrewingWorker extends BukkitRunnable {

    private final BrewingRecipe recipe;
    private final Location brewingStandLocation;

    private int brewTimer = 400;

    public BrewingWorker(@NotNull Location brewingStandLocation, BrewingRecipe recipe) {
        this.recipe = recipe;
        this.brewingStandLocation = brewingStandLocation;
    }

    public void start() {
        BrewingStand stand = (BrewingStand) brewingStandLocation.getBlock().getState();
        stand.setFuelLevel(stand.getFuelLevel() - 1);
        stand.update();
        runTaskTimer(StadtServer.getInstance(), 0, 1);
    }

    @Override
    public void run() {
        if (!brewingStandLocation.getChunk().isLoaded()) {
            return;
        }
        if (!(brewingStandLocation.getBlock().getState() instanceof BrewingStand stand)) {
            cancel();
            return;
        }
        BrewerInventory inventory = stand.getSnapshotInventory();

        if (illegalState(inventory)) {
            stand.update(true);
            cancel();
        } else if (brewTimer == 0) {
            // Use the ingredient
            ItemStack ingredient = Objects.requireNonNull(inventory.getIngredient());
            if (ingredient.getAmount() > 1) {
                ingredient.setAmount(ingredient.getAmount() - 1);
                inventory.setIngredient(ingredient);
            } else {
                inventory.setIngredient(new ItemStack(Material.AIR));
            }
            // Brew the recipe for each slot
            for (int i = 0; i < 3; i++) {
                ItemStack input = inventory.getItem(i);
                if (!stackEmpty(input) && recipe.isApplicable(input, ingredient.getType())) {
                    ItemStack result = recipe.brewPotion(input, ingredient.getType());
                    inventory.setItem(i, result);
                }
            }
            stand.getWorld().playSound(stand.getLocation(), Sound.BLOCK_BREWING_STAND_BREW, 1f, 1f);
            stand.update(true);
            BrewingManager.tryBrewing(brewingStandLocation.getBlock());
            cancel();
        } else {
            stand.setBrewingTime(brewTimer);
            stand.update(true);
            brewTimer--;
        }
    }

    private boolean illegalState(BrewerInventory inventory) {
        // The ingredient must not be null
        ItemStack ingredient = inventory.getIngredient();
        if (stackEmpty(ingredient))
            return true;

        // At least one slot must be valid
        for (int i = 0; i < 3; i++) {
            ItemStack input = inventory.getItem(i);
            if (stackEmpty(input) || !recipe.isApplicable(input, ingredient.getType()))
                continue;
            return false;
        }
        return true;
    }
}
