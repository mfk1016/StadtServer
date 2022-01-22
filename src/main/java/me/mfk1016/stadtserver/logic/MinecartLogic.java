package me.mfk1016.stadtserver.logic;

import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.util.Keys;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Rail;
import org.bukkit.block.data.type.RedstoneRail;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.UUID;

public class MinecartLogic {

    // Speedup multipliers
    public static final double MINECART_SPEED_FACTOR = 0.1D;
    public static final double MINECART_SPEED_VANILLA = 0.4D;
    public static final double MINECART_SPEED_LIMIT = 0.8D;

    // Train member deviation constants
    private static final float MIN_CART_DIST = 2.3F;
    private static final float MAX_CART_DIST = 2.4F;

    /* --- TRAIN LINKING AND MEMBERSHIP --- */

    // A train is defined as:
    //
    // A master cart leading the train linked by the rest of the train.
    // Each cart is linked to its follower (if any).
    // A train has at least 2 carts.
    // -> the master has always a follower

    public static boolean canBeFollower(Minecart cart, Minecart follower) {
        // A cart can be a follower if it is either not a member of a train or a master
        if (hasMaster(follower)) {
            Minecart followerMaster = getMaster(follower);
            // No cyclic trains
            if (hasMaster(cart)) {
                Minecart cartMaster = getMaster(cart);
                if (cartMaster == follower)
                    return false;
            }

            return followerMaster == follower;
        }
        return true;
    }

    public static void linkCarts(Minecart cart, Minecart follower) {
        // Assumes that both carts can be linked together
        assert !hasFollower(cart) && canBeFollower(cart, follower);
        // Link carts
        cart.setSlowWhenEmpty(false);
        follower.setSlowWhenEmpty(false);
        setFollower(cart, follower);
        // Update master value of train
        if (!hasMaster(cart)) {
            setMaster(cart, cart);
            updateMaster(cart, cart);
        } else {
            Minecart master = getMaster(cart);
            updateMaster(follower, master);
        }
    }

    public static void unlinkCart(Minecart cart) {
        // Assumes that the cart is a proper member of a train
        // assert isTrainMember(cart);

        cart.setSlowWhenEmpty(true);
        Minecart master = getMaster(cart);
        setMaster(cart, null);

        if (cart == master) {
            // In this case, the cart has a follower
            Minecart follower = getFollower(cart);
            setFollower(cart, null);
            if (!hasFollower(follower)) {
                // The train consists of this master and a single follower -> reset the other cart too
                setMaster(follower, null);
                follower.setSlowWhenEmpty(true);
            } else {
                // The train has more than 2 carts -> the subtrain remains with a new master
                updateMaster(follower, follower);
            }
        } else {
            detachCart(master, cart);

            // Check the masters subtrain
            if (!hasFollower(master)) {
                // In this case, the cart was the second cart of the train -> reset master since its alone now
                // Otherwise, the master's subtrain is a correct train, since the master is still correct
                setMaster(master, null);
                master.setSlowWhenEmpty(true);
            }

            // Check the followers subtrain
            if (hasFollower(cart)) {
                // In this case, the minecart is not at either end of the train
                Minecart follower = getFollower(cart);
                setFollower(cart, null);
                if (!hasFollower(follower)) {
                    // The cart is next to the last one -> reset the last
                    setMaster(follower, null);
                    follower.setSlowWhenEmpty(true);
                } else {
                    // The cart has a subtrain following the cart -> update master of subtrain
                    updateMaster(follower, follower);
                }
            }
        }
    }

    public static void checkTrainMember(Minecart cart) {
        // Unlinks the cart if the master of the cart is null
        // An illegal state is present if any of those is null
        if (!hasMaster(cart)) {
            setMaster(cart, null);
            setFollower(cart, null);
            cart.setSlowWhenEmpty(true);
        }
    }

