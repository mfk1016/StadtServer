package me.mfk1016.stadtserver.logic;

import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.enchantments.WrenchEnchantment;
import me.mfk1016.stadtserver.util.CropData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.Dropper;
import org.bukkit.block.Hopper;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Dispenser;
import org.bukkit.entity.Item;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Objects;

import static me.mfk1016.stadtserver.util.Functions.stackEmpty;

public class DispenserDropperLogic {

    private static final Vector TO_BLOCK_CENTER = new Vector(0.5, 0.1, 0.5);

    public static boolean tryAllDispenseActions(Block dispenserBlock, ItemStack item) {
        return tryPlacerAction(dispenserBlock, item) ||
                tryPlanting(dispenserBlock, item);
    }

    // Called after the InventoryMoveItemEvent is cancelled
    // this means, that the hopper has its original state
    public static void tryChuteAction(Block source, ItemStack item, Block target) {
        Hopper hopper = (Hopper) source.getState();
        int slot = hopper.getInventory().first(item.getType());
        ItemStack newItem = Objects.requireNonNull(hopper.getInventory().getItem(slot)).clone();
        newItem.setAmount(newItem.getAmount() - 1);
        newItem = newItem.getAmount() > 0 ? newItem : null;

        if (target.getState() instanceof Container container) {
            if (container.getInventory().addItem(item).isEmpty()) {
                hopper.getInventory().setItem(slot, newItem);
            }
        } else {
            Location targetPos = target.getLocation().clone().add(TO_BLOCK_CENTER);
            Item result = target.getWorld().dropItem(targetPos, item);
            result.setVelocity(new Vector());
            hopper.getInventory().setItem(slot, newItem);
        }
    }

    public static boolean tryPlacerAction(Block dispenserBlock, ItemStack item) {
        org.bukkit.block.Dispenser dispenserState = (org.bukkit.block.Dispenser) dispenserBlock.getState();
        if (!WrenchEnchantment.isWrenched(dispenserState))
            return false;

        Dispenser dispenser = (Dispenser) dispenserBlock.getBlockData();
        Block target = dispenserBlock.getRelative(dispenser.getFacing());
        if (!item.getType().isBlock() || !target.isEmpty())
            // The block placer overrides everything
            return true;

        target.setType(item.getType());
        StadtServer.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(StadtServer.getInstance(), () -> dispenserState.update(true));
        return true;
    }

    private static boolean tryPlanting(Block dispenserBlock, ItemStack item) {
        if (CropData.isPlantable(item.getType())) {
            // Check target and plant crop
            Material seed = item.getType();
            Dispenser dispenser = (Dispenser) dispenserBlock.getBlockData();
            Block target = dispenserBlock.getRelative(dispenser.getFacing());
            Block targetFarm = target.getRelative(0, -1, 0);
            if (target.getType() == Material.AIR && CropData.isFarmland(seed, targetFarm.getType())) {
                // Plant Crop
                target.setType(CropData.cropOfSeed(seed));
                // Remove item from dispenser
                org.bukkit.block.Dispenser disp = ((org.bukkit.block.Dispenser) dispenserBlock.getState());
                StadtServer.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(StadtServer.getInstance(), () -> {
                    // MERKEN: block dispense event: inventory has state WITHOUT the dispensed item
                    // -> cancel + update with this state removes exactly that item.
                    disp.update(true);
                });
                return true;
            }
        }
        return false;
    }
}
