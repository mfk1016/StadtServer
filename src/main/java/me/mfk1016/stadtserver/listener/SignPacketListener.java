package me.mfk1016.stadtserver.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import me.mfk1016.stadtserver.StadtServer;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class SignPacketListener extends PacketAdapter {

    private final SmallFunctionsListener signListener;

    public SignPacketListener(SmallFunctionsListener signListener) {
        super(StadtServer.getInstance(), ListenerPriority.NORMAL, PacketType.Play.Client.UPDATE_SIGN);
        this.signListener = signListener;
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {

        Player player = event.getPlayer();
        PacketContainer packet = event.getPacket();
        BlockPosition signPos = packet.getBlockPositionModifier().read(0);
        if (!signListener.isEdited(signPos))
            return;

        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                Location signLocation = signPos.toLocation(player.getWorld());
                Block signBlock = player.getWorld().getBlockAt(signLocation);
                Sign sign = (Sign) signBlock.getState();

                String[] lines = packet.getStringArrays().read(0);
                for (int i = 0; i < lines.length; i++) {
                    sign.line(i, Component.text(lines[i]));
                }
                sign.update();
                signListener.signEdited(signPos);
            }
        };
        event.setCancelled(true);
        runnable.runTaskLater(plugin, 0L);
    }

}
