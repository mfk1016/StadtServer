package me.mfk1016.stadtserver.logic.wrench.actions;

import me.mfk1016.stadtserver.logic.wrench.WrenchAction;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.Player;

public class WrenchActionStairs extends WrenchAction {

    private final boolean isSneak;

    public WrenchActionStairs(boolean isSneak) {
        this.isSneak = isSneak;
    }

    @Override
    protected Result wrenchBlock(Player player, Block target) {
        Stairs stairs = (Stairs) target.getBlockData();
        if (isSneak) {
            // Sneak: cycle shape
            int newShapeOrdinal = (stairs.getShape().ordinal() + 1) % Stairs.Shape.values().length;
            stairs.setShape(Stairs.Shape.values()[newShapeOrdinal]);
        } else {
            // No Sneak: cycle face
            BlockFace newFace = switch (stairs.getFacing()) {
                case WEST -> BlockFace.NORTH;
                case NORTH -> BlockFace.EAST;
                case EAST -> BlockFace.SOUTH;
                default -> BlockFace.WEST; // only W-E and N-S possible
            };
            stairs.setFacing(newFace);
        }
        target.setBlockData(stairs, false);
        return Result.TRUE;
    }

    @Override
    protected String wrenchMessage(Result result) {
        return null;
    }

    @Override
    protected boolean isEventCancelled(Result result) {
        return true;
    }
}
