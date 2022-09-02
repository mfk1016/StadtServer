package me.mfk1016.stadtserver.ritual;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.actor.blocktick.BlockTickEvent;
import me.mfk1016.stadtserver.util.json.ActiveRitualJSONAdapter;
import me.mfk1016.stadtserver.util.json.BlockLocationJSONAdapter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldSaveEvent;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RitualManager implements Listener {

    public static final List<RitualType> REGISTERED_RITUAL_TYPES = new ArrayList<>();
    public static final Map<Block, ActiveRitual> ACTIVE_RITUALS = new HashMap<>();

    @EventHandler(priority = EventPriority.NORMAL)
    public void onRitualTick(BlockTickEvent<RitualType> event) {
        RitualType ritualType = event.getSource();
        for (Block block : event.getBlocks()) {
            ActiveRitual ritual = ACTIVE_RITUALS.get(block);
            if (ritual == null)
                return;
            if (ritual.getShapeMap() == null)
                ritual.setShapeMap(ritualType.createShapeMap(ritual.getCenter()));
            ritualType.updateRitualCache(ritual);
            ritualType.onRitualTick(ritual);
            ritual.setTick(ritual.getTick() + 1);
        }
    }

    public static void spawnRitual(RitualType ritualType, Block center) {
        ActiveRitual ritual = new ActiveRitual(center);
        ritual.setShapeMap(ritualType.createShapeMap(ritual.getCenter()));
        ACTIVE_RITUALS.put(center, ritual);
        ritualType.onRitualSpawn(ritual);
        ritualType.updateRitualCache(ritual);
    }

    public static void stopRitual(RitualType ritualType, ActiveRitual ritual, boolean success) {
        ritualType.onRitualStop(ritual, success);
        ACTIVE_RITUALS.remove(ritual.getCenter());
    }

    public static void registerRitualType(RitualType ritualType) {
        REGISTERED_RITUAL_TYPES.add(ritualType);
    }

    /* --- Persistence --- */

    public static void onPluginEnable() {
        File activeRitualsFile = getActiveRitualsFile();
        if (!activeRitualsFile.exists()) {
            StadtServer.getInstance().saveResource(activeRitualsFile.getName(), false);
            return;
        }
        try {
            Gson gson = getGsonInstance();
            ActiveRitual[] loadedRituals = gson.fromJson(new FileReader(activeRitualsFile), ActiveRitual[].class);
            if (loadedRituals == null)
                return;
            ACTIVE_RITUALS.clear();
            for (ActiveRitual ritual : loadedRituals) {
                ACTIVE_RITUALS.put(ritual.getCenter(), ritual);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @EventHandler(priority = EventPriority.NORMAL)
    public void onWorldSave(WorldSaveEvent event) {
        if (event.getWorld().getEnvironment() != World.Environment.THE_END)
            return;
        StadtServer.LOGGER.info("Saving active rituals.");
        Gson gson = getGsonInstance();
        ActiveRitual[] toPersist = ACTIVE_RITUALS.values().toArray(new ActiveRitual[0]);
        String json = gson.toJson(toPersist);
        Bukkit.getServer().getScheduler().runTaskAsynchronously(StadtServer.getInstance(), () -> {
            try {
                getActiveRitualsFile().delete();
                FileWriter writer = new FileWriter(getActiveRitualsFile());
                writer.write(json);
                writer.close();
            } catch (IOException e) {
                StadtServer.LOGGER.info(e.getMessage());
            }
        });
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void onPluginDisable() {
        REGISTERED_RITUAL_TYPES.clear();
        Gson gson = getGsonInstance();
        ActiveRitual[] toPersist = ACTIVE_RITUALS.values().toArray(new ActiveRitual[0]);
        try {
            getActiveRitualsFile().delete();
            FileWriter writer = new FileWriter(getActiveRitualsFile());
            writer.write(gson.toJson(toPersist));
            writer.close();
        } catch (IOException e) {
            StadtServer.LOGGER.info(e.getMessage());
        }
    }

    private static File getActiveRitualsFile() {
        return new File(StadtServer.getInstance().getDataFolder(), "active_rituals.json");
    }

    private static Gson getGsonInstance() {
        GsonBuilder builder = new GsonBuilder().setPrettyPrinting();
        builder.registerTypeAdapter(Location.class, new BlockLocationJSONAdapter());
        builder.registerTypeAdapter(ActiveRitual.class, new ActiveRitualJSONAdapter());
        return builder.create();
    }
}
