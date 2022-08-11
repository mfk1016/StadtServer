package me.mfk1016.stadtserver.listener;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.candlestore.CandleStore;
import me.mfk1016.stadtserver.candlestore.CandleStoreManager;
import me.mfk1016.stadtserver.candlestore.util.CandleStoreException;
import me.mfk1016.stadtserver.util.Keys;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Candle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Comparator;
import java.util.*;

import static me.mfk1016.stadtserver.util.Functions.playerMessage;
import static me.mfk1016.stadtserver.util.Functions.stackEmpty;

public class CandleStoreListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerOpenCandleStoreView(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player.isSneaking())
            return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || CandleStoreManager.isCandleTool(event.getItem()))
            return;
        Block target = Objects.requireNonNull(event.getClickedBlock());
        if (!(target.getState() instanceof Dispenser dispenser))
            return;
        PersistentDataContainer pdc = dispenser.getPersistentDataContainer();
        if (!pdc.has(Keys.CANDLE_STORE_MEMBER_TYPE))
            return;
        String memberType = pdc.get(Keys.CANDLE_STORE_MEMBER_TYPE, PersistentDataType.STRING);
        if (CandleStoreManager.STORE_MEMBER_EXPORT.equals(memberType))
            return;
        Optional<CandleStore> currentStore = CandleStoreManager.getStore(dispenser);
        if (currentStore.isPresent()) {
            event.setCancelled(true);
            PersistentDataContainer playerPdc = player.getPersistentDataContainer();
            playerPdc.set(Keys.CANDLE_STORE, PersistentDataType.INTEGER, 1);
            player.openInventory(currentStore.get().getView());
            currentStore.get().updateView(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerClickCandleStoreView(InventoryClickEvent event) {
        if (event.isShiftClick())
            return;
        PersistentDataContainer pdc = event.getWhoClicked().getPersistentDataContainer();
        if (!pdc.has(Keys.CANDLE_STORE))
            return;
        if (event.getClickedInventory() instanceof PlayerInventory || event.getClickedInventory() == null)
            return;

        event.setCancelled(true);
        Inventory view = event.getClickedInventory();
        int slot = event.getSlot();
        ItemStack stack = view.getItem(slot);
        Player player = (Player) event.getWhoClicked();
        try {
            if (stackEmpty(player.getItemOnCursor())) {
                if (stackEmpty(stack))
                    return;
                int pullAmount = event.isRightClick() ? stack.getMaxStackSize() : 1;
                ItemStack pulled = CandleStoreManager.getStore(view).pullItemStack(stack.getType(), pullAmount);
                player.setItemOnCursor(pulled);
            } else {
                ItemStack toPush = player.getItemOnCursor();
                CandleStoreManager.getStore(view).pushItemStack(toPush);
                player.setItemOnCursor(null);
            }
        } catch (CandleStoreException e) {
            playerMessage(player, e.getMessage());
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerShiftClickCandleStoreView(InventoryClickEvent event) {
        if (!event.isShiftClick() || stackEmpty(event.getCurrentItem()))
            return;
        PersistentDataContainer pdc = event.getWhoClicked().getPersistentDataContainer();
        if (!pdc.has(Keys.CANDLE_STORE) || event.getClickedInventory() == null)
            return;

        event.setCancelled(true);
        Inventory source = event.getClickedInventory();
        int slot = event.getSlot();
        ItemStack clicked = event.getCurrentItem();
        Player player = (Player) event.getWhoClicked();
        try {
            if (!(source instanceof PlayerInventory)) {
                int pullAmount = clicked.getMaxStackSize();
                ItemStack pulled = CandleStoreManager.getStore(source).pullItemStack(clicked.getType(), pullAmount);
                Collection<ItemStack> left = player.getInventory().addItem(pulled).values();
                for (ItemStack leftStack : left)
                    CandleStoreManager.getStore(source).pushItemStack(leftStack);
            } else {
                CandleStoreManager.getStore(event.getView().getTopInventory()).pushItemStack(clicked);
                source.clear(slot);
            }
        } catch (CandleStoreException e) {
            playerMessage(player, e.getMessage());
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerCloseCandleStoreView(InventoryCloseEvent event) {
        PersistentDataContainer pdc = event.getPlayer().getPersistentDataContainer();
        if (pdc.has(Keys.CANDLE_STORE))
            pdc.remove(Keys.CANDLE_STORE);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerAddCandleStoreMember(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || !CandleStoreManager.isCandleTool(event.getItem()))
            return;
        Block target = Objects.requireNonNull(event.getClickedBlock());
        if (!(target.getState() instanceof Dispenser dispenser))
            return;
        if (!(target.getBlockData() instanceof Directional dispenserDirection))
            return;
        if (dispenserDirection.getFacing() != BlockFace.DOWN)
            return;
        if (!(target.getRelative(BlockFace.UP).getBlockData() instanceof Candle))
            return;

        Optional<CandleStore> current = CandleStoreManager.getStore(dispenser);
        if (current.isPresent()) {
            event.setCancelled(true);
            playerMessage(event.getPlayer(),
                    "Candle Store with " + current.get().getMemberCount() + " members and " +
                            current.get().getUsedSlots() + "/" + current.get().getStorageSlots() + " slots used.");
            return;
        }


        event.setCancelled(true);
        Material candleMat = target.getRelative(BlockFace.UP).getType();
        boolean hasChest = target.getRelative(BlockFace.DOWN).getState() instanceof Chest;
        boolean isExport = target.getRelative(BlockFace.DOWN).getState() instanceof Hopper;
        String memberType =
                hasChest ? CandleStoreManager.STORE_MEMBER_CHEST :
                        isExport ? CandleStoreManager.STORE_MEMBER_EXPORT : CandleStoreManager.STORE_MEMBER_VIEW;

        List<Block> dispensers = new ArrayList<>();
        for (int x = -16; x <= 16; x++) {
            for (int y = -16; y <= 16; y++) {
                for (int z = -16; z <= 16; z++) {
                    if (x == 0 && y == 0 && z == 0) {
                        continue;
                    }
                    Block relative = target.getRelative(x, y, z);
                    if (relative.getRelative(BlockFace.UP).getType() != candleMat)
                        continue;
                    if (relative.getState() instanceof Dispenser)
                        dispensers.add(relative);
                }
            }
        }
        dispensers.sort(Comparator.comparingDouble(a -> target.getLocation().distance(a.getLocation())));
        for (Block partnerBlock : dispensers) {
            Dispenser partner = (Dispenser) partnerBlock.getState();
            Optional<CandleStore> optStore = CandleStoreManager.getStore(partner);
            if (optStore.isPresent()) {
                CandleStoreManager.addToStore(dispenser, partner, hasChest, memberType);
                playerMessage(event.getPlayer(), "New Member added to store.");
                return;
            }
        }
        CandleStoreManager.addToStore(dispenser, null, hasChest, memberType);
        playerMessage(event.getPlayer(), "New store created.");
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBreakCandleStoreMember(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block target = event.getBlock();
        if (target.getBlockData() instanceof Candle) {
            Block possibleTarget = target.getRelative(BlockFace.DOWN);
            if (!(possibleTarget.getState() instanceof Dispenser dispenser))
                return;
            if (CandleStoreManager.getStore(dispenser).isPresent()) {
                boolean hasChest = possibleTarget.getRelative(BlockFace.DOWN).getState() instanceof Chest;
                CandleStoreManager.deleteFromStore(dispenser, hasChest);
                playerMessage(player, "Candle store member removed");
            }
        } else if (target.getState() instanceof Chest && target.getRelative(BlockFace.UP).getState() instanceof Dispenser dispenser) {
            if (CandleStoreManager.getStore(dispenser).isPresent()) {
                CandleStoreManager.updateStoreMember(dispenser, false, true, CandleStoreManager.STORE_MEMBER_VIEW);
                playerMessage(player, "Chest removed from candle store member");
            }
        } else if (target.getState() instanceof Dispenser dispenser) {
            boolean hasChest = target.getRelative(BlockFace.DOWN).getState() instanceof Chest;
            if (CandleStoreManager.getStore(dispenser).isPresent()) {
                CandleStoreManager.deleteFromStore(dispenser, hasChest);
                playerMessage(player, "Candle store member removed");
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onDestroyCandleStoreMember(BlockDestroyEvent event) {
        Block target = event.getBlock();
        if (target.getBlockData() instanceof Candle) {
            Block possibleTarget = target.getRelative(BlockFace.DOWN);
            if (!(possibleTarget.getState() instanceof Dispenser dispenser))
                return;
            boolean hasChest = possibleTarget.getRelative(BlockFace.DOWN).getState() instanceof Chest;
            CandleStoreManager.deleteFromStore(dispenser, hasChest);
        } else if (target.getState() instanceof Chest && target.getRelative(BlockFace.UP).getState() instanceof Dispenser dispenser) {
            CandleStoreManager.updateStoreMember(dispenser, false, true, CandleStoreManager.STORE_MEMBER_VIEW);
        } else if (target.getState() instanceof Dispenser dispenser) {
            boolean hasChest = target.getRelative(BlockFace.DOWN).getState() instanceof Chest;
            CandleStoreManager.deleteFromStore(dispenser, hasChest);
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
            CandleStoreManager.updateStoreMember(dispenser,
                    true, false, CandleStoreManager.STORE_MEMBER_CHEST);
            playerMessage(event.getPlayer(), "Chest added to candle store member");
        });
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPushStackToStore(InventoryMoveItemEvent event) {
        if (!(event.getSource().getHolder() instanceof Hopper hopper))
            return;
        if (!(event.getDestination().getHolder() instanceof Dispenser dispenser))
            return;
        Optional<CandleStore> optCandleStore = CandleStoreManager.getStore(dispenser);
        optCandleStore.ifPresent(candleStore -> {
            event.setCancelled(true);
            Block hopperBlock = hopper.getBlock();
            ItemStack item = event.getItem().clone();
            item.setAmount(1);
            Bukkit.getServer().getScheduler().runTaskLater(StadtServer.getInstance(), () -> {
                try {
                    candleStore.pushItemStack(item);
                    Hopper realHopper = (Hopper) hopperBlock.getState();
                    int slot = realHopper.getInventory().first(item.getType());
                    ItemStack newItem = Objects.requireNonNull(realHopper.getInventory().getItem(slot)).clone();
                    newItem.setAmount(newItem.getAmount() - 1);
                    newItem = newItem.getAmount() > 0 ? newItem : null;
                    realHopper.getInventory().setItem(slot, newItem);
                } catch (CandleStoreException ignored) {
                }
            }, 1L);
        });
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPullStackFromStore(InventoryMoveItemEvent event) {
        if (!(event.getDestination().getHolder() instanceof Hopper hopper))
            return;
        if (!(event.getSource().getHolder() instanceof Dispenser dispenser))
            return;
        Optional<CandleStore> optCandleStore = CandleStoreManager.getStore(dispenser);
        optCandleStore.ifPresent(candleStore -> {
            event.setCancelled(true);
            Block hopperBlock = hopper.getBlock();
            Block dispenserBlock = dispenser.getBlock();
            Bukkit.getServer().getScheduler().runTaskLater(StadtServer.getInstance(), () -> {
                Inventory filter = ((Dispenser) dispenserBlock.getState()).getInventory();
                for (ItemStack item : filter) {
                    if (stackEmpty(item))
                        continue;
                    try {
                        ItemStack realItem = candleStore.pullItemStack(item.getType(), 1);
                        Hopper realHopper = (Hopper) hopperBlock.getState();
                        realHopper.getInventory().addItem(realItem);
                        break;
                    } catch (CandleStoreException ignored) {
                    }
                }
            }, 1L);
        });
    }
}
