package me.mfk1016.stadtserver.logic;

import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.enchantments.WrenchEnchantment;
import me.mfk1016.stadtserver.util.CropData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.Dropper;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Dispenser;
import org.bukkit.entity.Item;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;

public class DispenserDropperLogic {

    private static final Vector TO_BLOCK_CENTER = new Vector(0.5, 0.1, 0.5);

    public static boolean tryAllDispenseActions(Block dispenserBlock, ItemStack item) {
        return tryPlacerAction(dispenserBlock, item) ||
                tryPlanting(dispenserBlock, item);
    }

    public static void tryChuteAction(Dropper dropperState, InventoryMoveItemEvent event) {
        if (!WrenchEnchantment.isWrenched(dropperState))
            return;

        Block dropperBlock = dropperState.getBlock();
        Directional dropperData = (Directional) dropperBlock.getBlockData();
        Block target = dropperBlock.getRelative(dropperData.getFacing());
        if (target.getState() instanceof Container container) {
            HashMap<Integer, ItemStack> invalid = container.getInventory().addItem(event.getItem());
            if (invalid.isEmpty()) {
                BukkitRunnable runnable = new BukkitRunnable() {
                    @Override
                    public void run() {
                        dropperState.update();
                    }
                };
                runnable.runTaskLater(StadtServer.getInstance(), 1L);
            } else {
                event.setCancelled(true);
            }
        } else {
            Location targetPos = target.getLocation().clone().add(TO_BLOCK_CENTER);
            Item result = dropperBlock.getWorld().dropItem(targetPos, event.getItem());
            BukkitRunnable runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    result.setVelocity(new Vector());
                    dropperState.update();
                }
            };
            runnable.runTaskLater(StadtServer.getInstance(), 1L);
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
