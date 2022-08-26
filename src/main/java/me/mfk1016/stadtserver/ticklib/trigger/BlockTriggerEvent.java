package me.mfk1016.stadtserver.ticklib.trigger;

import lombok.Getter;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

@Getter
public class BlockTriggerEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    public static HandlerList getHandlerList() { return HANDLER_LIST; }

    private final @NotNull BlockTriggerActorType blockActorType;
    private final @NotNull Stream<Block> blockStream;

    public BlockTriggerEvent(@NotNull BlockTriggerActorType blockActorType, @NotNull Stream<Block> blockStream) {
        super(false);
        this.blockActorType = blockActorType;
        this.blockStream = blockStream;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
