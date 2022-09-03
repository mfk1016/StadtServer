package me.mfk1016.stadtserver.ritual.type.boss;

import me.mfk1016.stadtserver.ritual.ActiveRitual;
import me.mfk1016.stadtserver.ritual.RitualState;
import me.mfk1016.stadtserver.ritual.ShapeUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Iterator;

import static me.mfk1016.stadtserver.util.Functions.stackEmpty;

public class EvokerRitualType extends BossRitualType {

    private static final String SK_PURPUR = "purpur";
    private static final String SK_FRAME = "frame";
    private static final String SK_DIAMOND = "diamond";
    private static final String SK_BREWING = "brew";

    private final HashMap<Block, ItemFrame[]> FRAME_CACHE = new HashMap<>();

    public EvokerRitualType() {
        super("villager_evoker", Material.LAPIS_BLOCK, EntityType.EVOKER, 20);
    }

    @Override
    public boolean isValidWorldState(World world) {
        return world.getEnvironment() == World.Environment.NORMAL;
    }

    @Override
    public boolean isValidSacrifice(Entity entity) {
        return entity.getType() == EntityType.VILLAGER;
    }

    @Override
    public HashMap<String, Block[]> createShapeMap(Block center) {
        HashMap<String, Block[]> shapeMap = new HashMap<>();
        shapeMap.put(SK_PURPUR, ShapeUtils.horizontalLineBlocks(center, 4, 0));
        shapeMap.put(SK_FRAME, ShapeUtils.horizontalLineBlocks(center, 4, 1));
        shapeMap.put(SK_DIAMOND, ShapeUtils.horizontalCornerBlocks(center, 3, 0));
        shapeMap.put(SK_BREWING, ShapeUtils.horizontalCornerBlocks(center, 3, 1));
        return shapeMap;
    }

    @Override
    public boolean isValidShape(HashMap<String, Block[]> shapeMap, int tick) {
        for (Block diamond : shapeMap.get(SK_DIAMOND))
            if (diamond.getType() != Material.DIAMOND_BLOCK)
                return false;
        for (Block brewing : shapeMap.get(SK_BREWING))
            if (brewing.getType() != Material.BREWING_STAND)
                return false;
        for (Block purpur : shapeMap.get(SK_PURPUR))
            if (purpur.getType() != Material.PURPUR_BLOCK)
                return false;
        for (Block frame : shapeMap.get(SK_FRAME)) {
            Location frameCenter = frame.getLocation().toCenterLocation();
            Iterator<ItemFrame> frames = frameCenter.getNearbyEntitiesByType(ItemFrame.class, 0.5D).iterator();
            if (!frames.hasNext())
                return false;
            ItemStack item = frames.next().getItem();
            if (tick == 0 && (stackEmpty(item) || item.getType() != Material.DIAMOND))
                return false;
        }
        return true;
    }

    @Override
    public void updateRitualCache(ActiveRitual ritual) {
        super.updateRitualCache(ritual);
        if (FRAME_CACHE.containsKey(ritual.getCenter()))
            return;
        Block[] frameBlocks = ritual.getShapeMap().get(SK_FRAME);
        ItemFrame[] frames = new ItemFrame[4];
        for (int i = 0; i < 4; i++) {
            Location frameCenter = frameBlocks[i].getLocation().toCenterLocation();
            Iterator<ItemFrame> entities = frameCenter.getNearbyEntitiesByType(ItemFrame.class, 0.5D).iterator();
            frames[i] = entities.next();
        }
        FRAME_CACHE.put(ritual.getCenter(), frames);
    }

    @Override
    public void onRitualSpawn(ActiveRitual ritual) {
        super.onRitualSpawn(ritual);
        Block[] frameBlocks = ritual.getShapeMap().get(SK_FRAME);
        ItemFrame[] frames = new ItemFrame[4];
        for (int i = 0; i < 4; i++) {
            Location frameCenter = frameBlocks[i].getLocation().toCenterLocation();
            Iterator<ItemFrame> entities = frameCenter.getNearbyEntitiesByType(ItemFrame.class, 0.5D).iterator();
            frames[i] = entities.next();
        }
        FRAME_CACHE.put(ritual.getCenter(), frames);
    }

    @Override
    public void onRitualStop(ActiveRitual ritual, boolean success) {
        super.onRitualStop(ritual, success);
        FRAME_CACHE.remove(ritual.getCenter());
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
            ItemFrame frame = FRAME_CACHE.get(activeRitual.getCenter())[target];
            if (stackEmpty(frame.getItem()) || frame.getItem().getType() != Material.DIAMOND)
                stopBossRitual(activeRitual, BOSS_CACHE.get(activeRitual.getCenter()), false);
            frame.setItem(null);
            frame.getWorld().playSound(frame.getLocation(), Sound.ENTITY_VILLAGER_DEATH, 4, 1);
        }
    }
}
