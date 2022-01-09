package me.mfk1016.stadtserver.rituals.boss;

import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.rituals.RitualState;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;

public class VindicatorPillagerBossRitual extends BossRitual {

    private final Block[] grindstoneBlocks;
    private final Block[] bannerBlocks;

    public VindicatorPillagerBossRitual(StadtServer plugin, Block focusBlock, EntityType bossType) {
        super(plugin, focusBlock, 20, bossType);
        bannerBlocks = new Block[]{focusBlock.getRelative(-3, 0, -3),
                focusBlock.getRelative(3, 0, -3),
                focusBlock.getRelative(-3, 0, 3),
                focusBlock.getRelative(3, 0, 3)
        };
        grindstoneBlocks = new Block[]{bannerBlocks[0].getRelative(BlockFace.EAST),
                bannerBlocks[0].getRelative(BlockFace.SOUTH),
                bannerBlocks[1].getRelative(BlockFace.WEST),
                bannerBlocks[1].getRelative(BlockFace.SOUTH),
                bannerBlocks[2].getRelative(BlockFace.NORTH),
                bannerBlocks[2].getRelative(BlockFace.EAST),
                bannerBlocks[3].getRelative(BlockFace.NORTH),
                bannerBlocks[3].getRelative(BlockFace.WEST)
        };
    }

    @Override
    public boolean hasProperShape() {
        // Must be day time in the overworld
        World world = focusBlock.getWorld();
        if (world.getEnvironment() != World.Environment.NORMAL)
            return false;
        if (world.getTime() >= 11000 || world.getTime() <= 1000)
            return false;
        // banner blocks: emerald block, deepslate bricks wall, banner
        for (Block block : bannerBlocks) {
            if (block.getType() != Material.EMERALD_BLOCK)
                return false;
            Block wall = block.getRelative(BlockFace.UP);
            if (wall.getType() != Material.DEEPSLATE_BRICK_WALL)
                return false;
            Block banner = wall.getRelative(BlockFace.UP);
            if (!banner.getType().name().endsWith("_BANNER"))
                return false;
        }
        // grindstone blocks: stone bricks, grindstone
        for (Block block : grindstoneBlocks) {
            if (block.getType() != Material.STONE_BRICKS)
                return false;
            Block grindstone = block.getRelative(BlockFace.UP);
            if (grindstone.getType() != Material.GRINDSTONE)
                return false;
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
            Block targetBanner = bannerBlocks[target].getRelative(0, 2, 0);
            targetBanner.setType(Material.AIR);
            StadtServer.broadcastSound(targetBanner, Sound.BLOCK_FIRE_EXTINGUISH, 4, 1);
        }
    }
}
