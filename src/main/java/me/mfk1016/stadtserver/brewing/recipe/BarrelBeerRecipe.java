package me.mfk1016.stadtserver.brewing.recipe;

import me.mfk1016.stadtserver.brewing.PotionManager;
import me.mfk1016.stadtserver.brewing.SpecialPotionType;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

import static me.mfk1016.stadtserver.util.Functions.stackEmpty;

public record BarrelBeerRecipe(String yeastID, Material herb, String resultID) {

    public boolean isMatched(Inventory barrel) {
        // slot 0/1 = water, slot 2 = 3 wheat, slot 3 = yeast, slot 4 = herb
        ItemStack water1 = barrel.getItem(0);
        ItemStack water2 = barrel.getItem(1);
        if (stackEmpty(water1) || stackEmpty(water2))
            return false;
        if (water1.getType() != Material.WATER_BUCKET || water2.getType() != Material.WATER_BUCKET)
            return false;
        ItemStack wheat = barrel.getItem(2);
        if (stackEmpty(wheat) || wheat.getType() != Material.WHEAT)
            return false;
        Optional<SpecialPotionType> yeastType = PotionManager.matchSpecialPotion(barrel.getItem(3));
        if (yeastType.isEmpty() || !yeastType.get().id().equals(yeastID))
            return false;
        ItemStack herbItem = barrel.getItem(4);
        return !stackEmpty(herbItem) && herbItem.getType() == herb;
    }
}
