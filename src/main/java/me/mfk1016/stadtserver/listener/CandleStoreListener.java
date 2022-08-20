package me.mfk1016.stadtserver.listener;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import io.papermc.paper.event.block.BlockBreakBlockEvent;
import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.candlestore.*;
import me.mfk1016.stadtserver.util.Keys;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
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

    @EventHandler(priority = EventPriority.LOWEST)
    public void onStopCandleStoreMemberDispense(BlockDispenseEvent event) {
        if (event.getBlock().getType() != Material.DISPENSER)
            return;
        if (CandleStoreManager.getStore((Dispenser) event.getBlock().getState(false)).isPresent())
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerOpenCandleStoreView(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player.isSneaking())
            return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || CandleStoreUtils.isCandleTool(event.getItem()))
            return;
        Block target = Objects.requireNonNull(event.getClickedBlock());
        if (!(target.getState(false) instanceof Dispenser dispenser))
            return;
        PersistentDataContainer pdc = dispenser.getPersistentDataContainer();
        if (!pdc.has(Keys.CANDLE_STORE_MEMBER_TYPE))
            return;
        if (CandleMemberType.getMemberType(pdc) == CandleMemberType.EXPORT)
            return;
        Optional<CandleStore> currentStore = CandleStoreManager.getStore(dispenser);
        if (currentStore.isPresent()) {
            event.setCancelled(true);
            if (currentStore.get().getStorageSlots() > 0) {
                player.openInventory(currentStore.get().getView());
                currentStore.get().updateView(true);
            } else {
                playerMessage(player, "The store has no storage slots.");
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerClickCandleStoreView(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory target = event.getClickedInventory();
        ItemStack clicked = event.getCurrentItem();
        if (event.isShiftClick() || target == null || target instanceof PlayerInventory)
            return;
        if (!(target.getHolder(false) instanceof CandleStore store))
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
        if (event.getView().getTopInventory().getHolder(false) instanceof CandleStore) {
            if (event.getRawSlots().stream().anyMatch(value -> value < 54))
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
        if (!(event.getView().getTopInventory().getHolder(false) instanceof CandleStore store))
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
        CandleMemberType memberType = switch (target.getRelative(BlockFace.DOWN).getType()) {
            case CHEST -> CandleMemberType.CHEST;
            case HOPPER -> CandleMemberType.EXPORT;
            default -> CandleMemberType.NORMAL;
        };
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
                CandleMemberType memberType = switch (target.getRelative(BlockFace.DOWN).getType()) {
                    case CHEST -> CandleMemberType.CHEST;
                    case HOPPER -> CandleMemberType.EXPORT;
                    default -> CandleMemberType.NORMAL;
                };
                CandleStoreManager.addToStore(dispenser, partner, memberType);
                playerMessage(event.getPlayer(), "New Member added to " + candleName + " store.");
                return;
            }
        }
        playerMessage(event.getPlayer(), "No " + candleName + " store found within 8 blocks.");
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBreakCandleStoreMember(BlockBreakEvent event) {
        CandleStoreUtils.onDestroyCandleStoreMember(event.getBlock(), event.getPlayer());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onDestroyCandleStoreMember(BlockDestroyEvent event) {
        CandleStoreUtils.onDestroyCandleStoreMember(event.getBlock(), null);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBreakCandleStoreMember(BlockBreakBlockEvent event) {
        CandleStoreUtils.onDestroyCandleStoreMember(event.getBlock(), null);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockExplodeCandleStoreMember(BlockExplodeEvent event) {
        for (Block target : event.blockList())
            CandleStoreUtils.onDestroyCandleStoreMember(target, null);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityExplodeCandleStoreMember(EntityExplodeEvent event) {
        for (Block target : event.blockList())
            CandleStoreUtils.onDestroyCandleStoreMember(target, null);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onAddBlockToStoreMember(BlockPlaceEvent event) {
        Material addedType = event.getItemInHand().getType();
        if (addedType != Material.CHEST && addedType != Material.HOPPER)
            return;
        Block possibleTarget = event.getBlockPlaced();
        if (!(possibleTarget.getRelative(BlockFace.UP).getState() instanceof Dispenser dispenser))
            return;
        CandleMemberType memberType = addedType == Material.CHEST ? CandleMemberType.CHEST : CandleMemberType.EXPORT;
        CandleStoreManager.getStore(dispenser).ifPresent(candleStore -> {
            CandleStoreManager.updateStoreMember(dispenser, memberType);
            Material candleMat = possibleTarget.getRelative(BlockFace.UP, 2).getType();
            String candleName = CandleStoreUtils.getCandleName(candleMat, true);
            String elementName = addedType.name().charAt(0) + addedType.name().substring(1).toLowerCase().replace("_", " ");
            playerMessage(event.getPlayer(), elementName + " added to " + candleName + " store member.");
        });
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onHopperPushStackToStore(InventoryMoveItemEvent event) {
        if (!(event.getSource().getHolder(false) instanceof Hopper hopper))
            return;
        if (!(event.getDestination().getHolder(false) instanceof Dispenser dispenser))
            return;
        Optional<CandleStore> optCandleStore = CandleStoreManager.getStore(dispenser);
        optCandleStore.ifPresent(candleStore -> {
            event.setCancelled(true);
            if (!candleStore.accepts(event.getItem().getType()))
                return;
            ItemStack toPush = new ItemStack(event.getItem().getType(), 1);
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
        if (!(event.getDestination().getHolder(false) instanceof Hopper hopper))
            return;
        if (!(event.getSource().getHolder(false) instanceof Dispenser dispenser))
            return;
        Optional<CandleStore> optCandleStore = CandleStoreManager.getStore(dispenser);
        optCandleStore.ifPresent(candleStore -> {
            event.setCancelled(true);
            Material mat = event.getItem().getType();
            if (!candleStore.contains(mat, 1))
                return;
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
