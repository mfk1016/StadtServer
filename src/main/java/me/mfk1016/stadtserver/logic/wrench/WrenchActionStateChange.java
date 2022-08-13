package me.mfk1016.stadtserver.logic.wrench;

import lombok.RequiredArgsConstructor;
import me.mfk1016.stadtserver.util.Keys;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

@RequiredArgsConstructor
public class WrenchActionStateChange extends WrenchAction {

    private final String blockName, wrenchedName;

    @Override
    protected Result wrenchBlock(Player player, Block target) {
        if (!(target.getState() instanceof TileState state))
            return Result.FALSE;
        PersistentDataContainer pdc = state.getPersistentDataContainer();
        int value = pdc.getOrDefault(Keys.IS_WRENCHED, PersistentDataType.INTEGER, 0);
        int result = value == 0 ? 1 : 0;
        pdc.set(Keys.IS_WRENCHED, PersistentDataType.INTEGER, result);
        state.update();
        return result == 1 ? Result.TRUE_ON : Result.TRUE_OFF;
    }

    @Override
    protected String wrenchMessage(Result result) {
        return switch (result) {
            case TRUE_ON -> blockName + " wrenched to be a " + wrenchedName;
            case TRUE_OFF -> blockName + " reset";
            default -> null;
        };
    }

    @Override
    protected boolean isEventCancelled(Result result) {
        return result != Result.FALSE;
    }
}
