package me.mfk1016.stadtserver.listener;

import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.logic.MinecartLogic;
import me.mfk1016.stadtserver.nms.v1_18_1.MinecartFix;
import me.mfk1016.stadtserver.util.Keys;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.Rail;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.vehicle.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.UUID;

import static me.mfk1016.stadtserver.util.Functions.playerMessage;

/*
    - Minecarts have increased top speed when running over a powered rail with a lapis block
    - Minecarts have decreased top speed when running over a powered rail with a coal block
    - Minecarts get pushed into the direction of underlying stairs, when a golden rail is powered
 */
public class MinecartListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onMinecartCreate(VehicleCreateEvent event) {
        if (!(event.getVehicle() instanceof Minecart cart))
            return;
        Block railBlock = cart.getLocation().getBlock();
        if (!(railBlock.getBlockData() instanceof Rail rail))
            return;
        if (rail.getShape() == Rail.Shape.NORTH_SOUTH) {
            MinecartFix.fixInitialMinecartYaw(cart);
        }
    }

    // Called every tick
    @EventHandler(priority = EventPriority.NORMAL)
    public void onMinecartTick(VehicleUpdateEvent event) {
        if (event.getVehicle() instanceof Minecart cart) {
            MinecartLogic.adjustVelocity(cart);
        }
    }

    // Called after update if the cart has moved during the tick
    @EventHandler(priority = EventPriority.NORMAL)
    public void onMinecartMove(VehicleMoveEvent event) {
        if (event.getVehicle() instanceof Minecart cart) {
            MinecartLogic.speedupMinecart(cart);
        }
    }

    // Called after update and move
    @EventHandler(priority = EventPriority.NORMAL)
    public void onTrainMemberCollide(VehicleEntityCollisionEvent event) {
        if (!(event.getVehicle() instanceof Minecart cart))
            return;
        if (!(event.getEntity() instanceof Minecart cart2))
            return;
        if (!MinecartLogic.hasMaster(cart) || !MinecartLogic.hasMaster(cart2))
            return;
        if (MinecartLogic.getMaster(cart) == MinecartLogic.getMaster(cart2)) {
            event.setCollisionCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onTrainMemberDestroy(VehicleDestroyEvent event) {
        if (!(event.getVehicle() instanceof Minecart cart))
            return;
        MinecartLogic.checkTrainMember(cart);
        if (!MinecartLogic.hasMaster(cart))
            return;
        MinecartLogic.unlinkCart(cart);
        if (event.getAttacker() instanceof Player player) {
            playDetachSound(cart);
            playerMessage(player, "Minecart unlinked");
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onStairsBoosterPush(BlockRedstoneEvent event) {
        if (event.getBlock().getType() == Material.POWERED_RAIL && event.getNewCurrent() > 0) {
            Block rail = event.getBlock();
            MinecartLogic.pushMinecarts(rail);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerRightClickMinecart(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Minecart cart))
            return;

        MinecartLogic.checkTrainMember(cart);
        Player player = event.getPlayer();
        ItemStack inHand = player.getInventory().getItemInMainHand();

        if (inHand.getType() == Material.LEAD) {

            // Linking
            if (hasCartAttached(player)) {
                Minecart master = getAttachedCart(player);
                boolean xNotEqual = cart.getLocation().getBlockX() != master.getLocation().getBlockX();
                boolean yNotEqual = cart.getLocation().getBlockY() != master.getLocation().getBlockY();
                boolean zNotEqual = cart.getLocation().getBlockZ() != master.getLocation().getBlockZ();
                if (cart == master) {
                    player.removeMetadata(Keys.CART_ATTACHED, StadtServer.getInstance());
                    playDetachSound(cart);
                    playerMessage(player, "Minecart linking cancelled");
                } else if (master.getLocation().distance(cart.getLocation()) > 3) {
                    playFailSound(player);
                    playerMessage(player, "Target Minecart too far away (> 3 Blocks)");
                } else if (yNotEqual || (xNotEqual && zNotEqual)) {
                    playFailSound(player);
                    playerMessage(player, "Target Minecart must be connected by uncurved rails");
                } else if (MinecartLogic.canBeFollower(master, cart)) {
                    MinecartLogic.linkCarts(master, cart);
                    player.removeMetadata(Keys.CART_ATTACHED, StadtServer.getInstance());
                    playLinkSound(cart);
                    playerMessage(player, "Minecarts linked");
                } else {
                    playFailSound(player);
                    playerMessage(player, "Minecart already linked by another Minecart or cyclic linking");
                }
            } else {
                if (!MinecartLogic.hasFollower(cart)) {
                    attachCart(player, cart);
                    playAttachSound(cart);
                    playerMessage(player, "Minecart attached");
                } else {
                    playerMessage(player, "Minecart already linked");
                    playFailSound(player);
                }
            }
            event.setCancelled(true);

        } else if (inHand.getType() == Material.SHEARS && player.hasMetadata(Keys.CART_ATTACHED)) {

            // Cancel attachment
            player.removeMetadata(Keys.CART_ATTACHED, StadtServer.getInstance());
            playDetachSound(cart);
            playerMessage(player, "Minecart linking cancelled (through shears)");

        } else if (inHand.getType() == Material.SHEARS) {

            // Unlinking
            if (MinecartLogic.hasMaster(cart)) {
                MinecartLogic.unlinkCart(cart);
                playDetachSound(cart);
                playerMessage(player, "Minecart unlinked");
            } else {
                playFailSound(player);
                playerMessage(player, "Minecart not part of any train");
            }
            event.setCancelled(true);
        }
    }

    private boolean hasCartAttached(Player player) {
        return player.hasMetadata(Keys.CART_ATTACHED) && getAttachedCart(player) != null;
    }

    private void attachCart(Player player, Minecart cart) {
        if (player.hasMetadata(Keys.CART_ATTACHED))
            player.removeMetadata(Keys.CART_ATTACHED, StadtServer.getInstance());
        player.setMetadata(Keys.CART_ATTACHED, new FixedMetadataValue(StadtServer.getInstance(), cart.getUniqueId().toString()));
    }

    private Minecart getAttachedCart(Player player) {
        String cartID = player.getMetadata(Keys.CART_ATTACHED).get(0).asString();
        return (Minecart) player.getWorld().getEntity(UUID.fromString(cartID));
    }

    private void playAttachSound(Minecart cart) {
        cart.getWorld().playSound(cart.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 1f, 1f);
    }

    private void playLinkSound(Minecart cart) {
        cart.getWorld().playSound(cart.getLocation(), Sound.BLOCK_DISPENSER_LAUNCH, 1f, 1f);
    }

    private void playDetachSound(Minecart cart) {
        cart.getWorld().playSound(cart.getLocation(), Sound.ENTITY_SHEEP_SHEAR, 1f, 1f);
    }

    private void playFailSound(Player player) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
    }

}
