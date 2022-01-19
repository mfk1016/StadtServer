package me.mfk1016.stadtserver.enchantments;

import io.papermc.paper.enchantments.EnchantmentRarity;
import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.origin.enchantment.EnchantmentOrigin;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityCategory;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

import static me.mfk1016.stadtserver.util.Functions.romanNumber;

public abstract class CustomEnchantment extends Enchantment implements Listener {

    protected final StadtServer plugin;
    private final String name;


    public CustomEnchantment(StadtServer plugin, String name, String namespace) {
        super(new NamespacedKey(plugin, namespace));
        this.name = name;
        this.plugin = plugin;
    }

    public abstract int getAnvilCost(ItemStack sacrifice, int level);

    public abstract Set<EnchantmentOrigin> getOrigins();

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public boolean isTreasure() {
        return true;
    }

    @Override
    public @NotNull Component displayName(int i) {
        return Component.text(getLoreEntry(i));
    }

    @Override
    public boolean isTradeable() {
        return false;
    }

    @Override
    public boolean isDiscoverable() {
        return false;
    }

    @Override
    public @NotNull EnchantmentRarity getRarity() {
        return EnchantmentRarity.VERY_RARE;
    }

    @Override
    public float getDamageIncrease(int i, @NotNull EntityCategory entityCategory) {
        return 0;
    }

    @Override
    public @NotNull Set<EquipmentSlot> getActiveSlots() {
        return Set.of(EquipmentSlot.values());
    }

    public String getLoreEntry(int level) {
        if (getMaxLevel() == 1)
            return ChatColor.GRAY + getName();
        else
            return ChatColor.GRAY + getName() + " " + romanNumber(level);
    }

    @Override
    public @NotNull String translationKey() {
        return getKey().toString();
    }
}
