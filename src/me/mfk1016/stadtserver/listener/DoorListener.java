package me.mfk1016.stadtserver.listener;

import me.mfk1016.stadtserver.StadtServer;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Door;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class DoorListener extends BasicListener {

    public DoorListener(StadtServer plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInteractDoor(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        Block doorBlock = event.getClickedBlock();
        if (doorBlock == null || doorBlock.getType() == Material.IRON_DOOR)
            return;
        if (doorBlock.getBlockData() instanceof Door door) {
            Block matchBlock = getMatchingDoor(doorBlock);
            if (matchBlock == null)
                return;
            Door match = (Door) matchBlock.getBlockData();
            match.setOpen(!match.isOpen());
            matchBlock.setBlockData(match);
        }
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
