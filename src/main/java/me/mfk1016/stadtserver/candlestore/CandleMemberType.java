package me.mfk1016.stadtserver.candlestore;

import me.mfk1016.stadtserver.util.Keys;
import org.bukkit.Material;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public enum CandleMemberType {
    NORMAL,
    CHEST,
    EXPORT,
    LIGHT;

    public static CandleMemberType getMemberType(PersistentDataContainer pdc) {
        int ordinal = Objects.requireNonNull(pdc.get(Keys.CANDLE_STORE_MEMBER_TYPE, PersistentDataType.INTEGER));
        return CandleMemberType.values()[ordinal];
    }

    public static void setMemberType(PersistentDataContainer pdc, CandleMemberType memberType) {
        pdc.set(Keys.CANDLE_STORE_MEMBER_TYPE, PersistentDataType.INTEGER, memberType.ordinal());
    }

    public static CandleMemberType getMemberType(Material subBlockMat) {
        return switch (subBlockMat) {
            case ENDER_CHEST -> CandleMemberType.CHEST;
            case HOPPER -> CandleMemberType.EXPORT;
            case REDSTONE_LAMP -> CandleMemberType.LIGHT;
            default -> CandleMemberType.NORMAL;
        };
    }

    public static boolean isActorType(@NotNull CandleMemberType memberType) {
        return switch (memberType) {
            case NORMAL, EXPORT, CHEST -> false;
            case LIGHT -> true;
        };
    }
}
