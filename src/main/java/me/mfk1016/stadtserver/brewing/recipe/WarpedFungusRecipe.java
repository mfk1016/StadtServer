package me.mfk1016.stadtserver.brewing.recipe;

import me.mfk1016.stadtserver.brewing.CustomPotionType;
import me.mfk1016.stadtserver.brewing.PotionManager;
import org.bukkit.Material;
import org.bukkit.event.inventory.BrewEvent;

public class WarpedFungusRecipe extends MultiPotionRecipe {

    public WarpedFungusRecipe() {
        super("warped_fungus", Material.WARPED_FUNGUS);
        for (CustomPotionType type : PotionManager.CUSTOM_POTION_TYPE.values()) {
            if (type.id().endsWith("3_1")) {
                String baseID = type.id().replace("3_1", "2_1");
                if (PotionManager.isValidPotionID(baseID))
                    recipeMap.put(baseID, type.id());
            } else if (type.id().endsWith("1_2")) {
                String baseID = type.id().replace("1_2", "0_2");
                if (PotionManager.isValidPotionID(baseID))
                    recipeMap.put(baseID, type.id());
            }
        }
        addRandomOutput(10, "failure");
    }

    @Override
    public void applyAdvancedRecipe(BrewEvent event) {}
}
