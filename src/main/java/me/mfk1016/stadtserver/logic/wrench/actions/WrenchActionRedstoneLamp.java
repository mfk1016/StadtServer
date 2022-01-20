package me.mfk1016.stadtserver.logic.wrench.actions;

import me.mfk1016.stadtserver.logic.wrench.WrenchAction;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Lightable;
import org.bukkit.entity.Player;

public class WrenchActionRedstoneLamp extends WrenchAction {

    @Override
    protected Result wrenchBlock(Player player, Block target) {
        if (target.getType() != Material.REDSTONE_LAMP)
            return Result.FALSE;
        Lightable lightData = (Lightable) target.getBlockData();
        lightData.setLit(!lightData.isLit());
        target.setBlockData(lightData);
        return Result.TRUE;
    }

    @Override
    protected String wrenchMessage(Result result) {
        return null;
    }

    @Override
    protected boolean isEventCancelled(Result result) {
        return false;
    }
}
