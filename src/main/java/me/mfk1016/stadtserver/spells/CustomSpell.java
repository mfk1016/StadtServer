package me.mfk1016.stadtserver.spells;

import me.mfk1016.stadtserver.enchantments.CustomEnchantment;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static me.mfk1016.stadtserver.util.Functions.undecoratedText;

/*
    Spells are special enchantments using levels as charges
 */
public abstract class CustomSpell extends CustomEnchantment {

    public CustomSpell(String name, String namespace) {
        super(name, namespace);
    }

    public abstract @NotNull Level spellLevel();

    @Override
    public @NotNull Component displayName(int i) {
        String content = i == 1 ? getName() : "" + i + " " + getName();
        return undecoratedText(content).color(spellLevel().color);
    }

    public abstract List<Recipe> getRecipes();

    @Override
    public int getAnvilCost(ItemStack sacrifice, int level) {
        return 0;
    }

    public abstract int getMaxCharges();

    @Override
    public int getMaxLevel() {
        return getMaxCharges();
    }

    @Override
    public int getStartLevel() {
        return 1;
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.ALL;
    }

    @Override
    public boolean conflictsWith(@NotNull Enchantment other) {
        return false;
    }

    @Override
    public boolean isCursed() {
        return false;
    }


    public enum Level {
        COMMON(NamedTextColor.GREEN),
        RARE(NamedTextColor.GOLD),
        LEGENDARY(NamedTextColor.RED);

        public final NamedTextColor color;

        Level(NamedTextColor c) {
            color = c;
        }
    }
}
