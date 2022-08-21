package me.mfk1016.stadtserver.listener;

import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.enchantments.EnchantmentManager;
import me.mfk1016.stadtserver.enchantments.WrenchEnchantment;
import me.mfk1016.stadtserver.logic.DispenserDropperLogic;
import me.mfk1016.stadtserver.logic.sorting.PluginCategories;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.*;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.type.Ladder;
import org.bukkit.block.data.type.Piston;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;

import static me.mfk1016.stadtserver.util.Functions.isFaceOfOrientation;
import static me.mfk1016.stadtserver.util.Functions.stackEmpty;

/*
    - Dispenser can plant wheat, potato, carrot, beetroot, melon, pumpkin and nether wart
    on proper farmland if placed adjacent.

    - Wrenched dropper = chute: outputs the items without any redstone requirement

    - Dirt + Shovel = path

    - Editable Signs

    - Sticky Piston + Lightning Rod = Block breaker
    - Sticky Piston + Chain = Crusher

    - Ladder placement helper
 */
public class SmallFunctionsListener implements Listener {

    private static final ItemStack fakePick = new ItemStack(Material.NETHERITE_PICKAXE);

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockDispense(BlockDispenseEvent event) {
        if (!event.isCancelled() && event.getBlock().getType() == Material.DISPENSER) {
            Block dispenserBlock = event.getBlock();
            ItemStack item = event.getItem();
            if (DispenserDropperLogic.tryAllDispenseActions(dispenserBlock, item)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onItemEnterChute(InventoryMoveItemEvent event) {
        if (!(event.getSource().getHolder(false) instanceof Hopper hopper))
            return;
        if (!(event.getDestination().getHolder(false) instanceof Dropper dropperState))
            return;
        if (!WrenchEnchantment.isWrenched(dropperState))
            return;
        Block source = hopper.getBlock();
        ItemStack item = event.getItem().clone();
        Block dropperBlock = dropperState.getBlock();
        Directional dropperData = (Directional) dropperBlock.getBlockData();
        Block target = dropperBlock.getRelative(dropperData.getFacing());
        event.setCancelled(true);
        new BukkitRunnable() {
            @Override
            public void run() {
                DispenserDropperLogic.tryChuteAction(source, item, target);
            }
        }.runTaskLater(StadtServer.getInstance(), 1L);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onShovelInteractDirt(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        Block dirt = Objects.requireNonNull(event.getClickedBlock());
        if (!PluginCategories.isShovel(event.getMaterial()) || dirt.getType() != Material.DIRT)
            return;
        if (EnchantmentManager.isEnchantedWith(event.getItem(), EnchantmentManager.WRENCH))
            return;
        if (EnchantmentManager.isEnchantedWith(event.getItem(), EnchantmentManager.TROWEL))
            return;

        dirt.setType(Material.DIRT_PATH);
        dirt.getWorld().playSound(dirt.getLocation(), Sound.ITEM_HOE_TILL, 1f, 1f);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onSignClick(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || !p.isSneaking())
            return;
        if (!stackEmpty(p.getInventory().getItemInMainHand()))
            return;
        Block signBlock = Objects.requireNonNull(event.getClickedBlock());
        if (!signBlock.getType().name().endsWith("_SIGN"))
            return;
        event.setCancelled(true);
        event.getPlayer().openSign((Sign) signBlock.getState());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPistonTrigger(BlockPistonExtendEvent event) {
        if (event.getBlock().getType() != Material.STICKY_PISTON)
            return;
        Piston piston = (Piston) event.getBlock().getBlockData();
        Block pistonTool = event.getBlock().getRelative(piston.getFacing());

        if (pistonTool.getType() == Material.LIGHTNING_ROD) {
            Directional rodFace = (Directional) pistonTool.getBlockData();
            if (rodFace.getFacing() != piston.getFacing())
                return;
            Block toBreak = pistonTool.getRelative(piston.getFacing());
            if (toBreak.getPistonMoveReaction() == PistonMoveReaction.MOVE)
                toBreak.breakNaturally(fakePick);

        } else if (pistonTool.getType() == Material.CHAIN) {
            Orientable chain = (Orientable) pistonTool.getBlockData();
            if (!isFaceOfOrientation(chain.getAxis(), piston.getFacing()))
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
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getMaterial() != Material.LADDER)
            return;
        Block ladderBlock = Objects.requireNonNull(event.getClickedBlock());
        if (ladderBlock.getType() != Material.LADDER)
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
            ItemStack item = Objects.requireNonNull(event.getItem());
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
