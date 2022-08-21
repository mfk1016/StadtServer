package me.mfk1016.stadtserver.candlestore;

import lombok.Getter;
import org.bukkit.*;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static me.mfk1016.stadtserver.util.Functions.stackEmpty;
import static me.mfk1016.stadtserver.util.Functions.undecoratedText;

@Getter
public class CandleStore implements InventoryHolder {

    public static final int MAX_STORAGE_SLOTS = 54;
    public static final int SLOTS_PER_CHEST = 9;

    public static CandleStore createStore(Long key, boolean hasChest, Vector centerLocation) {
        return new CandleStore(key, 1, hasChest ? SLOTS_PER_CHEST : 0, 0, new EnumMap<>(Material.class), centerLocation);
    }

    private final Long key;
    private int memberCount;
    private int storageSlots;
    private int usedSlots;
    private final Vector centerLocation;
    private final EnumMap<Material, Long> storage;
    private Inventory view;

    public CandleStore(Long key, int memberCount, int storageSlots, int usedSlots, EnumMap<Material, Long> storage, Vector centerLocation) {
        this.key = key;
        this.memberCount = memberCount;
        this.storageSlots = storageSlots;
        this.usedSlots = usedSlots;
        this.centerLocation = centerLocation;
        this.storage = storage;
        recreateView();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean contains(Material mat, long amount) {
        return storage.getOrDefault(mat, 0L) >= amount;
    }

    public boolean accepts(Material mat) {
        return storage.containsKey(mat) || usedSlots < getStorageSlots();
    }

    public void pushItemStack(ItemStack stack) throws CandleStoreException {
        assert !stackEmpty(stack);
        if (!CandleStoreUtils.isValidStoreItem(stack)) {
            throw CandleStoreException.invalidItem();
        }
        long currentAmount = storage.getOrDefault(stack.getType(), 0L);
        if (usedSlots == getStorageSlots() && currentAmount == 0) {
            throw CandleStoreException.storeFull();
        }
        usedSlots = currentAmount == 0 ? usedSlots + 1 : usedSlots;
        storage.put(stack.getType(), currentAmount + stack.getAmount());
        updateView(false);
    }

    public ItemStack pullItemStack(Material material, int amount) throws CandleStoreException {
        long currentAmount = storage.getOrDefault(material, 0L);
        if (currentAmount == 0) {
            throw CandleStoreException.itemNotFound();
        }
        long newAmount = currentAmount - amount;
        ItemStack stack = new ItemStack(material, amount);
        if (newAmount <= 0) {
            usedSlots--;
            stack.setAmount((int) currentAmount);
            storage.remove(material);
        } else {
            storage.put(material, newAmount);
        }
        updateView(false);
        return stack;
    }

    public void addMember(Location position, CandleMemberType memberType) {
        centerLocation.setX((centerLocation.getX() * memberCount + position.getX()) / (memberCount + 1));
        centerLocation.setY((centerLocation.getY() * memberCount + position.getY()) / (memberCount + 1));
        centerLocation.setZ((centerLocation.getZ() * memberCount + position.getZ()) / (memberCount + 1));
        memberCount++;
        updateMember(CandleMemberType.NORMAL, memberType);
    }

    public void updateMember(CandleMemberType oldType, CandleMemberType newType) {
        assert oldType != newType || oldType == CandleMemberType.NORMAL;
        if (newType == CandleMemberType.CHEST) {
            storageSlots += SLOTS_PER_CHEST;
            recreateView();
        } else if (oldType == CandleMemberType.CHEST) {
            storageSlots -= SLOTS_PER_CHEST;
            Iterator<Material> materialIterator = storage.keySet().iterator();
            while (usedSlots > getStorageSlots() && materialIterator.hasNext()) {
                Material mat = materialIterator.next();
                storage.remove(mat);
                usedSlots--;
            }
            recreateView();
        }
    }

    public void recreateView() {
        ArrayList<HumanEntity> currentViewers = new ArrayList<>(view == null ? List.of() : view.getViewers());
        view = null;
        if (getStorageSlots() > 0)
            view = Bukkit.createInventory(this, getStorageSlots(), undecoratedText("Candle Store Index"));
        updateView(true);
        for (HumanEntity entity : currentViewers) {
            entity.closeInventory();
            if (view != null)
                entity.openInventory(view);
        }
    }

    public void updateView(boolean force) {
        if (view == null || (view.getViewers().isEmpty() && !force))
            return;
        view.clear();
        Iterator<Map.Entry<Material, Long>> iterator =
                storage.entrySet().stream().sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue())).iterator();
        int slot = 0;
        while (iterator.hasNext() && slot < view.getSize()) {
            Map.Entry<Material, Long> entry = iterator.next();
            view.setItem(slot, CandleStoreUtils.createViewItem(entry.getKey(), entry.getValue()));
            slot++;
        }
    }

    public void deleteMember(Location position, CandleMemberType memberType) {
        centerLocation.setX((centerLocation.getX() * memberCount - position.getX()) / (memberCount - 1));
        centerLocation.setY((centerLocation.getY() * memberCount - position.getY()) / (memberCount - 1));
        centerLocation.setZ((centerLocation.getZ() * memberCount - position.getZ()) / (memberCount - 1));
        memberCount--;
        updateMember(memberType, CandleMemberType.NORMAL);
    }

    public int getStorageSlots() {
        return Math.min(storageSlots, MAX_STORAGE_SLOTS);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return view;
    }
}
