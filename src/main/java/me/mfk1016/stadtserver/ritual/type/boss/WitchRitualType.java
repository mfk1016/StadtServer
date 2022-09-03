package me.mfk1016.stadtserver.ritual.type.boss;

import me.mfk1016.stadtserver.ritual.RitualState;
import me.mfk1016.stadtserver.ritual.ActiveRitual;
import me.mfk1016.stadtserver.ritual.ShapeUtils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Candle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.HashMap;

public class WitchRitualType extends BossRitualType {

    private static final String SK_GOLD = "gold";
    private static final String SK_FLOWER_POT = "flower_pot";
    private static final String SK_SMOOTH_STONE = "smooth_stone";
    private static final String SK_CANDLE = "candle";


    public WitchRitualType() {
        super("sheep_witch", Material.LAPIS_BLOCK, EntityType.WITCH, 20);
    }

    @Override
    public boolean isValidWorldState(World world) {
        return world.getEnvironment() == World.Environment.NORMAL &&
                world.getTime() < 23000 && world.getTime() > 13000;
    }

    @Override
    public boolean isValidSacrifice(Entity entity) {
        return entity.getType() == EntityType.SHEEP;
    }

    @Override
    public HashMap<String, Block[]> createShapeMap(Block center) {
        HashMap<String, Block[]> shapeMap = new HashMap<>();
        shapeMap.put(SK_GOLD, ShapeUtils.horizontalLineBlocks(center, 4, 0));
        shapeMap.put(SK_FLOWER_POT, ShapeUtils.horizontalLineBlocks(center, 4, 1));
        shapeMap.put(SK_SMOOTH_STONE, ShapeUtils.horizontalCornerBlocks(center, 3, 0));
        shapeMap.put(SK_CANDLE, ShapeUtils.horizontalCornerBlocks(center, 3, 1));
        return shapeMap;
    }

    @Override
    public boolean isValidShape(HashMap<String, Block[]> shapeMap, int tick) {
        for (Block gold : shapeMap.get(SK_GOLD))
            if (gold.getType() != Material.GOLD_BLOCK)
                return false;
        for (Block flowerPot : shapeMap.get(SK_FLOWER_POT))
            if (!flowerPot.getType().name().startsWith("POTTED_"))
                return false;
        for (Block smoothStone : shapeMap.get(SK_SMOOTH_STONE))
            if (smoothStone.getType() != Material.SMOOTH_STONE)
                return false;
        for (Block candle : shapeMap.get(SK_CANDLE)) {
            if (!(candle.getBlockData() instanceof Candle candleState))
                return false;
            if (tick == 0 && !candleState.isLit())
                return false;
            if (candleState.getCandles() != candleState.getMaximumCandles())
                return false;
        }
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
            Block targetCandle = activeRitual.getShapeMap().get(SK_CANDLE)[target];
            Candle candle = (Candle) targetCandle.getBlockData();
            candle.setLit(false);
            targetCandle.setBlockData(candle);
            targetCandle.getWorld().playSound(targetCandle.getLocation(), Sound.ENTITY_SHEEP_AMBIENT, 4, 1);
        }
    }
}
