package me.mfk1016.stadtserver.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.mfk1016.stadtserver.StadtServer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.lang.reflect.InvocationTargetException;

public abstract class BasicListener implements Listener {

    private static final String CONFIG_PLAYER_MESSAGE = "send_player_message";

    protected final StadtServer plugin;

    public BasicListener(StadtServer plugin) {
        this.plugin = plugin;
    }

    protected void playerMessage(Player player, String string) {
        if (plugin.getConfig().getBoolean(CONFIG_PLAYER_MESSAGE)) {
            ProtocolManager pm = ProtocolLibrary.getProtocolManager();
            PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.SET_ACTION_BAR_TEXT);
            packetContainer.getChatComponents().write(0, WrappedChatComponent.fromText(string));
            try {
                pm.sendServerPacket(player, packetContainer);
            } catch (InvocationTargetException e2) {
                // ignore
            }
        }
    }
}
