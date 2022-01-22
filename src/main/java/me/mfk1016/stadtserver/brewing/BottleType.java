package me.mfk1016.stadtserver.brewing;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum BottleType {
    NORMAL(Material.POTION),
    SPLASH(Material.SPLASH_POTION),
    LINGER(Material.LINGERING_POTION);

    private final Material mat;

    BottleType(Material mat) {
        this.mat = mat;
    }

    public static BottleType ofStack(ItemStack stack) {
        return switch (stack.getType()) {
            case POTION -> BottleType.NORMAL;
            case SPLASH_POTION -> BottleType.SPLASH;
            case LINGERING_POTION -> BottleType.LINGER;
            default -> null;
        };
    }

    public ItemStack asItemStack() {
        return new ItemStack(mat);
    }
}
