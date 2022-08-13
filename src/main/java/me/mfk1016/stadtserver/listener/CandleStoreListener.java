package me.mfk1016.stadtserver.listener;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.candlestore.*;
import me.mfk1016.stadtserver.util.Keys;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Candle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataContainer;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static me.mfk1016.stadtserver.util.Functions.playerMessage;
import static me.mfk1016.stadtserver.util.Functions.stackEmpty;

public class CandleStoreListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerOpenCandleStoreView(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player.isSneaking())
            return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || CandleStoreUtils.isCandleTool(event.getItem()))
            return;
        Block target = Objects.requireNonNull(event.getClickedBlock());
        if (!(target.getState() instanceof Dispenser dispenser))
            return;
        PersistentDataContainer pdc = dispenser.getPersistentDataContainer();
        if (!pdc.has(Keys.CANDLE_STORE_MEMBER_TYPE))
            return;
        if (CandleMemberType.getMemberType(pdc) == CandleMemberType.EXPORT)
            return;
        Optional<CandleStore> currentStore = CandleStoreManager.getStore(dispenser);
        if (currentStore.isPresent()) {
            event.setCancelled(true);
            player.openInventory(currentStore.get().getView());
            currentStore.get().updateView(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerClickCandleStoreView(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory target = event.getClickedInventory();
        ItemStack clicked = event.getCurrentItem();
        if (event.isShiftClick() || target == null || target instanceof PlayerInventory)
            return;
        if (!(target.getHolder() instanceof CandleStore store))
            return;

        event.setCancelled(true);
        try {
            if (stackEmpty(player.getItemOnCursor())) {
                if (stackEmpty(clicked))
                    return;
                int pullAmount = event.isLeftClick() ? clicked.getMaxStackSize() : 1;
                ItemStack pulled = store.pullItemStack(clicked.getType(), pullAmount);
                player.setItemOnCursor(pulled);
            } else {
                ItemStack toPush = player.getItemOnCursor();
                store.pushItemStack(toPush);
                player.setItemOnCursor(null);
            }
        } catch (CandleStoreException ignored) {
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDragAtCandleStoreView(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof CandleStore) {
            if (!event.getRawSlots().stream().filter(value -> value < 54).toList().isEmpty())
                event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerShiftClickCandleStoreView(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory target = event.getClickedInventory();
        ItemStack clicked = event.getCurrentItem();
        if (!event.isShiftClick() || stackEmpty(clicked) || target == null)
            return;
        if (!(event.getView().getTopInventory().getHolder() instanceof CandleStore store))
            return;

        event.setCancelled(true);
        try {
            if (!(target instanceof PlayerInventory)) {
                int pullAmount = clicked.getMaxStackSize();
                ItemStack pulled = store.pullItemStack(clicked.getType(), pullAmount);
                Collection<ItemStack> left = player.getInventory().addItem(pulled).values();
                for (ItemStack leftStack : left)
                    store.pushItemStack(leftStack);
            } else {
                store.pushItemStack(clicked);
                target.clear(event.getSlot());
            }
        } catch (CandleStoreException ignored) {
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerCreateCandleStore(PlayerInteractEvent event) {
        if (!event.getPlayer().isSneaking())
            return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || !CandleStoreUtils.isCandleTool(event.getItem()))
            return;
        Block target = Objects.requireNonNull(event.getClickedBlock());
        if (!(target.getState() instanceof Dispenser dispenser))
            return;
        if (!CandleStoreUtils.checkValidToCreateElement(event, target, dispenser))
            return;

        event.setCancelled(true);
        Material candleMat = target.getRelative(BlockFace.UP).getType();
        String candleName = CandleStoreUtils.getCandleName(candleMat, true);
        List<Dispenser> dispensers = CandleStoreUtils.getNearestPotentialCandleStoreElements(target, candleMat, 64);
        for (Dispenser potentialElement : dispensers) {
            if (CandleStoreManager.getStore(potentialElement).isPresent()) {
                playerMessage(event.getPlayer(),
                        "A new " + candleName + " store must be at least 64 blocks away from the next store.");
                return;
            }
        }
        BlockState attachedBlockState = target.getRelative(BlockFace.DOWN).getState();
        CandleMemberType memberType = CandleMemberType.NORMAL;
        if (attachedBlockState instanceof Chest)
            memberType = CandleMemberType.CHEST;
        else if (attachedBlockState instanceof Hopper)
            memberType = CandleMemberType.EXPORT;
        CandleStoreManager.createStore(dispenser, memberType);
        playerMessage(event.getPlayer(), "New " + candleName + " store created.");
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerAddCandleStoreMember(PlayerInteractEvent event) {
        if (event.getPlayer().isSneaking())
            return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || !CandleStoreUtils.isCandleTool(event.getItem()))
            return;
        Block target = Objects.requireNonNull(event.getClickedBlock());
        if (!(target.getState() instanceof Dispenser dispenser))
            return;
        if (!CandleStoreUtils.checkValidToCreateElement(event, target, dispenser))
            return;

        event.setCancelled(true);
        Material candleMat = target.getRelative(BlockFace.UP).getType();
        String candleName = CandleStoreUtils.getCandleName(candleMat, true);
        List<Dispenser> dispensers = CandleStoreUtils.getNearestPotentialCandleStoreElements(target, candleMat, 8);
        for (Dispenser partner : dispensers) {
            Optional<CandleStore> optStore = CandleStoreManager.getStore(partner);
            if (optStore.isPresent()) {
                if (optStore.get().getCenterLocation().distance(dispenser.getLocation().toVector()) > 16D) {
                    playerMessage(event.getPlayer(),
                            "Too far away from " + candleName + " store center (must be within 16 Blocks)");
                    return;
                }
                BlockState attachedBlockState = target.getRelative(BlockFace.DOWN).getState();
                CandleMemberType memberType = CandleMemberType.NORMAL;
                if (attachedBlockState instanceof Chest)
                    memberType = CandleMemberType.CHEST;
                else if (attachedBlockState instanceof Hopper)
                    memberType = CandleMemberType.EXPORT;
                CandleStoreManager.addToStore(dispenser, partner, memberType);
                playerMessage(event.getPlayer(), "New Member added to " + candleName + " store.");
                return;
            }
        }
        playerMessage(event.getPlayer(), "No " + candleName + " store found within 8 blocks.");
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBreakCandleStoreMember(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block target = event.getBlock();
        BlockState targetState = target.getState();
        BlockData targetData = target.getBlockData();
        if (targetData instanceof Candle) {
            Block possibleTarget = target.getRelative(BlockFace.DOWN);
            if (!(possibleTarget.getState() instanceof Dispenser dispenser))
                return;
            if (CandleStoreManager.getStore(dispenser).isPresent()) {
                CandleStoreManager.deleteFromStore(dispenser);
                String candleName = CandleStoreUtils.getCandleName(target.getType(), false);
                playerMessage(player, candleName + " store member removed.");
            }
        } else if ((targetState instanceof Chest || targetState instanceof Hopper) &&
                   target.getRelative(BlockFace.UP).getState() instanceof Dispenser dispenser) {
            if (CandleStoreManager.getStore(dispenser).isPresent()) {
                CandleStoreManager.updateStoreMember(dispenser, CandleMemberType.NORMAL);
                Material candleMat = target.getRelative(BlockFace.UP, 2).getType();
                String candleName = CandleStoreUtils.getCandleName(candleMat, true);
                playerMessage(player, "Chest or Hopper removed from " + candleName + " store member.");
            }
        } else if (targetState instanceof Dispenser dispenser) {
            if (CandleStoreManager.getStore(dispenser).isPresent()) {
                CandleStoreManager.deleteFromStore(dispenser);
                Material candleMat = target.getRelative(BlockFace.UP).getType();
                String candleName = CandleStoreUtils.getCandleName(candleMat, false);
                playerMessage(player, candleName + " store member removed.");
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onDestroyCandleStoreMember(BlockDestroyEvent event) {
        Block target = event.getBlock();
        BlockState targetState = target.getState();
        BlockData targetData = target.getBlockData();
        if (targetData instanceof Candle) {
            Block possibleTarget = target.getRelative(BlockFace.DOWN);
            if (!(possibleTarget.getState() instanceof Dispenser dispenser))
                return;
            boolean hasChest = possibleTarget.getRelative(BlockFace.DOWN).getState() instanceof Chest;
            CandleStoreManager.deleteFromStore(dispenser);
        } else if ((targetState instanceof Chest || targetState instanceof Hopper) &&
                   target.getRelative(BlockFace.UP).getState() instanceof Dispenser dispenser) {
            CandleStoreManager.updateStoreMember(dispenser, CandleMemberType.NORMAL);
        } else if (targetState instanceof Dispenser dispenser) {
            boolean hasChest = target.getRelative(BlockFace.DOWN).getState() instanceof Chest;
            CandleStoreManager.deleteFromStore(dispenser);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onAddChestToStoreMember(BlockPlaceEvent event) {
        if (event.getItemInHand().getType() != Material.CHEST)
            return;
        Block possibleTarget = event.getBlockPlaced();
        if (!(possibleTarget.getRelative(BlockFace.UP).getState() instanceof Dispenser dispenser))
            return;
        CandleStoreManager.getStore(dispenser).ifPresent(candleStore -> {
            CandleStoreManager.updateStoreMember(dispenser, CandleMemberType.CHEST);
            Material candleMat = possibleTarget.getRelative(BlockFace.UP, 2).getType();
            String candleName = CandleStoreUtils.getCandleName(candleMat, true);
            playerMessage(event.getPlayer(), "Chest added to " + candleName + " store member.");
        });
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onAddHopperToStoreMember(BlockPlaceEvent event) {
        if (event.getItemInHand().getType() != Material.HOPPER)
            return;
        Block possibleTarget = event.getBlockPlaced();
        if (!(possibleTarget.getRelative(BlockFace.UP).getState() instanceof Dispenser dispenser))
            return;
        CandleStoreManager.getStore(dispenser).ifPresent(candleStore -> {
            CandleStoreManager.updateStoreMember(dispenser, CandleMemberType.EXPORT);
            Material candleMat = possibleTarget.getRelative(BlockFace.UP, 2).getType();
            String candleName = CandleStoreUtils.getCandleName(candleMat, true);
            playerMessage(event.getPlayer(), "Hopper added to " + candleName + " candle store member.");
        });
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onHopperPushStackToStore(InventoryMoveItemEvent event) {
        if (!(event.getSource().getHolder() instanceof Hopper hopper))
            return;
        if (!(event.getDestination().getHolder() instanceof Dispenser dispenser))
            return;
        Optional<CandleStore> optCandleStore = CandleStoreManager.getStore(dispenser);
        optCandleStore.ifPresent(candleStore -> {
            event.setCancelled(true);
            ItemStack toPush = event.getItem().clone();
            toPush.setAmount(1);
            Bukkit.getServer().getScheduler().runTaskLater(StadtServer.getInstance(), () -> {
                try {
                    candleStore.pushItemStack(toPush);
                    int slot = hopper.getInventory().first(toPush.getType());
                    ItemStack newItem = Objects.requireNonNull(hopper.getInventory().getItem(slot));
                    newItem.setAmount(newItem.getAmount() - 1);
                    if (newItem.getAmount() == 0)
                        hopper.getInventory().setItem(slot, null);
                } catch (CandleStoreException ignored) {
                }
            }, 1L);
        });
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onHopperPullStackFromStore(InventoryMoveItemEvent event) {
        if (!(event.getDestination().getHolder() instanceof Hopper hopper))
            return;
        if (!(event.getSource().getHolder() instanceof Dispenser dispenser))
            return;
        Optional<CandleStore> optCandleStore = CandleStoreManager.getStore(dispenser);
        optCandleStore.ifPresent(candleStore -> {
            event.setCancelled(true);
            Material mat = event.getItem().getType();
            Bukkit.getServer().getScheduler().runTaskLater(StadtServer.getInstance(), () -> {
                try {
                    ItemStack pulledItem = candleStore.pullItemStack(mat, 1);
                    hopper.getInventory().addItem(pulledItem);
                } catch (CandleStoreException ignored) {
                }
            }, 1L);
        });
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onWorldSave(WorldSaveEvent event) {
        if (event.getWorld().getEnvironment() != World.Environment.NORMAL)
            return;
        StadtServer.LOGGER.info("Saving candle stores.");
        CandleStoreManager.saveStoresOnWorldSave();
    }
}
