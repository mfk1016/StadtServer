package me.mfk1016.stadtserver.logic.wrench.actions;

import me.mfk1016.stadtserver.logic.wrench.WrenchAction;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Beehive;
import org.bukkit.block.Block;
import org.bukkit.block.data.Lightable;
import org.bukkit.entity.Player;

import java.awt.*;

public class WrenchActionBeeCheck extends WrenchAction {

    private String message = "";

    @Override
    protected Result wrenchBlock(Player player, Block target) {
        if (target.getType() != Material.BEE_NEST && target.getType() != Material.BEEHIVE)
            return Result.FALSE;
        Beehive beehive = (Beehive) target.getState();
        message = "The hive at " + messageLocString(target) + " contains " + beehive.getEntityCount() + " bees";
        return Result.TRUE;
    }

    @Override
    protected String wrenchMessage(Result result) {
        return message;
    }

    @Override
    protected boolean isEventCancelled(Result result) {
        return false;
    }

    private static String messageLocString(Block vineBlock) {
        Location loc = vineBlock.getLocation();
        return "" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ();
    }
}
