package me.mfk1016.stadtserver.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Functions {

    public static boolean stackEmpty(ItemStack stack) {
        return stack == null || stack.getType() == Material.AIR;
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
}
