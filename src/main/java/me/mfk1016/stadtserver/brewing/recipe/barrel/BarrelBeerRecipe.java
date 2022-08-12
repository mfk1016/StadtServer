package me.mfk1016.stadtserver.brewing.recipe.barrel;

import me.mfk1016.stadtserver.brewing.PotionManager;
import me.mfk1016.stadtserver.brewing.SpecialPotionType;
import me.mfk1016.stadtserver.brewing.recipe.BarrelRecipe;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

import static me.mfk1016.stadtserver.util.Functions.stackEmpty;

public class BarrelBeerRecipe extends BarrelRecipe {

    private final String yeastID;
    private final Material herb;

    public BarrelBeerRecipe(String yeastID, Material herb, String resultID) {
        super(resultID);
        this.yeastID = yeastID;
        this.herb = herb;
    }

    @Override
    public boolean isMatched(Inventory barrel) {
        if (!super.isMatched(barrel))
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
