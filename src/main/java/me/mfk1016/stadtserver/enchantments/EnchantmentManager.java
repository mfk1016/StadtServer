package me.mfk1016.stadtserver.enchantments;

import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.spells.CustomSpell;
import me.mfk1016.stadtserver.util.Keys;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.PluginManager;

import java.lang.reflect.Field;
import java.util.*;

import static me.mfk1016.stadtserver.util.Functions.stackEmpty;

public class EnchantmentManager {

    // Public stuff
    public static final Set<CustomEnchantment> ALL_ENCHANTMENTS = new HashSet<>();
    // Internal stuff
    private static final Set<CustomEnchantment> registeredEnchantments = new HashSet<>();
    // Add all custom enchantments here
    public static ChoppingEnchantment CHOPPING;
    public static SmithingEnchantment SMITHING;
    public static WrenchEnchantment WRENCH;
    public static EagleEyeEnchantment EAGLE_EYE;
    public static FarmingEnchantment FARMING;
    public static SacrificialEnchantment SACRIFICIAL;
    public static TrowelEnchantment TROWEL;

    /* --- INIT + STOP --- */

    public static void onPluginEnable() {

        // Initialize them here
        CHOPPING = new ChoppingEnchantment();
        SMITHING = new SmithingEnchantment();
        WRENCH = new WrenchEnchantment();
        EAGLE_EYE = new EagleEyeEnchantment();
        FARMING = new FarmingEnchantment();
        SACRIFICIAL = new SacrificialEnchantment();
        TROWEL = new TrowelEnchantment();

        // Add them here
        ALL_ENCHANTMENTS.add(CHOPPING);
        ALL_ENCHANTMENTS.add(SMITHING);
        ALL_ENCHANTMENTS.add(WRENCH);
        ALL_ENCHANTMENTS.add(EAGLE_EYE);
        ALL_ENCHANTMENTS.add(FARMING);
        ALL_ENCHANTMENTS.add(SACRIFICIAL);
        ALL_ENCHANTMENTS.add(TROWEL);

        // Registering
        registerEnchantments();

        // Attach Listeners
        PluginManager pm = StadtServer.getInstance().getServer().getPluginManager();
        for (CustomEnchantment e : ALL_ENCHANTMENTS) {
            pm.registerEvents(e, StadtServer.getInstance());
        }
    }

    public static void onPluginDisable() {
        unregisterEnchantments();
    }

    /* --- REGISTRATION --- */

    private static void registerEnchantments() {
        try {
            Field f = Enchantment.class.getDeclaredField("acceptingNew");
            f.setAccessible(true);
            f.set(null, true);
            for (CustomEnchantment enchantment : ALL_ENCHANTMENTS) {
                Enchantment.registerEnchantment(enchantment);
                registeredEnchantments.add(enchantment);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("deprecation")
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
        if (stackEmpty(item) || !item.hasItemMeta())
            return false;
        if (item.getType() == Material.ENCHANTED_BOOK)
            return ((EnchantmentStorageMeta) item.getItemMeta()).getStoredEnchantLevel(enchantment) == level;

        return item.getEnchantmentLevel(enchantment) == level;
    }

    public static boolean isEnchantedWith(ItemStack item, Enchantment enchantment) {
        if (stackEmpty(item) || !item.hasItemMeta())
            return false;
        if (item.getType() == Material.ENCHANTED_BOOK)
            return ((EnchantmentStorageMeta) item.getItemMeta()).getStoredEnchantLevel(enchantment) != 0;

        return item.getEnchantmentLevel(enchantment) != 0;
    }

    public static Map<Enchantment, Integer> getItemEnchantments(ItemStack item) {
        if (stackEmpty(item) || !item.hasItemMeta())
            return Map.of();
        if (item.getType() == Material.ENCHANTED_BOOK)
            return ((EnchantmentStorageMeta) item.getItemMeta()).getStoredEnchants();

        return item.getEnchantments();
    }

    public static boolean canEnchantItem(ItemStack item, Enchantment enchantment) {
        if (stackEmpty(item) || !enchantment.canEnchantItem(item))
            return false;
        for (Enchantment ench : getItemEnchantments(item).keySet()) {
            if (enchantment.conflictsWith(ench) || ench.conflictsWith(enchantment))
                return false;
        }
        return true;
    }

    public static void enchantItem(ItemStack item, Enchantment enchantment, int level) {
        if (Objects.requireNonNull(item.getItemMeta()) instanceof EnchantmentStorageMeta enchStore) {
            enchStore.addStoredEnchant(enchantment, level, true);
            item.setItemMeta(enchStore);
        } else {
            item.addUnsafeEnchantment(enchantment, level);
        }
        updateItemLore(item);
    }

    public static void disenchantItem(ItemStack item, Enchantment enchantment) {
        if (item.getEnchantmentLevel(enchantment) != 0) {
            item.removeEnchantment(enchantment);
        } else if (item.getItemMeta() instanceof EnchantmentStorageMeta meta) {
            if (meta.hasStoredEnchant(enchantment)) {
                meta.removeStoredEnchant(enchantment);
                item.setItemMeta(meta);
            }
        }
        updateItemLore(item);
    }

    public static void updateItemLore(ItemStack item) {
        Map<Enchantment, Integer> enchantments = getItemEnchantments(item);
        ItemMeta meta = item.getItemMeta();
        List<Component> newLore = new ArrayList<>();
        List<Component> spellLore = new ArrayList<>();

        for (var entry : enchantments.entrySet()) {
            if (entry.getKey() instanceof CustomEnchantment customEnchantment) {
                if (!(customEnchantment instanceof CustomSpell))
                    newLore.add(customEnchantment.displayName(entry.getValue()));
                else
                    spellLore.add(customEnchantment.displayName(entry.getValue()));
            }
        }

        // Repaired by Ritual Tag
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (pdc.has(Keys.IS_RITUAL_REPAIRED))
            newLore.add(Component.text("Repaired by Ritual").color(NamedTextColor.LIGHT_PURPLE));

        newLore.addAll(spellLore);
        meta.lore(newLore);
        item.setItemMeta(meta);
    }
}
