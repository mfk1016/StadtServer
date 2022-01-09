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
import org.bukkit.metadata.MetadataValue;

import java.lang.ref.WeakReference;
import java.util.Objects;

/*
    - Minecarts have increased top speed when running over a powered rail with a lapis block
    - Minecarts have decreased top speed when running over a powered rail with a coal block
    - Minecarts get pushed into the direction of underlying stairs, when a golden rail is powered
 */
public class MinecartListener extends BasicListener {

    private final MinecartLogic logic;

    public MinecartListener(StadtServer p, MinecartLogic l) {
        super(p);
        logic = l;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onVehicleMove(VehicleMoveEvent event) {
        if (event.getVehicle() instanceof Minecart cart) {
            logic.checkTrainMember(cart);
            logic.speedupMinecart(cart);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onVehicleUpdate(VehicleUpdateEvent event) {
        if (event.getVehicle() instanceof Minecart cart) {
            logic.adjustVelocity(cart);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        if (event.getVehicle() instanceof Minecart cart) {
            logic.checkTrainMember(cart);
            if (logic.isTrainMember(cart)) {
                logic.unlinkCart(cart);
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
            logic.pushMinecarts(rail);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerRightClick(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Minecart cart) {

            logic.checkTrainMember(cart);
            Player player = event.getPlayer();
            ItemStack inHand = player.getInventory().getItemInMainHand();

            if (inHand.getType() == Material.LEAD) {

                // Linking
                if (hasCartAttached(player)) {
                    Minecart master = getAttachedCart(player);
                    if (cart == master) {
                        player.removeMetadata(Keys.CART_ATTACHED, plugin);
                        playDetachSound(cart);
                        playerMessage(player, "Minecart linking cancelled");
                    } else if (master.getLocation().distance(cart.getLocation()) > 3) {
                        playFailSound(player);
                        playerMessage(player, "Target Minecart too far away (> 3 Blocks)");
                    } else if (logic.canBeFollower(master, cart)) {
                        logic.linkCarts(master, cart);
                        player.removeMetadata(Keys.CART_ATTACHED, plugin);
                        playLinkSound(cart);
                        playerMessage(player, "Minecarts linked");
                    } else {
                        playFailSound(player);
                        playerMessage(player, "Minecart already linked by another Minecart or cyclic linking");
                    }
                } else {
                    if (logic.canBeLinked(cart)) {
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
                player.removeMetadata(Keys.CART_ATTACHED, plugin);
                playDetachSound(cart);
                playerMessage(player, "Minecart linking cancelled (through shears)");

            } else if (inHand.getType() == Material.SHEARS) {

                // Unlinking
                if (logic.isTrainMember(cart)) {
                    logic.unlinkCart(cart);
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
        player.setMetadata(Keys.CART_ATTACHED, new FixedMetadataValue(plugin, new WeakReference<>(cart)));
    }

    private Minecart getAttachedCart(Player player) {
        MetadataValue val = player.getMetadata(Keys.CART_ATTACHED).get(0);
        return ((WeakReference<Minecart>) Objects.requireNonNull(val.value())).get();
    }

    private void playAttachSound(Minecart cart) {
        Block target = cart.getLocation().getBlock();
        StadtServer.broadcastSound(target, Sound.BLOCK_DISPENSER_FAIL, 1f, 1f);
    }

    private void playLinkSound(Minecart cart) {
        Block target = cart.getLocation().getBlock();
        StadtServer.broadcastSound(target, Sound.BLOCK_DISPENSER_LAUNCH, 1f, 1f);
    }

    private void playDetachSound(Minecart cart) {
        Block target = cart.getLocation().getBlock();
        StadtServer.broadcastSound(target, Sound.ENTITY_SHEEP_SHEAR, 1f, 1f);
    }

    private void playFailSound(Player player) {
        Block target = player.getLocation().getBlock();
        StadtServer.broadcastSound(target, Sound.ENTITY_VILLAGER_NO, 1f, 1f);
    }

}
