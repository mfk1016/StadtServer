package me.mfk1016.stadtserver.ticklib;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public abstract class BlockActorTypeBase {

    protected final @NotNull String key;

    public BlockActorTypeBase(@NotNull String key) {
        this.key = key;
    }


    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BlockActorTypeBase other)
            return key.equals(other.key);
        return false;
    }

    @Override
    public String toString() {
        return "BlockTickType(" + key + ")";
    }
}
