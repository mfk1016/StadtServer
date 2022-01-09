package me.mfk1016.stadtserver;

import com.destroystokyo.paper.event.inventory.PrepareResultEvent;
import me.mfk1016.stadtserver.enchantments.*;
import me.mfk1016.stadtserver.origin.enchantment.EnchantmentOrigin;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.GrindstoneInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;

import java.lang.reflect.Field;
import java.util.*;

public class EnchantmentManager {

    // Public stuff
    public static final Set<CustomEnchantment> ALL_ENCHANTMENTS = new HashSet<>();
    public static final Set<EnchantmentOrigin> ORIGINS = new HashSet<>();
    // Internal stuff
    private static final Set<CustomEnchantment> registeredEnchantments = new HashSet<>();
    // Add all custom enchantments here
    public static ChoppingEnchantment CHOPPING;
    public static SmithingEnchantment SMITHING;
    public static WrenchEnchantment WRENCH;
    public static EagleEyeEnchantment EAGLE_EYE;
    public static FarmingEnchantment FARMING;
    public static SacrificialEnchantment SACRIFICIAL;

    /* --- INIT + STOP --- */

    public static void onPluginEnable(StadtServer plugin) {

        // Initialize them here
        CHOPPING = new ChoppingEnchantment(plugin);
        SMITHING = new SmithingEnchantment(plugin);
        WRENCH = new WrenchEnchantment(plugin);
        EAGLE_EYE = new EagleEyeEnchantment(plugin);
        FARMING = new FarmingEnchantment(plugin);
        SACRIFICIAL = new SacrificialEnchantment(plugin);

        // Add them here
        ALL_ENCHANTMENTS.add(CHOPPING);
        ALL_ENCHANTMENTS.add(SMITHING);
        ALL_ENCHANTMENTS.add(WRENCH);
        ALL_ENCHANTMENTS.add(EAGLE_EYE);
        ALL_ENCHANTMENTS.add(FARMING);
        ALL_ENCHANTMENTS.add(SACRIFICIAL);

        // Registering
        for (CustomEnchantment e : ALL_ENCHANTMENTS) {
            registerEnchantment(e);
        }

        // Attach Listeners
        PluginManager pm = plugin.getServer().getPluginManager();
        for (CustomEnchantment e : ALL_ENCHANTMENTS) {
            pm.registerEvents(e, plugin);
            ORIGINS.addAll(e.getOrigins());
        }
    }

    public static void onPluginDisable() {
        ORIGINS.clear();
        unregisterEnchantments();
    }

    /* --- REGISTRATION --- */

    private static void registerEnchantment(CustomEnchantment enchantment) {
        boolean registered = true;
        try {
            Field f = Enchantment.class.getDeclaredField("acceptingNew");
            f.setAccessible(true);
            f.set(null, true);
            Enchantment.registerEnchantment(enchantment);
        } catch (Exception e) {
            registered = false;
            e.printStackTrace();
        }
        if (registered) {
            registeredEnchantments.add(enchantment);
        }
    }

    private static void unregisterEnchantments() {
        try {
            Field keyField = Enchantment.class.getDeclaredField("byKey");

            keyField.setAccessible(true);
            @SuppressWarnings("unchecked")
            HashMap<NamespacedKey, Enchantment> byKey = (HashMap<NamespacedKey, Enchantment>) keyField.get(null);

            for (Enchantment enchantment : registeredEnchantments) {
                byKey.remove(enchantment.getKey());
            }

            Field nameField = Enchantment.class.getDeclaredField("byName");

            nameField.setAccessible(true);
            @SuppressWarnings("unchecked")
            HashMap<String, Enchantment> byName = (HashMap<String, Enchantment>) nameField.get(null);

            for (Enchantment enchantment : registeredEnchantments) {
                byName.remove(enchantment.getName());
            }
        } catch (Exception ignored) {
        }
        registeredEnchantments.clear();
    }

    /* --- ENCHANTING --- */

    public static boolean isEnchantedWith(ItemStack item, Enchantment enchantment, int level) {
        return getItemEnchantments(item).getOrDefault(Enchantment.getByKey(enchantment.getKey()), -1) == level;
    }

    public static boolean isEnchantedWith(ItemStack item, Enchantment enchantment) {
        return getItemEnchantments(item).containsKey(Enchantment.getByKey(enchantment.getKey()));
    }

    public static Map<Enchantment, Integer> getItemEnchantments(ItemStack item) {
        if (item.getItemMeta() instanceof EnchantmentStorageMeta) {
            return ((EnchantmentStorageMeta) item.getItemMeta()).getStoredEnchants();
        } else {
            return item.getEnchantments();
        }
    }

    public static void enchantItem(ItemStack item, Enchantment enchantment, int level) {
        ItemMeta meta = Objects.requireNonNull(item.getItemMeta());
        if (meta instanceof EnchantmentStorageMeta enchStore) {
            enchStore.addStoredEnchant(enchantment, level, true);
        } else {
            meta.addEnchant(enchantment, level, true);
        }
        if (enchantment instanceof CustomEnchantment customEnchantment) {
            String ceString = customEnchantment.getLoreEntry(level);
            List<String> lore;
            if (meta.hasLore()) {
                lore = Objects.requireNonNull(meta.getLore());
                if (!lore.contains(ceString)) {
                    lore.add(ceString);
                }
            } else {
                lore = new ArrayList<>();
                lore.add(ceString);
            }
            meta.setLore(lore);
        }
        item.setItemMeta(meta);
    }

    public static void disenchantItem(ItemStack item, Enchantment enchantment) {
        if (item.getEnchantments().containsKey(enchantment)) {
            if (enchantment instanceof CustomEnchantment customEnchantment) {
                ItemMeta meta = item.getItemMeta();
                int level = item.getEnchantmentLevel(enchantment);
                List<String> lore = Objects.requireNonNull(meta).getLore();
                Objects.requireNonNull(lore).remove(customEnchantment.getLoreEntry(level));
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            item.removeEnchantment(enchantment);
        } else if (item.getItemMeta() instanceof EnchantmentStorageMeta meta) {
            if (meta.hasStoredEnchant(enchantment)) {
                if (enchantment instanceof CustomEnchantment customEnchantment) {
                    int level = meta.getStoredEnchantLevel(enchantment);
                    List<String> lore = meta.getLore();
                    Objects.requireNonNull(lore).remove(customEnchantment.getLoreEntry(level));
                    meta.setLore(lore);
                }
                meta.removeStoredEnchant(enchantment);
                item.setItemMeta(meta);
            }
        }
    }

    /* --- GRINDSTONE SUPPORT --- */

    public static void fixGrindstoneResult(PrepareResultEvent event) {
        if (!(event.getInventory() instanceof GrindstoneInventory))
            return;
        if (event.getResult() == null)
            return;

        // The result is technically correct, but there may be lore left
        ItemStack result = event.getResult();
        ItemMeta meta = result.getItemMeta();
        if (meta == null || meta.getLore() == null)
            return;
        List<String> lore = meta.getLore();
        ListIterator<String> loreIterator = lore.listIterator();
        while (loreIterator.hasNext()) {
            String entry = loreIterator.next();
            for (CustomEnchantment enchantment : ALL_ENCHANTMENTS) {
                String enchString = enchantment.getLoreEntry(1);
                if (entry.startsWith(enchString)) {
                    loreIterator.remove();
                    break;
                }
            }
        }
        meta.setLore(lore);
        result.setItemMeta(meta);
        event.setResult(result);
    }
}
