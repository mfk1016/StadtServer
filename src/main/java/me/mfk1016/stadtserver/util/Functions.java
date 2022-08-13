package me.mfk1016.stadtserver.util;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.mfk1016.stadtserver.StadtServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Axis;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.jetbrains.annotations.Contract;

import java.lang.reflect.InvocationTargetException;

public class Functions {

    @Contract("null -> true")
    public static boolean stackEmpty(ItemStack stack) {
        return stack == null || stack.getType() == Material.AIR;
    }

    public static TextComponent undecoratedText(String string) {
        return Component.text(string).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    public static String romanNumber(int level) {
        return "I".repeat(level)
                .replace("IIIII", "V")
                .replace("IIII", "IV")
                .replace("VV", "X")
                .replace("VIV", "IX")
                .replace("XXXXX", "L")
                .replace("XXXX", "XL")
                .replace("LL", "C")
                .replace("LXL", "XC");
    }

    public static void playerMessage(Player player, String string) {
        if (StadtServer.getInstance().getConfig().getBoolean(Keys.CONFIG_PLAYER_MESSAGE)) {
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

    public static MerchantRecipe copyRecipe(MerchantRecipe recipe, ItemStack result) {
        MerchantRecipe newRecipe = new MerchantRecipe(result, recipe.getUses(), recipe.getMaxUses(),
                recipe.hasExperienceReward(), recipe.getVillagerExperience(),
                recipe.getPriceMultiplier());
        newRecipe.setIngredients(recipe.getIngredients());
        return newRecipe;
    }

    public static boolean isFaceOfOrientation(Axis axis, BlockFace face) {
        return switch (axis) {
            case X -> face == BlockFace.WEST || face == BlockFace.EAST;
            case Y -> face == BlockFace.UP || face == BlockFace.DOWN;
            case Z -> face == BlockFace.NORTH || face == BlockFace.SOUTH;
        };
    }
}
