package me.mfk1016.stadtserver.brewing.recipe;

import me.mfk1016.stadtserver.brewing.BrewingRecipe;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class DyePotionRecipe extends BrewingRecipe {

    public DyePotionRecipe() {
        super("dye_potion");
    }

    @Override
    public boolean isApplicable(@NotNull ItemStack input, Material ingredient) {
        switch (ingredient) {
            case WHITE_DYE, ORANGE_DYE, MAGENTA_DYE, LIGHT_BLUE_DYE, YELLOW_DYE,
                    LIME_DYE, PINK_DYE, GRAY_DYE, LIGHT_GRAY_DYE, CYAN_DYE, PURPLE_DYE,
                    BLUE_DYE, BROWN_DYE, GREEN_DYE, RED_DYE, BLACK_DYE -> {
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    @Override
    public ItemStack brewPotion(@NotNull ItemStack input, Material ingredient) {
        ItemStack result = input.clone();
        PotionMeta potion = (PotionMeta) Objects.requireNonNull(input.getItemMeta());
        switch (ingredient) {
            case WHITE_DYE -> potion.setColor(Color.WHITE);
            case ORANGE_DYE -> potion.setColor(Color.ORANGE);
            case MAGENTA_DYE -> potion.setColor(Color.fromRGB(198, 79, 189));
            case LIGHT_BLUE_DYE -> potion.setColor(Color.fromRGB(58, 179, 218));
            case YELLOW_DYE -> potion.setColor(Color.YELLOW);
            case LIME_DYE -> potion.setColor(Color.LIME);
            case PINK_DYE -> potion.setColor(Color.fromRGB(243, 140, 170));
            case GRAY_DYE -> potion.setColor(Color.GRAY);
            case LIGHT_GRAY_DYE -> potion.setColor(Color.fromRGB(156, 157, 151));
            case CYAN_DYE -> potion.setColor(Color.fromRGB(22, 156, 157));
            case PURPLE_DYE -> potion.setColor(Color.PURPLE);
            case BLUE_DYE -> potion.setColor(Color.BLUE);
            case BROWN_DYE -> potion.setColor(Color.fromRGB(130, 84, 50));
            case GREEN_DYE -> potion.setColor(Color.GREEN);
            case RED_DYE -> potion.setColor(Color.RED);
            case BLACK_DYE -> potion.setColor(Color.BLACK);
        }
        result.setItemMeta(potion);
        return result;
    }
}
