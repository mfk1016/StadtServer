package me.mfk1016.stadtserver.ticklib.tick;

import lombok.Getter;
import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.ticklib.BlockActorTypeBase;
import org.jetbrains.annotations.NotNull;

@Getter
public class BlockTickActorType extends BlockActorTypeBase {

    private final int delay;
    private final int delayMod;

    public BlockTickActorType(@NotNull String key, int delay) {
        super(key);
        this.delay = delay;
        this.delayMod = StadtServer.RANDOM.nextInt(delay);
    }
}
