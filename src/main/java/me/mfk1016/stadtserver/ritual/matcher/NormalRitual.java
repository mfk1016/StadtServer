package me.mfk1016.stadtserver.ritual.matcher;

import me.mfk1016.stadtserver.ritual.RitualManager;
import me.mfk1016.stadtserver.ritual.RitualType;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Optional;

public interface NormalRitual {

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean isValidWorldState(World world);

    static Optional<RitualType> matchRitualType(Block center) {
        if (RitualManager.ACTIVE_RITUALS.containsKey(center))
            return Optional.empty();
        for (RitualType ritualType : RitualManager.REGISTERED_RITUAL_TYPES) {
            if (ritualType.getFocusMaterial() != center.getType())
                continue;
            if (!ritualType.isValidWorldState(center.getWorld()))
                continue;
            if (!ritualType.isValidShape(ritualType.createShapeMap(center), 0))
                continue;
            return Optional.of(ritualType);
        }
        return Optional.empty();
    }
}
