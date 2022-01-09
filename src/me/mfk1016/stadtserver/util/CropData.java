package me.mfk1016.stadtserver.util;

import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

public enum CropData {

    WHEAT(Material.WHEAT_SEEDS, Material.WHEAT, Material.FARMLAND),
    CARROT(Material.CARROT, Material.CARROTS, Material.FARMLAND),
    POTATO(Material.POTATO, Material.POTATOES, Material.FARMLAND),
    BEETROOT(Material.BEETROOT_SEEDS, Material.BEETROOTS, Material.FARMLAND),
    PUMPKIN(Material.PUMPKIN_SEEDS, Material.PUMPKIN_STEM, Material.FARMLAND),
    MELON(Material.MELON_SEEDS, Material.MELON_STEM, Material.FARMLAND),
    NETHER_WART(Material.NETHER_WART, Material.NETHER_WART, Material.SOUL_SAND);

    private static final Map<Material, CropData> cropDataMap;

    static {
        cropDataMap = new HashMap<>();
        for (CropData value : CropData.values()) {
            cropDataMap.put(value.seed, value);
        }
    }

    public final Material seed;
    public final Material crop;
    public final Material farmland;

    CropData(Material s, Material c, Material f) {
        seed = s;
        crop = c;
        farmland = f;
    }

    public static boolean isPlantable(Material mat) {
        return cropDataMap.containsKey(mat);
    }

    public static boolean isFarmland(Material seedMat, Material farmMat) {
        return cropDataMap.get(seedMat).farmland == farmMat;
    }

    public static Material cropOfSeed(Material seedMat) {
        return cropDataMap.get(seedMat).crop;
    }
}
