package me.mfk1016.stadtserver.enchantments;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.origin.enchantment.EnchantmentOrigin;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import static me.mfk1016.stadtserver.util.Functions.romanNumber;

public abstract class CustomEnchantment extends Enchantment implements Listener {

    private static final String CONFIG_PLAYER_MESSAGE = "send_player_message";
    protected final StadtServer plugin;
    private final String name;


    public CustomEnchantment(StadtServer plugin, String name, String namespace) {
        super(new NamespacedKey(plugin, namespace));
        this.name = name;
        this.plugin = plugin;
    }

    public abstract int getAnvilCost(ItemStack sacrifice, int level);

    public abstract Set<EnchantmentOrigin> getOrigins();

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public boolean isTreasure() {
        return true;
    }

    public String getLoreEntry(int level) {
        if (getMaxLevel() == 1)
            return ChatColor.GRAY + getName();
        else
            return ChatColor.GRAY + getName() + " " + romanNumber(level);
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