    private static void updateMaster(Minecart cart, Minecart master) {
        // Internally called to update the master of the entire train
        setMaster(cart, master);
        if (hasFollower(cart)) {
            Minecart follower = getFollower(cart);
            updateMaster(follower, master);
        }
    }

    private static void detachCart(Minecart cart, Minecart target) {
        // Internally called to detach the given minecart from the master's train
        if (hasFollower(cart)) {
            Minecart follower = getFollower(cart);
            if (follower == target) {
                setFollower(cart, null);
            } else {
                detachCart(follower, target);
            }
        }
    }


    /* --- CART SPEEDUP --- */

    public static void speedupMinecart(Minecart cart) {
        Location cartLocation = cart.getLocation();
        Block rail = cart.getWorld().getBlockAt(cartLocation);

        if (adjustMinecartSpeed(cart, rail))
            return;
        if (cart.getVelocity().getZ() == 0D) {
            if (!adjustMinecartSpeed(cart, rail.getRelative(1, 0, 0)))
                adjustMinecartSpeed(cart, rail.getRelative(-1, 0, 0));
        } else if (cart.getVelocity().getX() == 0D) {
            if (!adjustMinecartSpeed(cart, rail.getRelative(0, 0, 1)))
                adjustMinecartSpeed(cart, rail.getRelative(0, 0, -1));
        }
    }

    private static boolean adjustMinecartSpeed(Minecart cart, Block rail) {
        if (hasMaster(cart)) {
            return adjustTrainSpeed(getMaster(cart), rail);
        } else {
            return adjustSingleMinecartSpeed(cart, rail);
        }
    }

    private static boolean adjustTrainSpeed(Minecart cart, Block rail) {
        if (hasFollower(cart)) {
            adjustTrainSpeed(getFollower(cart), rail);
        }
        return adjustSingleMinecartSpeed(cart, rail);
    }

    private static boolean adjustSingleMinecartSpeed(Minecart cart, Block rail) {
        Material railMat = rail.getType();
        Block foundation = rail.getRelative(0, -1, 0);
        Material foundationMat = foundation.getType();
        if (railMat == Material.POWERED_RAIL) {

            if (foundationMat == Material.LAPIS_BLOCK && cart.getMaxSpeed() < MINECART_SPEED_LIMIT) {
                // Retrieve the last location at which the cart was boosted (to prevent double boosting)
                if (!cart.getMetadata(Keys.LAST_RAIL_X).isEmpty()) {
                    int lastX = cart.getMetadata(Keys.LAST_RAIL_X).get(0).asInt();
                    int lastZ = cart.getMetadata(Keys.LAST_RAIL_Z).get(0).asInt();
                    if (rail.getLocation().getBlockX() == lastX && rail.getLocation().getBlockZ() == lastZ)
                        return false;
                }
                // Lapis Block: Increase max speed by 25%
                cart.setMaxSpeed(cart.getMaxSpeed() + MINECART_SPEED_FACTOR);
                cart.setMetadata(Keys.LAST_RAIL_X, new FixedMetadataValue(StadtServer.getInstance(), rail.getLocation().getBlockX()));
                cart.setMetadata(Keys.LAST_RAIL_Z, new FixedMetadataValue(StadtServer.getInstance(), rail.getLocation().getBlockZ()));
                return true;
            } else if (foundationMat == Material.COAL_BLOCK && cart.getMaxSpeed() > MINECART_SPEED_VANILLA) {
                // Retrieve the last location at which the cart was boosted (to prevent double boosting)
                if (!cart.getMetadata(Keys.LAST_RAIL_X).isEmpty()) {
                    int lastX = cart.getMetadata(Keys.LAST_RAIL_X).get(0).asInt();
                    int lastZ = cart.getMetadata(Keys.LAST_RAIL_Z).get(0).asInt();
                    if (rail.getLocation().getBlockX() == lastX && rail.getLocation().getBlockZ() == lastZ)
                        return false;
                }
                // Coal Block: Decrease max speed by 25%
                cart.setMaxSpeed(cart.getMaxSpeed() - MINECART_SPEED_FACTOR);
                cart.setMetadata(Keys.LAST_RAIL_X, new FixedMetadataValue(StadtServer.getInstance(), rail.getLocation().getBlockX()));
                cart.setMetadata(Keys.LAST_RAIL_Z, new FixedMetadataValue(StadtServer.getInstance(), rail.getLocation().getBlockZ()));
                return true;
            } else if (foundationMat == Material.EMERALD_BLOCK) {
                // Emerald Block: Reset to 0.4
                cart.setMaxSpeed(MINECART_SPEED_VANILLA);
                cart.removeMetadata(Keys.LAST_RAIL_X, StadtServer.getInstance());
                cart.removeMetadata(Keys.LAST_RAIL_Z, StadtServer.getInstance());
                return true;
            }
        }
        return false;
    }

