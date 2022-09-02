package me.mfk1016.stadtserver.ritual.matcher;

import me.mfk1016.stadtserver.ritual.RitualManager;
import me.mfk1016.stadtserver.ritual.RitualType;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

import java.util.Optional;

public interface SacrificeRitual extends NormalRitual {

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean isValidSacrifice(Entity entity);

    static Optional<RitualType> matchRitualType(Block center, Entity sacrifice) {
        if (RitualManager.ACTIVE_RITUALS.containsKey(center))
            return Optional.empty();
        for (RitualType ritualType : RitualManager.REGISTERED_RITUAL_TYPES) {
            if (!(ritualType instanceof SacrificeRitual sacrificeRitual))
                continue;
            if (ritualType.getFocusMaterial() != center.getType())
                continue;
            if (!sacrificeRitual.isValidSacrifice(sacrifice))
                continue;
            if (!sacrificeRitual.isValidWorldState(center.getWorld()))
                continue;
            if (!ritualType.isValidShape(ritualType.createShapeMap(center), 0))
                continue;
            return Optional.of(ritualType);
        }
        return Optional.empty();
    }
}
