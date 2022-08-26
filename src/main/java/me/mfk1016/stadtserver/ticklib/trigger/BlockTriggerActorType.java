package me.mfk1016.stadtserver.ticklib.trigger;

import me.mfk1016.stadtserver.ticklib.BlockActorTypeBase;
import me.mfk1016.stadtserver.ticklib.BlockActorManager;
import org.jetbrains.annotations.NotNull;

public class BlockTriggerActorType extends BlockActorTypeBase {

    public BlockTriggerActorType(@NotNull String key) {
        super(key);
    }

    public void trigger() {
        BlockActorManager.executeTrigger(this);
    }
}