    /* --- CART STAIR PUSHING --- */

    public static void pushMinecarts(Block rail) {
        Block foundation = rail.getRelative(0, -1, 0);

        // Push the cart based on the direction of the stairs (the open side points to the direction)
        if (foundation.getBlockData() instanceof Stairs stairs) {
            RedstoneRail data = (RedstoneRail) rail.getBlockData();
            if (data.getShape() == Rail.Shape.NORTH_SOUTH) {
                if (stairs.getFacing() == BlockFace.NORTH) {
                    pushMinecartsVelocity(rail, 0.5D, 0.7D, new Vector(0, 0, 10));
                } else if (stairs.getFacing() == BlockFace.SOUTH) {
                    pushMinecartsVelocity(rail, 0.5D, 0.7D, new Vector(0, 0, -10));
                }
            } else if (data.getShape() == Rail.Shape.EAST_WEST) {
                if (stairs.getFacing() == BlockFace.WEST) {
                    pushMinecartsVelocity(rail, 0.7D, 0.5D, new Vector(10, 0, 0));
                } else if (stairs.getFacing() == BlockFace.EAST) {
                    pushMinecartsVelocity(rail, 0.7D, 0.5D, new Vector(-10, 0, 0));
                }
            }
        }
    }

    private static void pushMinecartsVelocity(Block rail, double boxX, double boxZ, Vector velocity) {
        Collection<Entity> entities = rail.getWorld().getNearbyEntities(rail.getLocation(), boxX, 1D, boxZ);
        for (Entity e : entities)
            if (e instanceof Minecart && e.getVelocity().equals(new Vector())) {
                e.setVelocity(velocity);
            }
    }

    /* --- TRAIN MOVEMENT / COLLISION --- */

    public static void adjustVelocity(@NotNull Minecart cart) {
        if (hasMaster(cart) && hasFollower(cart))
            adjustMinecartVelocity(cart, getFollower(cart));
    }

