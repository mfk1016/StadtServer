package me.mfk1016.stadtserver.origin.mob;

import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.util.Keys;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public abstract class BossMobOrigin extends MobOrigin {

    protected final int minlevel;

    public BossMobOrigin(int chance, EntityType entityType, int minlevel, World.Environment environment) {
        super(chance, entityType, environment);
        this.minlevel = minlevel;
        assert minlevel >= 1;
    }

    @Override
    protected boolean isMatched(Entity entity) {
        return entity.getType() == entityType
                && entity.getWorld().getEnvironment() == environment
                && isApplicable(entity);
    }

    @Override
    protected boolean isApplicable(Entity entity) {
        if (!entity.hasMetadata(Keys.IS_BOSS))
            return false;
        int bossLevel = entity.getMetadata(Keys.IS_BOSS).get(0).asInt();
        int newChance = chance * Math.max((bossLevel - minlevel + 1), 1);
        return bossLevel >= minlevel && StadtServer.RANDOM.nextInt(100) < newChance;
    }
}
