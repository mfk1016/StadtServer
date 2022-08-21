package me.mfk1016.stadtserver.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Door;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Objects;

public class DoorListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInteractDoor(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getPlayer().isSneaking())
            return;
        Block doorBlock = Objects.requireNonNull(event.getClickedBlock());
        if (doorBlock.getType() == Material.IRON_DOOR || !doorBlock.getType().name().endsWith("_DOOR"))
            return;
        Block matchBlock = getMatchingDoor(doorBlock);
        if (matchBlock == null)
            return;
        Door match = (Door) matchBlock.getBlockData();
        match.setOpen(!match.isOpen());
        matchBlock.setBlockData(match);
    }

    private Block getMatchingDoor(Block doorBlock) {
        Door door = (Door) doorBlock.getBlockData();
        // Bottom half is relevant
        if (door.getHalf() == Bisected.Half.TOP) {
            return getMatchingDoor(doorBlock.getRelative(BlockFace.DOWN));
        }

        BlockFace matchDirection = BlockFace.DOWN;
        switch (door.getFacing()) {
            case NORTH -> {
                if (door.getHinge() == Door.Hinge.LEFT)
                    matchDirection = BlockFace.EAST;
                else
                    matchDirection = BlockFace.WEST;
            }
            case EAST -> {
                if (door.getHinge() == Door.Hinge.LEFT)
                    matchDirection = BlockFace.SOUTH;
                else
                    matchDirection = BlockFace.NORTH;
            }
            case SOUTH -> {
                if (door.getHinge() == Door.Hinge.LEFT)
                    matchDirection = BlockFace.WEST;
                else
                    matchDirection = BlockFace.EAST;
            }
            case WEST -> {
                if (door.getHinge() == Door.Hinge.LEFT)
                    matchDirection = BlockFace.NORTH;
                else
                    matchDirection = BlockFace.SOUTH;
            }
        }

        Block matchBlock = doorBlock.getRelative(matchDirection);
        if (matchBlock.getBlockData() instanceof Door match) {
            if (matchBlock.getType() == Material.IRON_DOOR)
                return null;
            if (match.getHinge() == door.getHinge())
                return null;
            if (match.isOpen() != door.isOpen())
                return null;
            return matchBlock;
        }
        return null;
    }
}
