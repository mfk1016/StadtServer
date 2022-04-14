package me.mfk1016.stadtserver.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.logic.DispenserDropperLogic;
import me.mfk1016.stadtserver.logic.sorting.PluginCategories;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.*;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Ladder;
import org.bukkit.block.data.type.Piston;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/*
    - Dispenser can plant wheat, potato, carrot, beetroot, melon, pumpkin and nether wart
    on proper farmland if placed adjacent.

    - Wrenched dropper = chute: outputs the items without any redstone requirement

    - Dirt + Shovel = path

    - Editable Signs

    - Sticky Piston + Lightning Rod = Block breaker
    - Sticky Piston + Grindstone = Crusher

    - Ladder placement helper
 */
public class SmallFunctionsListener implements Listener {

    private static final ItemStack fakePick = new ItemStack(Material.NETHERITE_PICKAXE);
    private final Set<BlockPosition> editedSigns = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockDispense(BlockDispenseEvent event) {
        if (event.getBlock().getType() == Material.DISPENSER) {
            Block dispenserBlock = event.getBlock();
            ItemStack item = event.getItem();
            if (DispenserDropperLogic.tryAllDispenseActions(dispenserBlock, item)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onItemEnterChute(InventoryMoveItemEvent event) {
        if (!(event.getSource().getHolder() instanceof Hopper))
            return;
        if (!(event.getDestination().getHolder() instanceof Dropper dropperState))
            return;
        if (dropperState.getInventory().firstEmpty() == -1)
            return;
        DispenserDropperLogic.tryChuteAction(dropperState, event);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onShovelInteractDirt(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null || event.getItem() == null)
            return;
        Block dirt = Objects.requireNonNull(event.getClickedBlock());
        if (dirt.getType() != Material.DIRT || !PluginCategories.isShovel(event.getItem().getType()))
            return;

        dirt.setType(Material.DIRT_PATH);
        dirt.getWorld().playSound(dirt.getLocation(), Sound.ITEM_HOE_TILL, 1f, 1f);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onSignClick(PlayerInteractEvent event) {

        Player p = event.getPlayer();
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || !p.isSneaking())
            return;
        if (p.getInventory().getItemInMainHand().getType() == Material.GLOW_INK_SAC)
            return;
        if (!event.hasBlock())
            return;
        Block b = Objects.requireNonNull(event.getClickedBlock());
        if (!(b.getState() instanceof Sign s))
            return;
        Location loc = b.getLocation();
        BlockPosition signPos = new BlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        if (s.isEditable() || editedSigns.contains(signPos))
            return;
        ProtocolManager pm = ProtocolLibrary.getProtocolManager();
        PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.OPEN_SIGN_EDITOR);
        packetContainer.getBlockPositionModifier().write(0, signPos);
        try {
            pm.sendServerPacket(p, packetContainer);
            editedSigns.add(signPos);
        } catch (InvocationTargetException e2) {
            // ignore
        }
    }

    public void signEdited(BlockPosition sign) {
        editedSigns.remove(sign);
    }

    public boolean isEdited(BlockPosition sign) {
        return editedSigns.contains(sign);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPistonTrigger(BlockPhysicsEvent event) {
        if (event.getBlock().getType() != Material.STICKY_PISTON)
            return;
        if (!event.getBlock().isBlockIndirectlyPowered())
            return;
        Piston piston = (Piston) event.getBlock().getBlockData();
        if (piston.isExtended())
            return;
        Block pistonTool = event.getBlock().getRelative(piston.getFacing());

        if (pistonTool.getType() == Material.LIGHTNING_ROD) {
            Directional rodFace = (Directional) pistonTool.getBlockData();
            if (rodFace.getFacing() != piston.getFacing())
                return;

            Block toBreak = pistonTool.getRelative(piston.getFacing());
            if (toBreak.getPistonMoveReaction() == PistonMoveReaction.MOVE)
                toBreak.breakNaturally(fakePick);

        } else if (pistonTool.getType() == Material.GRINDSTONE) {
            Directional grindstoneFace = (Directional) pistonTool.getBlockData();
            if (grindstoneFace.getFacing() != piston.getFacing())
                return;

            Block toBreak = pistonTool.getRelative(piston.getFacing());
            if (toBreak.getPistonMoveReaction() == PistonMoveReaction.MOVE)
                grindBlock(toBreak);
        }
    }

    private static void grindBlock(Block toBreak) {
        ItemStack drop = switch (toBreak.getType()) {
            case COBBLESTONE -> new ItemStack(Material.GRAVEL);
            case GRAVEL, SANDSTONE, CHISELED_SANDSTONE, CUT_SANDSTONE -> new ItemStack(Material.SAND);
            case RED_SANDSTONE, CHISELED_RED_SANDSTONE, CUT_RED_SANDSTONE -> new ItemStack(Material.RED_SAND);
            case ROOTED_DIRT -> new ItemStack(Material.DIRT);
            case AMETHYST_BLOCK -> new ItemStack(Material.AMETHYST_SHARD, 3);
            default -> null;
        };
        if (drop != null) {
            toBreak.setType(Material.AIR);
            if (StadtServer.RANDOM.nextInt(5) != 0)
                toBreak.getWorld().dropItemNaturally(toBreak.getLocation(), drop);
            toBreak.getWorld().playSound(toBreak.getLocation(), Sound.BLOCK_GRINDSTONE_USE, 1f, 1f);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerRightClickLadder(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        if (event.getItem() == null || event.getItem().getType() != Material.LADDER)
            return;
        Block ladderBlock = event.getClickedBlock();
        if (ladderBlock == null || ladderBlock.getType() != Material.LADDER)
            return;
        Block targetBlock = getLadderTargetBlock(ladderBlock);
        if (targetBlock == null)
            return;

        Ladder ladder = (Ladder) ladderBlock.getBlockData();
        Block targetSupportBlock = targetBlock.getRelative(ladder.getFacing().getOppositeFace());
        if (!targetSupportBlock.getType().isSolid() || targetSupportBlock.isEmpty())
            return;
        Ladder target = (Ladder) ladder.clone();
        target.setWaterlogged(targetBlock.isLiquid());
        targetBlock.setType(Material.LADDER);
        targetBlock.setBlockData(target);

        if (event.getPlayer().getGameMode() == GameMode.SURVIVAL || event.getPlayer().getGameMode() == GameMode.ADVENTURE) {
            ItemStack item = event.getItem();
            item.setAmount(item.getAmount() - 1);
        }
        targetBlock.getWorld().playSound(targetBlock.getLocation(), Sound.BLOCK_LADDER_PLACE, 1f, 1f);
    }

    private Block getLadderTargetBlock(Block ladderBlock) {
        Block belowBlock = ladderBlock.getRelative(BlockFace.DOWN);
        if (belowBlock.isEmpty() || belowBlock.getType() == Material.WATER)
            return belowBlock;
        if (belowBlock.getType() != ladderBlock.getType())
            return null;

        Ladder ladder = (Ladder) ladderBlock.getBlockData();
        Ladder below = (Ladder) belowBlock.getBlockData();
        if (ladder.getFacing() == below.getFacing())
            return getLadderTargetBlock(belowBlock);
        else
            return null;
    }
}
