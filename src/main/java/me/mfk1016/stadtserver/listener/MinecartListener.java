package me.mfk1016.stadtserver.listener;

import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.logic.MinecartLogic;
import me.mfk1016.stadtserver.util.Keys;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.event.vehicle.VehicleUpdateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.UUID;

/*
    - Minecarts have increased top speed when running over a powered rail with a lapis block
    - Minecarts have decreased top speed when running over a powered rail with a coal block
    - Minecarts get pushed into the direction of underlying stairs, when a golden rail is powered
 */
public class MinecartListener extends BasicListener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onVehicleMove(VehicleMoveEvent event) {
        if (event.getVehicle() instanceof Minecart cart) {
            MinecartLogic.checkTrainMember(cart);
            MinecartLogic.speedupMinecart(cart);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onVehicleUpdate(VehicleUpdateEvent event) {
        if (event.getVehicle() instanceof Minecart cart) {
            MinecartLogic.adjustVelocity(cart);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        if (event.getVehicle() instanceof Minecart cart) {
            MinecartLogic.checkTrainMember(cart);
            if (MinecartLogic.isTrainMember(cart)) {
                MinecartLogic.unlinkCart(cart);
                if (event.getAttacker() instanceof Player player) {
                    playDetachSound(cart);
                    playerMessage(player, "Minecart unlinked");
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onRedstone(BlockRedstoneEvent event) {
        if (event.getBlock().getType() == Material.POWERED_RAIL && event.getNewCurrent() > 0) {
            Block rail = event.getBlock();
            MinecartLogic.pushMinecarts(rail);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerRightClick(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Minecart cart) {

            MinecartLogic.checkTrainMember(cart);
            Player player = event.getPlayer();
            ItemStack inHand = player.getInventory().getItemInMainHand();

            if (inHand.getType() == Material.LEAD) {

                // Linking
                if (hasCartAttached(player)) {
                    Minecart master = getAttachedCart(player);
                    if (cart == master) {
                        player.removeMetadata(Keys.CART_ATTACHED, StadtServer.getInstance());
                        playDetachSound(cart);
                        playerMessage(player, "Minecart linking cancelled");
                    } else if (master.getLocation().distance(cart.getLocation()) > 3) {
                        playFailSound(player);
                        playerMessage(player, "Target Minecart too far away (> 3 Blocks)");
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
                    if (MinecartLogic.canBeLinked(cart)) {
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
                if (MinecartLogic.isTrainMember(cart)) {
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
    }

    private boolean hasCartAttached(Player player) {
        return player.hasMetadata(Keys.CART_ATTACHED);
    }

    private void attachCart(Player player, Minecart cart) {
        player.setMetadata(Keys.CART_ATTACHED, new FixedMetadataValue(StadtServer.getInstance(), cart.getUniqueId().toString()));
    }

    private Minecart getAttachedCart(Player player) {
        String cartID = player.getMetadata(Keys.CART_ATTACHED).get(0).asString();
        return (Minecart) player.getWorld().getEntity(UUID.fromString(cartID));
    }

    private void playAttachSound(Minecart cart) {
        Block target = cart.getLocation().getBlock();
        target.getWorld().playSound(target.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 1f, 1f);
    }

    private void playLinkSound(Minecart cart) {
        Block target = cart.getLocation().getBlock();
        target.getWorld().playSound(target.getLocation(), Sound.BLOCK_DISPENSER_LAUNCH, 1f, 1f);
    }

    private void playDetachSound(Minecart cart) {
        Block target = cart.getLocation().getBlock();
        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_SHEEP_SHEAR, 1f, 1f);
    }

    private void playFailSound(Player player) {
        Block target = player.getLocation().getBlock();
        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
    }

}
