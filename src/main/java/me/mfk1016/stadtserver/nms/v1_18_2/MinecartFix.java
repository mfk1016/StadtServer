package me.mfk1016.stadtserver.nms.v1_18_2;

import net.minecraft.world.entity.vehicle.AbstractMinecart;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftMinecart;
import org.bukkit.entity.Minecart;

public class MinecartFix {

    public static void fixInitialMinecartYaw(Minecart cart) {
        CraftMinecart craftCart = (CraftMinecart) cart;
        AbstractMinecart realCart = craftCart.getHandle();
        realCart.setYRot(90.0F);
        realCart.setRot(realCart.getYRot(), realCart.getXRot());
    }

}
