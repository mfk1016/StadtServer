package me.mfk1016.stadtserver.rituals.boss;

import me.mfk1016.stadtserver.enchantments.EnchantmentManager;
import me.mfk1016.stadtserver.rituals.RitualState;
import me.mfk1016.stadtserver.util.Keys;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;

import static me.mfk1016.stadtserver.util.Functions.stackEmpty;

public class WitherSkeletonBossRitual extends BossRitual {

    private final Block[] lineBlocks;
    private final Block[] edgeBlocks;
    private final ItemFrame[] frames = {null, null, null, null};
    private boolean validItemFound = false;

    public WitherSkeletonBossRitual(Block focusBlock) {
        super(focusBlock, 20, EntityType.WITHER_SKELETON);
        lineBlocks = new Block[]{focusBlock.getRelative(-4, 0, 0),
                focusBlock.getRelative(4, 0, 0),
                focusBlock.getRelative(0, 0, -4),
                focusBlock.getRelative(0, 0, 4)};
        edgeBlocks = new Block[]{focusBlock.getRelative(-3, 0, -3),
                focusBlock.getRelative(3, 0, -3),
                focusBlock.getRelative(-3, 0, 3),
                focusBlock.getRelative(3, 0, 3)};
    }

    @Override
    public boolean hasProperShape() {
        // Must be in the nether
        World world = focusBlock.getWorld();
        if (world.getEnvironment() != World.Environment.NETHER)
            return false;
        // circle of gold block / quartz block; soul camp fire on the edges (quartz), item frames with damaged netherite items on the lines (gold)
        for (Block block : edgeBlocks) {
            if (block.getType() != Material.QUARTZ_BLOCK)
                return false;
            Block soulFire = block.getRelative(BlockFace.UP);
            if (soulFire.getType() != Material.SOUL_CAMPFIRE)
                return false;
        }
        int frameSlot = 0;
        for (Block block : lineBlocks) {
            if (block.getType() != Material.GOLD_BLOCK)
                return false;
            Block frameBlock = block.getRelative(BlockFace.UP);
            for (var entity : frameBlock.getWorld().getNearbyEntities(frameBlock.getLocation(), 1, 1, 1)) {
                if (entity instanceof ItemFrame f) {
                    frames[frameSlot] = f;
                    break;
                }
            }
            if (frames[frameSlot] == null || !isValidItem(frames[frameSlot].getItem()))
                return false;
            frameSlot++;
        }
        return validItemFound;
    }

    @Override
    public void onRitualCheck(int timer) {
        super.onRitualCheck(timer);
        if (state == RitualState.SUCCESS)
            repairItems();
        if (state != RitualState.INIT)
            return;
        int target = switch (timer) {
            case 4 -> 0;
            case 8 -> 1;
            case 12 -> 2;
            case 16 -> 3;
            default -> -1;
        };
        if (target != -1) {
            Block soulFire = edgeBlocks[target].getRelative(BlockFace.UP);
            soulFire.getWorld().playSound(soulFire.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 4, 1);
        }
    }

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
                boolean result = pdc.getOrDefault(Keys.IS_RITUAL_REPAIRED, PersistentDataType.INTEGER, 0) == 0;
                if (result)
                    validItemFound = true;
                return result;
            }
            default -> {
                return false;
            }
        }
    }

    private void repairItems() {
        for (ItemFrame frame : frames) {
            ItemStack item = frame.getItem();
            if (!isValidItem(item) || stackEmpty(item))
                continue;
            Damageable itemDamage = (Damageable) Objects.requireNonNull(item.getItemMeta());
            itemDamage.setDamage(0);
            PersistentDataContainer pdc = itemDamage.getPersistentDataContainer();
            pdc.set(Keys.IS_RITUAL_REPAIRED, PersistentDataType.INTEGER, 1);
            item.setItemMeta(itemDamage);
            EnchantmentManager.updateLore(item);
            frame.setItem(item);
        }
        focusBlock.getWorld().playSound(focusBlock.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 4, 1);
    }
}
