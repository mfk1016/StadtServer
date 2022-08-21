package me.mfk1016.stadtserver.listener;

import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.brewing.BarrelManager;
import me.mfk1016.stadtserver.brewing.PotionManager;
import me.mfk1016.stadtserver.brewing.SpecialPotionType;
import me.mfk1016.stadtserver.util.Pair;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Barrel;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;
import java.util.Optional;

import static me.mfk1016.stadtserver.util.Functions.playerMessage;
import static me.mfk1016.stadtserver.util.Functions.stackEmpty;

public class BarrelListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlaceFluidBarrel(BlockPlaceEvent event) {
        if (!BarrelManager.isFluidBarrel(event.getItemInHand()))
            return;
        ItemStack barrel = event.getItemInHand();
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

    private void onPlayerInteractFilledBarrel(PlayerInteractEvent event, String typeID, int amount) {
        ItemStack item = event.getItem();
        EquipmentSlot targetSlot = Objects.requireNonNull(event.getHand());
        if (stackEmpty(item) || item.getType() != Material.GLASS_BOTTLE) {
            playerMessage(event.getPlayer(), BarrelManager.getFluidBarrelInfo(event.getClickedBlock()));
            return;
        }
        SpecialPotionType type = PotionManager.SPECIAL_POTION_TYPE.get(typeID);
        if (item.getAmount() == 1) {
            event.getPlayer().getInventory().setItem(targetSlot, type.createPotion());
        } else {
            var result = event.getPlayer().getInventory().addItem(type.createPotion());
            if (!result.isEmpty())
                return;
            ItemStack newItem = new ItemStack(Material.GLASS_BOTTLE, item.getAmount() - 1);
            event.getPlayer().getInventory().setItem(targetSlot, newItem);
        }
        BarrelManager.setFluidOfBarrel(event.getClickedBlock(), typeID, amount - 1);
        World world = event.getPlayer().getWorld();
        world.playSound(event.getPlayer().getLocation(), Sound.ITEM_BOTTLE_FILL, 1f, 1f);
    }

    private void onPlayerInteractEmptyBarrel(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (stackEmpty(item)) {
            playerMessage(event.getPlayer(), BarrelManager.getFluidBarrelInfo(event.getClickedBlock()));
            return;
        }
        Barrel barrel = (Barrel) Objects.requireNonNull(event.getClickedBlock()).getState();
        int nextSlot = barrel.getInventory().firstEmpty();
        ItemStack newItemInHand = null;
        boolean success = false;
        switch (nextSlot) {
            case 0, 1:
                if (item.getType() == Material.WATER_BUCKET) {
                    success = true;
                    newItemInHand = new ItemStack(Material.BUCKET);
                }
                break;
            case 2:
                if (item.getType() == Material.WHEAT && item.getAmount() >= 3) {
                    success = true;
                    if (item.getAmount() > 3) {
                        newItemInHand = item.clone();
                        newItemInHand.setAmount(item.getAmount() - 3);
                    }
                }
                break;
            case 3:
                var match = PotionManager.matchSpecialPotion(item);
                if (match.isPresent() && match.get().id().contains("yeast"))
                    success = true;
                break;
            case 4:
                if (item.getAmount() == 1 && item.getType().getMaxStackSize() != 1) {
                    success = true;
                    BarrelManager.startBarrelBrewing(event.getClickedBlock());
                }
                break;
        }
        if (!success) {
            playerMessage(event.getPlayer(), BarrelManager.getFluidBarrelInfo(event.getClickedBlock()));
            return;
        }
        barrel.getInventory().addItem(item);
        World world = event.getPlayer().getWorld();
        event.getPlayer().getInventory().setItem(Objects.requireNonNull(event.getHand()), newItemInHand);
        world.playSound(event.getClickedBlock().getLocation(), Sound.BLOCK_BARREL_OPEN, 1f, 1f);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteractFluidBarrel(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        if (!BarrelManager.isFluidBarrel(Objects.requireNonNull(event.getClickedBlock())))
            return;
        Optional<Pair<String, Integer>> fluidContent = BarrelManager.getFluidOfBarrel(event.getClickedBlock());
        event.setCancelled(true);
        if (fluidContent.isPresent())
            onPlayerInteractFilledBarrel(event, fluidContent.get()._1, fluidContent.get()._2);
        else
            onPlayerInteractEmptyBarrel(event);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBarrelMoveInventory(InventoryMoveItemEvent event) {
        if (event.getSource().getHolder(false) instanceof Barrel barrelSource)
            event.setCancelled(BarrelManager.isFluidBarrel(barrelSource));
        else if (event.getDestination().getHolder(false) instanceof Barrel barrelTarget)
            event.setCancelled(BarrelManager.isFluidBarrel(barrelTarget));
    }
}
