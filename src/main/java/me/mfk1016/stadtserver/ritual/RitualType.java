package me.mfk1016.stadtserver.ritual;

import lombok.Getter;
import me.mfk1016.stadtserver.actor.BlockActorManager;
import me.mfk1016.stadtserver.actor.blocktick.BlockTickActor;
import me.mfk1016.stadtserver.ritual.matcher.NormalRitual;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.HashMap;

public abstract class RitualType implements NormalRitual {

    @Getter
    private final String key;
    @Getter
    private final Material focusMaterial;
    @Getter
    private final BlockTickActor<RitualType> tickActor;

    public RitualType(String key, Material focusMaterial, int tickDelay) {
        this.key = key;
        this.focusMaterial = focusMaterial;
        this.tickActor = new BlockTickActor<>("ritual:" + key, this, tickDelay);
        BlockActorManager.registerBlockActor(tickActor);
    }

    public void onRitualSpawn(ActiveRitual ritual) {
        BlockActorManager.registerBlock(tickActor, ritual.getCenter());
    }

    public void onRitualStop(ActiveRitual ritual, boolean success) {
        BlockActorManager.unregisterBlock(tickActor, ritual.getCenter());
    }

    public abstract HashMap<String, Block[]> createShapeMap(Block center);

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public abstract boolean isValidShape(HashMap<String, Block[]> shapeMap, int tick);

    /**
     * Updates the ritual types ritual cache.
     * The ritual data should always be the root origin of the cache
     *
     * @param ritual The ritual to create/update the cache for.
     */
    public abstract void updateRitualCache(ActiveRitual ritual);

    public abstract void onRitualTick(ActiveRitual activeRitual);
}
