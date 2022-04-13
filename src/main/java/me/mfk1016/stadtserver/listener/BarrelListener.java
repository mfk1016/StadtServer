package me.mfk1016.stadtserver.listener;

import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.brewing.barrel.BarrelManager;
import me.mfk1016.stadtserver.util.Pair;
import org.bukkit.Material;
import org.bukkit.block.Barrel;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;
import java.util.Optional;

public class BarrelListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlaceFluidBarrel(BlockPlaceEvent event) {
        if (!BarrelManager.isFluidBarrel(event.getItemInHand()))
            return;
        ItemStack barrel = event.getItemInHand().clone();
        new BukkitRunnable() {
            @Override
            public void run() {
                BarrelManager.fluidBarrelToBlock(barrel, event.getBlockPlaced());
            }
        }.runTaskLater(StadtServer.getInstance(), 1L);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerBreakFluidBarrel(BlockBreakEvent event) {
        if (!BarrelManager.isFluidBarrel(event.getBlock()))
            return;
        ItemStack barrel = BarrelManager.fluidBarrelAsItem(event.getBlock());
        event.getBlock().setType(Material.AIR);
        event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), barrel);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteractFluidBarrel(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        if (!BarrelManager.isFluidBarrel(Objects.requireNonNull(event.getClickedBlock())))
            return;
        Optional<Pair<String, Integer>> content = BarrelManager.getFluidOfBarrel(event.getClickedBlock());
        event.setCancelled(true);

    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBarrelMoveInventory(InventoryMoveItemEvent event) {
        if (event.getSource().getHolder() instanceof Barrel barrelSource)
            event.setCancelled(BarrelManager.isFluidBarrel(barrelSource.getBlock()));
        else if (event.getDestination().getHolder() instanceof Barrel barrelTarget)
            event.setCancelled(BarrelManager.isFluidBarrel(barrelTarget.getBlock()));
    }
}
