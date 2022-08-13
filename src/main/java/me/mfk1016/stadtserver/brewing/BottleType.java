package me.mfk1016.stadtserver.brewing;

import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
public enum BottleType {
    NORMAL(Material.POTION),
    SPLASH(Material.SPLASH_POTION),
    LINGER(Material.LINGERING_POTION);

    private final Material mat;

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
