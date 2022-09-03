package me.mfk1016.stadtserver.ritual.type;

import me.mfk1016.stadtserver.ritual.ActiveRitual;
import me.mfk1016.stadtserver.ritual.RitualType;
import me.mfk1016.stadtserver.ritual.ShapeUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.HashMap;
import java.util.Objects;

import static me.mfk1016.stadtserver.util.Functions.isBlockLoaded;

public abstract class RangedRitualType extends RitualType {

    private static final String SK_RANGE_BORDER = "ranged:border";

    protected final int range;

    public RangedRitualType(String key, Material focusMaterial, int tickDelay, int range) {
        super(key, focusMaterial, tickDelay);
        this.range = range;
    }

    @Override
    public HashMap<String, Block[]> createShapeMap(Block center) {
        HashMap<String, Block[]> shapeMap = new HashMap<>();
        shapeMap.put(SK_RANGE_BORDER, ShapeUtils.horizontalCornerBlocks(center, range, 0));
        return shapeMap;
    }

    protected boolean isRangeLoaded(ActiveRitual ritual) {
        HashMap<String, Block[]> shapeMap = Objects.requireNonNull(ritual.getShapeMap());
        for (Block border : shapeMap.get(SK_RANGE_BORDER))
            if (!isBlockLoaded(border))
                return false;
        return true;
    }
}
