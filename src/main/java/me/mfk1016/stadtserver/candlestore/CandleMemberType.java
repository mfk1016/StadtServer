package me.mfk1016.stadtserver.candlestore;

import me.mfk1016.stadtserver.util.Keys;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;

public enum CandleMemberType {
    NORMAL,
    CHEST,
    EXPORT;

    public static CandleMemberType getMemberType(PersistentDataContainer pdc) {
        int ordinal = Objects.requireNonNull(pdc.get(Keys.CANDLE_STORE_MEMBER_TYPE, PersistentDataType.INTEGER));
        return CandleMemberType.values()[ordinal];
    }

    public static void setMemberType(PersistentDataContainer pdc, CandleMemberType memberType) {
        pdc.set(Keys.CANDLE_STORE_MEMBER_TYPE, PersistentDataType.INTEGER, memberType.ordinal());
    }
}
