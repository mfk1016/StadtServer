package me.mfk1016.stadtserver.actor;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.actor.blocktick.BlockTickActor;
import me.mfk1016.stadtserver.actor.blocktick.BlockTickEvent;
import me.mfk1016.stadtserver.actor.blocktrigger.BlockTriggerActor;
import me.mfk1016.stadtserver.actor.blocktrigger.BlockTriggerEvent;
import me.mfk1016.stadtserver.util.Functions;
import me.mfk1016.stadtserver.util.json.BlockLocationJSONAdapter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldSaveEvent;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class BlockActorManager implements Listener {

    private static final HashSet<BlockActorBase> REGISTERED_ACTOR_TYPES = new HashSet<>();
    private static final HashMap<String, List<Block>> REGISTERED_BLOCKS = new HashMap<>();

    /* --- Actor Specific --- */

    @EventHandler(priority = EventPriority.NORMAL)
    public void onServerTick(ServerTickEndEvent event) {
        int tickNumber = event.getTickNumber();
        for (BlockActorBase actorType : REGISTERED_ACTOR_TYPES) {
            if (!(actorType instanceof BlockTickActor<?> tickActor))
                continue;
            if (!tickActor.isTickOfActor(tickNumber))
                continue;
            List<Block> registered = REGISTERED_BLOCKS.getOrDefault(tickActor.getKey(), List.of());
            if (registered.isEmpty())
                continue;
            List<Block> blocks = registered.parallelStream().filter(Functions::isBlockLoaded).toList();
            BlockTickEvent<?> tickEvent = tickActor.createEvent(blocks);
            StadtServer.getInstance().getServer().getPluginManager().callEvent(tickEvent);
        }
    }

    public static void onExecuteTrigger(BlockTriggerActor<?> triggerActor) {
        List<Block> registered = REGISTERED_BLOCKS.getOrDefault(triggerActor.getKey(), List.of());
        if (registered.isEmpty())
            return;
        List<Block> blocks = registered.parallelStream().filter(Functions::isBlockLoaded).toList();
        BlockTriggerEvent<?> triggerEvent = triggerActor.createEvent(blocks);
        StadtServer.getInstance().getServer().getPluginManager().callEvent(triggerEvent);
    }

    /* --- Registration --- */

    public static void registerBlockActor(@NotNull BlockActorBase blockActor) {
        REGISTERED_ACTOR_TYPES.add(blockActor);
    }

    public static void unregisterBlockActor(@NotNull BlockActorBase blockActor) {
        REGISTERED_ACTOR_TYPES.remove(blockActor);
        REGISTERED_BLOCKS.remove(blockActor.getKey());
    }

    public static void registerBlock(@NotNull BlockActorBase blockActor, @NotNull Block block) {
        if (REGISTERED_BLOCKS.containsKey(blockActor.getKey())) {
            List<Block> registered = REGISTERED_BLOCKS.get(blockActor.getKey());
            registered.add(block);
        } else {
            List<Block> registered = new ArrayList<>();
            registered.add(block);
            REGISTERED_BLOCKS.put(blockActor.getKey(), registered);
        }
    }

    public static void unregisterBlock(@NotNull BlockActorBase blockActor, @NotNull Block block) {
        if (REGISTERED_BLOCKS.containsKey(blockActor.getKey())) {
            List<Block> registered = REGISTERED_BLOCKS.get(blockActor.getKey());
            registered.remove(block);
            if (registered.isEmpty())
                REGISTERED_BLOCKS.remove(blockActor.getKey());
        }
    }

    /* --- Persistence --- */

    public static void loadRegisteredBlocks() {
        File registeredBlockTicksFile = getRegisteredActorBlocksFile();
        if (!registeredBlockTicksFile.exists()) {
            StadtServer.getInstance().saveResource(registeredBlockTicksFile.getName(), false);
            return;
        }
        try {
            Gson gson = getGsonInstance();
            Type mapType = new TypeToken<Map<String, Location[]>>() {}.getType();
            Map<String, Location[]> loadedMap = gson.fromJson(new FileReader(registeredBlockTicksFile), mapType);
            if (loadedMap == null)
                return;
            REGISTERED_BLOCKS.clear();
            for (Map.Entry<String, Location[]> entry : loadedMap.entrySet()) {
                ArrayList<Block> blocks = new ArrayList<>();
                for (Location loc : entry.getValue())
                    blocks.add(loc.getBlock());
                REGISTERED_BLOCKS.put(entry.getKey(), blocks);
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
        StadtServer.LOGGER.info("Saving registered block actors.");
        Gson gson = getGsonInstance();
        Map<String, Location[]> toPersist = new HashMap<>();
        for (Map.Entry<String, List<Block>> registered : REGISTERED_BLOCKS.entrySet()) {
            Location[] array = registered.getValue().stream().map(Block::getLocation).toArray(Location[]::new);
            toPersist.put(registered.getKey(), array);
        }
        String json = gson.toJson(toPersist);
        Bukkit.getServer().getScheduler().runTaskAsynchronously(StadtServer.getInstance(), () -> {
            try {
                getRegisteredActorBlocksFile().delete();
                FileWriter writer = new FileWriter(getRegisteredActorBlocksFile());
                writer.write(json);
                writer.close();
            } catch (IOException e) {
                StadtServer.LOGGER.info(e.getMessage());
            }
        });
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void onPluginDisable() {
        REGISTERED_ACTOR_TYPES.clear();
        Gson gson = getGsonInstance();
        Map<String, Location[]> toPersist = new HashMap<>();
        for (Map.Entry<String, List<Block>> registered : REGISTERED_BLOCKS.entrySet()) {
            Location[] array = registered.getValue().stream().map(Block::getLocation).toArray(Location[]::new);
            toPersist.put(registered.getKey(), array);
        }
        try {
            getRegisteredActorBlocksFile().delete();
            FileWriter writer = new FileWriter(getRegisteredActorBlocksFile());
            writer.write(gson.toJson(toPersist));
            writer.close();
        } catch (IOException e) {
            StadtServer.LOGGER.info(e.getMessage());
        }
    }

    private static File getRegisteredActorBlocksFile() {
        return new File(StadtServer.getInstance().getDataFolder(), "actor_blocks.json");
    }

    private static Gson getGsonInstance() {
        GsonBuilder builder = new GsonBuilder().setPrettyPrinting();
        builder.registerTypeAdapter(Location.class, new BlockLocationJSONAdapter());
        return builder.create();
    }
}
