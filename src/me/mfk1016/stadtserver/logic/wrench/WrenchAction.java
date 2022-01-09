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
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.lang.reflect.InvocationTargetException;

public abstract class WrenchAction {

    private static final String CONFIG_PLAYER_MESSAGE = "send_player_message";

    protected final StadtServer plugin;

    public WrenchAction(StadtServer plugin) {
        this.plugin = plugin;
    }

    public static WrenchAction actionFactory(Block block, StadtServer plugin) {
        return switch (block.getType()) {
            case DROPPER -> new WrenchActionStateChange(plugin, "Dropper", "Chute");
            case DISPENSER -> new WrenchActionStateChange(plugin, "Dispenser", "Block Placer");
            case REDSTONE_LAMP -> new WrenchActionRedstoneLamp(plugin);
            case NOTE_BLOCK -> new WrenchActionNoteBlock(plugin);
            case CHEST -> new WrenchActionInventory(plugin, "Chest");
            case BARREL -> new WrenchActionInventory(plugin, "Barrel");
            default -> new WrenchActionNothing(plugin);
        };
    }

    protected abstract Result wrenchBlock(Player player, Block target);

    protected abstract String wrenchMessage(Result result);

    protected abstract boolean isEventCancelled(Result result);

    public void onWrenchBlock(Player player, Block target, PlayerInteractEvent event) {

        Result result = wrenchBlock(player, target);

        if (plugin.getConfig().getBoolean(CONFIG_PLAYER_MESSAGE)) {
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
