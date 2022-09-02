package me.mfk1016.stadtserver.ritual.type;

import me.mfk1016.stadtserver.ritual.RitualState;
import me.mfk1016.stadtserver.ritual.ActiveRitual;
import me.mfk1016.stadtserver.ritual.ShapeUtils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.HashMap;

public class PillagerRitualType extends BossRitualType {

    private static final String SK_EMERALD = "emerald";
    private static final String SK_DEEPSLATE = "deepslate_brick_wall";
    private static final String SK_BANNER = "banner";
    private static final String SK_STONE_1 = "stone_bricks_1";
    private static final String SK_STONE_2 = "stone_bricks_2";
    private static final String SK_GRIND_1 = "grindstone_1";
    private static final String SK_GRIND_2 = "grindstone_2";


    public PillagerRitualType() {
        super("cow_pillager", Material.LAPIS_BLOCK, EntityType.PILLAGER, 20);
    }

    @Override
    public boolean isValidWorldState(World world) {
        return world.getEnvironment() == World.Environment.NORMAL &&
                world.getTime() >= 1000 && world.getTime() <= 11000;
    }

    @Override
    public boolean isValidSacrifice(Entity entity) {
        return entity.getType() == EntityType.COW;
    }

    @Override
    public HashMap<String, Block[]> createShapeMap(Block center) {
        HashMap<String, Block[]> shapeMap = new HashMap<>();
        shapeMap.put(SK_EMERALD, ShapeUtils.horizontalCornerBlocks(center, 3, 0));
        shapeMap.put(SK_DEEPSLATE, ShapeUtils.horizontalCornerBlocks(center, 3, 1));
        shapeMap.put(SK_BANNER, ShapeUtils.horizontalCornerBlocks(center, 3, 2));
        shapeMap.put(SK_STONE_1, ShapeUtils.horizontalBlocks(center, 2, 0, 3));
        shapeMap.put(SK_GRIND_1, ShapeUtils.horizontalBlocks(center, 2, 1, 3));
        shapeMap.put(SK_STONE_2, ShapeUtils.horizontalBlocks(center, 3, 0, 2));
        shapeMap.put(SK_GRIND_2, ShapeUtils.horizontalBlocks(center, 3, 1, 2));
        return shapeMap;
    }

    @Override
    public boolean isValidShape(HashMap<String, Block[]> shapeMap, int tick) {
        for (Block emerald : shapeMap.get(SK_EMERALD))
            if (emerald.getType() != Material.EMERALD_BLOCK)
                return false;
        for (Block deepslate : shapeMap.get(SK_DEEPSLATE))
            if (deepslate.getType() != Material.DEEPSLATE_BRICK_WALL)
                return false;
        for (Block banner : shapeMap.get(SK_BANNER))
            if (tick == 0 && !banner.getType().name().endsWith("_BANNER"))
                return false;
        for (Block bricks : shapeMap.get(SK_STONE_1))
            if (bricks.getType() != Material.STONE_BRICKS)
                return false;
        for (Block bricks : shapeMap.get(SK_STONE_2))
            if (bricks.getType() != Material.STONE_BRICKS)
                return false;
        for (Block grindstone : shapeMap.get(SK_GRIND_1))
            if (grindstone.getType() != Material.GRINDSTONE)
                return false;
        for (Block grindstone : shapeMap.get(SK_GRIND_2))
            if (grindstone.getType() != Material.GRINDSTONE)
                return false;
        return true;
    }

    @Override
    public void onRitualTick(ActiveRitual activeRitual) {
        super.onRitualTick(activeRitual);
        if (activeRitual.getState() != RitualState.INIT)
            return;
        if (!isValidShape(activeRitual.getShapeMap(), activeRitual.getTick()))
            return;
        int target = switch (activeRitual.getTick()) {
            case 4 -> 0;
            case 8 -> 1;
            case 12 -> 2;
            case 16 -> 3;
            default -> -1;
        };
        if (target != -1) {
            Block banner = activeRitual.getShapeMap().get(SK_BANNER)[target];
            banner.setType(Material.AIR);
            banner.getWorld().playSound(banner.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 4, 1);
        }
    }
}
