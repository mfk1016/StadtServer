package me.mfk1016.stadtserver.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.logic.DispenserDropperLogic;
import me.mfk1016.stadtserver.logic.MaterialTypes;
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

    - Coarse Dirt + Bonemeal + Water = Clay

    - Ladder placement helper
 */
public class SmallFunctionsListener extends BasicListener {

    private static final ItemStack fakePick = new ItemStack(Material.NETHERITE_PICKAXE);
    private final Set<BlockPosition> editedSigns = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final DispenserDropperLogic action;

    public SmallFunctionsListener(StadtServer p, DispenserDropperLogic a) {
        super(p);
        action = a;
    }

    /* --- DISPENSER PLANTING --- */

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockDispense(BlockDispenseEvent event) {
        if (event.getBlock().getType() == Material.DISPENSER) {
            Block dispenserBlock = event.getBlock();
            ItemStack item = event.getItem();
            if (action.tryAllDispenseActions(dispenserBlock, item)) {
                event.setCancelled(true);
            }
        }
    }

    /* --- DROPPER + WRENCH = CHUTE --- */

    @EventHandler(priority = EventPriority.NORMAL)
    public void onItemEnterChute(InventoryMoveItemEvent event) {
        if (!(event.getDestination().getHolder() instanceof Dropper dropperState))
            return;

        action.tryChuteAction(dropperState, event.getItem());
    }

    /* --- DIRT TO PATH --- */

    @EventHandler(priority = EventPriority.NORMAL)
    public void onShovelInteractDirt(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null || event.getItem() == null)
            return;
        Block dirt = Objects.requireNonNull(event.getClickedBlock());
        if (dirt.getType() != Material.DIRT || !MaterialTypes.isShovel(event.getItem().getType()))
            return;

        dirt.setType(Material.DIRT_PATH);
        StadtServer.broadcastSound(dirt, Sound.ITEM_HOE_TILL, 1f, 1f);
    }

    /* --- SIGN EDITING --- */

    @EventHandler(priority = EventPriority.NORMAL)
    public void onSignClick(PlayerInteractEvent event) {

        Player p = event.getPlayer();
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && p.isSneaking())
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

    /* --- STICKY PISTON + LIGHTNING ROD --- */

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPistonTrigger(BlockPhysicsEvent event) {
        if (event.getBlock().getType() != Material.STICKY_PISTON)
            return;
        if (!event.getBlock().isBlockIndirectlyPowered())
            return;
        Piston piston = (Piston) event.getBlock().getBlockData();
        if (piston.isExtended())
            return;

        Block lightningRod = event.getBlock().getRelative(piston.getFacing());
        if (lightningRod.getType() != Material.LIGHTNING_ROD)
            return;
        Directional rodFace = (Directional) lightningRod.getBlockData();
        if (rodFace.getFacing() != piston.getFacing())
            return;

        Block toBreak = lightningRod.getRelative(piston.getFacing());
        if (toBreak.getPistonMoveReaction() == PistonMoveReaction.MOVE)
            toBreak.breakNaturally(fakePick);
    }

    /* --- RIGHT CLICK LADDER WITH LADDER --- */

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
        StadtServer.broadcastSound(targetBlock, Sound.BLOCK_LADDER_PLACE, 1f, 1f);
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
