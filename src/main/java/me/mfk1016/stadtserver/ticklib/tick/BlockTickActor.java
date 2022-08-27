package me.mfk1016.stadtserver.ticklib.tick;

import lombok.Getter;
import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.ticklib.BlockActorBase;
import org.jetbrains.annotations.NotNull;

@Getter
public class BlockTickActor extends BlockActorBase {

    private final int delay;
    private final int delayMod;

    public BlockTickActor(@NotNull String key, int delay) {
        super(key);
        this.delay = delay;
        this.delayMod = StadtServer.RANDOM.nextInt(delay);
    }
}
