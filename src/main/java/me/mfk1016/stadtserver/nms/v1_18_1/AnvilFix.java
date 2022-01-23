package me.mfk1016.stadtserver.nms.v1_18_1;

import net.minecraft.world.inventory.AnvilMenu;
import org.bukkit.craftbukkit.v1_18_R1.inventory.CraftInventoryAnvil;
import org.bukkit.inventory.AnvilInventory;

import java.lang.reflect.Field;

public class AnvilFix {

    // TODO: obsolet mit nächstem spigot update
    @SuppressWarnings("JavaReflectionMemberAccess")
    public static boolean fixAnvilSacrificeUsage(AnvilInventory inventory, int sacrificeCount) {
        CraftInventoryAnvil craftAnvil = (CraftInventoryAnvil) inventory;
        try {
            Field container = CraftInventoryAnvil.class.getDeclaredField("container");
            container.setAccessible(true);
            AnvilMenu menu = (AnvilMenu) container.get(craftAnvil);
            // mappings: u -> repairItemCountCost
            Field repairItemCountCost = AnvilMenu.class.getDeclaredField("u");
            repairItemCountCost.setAccessible(true);
            repairItemCountCost.set(menu, sacrificeCount);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
