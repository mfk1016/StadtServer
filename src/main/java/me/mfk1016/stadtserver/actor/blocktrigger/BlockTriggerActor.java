package me.mfk1016.stadtserver.actor.blocktrigger;

import lombok.Getter;
import me.mfk1016.stadtserver.actor.BlockActorBase;
import me.mfk1016.stadtserver.actor.BlockActorManager;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class BlockTriggerActor<T> extends BlockActorBase {

    private final T source;

    public BlockTriggerActor(@NotNull String key, T source) {
        super(key);
        this.source = source;
    }

    public void trigger() {
        BlockActorManager.onExecuteTrigger(this);
    }

    @Override
    public BlockTriggerEvent<T> createEvent(List<Block> blocks) {
        return new BlockTriggerEvent<>(this, blocks);
    }
}
