package me.mfk1016.stadtserver.brewing;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public abstract class BrewingRecipe {

    public abstract boolean isApplicable(ItemStack input, Material ingredient);

    public abstract ItemStack brewPotion(ItemStack input, Material ingredient);

}
