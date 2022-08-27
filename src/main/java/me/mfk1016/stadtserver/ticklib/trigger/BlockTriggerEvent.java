package me.mfk1016.stadtserver.ticklib.trigger;

import lombok.Getter;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

@Getter
public class BlockTriggerEvent<T> extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    public static HandlerList getHandlerList() { return HANDLER_LIST; }

    private final @NotNull BlockTriggerActor<T> blockActorType;
    private final @NotNull Stream<Block> blockStream;

    public BlockTriggerEvent(@NotNull BlockTriggerActor<T> blockActorType, @NotNull Stream<Block> blockStream) {
        super(false);
        this.blockActorType = blockActorType;
        this.blockStream = blockStream;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public T getSource() {
        return blockActorType.getSource();
    }
}
