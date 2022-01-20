package me.mfk1016.stadtserver.brewing;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public abstract class BrewingRecipe {

    private final String recipeID;

    public BrewingRecipe(String id) {
        this.recipeID = id;
    }

    public String getRecipeID() {
        return recipeID;
    }

    public abstract boolean isApplicable(ItemStack input, Material ingredient);

    public abstract ItemStack brewPotion(ItemStack input, Material ingredient);

}
