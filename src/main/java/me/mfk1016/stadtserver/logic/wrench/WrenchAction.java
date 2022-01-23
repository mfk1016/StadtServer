package me.mfk1016.stadtserver.logic.wrench;

import me.mfk1016.stadtserver.logic.wrench.actions.WrenchActionInventory;
import me.mfk1016.stadtserver.logic.wrench.actions.WrenchActionNoteBlock;
import me.mfk1016.stadtserver.logic.wrench.actions.WrenchActionRedstoneLamp;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import static me.mfk1016.stadtserver.util.Functions.playerMessage;

public abstract class WrenchAction {

    public static WrenchAction actionFactory(Block block) {
        return switch (block.getType()) {
            case DROPPER -> new WrenchActionStateChange("Dropper", "Chute");
            case DISPENSER -> new WrenchActionStateChange("Dispenser", "Block Placer");
            case REDSTONE_LAMP -> new WrenchActionRedstoneLamp();
            case NOTE_BLOCK -> new WrenchActionNoteBlock();
            case CHEST -> new WrenchActionInventory("Chest");
            case BARREL -> new WrenchActionInventory("Barrel");
            default -> new WrenchActionNothing();
        };
    }

    protected abstract Result wrenchBlock(Player player, Block target);

    protected abstract String wrenchMessage(Result result);

    protected abstract boolean isEventCancelled(Result result);

    public void onWrenchBlock(Player player, Block target, PlayerInteractEvent event) {
        Result result = wrenchBlock(player, target);
        String message = wrenchMessage(result);
        if (message != null)
            playerMessage(player, message);
        if (isEventCancelled(result))
            event.setCancelled(true);
    }

    public enum Result {
        FALSE,
        TRUE,
        TRUE_ON,
        TRUE_OFF
    }

}
