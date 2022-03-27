package me.mfk1016.stadtserver.logic.wrench;

import me.mfk1016.stadtserver.logic.wrench.actions.WrenchActionBeeCheck;
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
            case SHULKER_BOX, BLACK_SHULKER_BOX, BLUE_SHULKER_BOX,
                    BROWN_SHULKER_BOX, CYAN_SHULKER_BOX, GRAY_SHULKER_BOX, GREEN_SHULKER_BOX,
                    LIGHT_BLUE_SHULKER_BOX, MAGENTA_SHULKER_BOX, LIGHT_GRAY_SHULKER_BOX, LIME_SHULKER_BOX,
                    ORANGE_SHULKER_BOX, PINK_SHULKER_BOX, PURPLE_SHULKER_BOX, RED_SHULKER_BOX, WHITE_SHULKER_BOX,
                    YELLOW_SHULKER_BOX -> new WrenchActionInventory("Shulker Box");
            case BEE_NEST, BEEHIVE -> new WrenchActionBeeCheck();
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
