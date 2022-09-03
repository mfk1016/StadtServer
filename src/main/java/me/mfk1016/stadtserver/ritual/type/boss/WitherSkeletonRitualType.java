package me.mfk1016.stadtserver.ritual.type.boss;

import me.mfk1016.stadtserver.enchantments.EnchantmentManager;
import me.mfk1016.stadtserver.ritual.ActiveRitual;
import me.mfk1016.stadtserver.ritual.RitualState;
import me.mfk1016.stadtserver.ritual.ShapeUtils;
import me.mfk1016.stadtserver.util.Keys;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;

import static me.mfk1016.stadtserver.util.Functions.stackEmpty;

public class WitherSkeletonRitualType extends BossRitualType {

    private static final String SK_GOLD = "gold";
    private static final String SK_FRAME = "frame";
    private static final String SK_QUARTZ = "quartz";
    private static final String SK_SOUL = "soul";

    private final HashMap<Block, ItemFrame[]> FRAME_CACHE = new HashMap<>();

    public WitherSkeletonRitualType() {
        super("chicken_wither_skeleton", Material.LAPIS_BLOCK, EntityType.WITHER_SKELETON, 20);
    }

    @Override
    public boolean isValidWorldState(World world) {
        return world.getEnvironment() == World.Environment.NETHER;
    }

    @Override
    public boolean isValidSacrifice(Entity entity) {
        return entity.getType() == EntityType.CHICKEN;
    }

    @Override
    public HashMap<String, Block[]> createShapeMap(Block center) {
        HashMap<String, Block[]> shapeMap = new HashMap<>();
        shapeMap.put(SK_GOLD, ShapeUtils.horizontalLineBlocks(center, 4, 0));
        shapeMap.put(SK_FRAME, ShapeUtils.horizontalLineBlocks(center, 4, 1));
        shapeMap.put(SK_QUARTZ, ShapeUtils.horizontalCornerBlocks(center, 3, 0));
        shapeMap.put(SK_SOUL, ShapeUtils.horizontalCornerBlocks(center, 3, 1));
        return shapeMap;
    }

    @Override
    public boolean isValidShape(HashMap<String, Block[]> shapeMap, int tick) {
        for (Block quartz : shapeMap.get(SK_QUARTZ))
            if (quartz.getType() != Material.QUARTZ_BLOCK)
                return false;
        for (Block soulFire : shapeMap.get(SK_SOUL))
            if (soulFire.getType() != Material.SOUL_CAMPFIRE)
                return false;
        for (Block gold : shapeMap.get(SK_GOLD))
            if (gold.getType() != Material.GOLD_BLOCK)
                return false;
        int itemsFound = 0;
        for (Block frame : shapeMap.get(SK_FRAME)) {
            Location frameCenter = frame.getLocation().toCenterLocation();
            Iterator<ItemFrame> frames = frameCenter.getNearbyEntitiesByType(ItemFrame.class, 0.5D).iterator();
            if (!frames.hasNext())
                return false;
            ItemStack item = frames.next().getItem();
            if (!isValidItem(item))
                return false;
            itemsFound = stackEmpty(item) ? itemsFound : itemsFound + 1;
        }
        return tick >= getSpawnTick() || itemsFound > 0;
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
        Block center = ritual.getCenter();
        ItemFrame[] frames = FRAME_CACHE.remove(center);
        if (!success)
            return;
        for (ItemFrame frame : frames) {
            ItemStack item = frame.getItem();
            if (!isValidItem(item) || stackEmpty(item))
                continue;
            Damageable itemDamage = (Damageable) Objects.requireNonNull(item.getItemMeta());
            itemDamage.setDamage(0);
            PersistentDataContainer pdc = itemDamage.getPersistentDataContainer();
            pdc.set(Keys.IS_RITUAL_REPAIRED, PersistentDataType.INTEGER, 1);
            item.setItemMeta(itemDamage);
            EnchantmentManager.updateItemLore(item);
            frame.setItem(item);
        }
        center.getWorld().playSound(center.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 4, 1);
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
            Block soulFire = activeRitual.getShapeMap().get(SK_SOUL)[target];
            soulFire.getWorld().playSound(soulFire.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 4, 1);
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isValidItem(ItemStack item) {
        if (stackEmpty(item))
            return true;
        switch (item.getType()) {
            case NETHERITE_AXE, NETHERITE_HOE, NETHERITE_PICKAXE, NETHERITE_SHOVEL, NETHERITE_SWORD,
                    NETHERITE_BOOTS, NETHERITE_CHESTPLATE, NETHERITE_HELMET, NETHERITE_LEGGINGS -> {
                Damageable itemDamage = (Damageable) Objects.requireNonNull(item.getItemMeta());
                if (!itemDamage.hasDamage())
                    return false;
                PersistentDataContainer pdc = itemDamage.getPersistentDataContainer();
                return pdc.getOrDefault(Keys.IS_RITUAL_REPAIRED, PersistentDataType.INTEGER, 0) == 0;
            }
            default -> {
                return false;
            }
        }
    }
}
