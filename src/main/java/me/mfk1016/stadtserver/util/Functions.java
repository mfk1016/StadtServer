package me.mfk1016.stadtserver.util;

import me.mfk1016.stadtserver.StadtServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Axis;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.jetbrains.annotations.Contract;

public class Functions {

    @Contract("null -> true")
    public static boolean stackEmpty(ItemStack stack) {
        return stack == null || stack.getType().isAir();
    }

    public static TextComponent undecoratedText(String string) {
        return Component.text(string).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    public static String getMaterialName(Material candleMat, boolean lowerCase) {
        if (lowerCase)
            return candleMat.name().toLowerCase().replace('_', ' ');
        return "" + candleMat.name().charAt(0) +
                candleMat.name().substring(1).toLowerCase().replace('_', ' ');
    }

    public static String largeAmountString(long value) {
        if (value >= 1000000L) {
            long num = value / 1000000L;
            long point = (value % 1000000L) / 100000L;
            return num + "." + point + "m";
        } else if (value >= 10000L) {
            long num = value / 1000L;
            long point = (value % 1000L) / 100L;
            return num + "." + point + "k";
        }
        return String.valueOf(value);
    }

    public static String romanNumber(int level) {
        return "I".repeat(level)
                .replace("IIIII", "V")
                .replace("IIII", "IV")
                .replace("VV", "X")
                .replace("VIV", "IX")
                .replace("XXXXX", "L")
                .replace("XXXX", "XL")
                .replace("LL", "C")
                .replace("LXL", "XC");
    }

    public static void playerMessage(Player player, String string) {
        if (StadtServer.getInstance().getConfig().getBoolean(Keys.CONFIG_PLAYER_MESSAGE)) {
            player.sendActionBar(undecoratedText(string));
        }
    }

    public static MerchantRecipe copyMerchantRecipe(MerchantRecipe recipe, ItemStack result) {
        MerchantRecipe newRecipe = new MerchantRecipe(result, recipe.getUses(), recipe.getMaxUses(),
                recipe.hasExperienceReward(), recipe.getVillagerExperience(),
                recipe.getPriceMultiplier());
        newRecipe.setIngredients(recipe.getIngredients());
        return newRecipe;
    }

    public static boolean isFaceOfOrientation(Axis axis, BlockFace face) {
        return switch (axis) {
            case X -> face == BlockFace.WEST || face == BlockFace.EAST;
            case Y -> face == BlockFace.UP || face == BlockFace.DOWN;
            case Z -> face == BlockFace.NORTH || face == BlockFace.SOUTH;
        };
    }
}
