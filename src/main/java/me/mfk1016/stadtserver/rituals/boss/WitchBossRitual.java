package me.mfk1016.stadtserver.rituals.boss;

import me.mfk1016.stadtserver.rituals.RitualState;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Candle;
import org.bukkit.entity.EntityType;

public class WitchBossRitual extends BossRitual {

    private final Block[] lineBlocks;
    private final Block[] edgeBlocks;

    public WitchBossRitual(Block focusBlock) {
        super(focusBlock, 20, EntityType.WITCH);
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
        // Must be nighttime in the overworld
        World world = focusBlock.getWorld();
        if (world.getEnvironment() != World.Environment.NORMAL)
            return false;
        if (world.getTime() >= 23000 || world.getTime() <= 13000)
            return false;
        // circle of red wool / gold; lit candles on the edges (gold), filled flower pots on the lines (wool)
        for (Block block : edgeBlocks) {
            if (block.getType() != Material.SMOOTH_STONE)
                return false;
            Block candleBlock = block.getRelative(BlockFace.UP);
            if (!(candleBlock.getBlockData() instanceof Candle candle))
                return false;
            if (!candle.isLit() || candle.getCandles() != candle.getMaximumCandles())
                return false;
        }
        for (Block block : lineBlocks) {
            if (block.getType() != Material.GOLD_BLOCK)
                return false;
            Block flowerPot = block.getRelative(BlockFace.UP);
            if (!flowerPot.getType().name().startsWith("POTTED_"))
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
            Block targetCandle = edgeBlocks[target].getRelative(BlockFace.UP);
            if (!(targetCandle.getBlockData() instanceof Candle candle)) {
                state = RitualState.FAILURE;
                return;
            }
            candle.setLit(false);
            targetCandle.setBlockData(candle);
            targetCandle.getWorld().playSound(targetCandle.getLocation(), Sound.ENTITY_SHEEP_AMBIENT, 4, 1);
        }
    }
}
