package me.mfk1016.stadtserver.ticklib.trigger;

import lombok.Getter;
import me.mfk1016.stadtserver.ticklib.BlockActorBase;
import me.mfk1016.stadtserver.ticklib.BlockActorManager;
import org.jetbrains.annotations.NotNull;

@Getter
public class BlockTriggerActor<T> extends BlockActorBase {

    private final T source;

    public BlockTriggerActor(@NotNull String key, T source) {
        super(key);
        this.source = source;
    }

    public void trigger() {
        BlockActorManager.executeTrigger(this);
    }
}
