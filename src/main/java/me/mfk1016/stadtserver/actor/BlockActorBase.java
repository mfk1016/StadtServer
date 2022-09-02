package me.mfk1016.stadtserver.actor;

import lombok.Getter;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public abstract class BlockActorBase {

    protected final @NotNull String key;

    public BlockActorBase(@NotNull String key) {
        this.key = key;
    }


    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BlockActorBase other)
            return key.equals(other.key);
        return false;
    }

    public abstract Event createEvent(List<Block> blockStream);
}
