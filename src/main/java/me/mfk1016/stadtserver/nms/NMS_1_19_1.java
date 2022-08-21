package me.mfk1016.stadtserver.nms;

import net.minecraft.world.entity.vehicle.AbstractMinecart;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftMinecart;
import org.bukkit.entity.Minecart;

public class NMS_1_19_1 {

    public static void fixInitialMinecartYaw(Minecart cart) {
        CraftMinecart craftCart = (CraftMinecart) cart;
        AbstractMinecart realCart = craftCart.getHandle();
        realCart.setYRot(90.0F);
        realCart.setRot(realCart.getYRot(), realCart.getXRot());
    }

}