    private static void adjustMinecartVelocity(@NotNull Minecart cart, @NotNull Minecart follower) {

        // Act only if the angle between the cart yaws is 0 or 45 degree and both angles can be divided by 45
        // This means, that carts are adjusted, which run no 90 or 180 degree turn
        Vector cartLocation = cart.getLocation().toVector();
        Vector followerLocation = follower.getLocation().toVector();
        Vector directionToFollower = followerLocation.clone().subtract(cartLocation).setY(0);
        Vector directionToMaster = new Vector(-1, 1, -1).multiply(directionToFollower);

        // Determine the leading cart and handle cases with no velocity
        boolean cartStopped = cart.getVelocity().length() < 0.1;
        boolean followerStopped = follower.getVelocity().length() < 0.1;

        if (cartStopped && followerStopped) {
            // Adjustments only if a cart is moving
            return;
        } else if (cartStopped) {
            if (isLeadingVelocity(directionToFollower, follower.getVelocity())) {
                // cart stopped -> stop follower
                follower.setVelocity(new Vector());
            } else {
                // follower is leading, cart not moved -> start cart
                cart.setVelocity(follower.getVelocity().clone());
            }
        } else if (followerStopped) {
            if (isLeadingVelocity(directionToMaster, cart.getVelocity())) {
                // follower is leading and has stopped -> stop cart
                cart.setVelocity(new Vector());
            } else {
                // cart is leading, follower not moved -> start follower
                follower.setVelocity(cart.getVelocity().clone());
            }
        } else {
            // Both carts are moving -> exchange cart and follower if necessary
            if (!isLeadingVelocity(directionToFollower, follower.getVelocity())) {
                Minecart tmp = cart;
                cart = follower;
                follower = tmp;
            }
        }

        // Determine the yaws of both carts and check them
        float cartYaw = cart.getLocation().getYaw();
        float followerYaw = follower.getLocation().getYaw();

        if (cartYaw % 45.0F != 0 || followerYaw % 45.0F != 0)
            return;

        cartYaw = normalizeYaw(cartYaw);
        followerYaw = normalizeYaw(followerYaw);

        //if (Math.abs(cartYaw - followerYaw) == 90F) {
        //    // HACK: Occurs only at train start
        //    follower.setVelocity(cart.getVelocity().clone().setY(follower.getVelocity().getY()));
        //    return;
        //}

        // Now it is known that the follower trails the cart and that both carts are moving
        // The cart determines the speed, while the follower has to adjust itself against the cart

        if (cartYaw == followerYaw) {

            // Both carts move on the same axis -> follower gets cart velocity ignoring Y
            updateFollowerVelocity(follower, cart.getVelocity().getX(), cart.getVelocity().getZ());

        } else {
            double cartVelX = cart.getVelocity().getX();
            double cartVelZ = cart.getVelocity().getZ();
            double cartSpeed = cart.getVelocity().length();
            switch ((int) cartYaw) {
                case 0:
                    // Cart moves on east / west axis
                    if (followerYaw == 45F) {
                        // cart west -> x negative, follower north-west, x + z negative
                        // cart east -> x positive, follower south-east, x + z positive
                        updateFollowerVelocity(follower, cart.getVelocity().getX(), cart.getVelocity().getX());
                    } else if (followerYaw == 135F) {
                        // cart west -> x negative, follower south-west, x negative, z positive
                        // cart east -> x positive, follower north-east, x positive, z negative
                        updateFollowerVelocity(follower, cart.getVelocity().getX(), -cart.getVelocity().getX());
                    }
                    break;
                case 45:
                    // Cart moves on north-west / south-east axis
                    if (followerYaw == 90F) {
                        if (cartVelX < 0 || cartVelZ < 0) {
                            // cart north-west -> x + z negative, follower north, z negative
                            updateFollowerVelocity(follower, 0D, -cartSpeed);
                        } else {
                            // cart south-east -> x + z positive, follower south, z positive
                            updateFollowerVelocity(follower, 0D, cartSpeed);
                        }
                    } else if (followerYaw == 0F) {
                        if (cartVelX < 0 || cartVelZ < 0) {
                            // cart north-west -> x + z negative, follower west, x negative
                            updateFollowerVelocity(follower, -cartSpeed, 0D);
                        } else {
                            // cart south-east -> x + z positive, follower east, x positive
                            updateFollowerVelocity(follower, cartSpeed, 0D);
                        }
                    }
                    break;
                case 90:
                    // Cart moves on north / south axis
                    if (followerYaw == 135F) {
                        // cart north -> z negative, follower north-east, x positive, z negative
                        // cart south -> z positive, follower south-west, x negative, z positive
                        updateFollowerVelocity(follower, -cart.getVelocity().getZ(), cart.getVelocity().getZ());
                    } else if (followerYaw == 45F) {
                        // cart north -> z negative, follower north-west, x negative, z negative
                        // cart south -> z positive, follower south-east, x positive, z positive
                        updateFollowerVelocity(follower, cart.getVelocity().getZ(), cart.getVelocity().getZ());
                    }
                    break;
                case 135:
                    // Cart moves on north-east / south-west axis
                    if (followerYaw == 0F) {
                        if (cartVelX > 0 || cartVelZ < 0) {
                            // cart north-east -> x positive + z negative, follower east, x positive
                            updateFollowerVelocity(follower, cartSpeed, 0D);
                        } else {
                            // cart south-west -> x negative + z positive, follower west, x negative
                            updateFollowerVelocity(follower, -cartSpeed, 0D);
                        }
                    } else if (followerYaw == 90F) {
                        if (cartVelX > 0 || cartVelZ < 0) {
                            // cart north-east -> x positive + z negative, follower north, z negative
                            updateFollowerVelocity(follower, 0D, -cartSpeed);
                        } else {
                            // cart south-west -> x negative + z positive, follower south, z positive
                            updateFollowerVelocity(follower, 0D, cartSpeed);
                        }
                    }
                    break;
            }
        }

        // Adjust cart distance
        double distance = directionToFollower.length();
        if (distance < MIN_CART_DIST) {
            follower.setVelocity(follower.getVelocity().multiply(0.9D));
        } else if (distance > MAX_CART_DIST) {
            follower.setVelocity(follower.getVelocity().multiply(1.1D));
        }

        // Cart inhabitant ? adjust velocity too
        for (var entity : cart.getPassengers())
            entity.setVelocity(cart.getVelocity());
        for (var entity : follower.getPassengers())
            entity.setVelocity(follower.getVelocity());
    }

