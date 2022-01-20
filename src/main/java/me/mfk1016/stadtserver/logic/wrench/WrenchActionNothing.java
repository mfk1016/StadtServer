package me.mfk1016.stadtserver.logic.wrench;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class WrenchActionNothing extends WrenchAction {

    @Override
    protected Result wrenchBlock(Player player, Block target) {
        return Result.FALSE;
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
