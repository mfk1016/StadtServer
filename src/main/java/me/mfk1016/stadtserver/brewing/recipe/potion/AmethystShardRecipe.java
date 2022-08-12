package me.mfk1016.stadtserver.brewing.recipe.potion;

import me.mfk1016.stadtserver.brewing.CustomPotionType;
import me.mfk1016.stadtserver.brewing.PotionManager;
import org.bukkit.Material;
import org.bukkit.event.inventory.BrewEvent;

public class AmethystShardRecipe extends MultiPotionRecipe {

    public AmethystShardRecipe() {
        super("amethyst_shard", Material.AMETHYST_SHARD);
        for (CustomPotionType type : PotionManager.CUSTOM_POTION_TYPE.values()) {
            if (type.id().endsWith("0_3")) {
                String baseID = type.id().replace("0_3", "0_2");
                if (PotionManager.isValidPotionID(baseID))
                    recipeMap.put(baseID, type.id());
            } else if (type.id().endsWith("1_2")) {
                String baseID = type.id().replace("1_2", "2_1");
                if (PotionManager.isValidPotionID(baseID))
                    recipeMap.put(baseID, type.id());
            }
        }
        addRandomOutput(17, "failure");
        addRandomOutput(2, "luck:0_2");
        addRandomOutput(1, "bad_omen:0_4");
    }

    @SuppressWarnings("EmptyMethod")
    @Override
    public void applyAdvancedRecipe(BrewEvent event) {
    }
}
