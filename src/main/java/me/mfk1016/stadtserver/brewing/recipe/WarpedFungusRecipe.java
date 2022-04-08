package me.mfk1016.stadtserver.brewing.recipe;

import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.brewing.BottleType;
import me.mfk1016.stadtserver.brewing.CustomPotionType;
import me.mfk1016.stadtserver.brewing.PotionManager;
import org.bukkit.Material;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;

import static me.mfk1016.stadtserver.util.Functions.stackEmpty;

public class WarpedFungusRecipe extends MultiPotionRecipe {

    private static final int FAIL_CHANCE = 10;

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
    }

    @Override
    public void applyAdvancedRecipe(BrewEvent event) {
        BrewerInventory inventory = event.getContents();
        for (int i = 0; i < 3; i++) {
            ItemStack input = inventory.getItem(i);
            if (stackEmpty(input) || !isInputMatched(input))
                continue;
            if (StadtServer.RANDOM.nextInt(100) < FAIL_CHANCE)
                event.getResults().set(i, PotionManager.brewPotion("failure", BottleType.ofStack(input)));
        }

    }
}
