package me.mfk1016.stadtserver.candlestore;

import me.mfk1016.stadtserver.util.Keys;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import static me.mfk1016.stadtserver.util.Functions.stackEmpty;
import static me.mfk1016.stadtserver.util.Functions.undecoratedText;

public class CandleStoreUtils {

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

    public static ItemStack getViewStack(Material mat, long amount) {
        ItemStack elem = new ItemStack(mat);
        ItemMeta meta = elem.getItemMeta();
        meta.displayName(undecoratedText(String.valueOf(amount)));
        elem.setItemMeta(meta);
        return elem;
    }

    public static String getCandleName(Material candleMat, boolean lowerCase) {
        if (lowerCase)
            return candleMat.name().toLowerCase().replace('_', ' ');
        return "" + candleMat.name().charAt(0) +
                candleMat.name().substring(1).toLowerCase().replace('_', ' ');
    }

    /* --- Candle Tool --- */

    public static ItemStack getCandleTool() {
        ItemStack tool = new ItemStack(Material.GOLDEN_SWORD);
        ItemMeta meta = tool.getItemMeta();
        meta.displayName(undecoratedText("Candle Tool").color(NamedTextColor.LIGHT_PURPLE));
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(Keys.CANDLE_STORE, PersistentDataType.INTEGER, 1);
        tool.setItemMeta(meta);
        return tool;
    }

    public static boolean isCandleTool(ItemStack item) {
        if (stackEmpty(item) || item.getType() != Material.GOLDEN_SWORD)
            return false;
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().has(Keys.CANDLE_STORE);
    }
}