    private static boolean isLeadingVelocity(@NotNull Vector directionToPartner, @NotNull Vector cartVelocity) {
        double angle = cartVelocity.clone().setY(0).angle(directionToPartner);
        angle = Math.toDegrees(angle);
        if (angle < 0) {
            angle = angle + 360D;
        }

        return angle > 90D && angle < 270D;
    }

    private static void updateFollowerVelocity(@NotNull Minecart follower, double x, double z) {
        follower.setVelocity(new Vector(x, follower.getVelocity().getY(), z));
    }

    // Helpers

    public static boolean hasMaster(@NotNull Minecart cart) {
        return cart.hasMetadata(Keys.CART_MASTER) && getMaster(cart) != null;
    }

    public static Minecart getMaster(@NotNull Minecart cart) {
        String metaCartID = cart.getMetadata(Keys.CART_MASTER).get(0).asString();
        return (Minecart) cart.getWorld().getEntity(UUID.fromString(metaCartID));
    }

    public static void setMaster(@NotNull Minecart cart, Minecart master) {
        if (master != null)
            cart.setMetadata(Keys.CART_MASTER, new FixedMetadataValue(StadtServer.getInstance(), master.getUniqueId().toString()));
        else
            cart.removeMetadata(Keys.CART_MASTER, StadtServer.getInstance());
    }

    public static boolean hasFollower(@NotNull Minecart cart) {
        return cart.hasMetadata(Keys.CART_FOLLOWER) && getFollower(cart) != null;
    }

    public static Minecart getFollower(@NotNull Minecart cart) {
        String metaCartID = cart.getMetadata(Keys.CART_FOLLOWER).get(0).asString();
        return (Minecart) cart.getWorld().getEntity(UUID.fromString(metaCartID));
    }

    public static void setFollower(@NotNull Minecart cart, Minecart follower) {
        if (follower != null)
            cart.setMetadata(Keys.CART_FOLLOWER, new FixedMetadataValue(StadtServer.getInstance(), follower.getUniqueId().toString()));
        else
            cart.removeMetadata(Keys.CART_FOLLOWER, StadtServer.getInstance());
    }

    private static float normalizeYaw(float yaw) {
        if (yaw < 0) {
            yaw = yaw + 360.0F;
        }
        // 0 = east / west
        // 45 = north-west / south-east
        // 90 = north / south
        // 135 = north-east / south-west
        return yaw % 180.0F;
    }
}
