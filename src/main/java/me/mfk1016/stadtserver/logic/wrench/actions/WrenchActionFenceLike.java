package me.mfk1016.stadtserver.logic.wrench.actions;

import me.mfk1016.stadtserver.logic.wrench.WrenchAction;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Fence;
import org.bukkit.entity.Player;

public class WrenchActionFenceLike extends WrenchAction {

    @Override
    protected WrenchAction.Result wrenchBlock(Player player, Block target) {
        Fence fence = (Fence) target.getBlockData();
        for (BlockFace face : fence.getAllowedFaces()) {
            if (face == BlockFace.UP || face == BlockFace.DOWN)
                continue;
            Material neighborType = target.getRelative(face).getType();
            if (!neighborType.isSolid())
                continue;
            fence.setFace(face, true);
        }
        target.setBlockData(fence, false);
        return WrenchAction.Result.TRUE;
    }

    @Override
    protected String wrenchMessage(WrenchAction.Result result) {
        return null;
    }

    @Override
    protected boolean isEventCancelled(WrenchAction.Result result) {
        return true;
    }
}
