package me.mfk1016.stadtserver.rituals.boss;

import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.rituals.RitualState;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;

public class EvokerBossRitual extends BossRitual {

    private final Block[] lineBlocks;
    private final Block[] edgeBlocks;
    private final ItemFrame[] frames = {null, null, null, null};

    public EvokerBossRitual(StadtServer plugin, Block focusBlock) {
        super(plugin, focusBlock, 20, EntityType.EVOKER);
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
        // Must be in the overworld
        World world = focusBlock.getWorld();
        if (world.getEnvironment() != World.Environment.NORMAL)
            return false;
        // circle of diamond block / purpur block; brewing stands on the edges (diamond), item frames with diamonds on the lines (purpur)
        for (Block block : edgeBlocks) {
            if (block.getType() != Material.DIAMOND_BLOCK)
                return false;
            Block brewingStand = block.getRelative(BlockFace.UP);
            if (brewingStand.getType() != Material.BREWING_STAND)
                return false;
        }
        int frameSlot = 0;
        for (Block block : lineBlocks) {
            if (block.getType() != Material.PURPUR_BLOCK)
                return false;
            Block frameBlock = block.getRelative(BlockFace.UP);
            for (var entity : frameBlock.getWorld().getNearbyEntities(frameBlock.getLocation(), 1, 1, 1)) {
                if (entity instanceof ItemFrame f) {
                    frames[frameSlot] = f;
                    break;
                }
            }
            if (frames[frameSlot] == null || frames[frameSlot].getItem().getType() != Material.DIAMOND)
                return false;
            frameSlot++;
        }
        return true;
    }

    @Override
    public void onRitualCheck(int timer) {
        super.onRitualCheck(timer);
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
            ItemFrame targetFrame = frames[target];
            targetFrame.setItem(null);
            targetFrame.getWorld().playSound(targetFrame.getLocation(), Sound.ENTITY_VILLAGER_DEATH, 4, 1);
        }
    }
}
