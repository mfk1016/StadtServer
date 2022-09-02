package me.mfk1016.stadtserver.ritual;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

@Getter
@Setter
public class ActiveRitual {

    private final @NotNull Block center;
    private @NotNull RitualState state;
    private int tick;
    private @NotNull String[] data;

    private HashMap<String, Block[]> shapeMap = null;

    public ActiveRitual(@NotNull Block center) {
        this(center, 0, RitualState.INIT, new String[0]);
    }

    public ActiveRitual(@NotNull Block center, int tick, @NotNull RitualState state, @NotNull String[] data) {
        this.center = center;
        this.tick = tick;
        this.state = state;
        this.data = data;
    }
}
