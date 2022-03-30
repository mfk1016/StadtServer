package me.mfk1016.stadtserver.enchantments;

import io.papermc.paper.enchantments.EnchantmentRarity;
import me.mfk1016.stadtserver.StadtServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityCategory;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

import static me.mfk1016.stadtserver.util.Functions.romanNumber;
import static me.mfk1016.stadtserver.util.Functions.undecoratedText;

public abstract class CustomEnchantment extends Enchantment implements Listener {

    private final String name;

    public CustomEnchantment(String name, String namespace) {
        super(new NamespacedKey(StadtServer.getInstance(), namespace));
        this.name = name;
    }

    public abstract int getAnvilCost(ItemStack sacrifice, int level);

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
        String content = getMaxLevel() == 1 ? getName() : getName() + " " + romanNumber(i);
        return undecoratedText(content).color(NamedTextColor.GRAY);
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

    @Override
    public @NotNull String translationKey() {
        return getKey().toString();
    }
}
