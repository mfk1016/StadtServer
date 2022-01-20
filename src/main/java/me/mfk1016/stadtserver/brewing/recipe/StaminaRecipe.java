package me.mfk1016.stadtserver.brewing.recipe;

import me.mfk1016.stadtserver.brewing.BottleType;
import me.mfk1016.stadtserver.brewing.BrewingRecipe;
import me.mfk1016.stadtserver.brewing.PotionLibrary;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

public class StaminaRecipe extends BrewingRecipe {

    public StaminaRecipe() {
        super("stamina");
    }

    @Override
    public boolean isApplicable(ItemStack input, Material ingredient) {
        if (ingredient != Material.GLOW_BERRIES)
            return false;

        if (!(input.getItemMeta() instanceof PotionMeta potion))
            return false;

        return potion.getBasePotionData().getType() == PotionType.AWKWARD;
    }

    @Override
    public ItemStack brewPotion(ItemStack input, Material ingredient) {
        return PotionLibrary.buildCustomBrew(BottleType.fromItemStack(input), PotionLibrary.HASTE_1_1);
    }
}
