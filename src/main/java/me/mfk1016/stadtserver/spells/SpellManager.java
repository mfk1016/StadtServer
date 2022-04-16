package me.mfk1016.stadtserver.spells;

import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.enchantments.CustomEnchantment;
import me.mfk1016.stadtserver.enchantments.EnchantmentManager;
import me.mfk1016.stadtserver.util.Pair;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;

import java.lang.reflect.Field;
import java.util.*;

import static me.mfk1016.stadtserver.util.Functions.undecoratedText;

public class SpellManager {

    // Public stuff
    public static final Set<CustomSpell> ALL_SPELLS = new HashSet<>();
    // Internal stuff
    private static final Set<CustomSpell> registeredSpells = new HashSet<>();

    // Add all custom spells here
    public static DarknessSpell DARKNESS;
    public static SummonSpell SUMMON;
    public static MagnetSpell MAGNET;
    public static DestructorSpell DESTRUCTOR;

    /* --- INIT + STOP --- */

    public static void onPluginEnable() {

        // Initialize them here
        DARKNESS = new DarknessSpell();
        SUMMON = new SummonSpell();
        MAGNET = new MagnetSpell();
        DESTRUCTOR = new DestructorSpell();

        // Add them here
        ALL_SPELLS.add(DARKNESS);
        ALL_SPELLS.add(SUMMON);
        ALL_SPELLS.add(MAGNET);
        ALL_SPELLS.add(DESTRUCTOR);

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
        int newCharges = Math.min(getSpellCharges(item, spell) + charges, spell.getMaxCharges());
        EnchantmentManager.enchantItem(item, spell, newCharges);
    }

    public static void removeSpell(ItemStack item, CustomSpell spell) {
        EnchantmentManager.disenchantItem(item, spell);
    }

    public static int getSpellCharges(ItemStack item, CustomSpell spell) {
        return EnchantmentManager.getItemEnchantments(item).getOrDefault(spell, 0);
    }

    public static void useSpellCharge(ItemStack item, CustomSpell spell) {
        int charges = getSpellCharges(item, spell);
        switch (charges) {
            case 0:
                break;
            case 1:
                removeSpell(item, spell);
                break;
            default:
                EnchantmentManager.enchantItem(item, spell, charges - 1);
                break;
        }
    }

    /* --- MEDIUMS --- */

    public static final Material[] SPELL_MEDIUMS = {
            Material.REDSTONE, Material.GLOWSTONE_DUST, Material.LAPIS_LAZULI, Material.AMETHYST_SHARD
    };

    private static final String[] SPELL_MEDIUM_NAMES = {
            "Electrified Redstone Dust", "Luminous Glowstone Dust",
            "Aquatic Lapis Lazuli", "Glittering Amethyst Shard"
    };

    public static ItemStack createSpellMedium(CustomSpell spell, int charges) {
        int slot = StadtServer.RANDOM.nextInt(SPELL_MEDIUMS.length);
        Material mat = SPELL_MEDIUMS[slot];
        String name = SPELL_MEDIUM_NAMES[slot];
        ItemStack result = new ItemStack(mat);
        ItemMeta meta = result.getItemMeta();
        meta.displayName(undecoratedText(name).color(NamedTextColor.AQUA));
        result.setItemMeta(meta);
        addSpell(result, spell, charges);
        return result;
    }

    public static Optional<Pair<CustomSpell, Integer>> getMediumSpell(ItemStack item) {
        boolean isMediumMaterial = false;
        for (Material mat : SPELL_MEDIUMS) {
            if (mat == item.getType())
                isMediumMaterial = true;
        }
        Map<Enchantment, Integer> spells = item.getEnchantments();
        if (!isMediumMaterial || spells.size() != 1 || item.getAmount() != 1)
            return Optional.empty();
        for (var entry : spells.entrySet()) {
            if (entry.getKey() instanceof CustomSpell spell)
                return Optional.of(new Pair<>(spell, entry.getValue()));
        }
        return Optional.empty();
    }
}
