package me.mfk1016.stadtserver.spells;

import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.enchantments.CustomEnchantment;
import me.mfk1016.stadtserver.enchantments.EnchantmentManager;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class SpellManager {

    // Public stuff
    public static final Set<CustomSpell> ALL_SPELLS = new HashSet<>();
    // Internal stuff
    private static final Set<CustomSpell> registeredSpells = new HashSet<>();

    /* --- INIT + STOP --- */

    public static void onPluginEnable() {

        // Initialize them here

        // Add them here

        // Registering
        registerSpells();

        // Attach Listeners
        PluginManager pm = StadtServer.getInstance().getServer().getPluginManager();
        for (CustomEnchantment e : ALL_SPELLS) {
            pm.registerEvents(e, StadtServer.getInstance());
        }
    }

    public static void onPluginDisable() {
        unregisterSpells();
    }

    /* --- REGISTRATION --- */

    private static void registerSpells() {
        try {
            Field f = Enchantment.class.getDeclaredField("acceptingNew");
            f.setAccessible(true);
            f.set(null, true);
            for (CustomSpell spell : ALL_SPELLS) {
                Enchantment.registerEnchantment(spell);
                registeredSpells.add(spell);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void unregisterSpells() {
        try {
            Field keyField = Enchantment.class.getDeclaredField("byKey");

            keyField.setAccessible(true);
            @SuppressWarnings("unchecked")
            HashMap<NamespacedKey, Enchantment> byKey = (HashMap<NamespacedKey, Enchantment>) keyField.get(null);

            for (Enchantment spell : registeredSpells) {
                byKey.remove(spell.getKey());
            }

            Field nameField = Enchantment.class.getDeclaredField("byName");

            nameField.setAccessible(true);
            @SuppressWarnings("unchecked")
            HashMap<String, Enchantment> byName = (HashMap<String, Enchantment>) nameField.get(null);

            for (Enchantment spell : registeredSpells) {
                byName.remove(spell.getName());
            }
        } catch (Exception ignored) {
        }
        registeredSpells.clear();
    }

    /* --- SPELLS --- */

    public static void addSpell(ItemStack item, CustomSpell spell, int charges) {
        EnchantmentManager.enchantItem(item, spell, charges);
    }

    public static void removeSpell(ItemStack item, CustomSpell spell) {
        EnchantmentManager.disenchantItem(item, spell);
    }

    public static int getSpellCharges(ItemStack item, CustomSpell spell) {
        return EnchantmentManager.getItemEnchantments(item).getOrDefault(spell, 0);
    }

    public static boolean useSpellCharge(ItemStack item, CustomSpell spell) {
        int charges = EnchantmentManager.getItemEnchantments(item).getOrDefault(spell, 0);
        if (charges == 0)
            return false;
        EnchantmentManager.disenchantItem(item, spell);
        if (charges > 1)
            EnchantmentManager.enchantItem(item, spell, charges - 1);
        return true;
    }
}
