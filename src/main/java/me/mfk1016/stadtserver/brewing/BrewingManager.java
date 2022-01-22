package me.mfk1016.stadtserver.brewing;

import me.mfk1016.stadtserver.RecipeManager;
import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.listener.BasicListener;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BrewingStand;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.List;
import java.util.Objects;

import static me.mfk1016.stadtserver.util.Functions.stackEmpty;

public class BrewingManager extends BasicListener {

    private static final int INGREDIENT_SLOT = 3;

    public static void tryBrewing(Block brewingStandBlock) {
        // 2 Ticks delay to respect a possible fuel refill
        StadtServer.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(StadtServer.getInstance(), () -> {
            BrewingStand stand = (BrewingStand) brewingStandBlock.getState();
            if (stand.getBrewingTime() != 0)
                return;
            BrewerInventory inventory = stand.getInventory();
            BrewingRecipe recipe = RecipeManager.matchBrewingRecipe(inventory, false);
            if (stand.getFuelLevel() > 0 && recipe != null) {
                BrewingWorker worker = new BrewingWorker(brewingStandBlock.getLocation(), recipe);
                worker.start();
            }
        }, 2);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onVanillaBrewFinished(BrewEvent event) {
        // This is hopefully only called, when a vanilla brewing process ends
        List<ItemStack> results = event.getResults();
        BrewerInventory inventory = event.getContents();
        ItemStack ingredient = Objects.requireNonNull(inventory.getIngredient());
        BrewingRecipe recipe = RecipeManager.matchBrewingRecipe(inventory, true);
        if (recipe != null) {
            for (int i = 0; i < 3; i++) {
                if (stackEmpty(inventory.getItem(i)))
                    continue;
                if (recipe.isApplicable(Objects.requireNonNull(inventory.getItem(i)), ingredient.getType()))
                    results.set(i, recipe.brewPotion(Objects.requireNonNull(inventory.getItem(i)), ingredient.getType()));
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerChangeBrewingStand(InventoryClickEvent event) {

        InventoryView view = event.getView();
        if (!(view.getTopInventory() instanceof BrewerInventory inventory))
            return;

        boolean standClicked = event.getClickedInventory() instanceof BrewerInventory;
        boolean isCustomBrewing = RecipeManager.matchBrewingRecipe(inventory, true) != null;
        boolean isIngredientSlot = standClicked && event.getSlotType() == InventoryType.SlotType.FUEL;

        Player player = (Player) event.getView().getPlayer();
        ItemStack mouse = event.getCursor();
        ItemStack target = event.getCurrentItem();
        if (stackEmpty(mouse) && stackEmpty(target))
            return;

        if (!standClicked && (event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT)) {
            ItemStack ingredient = inventory.getIngredient();
            boolean moveToIngredientSlot = !stackEmpty(target) && switch (target.getType()) {
                case BLAZE_POWDER, POTION, SPLASH_POTION, LINGERING_POTION -> false;
                default -> true;
            } && (stackEmpty(ingredient) || ingredient.isSimilar(target));
            int slot = event.getSlot();

            if (moveToIngredientSlot) {
                // Move the stack from the clicked slot to the ingredient slot
                if (stackEmpty(ingredient)) {
                    inventory.setItem(INGREDIENT_SLOT, target);
                    view.getBottomInventory().setItem(slot, null);
                } else {
                    int missing = ingredient.getMaxStackSize() - ingredient.getAmount();
                    if (target.getAmount() > missing) {
                        ingredient.setAmount(ingredient.getMaxStackSize());
                        target.setAmount(target.getAmount() - missing);
                        view.getBottomInventory().setItem(slot, target);
                    } else {
                        ingredient.setAmount(ingredient.getAmount() + target.getAmount());
                        view.getBottomInventory().setItem(slot, null);
                    }
                    inventory.setItem(INGREDIENT_SLOT, ingredient);
                }
                isIngredientSlot = true;
            }
        } else if (event.getClick() == ClickType.LEFT && isIngredientSlot) {
            if (stackEmpty(mouse) || stackEmpty(target) || !mouse.isSimilar(target)) {
                // Swap the items
                player.setItemOnCursor(target);
                inventory.setItem(INGREDIENT_SLOT, mouse);
            } else {
                // Merge the cursor onto the stack
                int space = target.getMaxStackSize() - target.getAmount();
                if (mouse.getAmount() <= space) {
                    target.setAmount(target.getAmount() + mouse.getAmount());
                    inventory.setItem(INGREDIENT_SLOT, target);
                    player.setItemOnCursor(null);
                } else {
                    target.setAmount(target.getMaxStackSize());
                    mouse.setAmount(mouse.getAmount() - space);
                    inventory.setItem(INGREDIENT_SLOT, target);
                    player.setItemOnCursor(mouse);
                }
            }
        } else if (event.getClick() == ClickType.RIGHT && !stackEmpty(target) && isIngredientSlot) {
            if (stackEmpty(mouse)) {
                // Take half of the target
                int toTake = target.getAmount() / 2;
                ItemStack newMouse = target.clone();
                target.setAmount(target.getAmount() - toTake);
                inventory.setItem(INGREDIENT_SLOT, target);
                newMouse.setAmount(toTake);
                player.setItemOnCursor(newMouse);
            } else if (mouse.isSimilar(target)) {
                // Put one item to the target
                target.setAmount(target.getAmount() + 1);
                inventory.setItem(INGREDIENT_SLOT, target);
                mouse.setAmount(mouse.getAmount() - 1);
                if (mouse.getAmount() == 0) {
                    player.setItemOnCursor(null);
                } else {
                    player.setItemOnCursor(mouse);
                }
            }
        }

        if (isIngredientSlot) {
            inventory.setIngredient(inventory.getItem(INGREDIENT_SLOT));
            event.setCancelled(true);
        }

        BrewingStand stand = inventory.getHolder();
        if (stand == null || isCustomBrewing)
            return;

        Block brewingStandBlock = stand.getBlock();
        tryBrewing(brewingStandBlock);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onMoveItemToIngredientSlot(InventoryMoveItemEvent event) {
        if (!(event.getDestination().getHolder() instanceof BrewingStand target))
            return;
        if (!(event.getSource().getHolder() instanceof Container source))
            return;
        if (!target.getBlock().getRelative(BlockFace.UP).equals(source.getBlock()))
            return;

        // cancel -> move item if possible
        event.setCancelled(true);
        ItemStack item = event.getItem();
        BrewerInventory brewerInventory = target.getInventory();
        boolean isCustomBrewing = RecipeManager.matchBrewingRecipe(brewerInventory, true) != null;
        ItemStack ingredient = brewerInventory.getIngredient();
        boolean moved = false;
        if (stackEmpty(ingredient)) {
            brewerInventory.setIngredient(item);
            moved = true;
        } else if (ingredient.isSimilar(item) && ingredient.getAmount() != ingredient.getMaxStackSize()) {
            ingredient.setAmount(ingredient.getAmount() + 1);
            moved = true;
        }

        if (moved) {
            Inventory sourceInventory = source.getInventory();
            int slot = sourceInventory.first(item.getType());
            if (Objects.requireNonNull(sourceInventory.getItem(slot)).getAmount() == 1) {
                sourceInventory.setItem(slot, new ItemStack(Material.AIR));
            } else {
                ItemStack sourceItem = Objects.requireNonNull(sourceInventory.getItem(slot));
                sourceItem.setAmount(sourceItem.getAmount() - 1);
                sourceInventory.setItem(slot, sourceItem);
            }

            if (!isCustomBrewing)
                tryBrewing(target.getBlock());
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onCustomLingerSplash(LingeringPotionSplashEvent event) {
        ItemStack input = event.getEntity().getItem();
        if (input.getType() != Material.LINGERING_POTION)
            return;
        if (!PotionLibrary.isCustomBrew(input))
            return;
        // Fix the linger potion effect duration to be equivalent to the correct potion description
        for (PotionEffect e : event.getAreaEffectCloud().getCustomEffects()) {
            if (e.getDuration() > 10) {
                PotionEffect fixed = new PotionEffect(e.getType(), e.getDuration() / 4, e.getAmplifier());
                event.getAreaEffectCloud().addCustomEffect(fixed, true);
            }
        }
    }
}
