package me.mfk1016.stadtserver.listener;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.candlestore.CandleStore;
import me.mfk1016.stadtserver.candlestore.CandleStoreException;
import me.mfk1016.stadtserver.candlestore.CandleStoreManager;
import me.mfk1016.stadtserver.candlestore.CandleStoreUtils;
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
import org.bukkit.event.inventory.InventoryDragEvent;
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
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || CandleStoreUtils.isCandleTool(event.getItem()))
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
        if (!CandleStoreManager.isStoreView(target))
            return;

        event.setCancelled(true);
        try {
            if (stackEmpty(player.getItemOnCursor())) {
                if (stackEmpty(clicked))
                    return;
                int pullAmount = event.isLeftClick() ? clicked.getMaxStackSize() : 1;
                ItemStack pulled = CandleStoreManager.getStore(target).pullItemStack(clicked.getType(), pullAmount);
                player.setItemOnCursor(pulled);
            } else {
                ItemStack toPush = player.getItemOnCursor();
                CandleStoreManager.getStore(target).pushItemStack(toPush);
                player.setItemOnCursor(null);
            }
        } catch (CandleStoreException ignored) {
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDragAtCandleStoreView(InventoryDragEvent event) {
        if (CandleStoreManager.isStoreView(event.getView().getTopInventory())) {
            if (!event.getInventorySlots().stream().filter(value -> value < 54).toList().isEmpty())
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
        if (!CandleStoreManager.isStoreView(event.getView().getTopInventory()))
            return;

        event.setCancelled(true);
        try {
            if (!(target instanceof PlayerInventory)) {
                int pullAmount = clicked.getMaxStackSize();
                ItemStack pulled = CandleStoreManager.getStore(target).pullItemStack(clicked.getType(), pullAmount);
                Collection<ItemStack> left = player.getInventory().addItem(pulled).values();
                for (ItemStack leftStack : left)
                    CandleStoreManager.getStore(target).pushItemStack(leftStack);
            } else {
                CandleStoreManager.getStore(event.getView().getTopInventory()).pushItemStack(clicked);
                target.clear(event.getSlot());
            }
        } catch (CandleStoreException ignored) {
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerAddCandleStoreMember(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || !CandleStoreUtils.isCandleTool(event.getItem()))
            return;
        Block target = Objects.requireNonNull(event.getClickedBlock());
        if (!(target.getState() instanceof Dispenser dispenser))
            return;

        Optional<CandleStore> current = CandleStoreManager.getStore(dispenser);
        if (current.isPresent()) {
            event.setCancelled(true);
            Material candleMat = target.getRelative(BlockFace.UP).getType();
            playerMessage(event.getPlayer(),
                    CandleStoreUtils.getCandleName(candleMat, false) + " store with " +
                            current.get().getMemberCount() + " members and " +
                            current.get().getUsedSlots() + "/" + current.get().getStorageSlots() + " slots used.");
            return;
        }
        if (!dispenser.getInventory().isEmpty()) {
            playerMessage(event.getPlayer(), "The dispenser must be empty.");
            return;
        }
        Directional dispenserDirection = (Directional) target.getBlockData();
        if (dispenserDirection.getFacing() != BlockFace.DOWN) {
            playerMessage(event.getPlayer(), "The dispenser must face down.");
            return;
        }
        if (!(target.getRelative(BlockFace.UP).getBlockData() instanceof Candle)) {
            playerMessage(event.getPlayer(), "A candle must be placed on the dispenser.");
            return;
        }

        event.setCancelled(true);
        Material candleMat = target.getRelative(BlockFace.UP).getType();
        BlockState attachedBlockState = target.getRelative(BlockFace.DOWN).getState();
        boolean hasChest = attachedBlockState instanceof Chest;
        boolean isExport = attachedBlockState instanceof Hopper;
        String memberType = hasChest ? CandleStoreManager.STORE_MEMBER_CHEST :
                isExport ? CandleStoreManager.STORE_MEMBER_EXPORT :
                        CandleStoreManager.STORE_MEMBER_VIEW;

        List<Block> dispensers = new ArrayList<>();
        for (int x = -8; x <= 8; x++) {
            for (int y = -8; y <= 8; y++) {
                for (int z = -8; z <= 8; z++) {
                    if (x == 0 && y == 0 && z == 0)
                        continue;
                    Block relative = target.getRelative(x, y, z);
                    if (relative.getRelative(BlockFace.UP).getType() != candleMat)
                        continue;
                    if (relative.getState() instanceof Dispenser)
                        dispensers.add(relative);
                }
            }
        }
        dispensers.sort(Comparator.comparingDouble(a -> target.getLocation().distance(a.getLocation())));
        String candleName = CandleStoreUtils.getCandleName(candleMat, true);
        for (Block partnerBlock : dispensers) {
            Dispenser partner = (Dispenser) partnerBlock.getState();
            Optional<CandleStore> optStore = CandleStoreManager.getStore(partner);
            if (optStore.isPresent()) {
                if (optStore.get().getCenterLocation().distance(dispenser.getLocation().toVector()) > 16D) {
                    playerMessage(event.getPlayer(),
                            "Too near to " + candleName + " store while further than 16 blocks away from the store center.");
                    return;
                }
                CandleStoreManager.addToStore(dispenser, partner, hasChest, memberType);
                playerMessage(event.getPlayer(), "New Member added to " + candleName + " store.");
                return;
            }
        }
        CandleStoreManager.addToStore(dispenser, null, hasChest, memberType);
        playerMessage(event.getPlayer(), "New " + candleName + " store created.");
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
                String candleName = CandleStoreUtils.getCandleName(target.getType(), false);
                playerMessage(player, candleName + " store member removed.");
            }
        } else if (target.getState() instanceof Chest &&
                target.getRelative(BlockFace.UP).getState() instanceof Dispenser dispenser) {
            if (CandleStoreManager.getStore(dispenser).isPresent()) {
                CandleStoreManager.updateStoreMember(dispenser, false, true, CandleStoreManager.STORE_MEMBER_VIEW);
                Material candleMat = target.getRelative(BlockFace.UP, 2).getType();
                String candleName = CandleStoreUtils.getCandleName(candleMat, true);
                playerMessage(player, "Chest removed from " + candleName + " store member.");
            }
        } else if (target.getState() instanceof Dispenser dispenser) {
            boolean hasChest = target.getRelative(BlockFace.DOWN).getState() instanceof Chest;
            if (CandleStoreManager.getStore(dispenser).isPresent()) {
                CandleStoreManager.deleteFromStore(dispenser, hasChest);
                Material candleMat = target.getRelative(BlockFace.UP).getType();
                String candleName = CandleStoreUtils.getCandleName(candleMat, false);
                playerMessage(player, candleName + " store member removed.");
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
        } else if (target.getState() instanceof Chest &&
                target.getRelative(BlockFace.UP).getState() instanceof Dispenser dispenser) {
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
            CandleStoreManager.updateStoreMember(dispenser,
                    false, false, CandleStoreManager.STORE_MEMBER_EXPORT);
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
}
