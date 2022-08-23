package me.mfk1016.stadtserver.util.pdc;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

public class PersistentDataVector implements PersistentDataType<byte[], Vector> {

    public static final PersistentDataVector TYPE = new PersistentDataVector();

    @Override
    public @NotNull Class<byte[]> getPrimitiveType() {
        return byte[].class;
    }

    @Override
    public @NotNull Class<Vector> getComplexType() {
        return Vector.class;
    }

    @Override
    public byte @NotNull [] toPrimitive(@NotNull Vector complex, @NotNull PersistentDataAdapterContext context) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[24]);
        bb.putDouble(complex.getX());
        bb.putDouble(complex.getY());
        bb.putDouble(complex.getZ());
        return bb.array();
    }

    @Override
    public @NotNull Vector fromPrimitive(byte @NotNull [] primitive, @NotNull PersistentDataAdapterContext context) {
        ByteBuffer bb = ByteBuffer.wrap(primitive);
        double x = bb.getDouble();
        double y = bb.getDouble();
        double z = bb.getDouble();
        return new Vector(x, y, z);
    }
}
