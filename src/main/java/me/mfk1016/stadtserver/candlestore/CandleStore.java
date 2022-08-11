package me.mfk1016.stadtserver.candlestore;

import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.candlestore.util.CandleStoreException;
import me.mfk1016.stadtserver.util.Keys;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

import static me.mfk1016.stadtserver.util.Functions.stackEmpty;
import static me.mfk1016.stadtserver.util.Functions.undecoratedText;

public class CandleStore {

    public static CandleStore createStore(String key, boolean hasChest) {
        return new CandleStore(key, 1, hasChest ? getConfigSlotsOfChest() : 0, 0, new EnumMap<>(Material.class));
    }

    private static int getConfigSlotsOfChest() {
        return StadtServer.getInstance().getConfig().getInt(Keys.CONFIG_CANDLE_STORE_CHEST_SLOTS);
    }

    private final String key;
    private int memberCount;
    private int storageSlots;

    private int usedSlots;

    private final EnumMap<Material, Long> storage;
    private final Inventory view;

    public CandleStore(String key, int memberCount, int storageSlots, int usedSlots, EnumMap<Material, Long> storage) {
        this.key = key;
        this.memberCount = memberCount;
        this.storageSlots = storageSlots;
        this.usedSlots = usedSlots;
        this.storage = storage;
        view = Bukkit.createInventory(null, 54, undecoratedText("Candle Store Index"));
        view.setMaxStackSize(Integer.MAX_VALUE);
        updateView(true);
    }

    public long getItemAmount(Material material) {
        return storage.getOrDefault(material, 0L);
    }

    public void pushItemStack(ItemStack stack) throws CandleStoreException {
        assert !stackEmpty(stack);
        if (!isValidStoreItem(stack)) {
            throw CandleStoreException.invalidItem();
        }
        long currentAmount = getItemAmount(stack.getType());
        if (usedSlots == storageSlots && currentAmount == 0) {
            throw CandleStoreException.storeFull();
        }
        usedSlots = currentAmount == 0 ? usedSlots + 1 : usedSlots;
        storage.put(stack.getType(), currentAmount + stack.getAmount());
        updateView(false);
    }

    public ItemStack pullItemStack(Material material, int amount) throws CandleStoreException {
        long currentAmount = getItemAmount(material);
        if (currentAmount == 0) {
            throw CandleStoreException.itemNotFound();
        }
        long newAmount = currentAmount - amount;
        ItemStack stack = new ItemStack(material, amount);
        if (newAmount <= 0) {
            usedSlots--;
            stack.setAmount((int) currentAmount);
            storage.remove(stack.getType());
        } else {
            storage.put(stack.getType(), newAmount);
        }
        updateView(false);
        return stack;
    }

    public void addMember(boolean hasChest) {
        memberCount++;
        updateMember(hasChest, false);
    }

    public void updateMember(boolean chestAdded, boolean chestRemoved) {
        assert !(chestAdded && chestRemoved);
        if (chestAdded) {
            storageSlots += getConfigSlotsOfChest();
        } else if (chestRemoved) {
            storageSlots -= getConfigSlotsOfChest();
            Iterator<Material> materialIterator = storage.keySet().iterator();
            while (usedSlots > storageSlots && materialIterator.hasNext()) {
                Material mat = materialIterator.next();
                storage.remove(mat);
                usedSlots--;
            }
        }
    }

    public void updateView(boolean force) {
        if (view.getViewers().isEmpty() && !force)
            return;
        view.clear();
        List<ItemStack> elements = storage.entrySet().stream()
                .sorted(Comparator.comparingLong(Map.Entry::getValue))
                .map(entry -> {
                    ItemStack elem = new ItemStack(entry.getKey());
                    ItemMeta meta = elem.getItemMeta();
                    meta.displayName(undecoratedText(entry.getValue().toString()));
                    elem.setItemMeta(meta);
                    return elem;
                }).toList();
        int slot = 0;
        for (int i = elements.size() - 1; i >= 0; i--) {
            if (slot == 54) break;
            view.setItem(slot, elements.get(i));
            slot++;
        }
    }

    public void deleteMember(boolean hasChest) {
        memberCount--;
        updateMember(false, hasChest);
    }

    public String getKey() {
        return key;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public int getStorageSlots() {
        return storageSlots;
    }

    public int getUsedSlots() {
        return usedSlots;
    }

    public EnumMap<Material, Long> getStorage() {
        return storage;
    }

    public Inventory getView() {
        return view;
    }

    public static boolean isValidStoreItem(ItemStack item) {
        if (item.getEnchantments().size() > 0)
            return false;
        if (item.getMaxStackSize() > 1)
            return true;
        return switch (item.getType()) {
            case CLOCK, COMPASS -> true;
            default -> false;
        };
    }
}
