package me.mfk1016.stadtserver.brewing;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public abstract class BrewingRecipe {

    public BrewingRecipe() {}

    public abstract boolean isApplicable(@NotNull ItemStack input, Material ingredient);

    public abstract ItemStack brewPotion(@NotNull ItemStack input, Material ingredient);

}
