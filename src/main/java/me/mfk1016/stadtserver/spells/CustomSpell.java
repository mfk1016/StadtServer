package me.mfk1016.stadtserver.spells;

import me.mfk1016.stadtserver.enchantments.CustomEnchantment;
import me.mfk1016.stadtserver.origin.enchantment.EnchantmentOrigin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

import static me.mfk1016.stadtserver.util.Functions.undecoratedText;

/*
    Spells are special enchantments using levels as charges
 */
public abstract class CustomSpell extends CustomEnchantment {

    public CustomSpell(String name, String namespace) {
        super(name, namespace);
    }

    @Override
    public @NotNull Component displayName(int i) {
        String content = getName() + " " + i;
        return undecoratedText(content).color(NamedTextColor.GREEN);
    }

    @Override
    public Set<EnchantmentOrigin> getOrigins() {
        return null;
    }

    @Override
    public int getAnvilCost(ItemStack sacrifice, int level) {
        return 0;
    }
}
