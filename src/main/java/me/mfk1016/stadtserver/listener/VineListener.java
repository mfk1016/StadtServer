package me.mfk1016.stadtserver.listener;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import me.mfk1016.stadtserver.StadtServer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;

import static me.mfk1016.stadtserver.util.Functions.playerMessage;

public class VineListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerShearVine(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        if (!event.hasBlock() || !isVine(Objects.requireNonNull(event.getClickedBlock()).getType()))
            return;
        if (!event.hasItem() || Objects.requireNonNull(event.getItem()).getType() != Material.SHEARS)
            return;

        // Stop vine growth at x/z position
        Block vineBlock = event.getClickedBlock();
        NamespacedKey vineKey = getChunkContainerKey(vineBlock);
        PersistentDataContainer pdc = vineBlock.getChunk().getPersistentDataContainer();
        if (pdc.has(vineKey))
            playerMessage(event.getPlayer(), "Vine growth at " + messageLocString(vineBlock) + " already stopped");
        else {
            pdc.set(vineKey, PersistentDataType.BYTE, (byte) 1);
            vineBlock.getWorld().playSound(event.getPlayer().getLocation(), Sound.ENTITY_SHEEP_SHEAR, 1f, 1f);
            playerMessage(event.getPlayer(), "Vine growth stopped at " + messageLocString(vineBlock));
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerBonemealVine(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        if (!event.hasBlock() || !isVine(Objects.requireNonNull(event.getClickedBlock()).getType()))
            return;
        if (!event.hasItem() || Objects.requireNonNull(event.getItem()).getType() != Material.BONE_MEAL)
            return;

        // Resume vine growth at x/z position
        Block vineBlock = event.getClickedBlock();
        NamespacedKey vineKey = getChunkContainerKey(vineBlock);
        PersistentDataContainer pdc = vineBlock.getChunk().getPersistentDataContainer();
        if (!pdc.has(vineKey))
            playerMessage(event.getPlayer(), "Normal vine growth at " + messageLocString(vineBlock));
        else {
            pdc.remove(vineKey);
            vineBlock.getWorld().playSound(event.getPlayer().getLocation(), Sound.ITEM_BONE_MEAL_USE, 1f, 1f);
            playerMessage(event.getPlayer(), "Vine growth resumed at " + messageLocString(vineBlock));
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDestroyVine(BlockBreakEvent event) {
        if (!isVine(event.getBlock().getType()))
            return;
        if (isVine(event.getBlock().getRelative(BlockFace.UP).getType()))
            return;

        Block vineBlock = event.getBlock();
        NamespacedKey vineKey = getChunkContainerKey(vineBlock);
        PersistentDataContainer pdc = vineBlock.getChunk().getPersistentDataContainer();
        if (pdc.has(vineKey)) {
            pdc.remove(vineKey);
            playerMessage(event.getPlayer(), "Vine growth resumed at " + messageLocString(vineBlock));
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onVineDestroy(BlockDestroyEvent event) {
        if (!isVine(event.getBlock().getType()))
            return;
        if (isVine(event.getBlock().getRelative(BlockFace.UP).getType()))
            return;

        Block vineBlock = event.getBlock();
        NamespacedKey vineKey = getChunkContainerKey(vineBlock);
        PersistentDataContainer pdc = vineBlock.getChunk().getPersistentDataContainer();
        if (pdc.has(vineKey)) {
            pdc.remove(vineKey);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onVineGrow(BlockSpreadEvent event) {
        if (!isVine(event.getSource().getType()))
            return;
        Location sourceLoc = event.getSource().getLocation();
        Location targetLoc = event.getBlock().getLocation();
        if (sourceLoc.getBlockX() != targetLoc.getBlockX() || sourceLoc.getBlockZ() != targetLoc.getBlockZ())
            return;
        if (targetLoc.getBlockY() >= sourceLoc.getBlockY())
            return;

        Block sourceVine = event.getSource();
        NamespacedKey vineKey = getChunkContainerKey(sourceVine);
        PersistentDataContainer pdc = sourceVine.getChunk().getPersistentDataContainer();
        if (pdc.has(vineKey))
            event.setCancelled(true);
    }

    private NamespacedKey getChunkContainerKey(Block vineBlock) {
        Location loc = vineBlock.getLocation();
        int x = loc.getBlockX();
        int z = loc.getBlockZ();
        return new NamespacedKey(StadtServer.getInstance(), "X" + x + "Z" + z);
    }

    private String messageLocString(Block vineBlock) {
        Location loc = vineBlock.getLocation();
        return "X: " + loc.getBlockX() + " Z: " + loc.getBlockZ();
    }

    private static boolean isVine(Material mat) {
        return switch (mat) {
            case VINE, CAVE_VINES_PLANT, CAVE_VINES -> true;
            default -> false;
        };
    }

}
