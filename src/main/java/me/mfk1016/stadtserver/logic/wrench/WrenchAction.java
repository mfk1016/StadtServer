package me.mfk1016.stadtserver.logic.wrench;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.logic.wrench.actions.WrenchActionInventory;
import me.mfk1016.stadtserver.logic.wrench.actions.WrenchActionNoteBlock;
import me.mfk1016.stadtserver.logic.wrench.actions.WrenchActionRedstoneLamp;
import me.mfk1016.stadtserver.util.Keys;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.lang.reflect.InvocationTargetException;

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

        if (StadtServer.getInstance().getConfig().getBoolean(Keys.CONFIG_PLAYER_MESSAGE)) {
            String message = wrenchMessage(result);
            if (message != null) {
                ProtocolManager pm = ProtocolLibrary.getProtocolManager();
                PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.SET_ACTION_BAR_TEXT);
                packetContainer.getChatComponents().write(0, WrappedChatComponent.fromText(message));
                try {
                    pm.sendServerPacket(player, packetContainer);
                } catch (InvocationTargetException e2) {
                    // ignore
                }
            }
        }
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
