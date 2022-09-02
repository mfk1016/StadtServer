package me.mfk1016.stadtserver.actor.blocktick;

import lombok.Getter;
import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.actor.BlockActorBase;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class BlockTickActor<T> extends BlockActorBase {

    private final T source;
    private final int delay;
    private final int delayMod;

    public BlockTickActor(@NotNull String key, T source, int delay) {
        super(key);
        this.source = source;
        this.delay = delay;
        this.delayMod = StadtServer.RANDOM.nextInt(delay);
    }

    public boolean isTickOfActor(int tickNumber) {
        return tickNumber % delay == delayMod;
    }

    @Override
    public BlockTickEvent<T> createEvent(List<Block> blocks) {
        return new BlockTickEvent<>(this, blocks);
    }
}
