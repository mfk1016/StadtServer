package me.mfk1016.stadtserver.brewing.recipe;

import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import static me.mfk1016.stadtserver.util.Functions.stackEmpty;

@RequiredArgsConstructor
public abstract class BarrelRecipe {

    protected final String resultID;

    public boolean isMatched(Inventory barrel) {
        // slot 0/1 = water
        ItemStack water1 = barrel.getItem(0);
        ItemStack water2 = barrel.getItem(1);
        if (stackEmpty(water1) || stackEmpty(water2))
            return false;
        return water1.getType() == Material.WATER_BUCKET && water2.getType() == Material.WATER_BUCKET;
    }

    public String getResultID() {
        return resultID;
    }
}
