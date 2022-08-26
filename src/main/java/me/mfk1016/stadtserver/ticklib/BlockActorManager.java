package me.mfk1016.stadtserver.ticklib;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.ticklib.tick.BlockTickActorType;
import me.mfk1016.stadtserver.ticklib.tick.BlockTickEvent;
import me.mfk1016.stadtserver.ticklib.trigger.BlockTriggerActorType;
import me.mfk1016.stadtserver.ticklib.trigger.BlockTriggerEvent;
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
import java.util.stream.Stream;

public class BlockActorManager implements Listener {

    private static final HashSet<BlockActorTypeBase> REGISTERED_ACTOR_TYPES = new HashSet<>();
    private static final HashMap<String, List<Block>> REGISTERED_BLOCKS = new HashMap<>();

    @EventHandler(priority = EventPriority.NORMAL)
    public void onServerTick(ServerTickEndEvent event) {
        int tickNumber = event.getTickNumber();
        for (BlockActorTypeBase actorType : REGISTERED_ACTOR_TYPES) {
            if (!(actorType instanceof BlockTickActorType tickType))
                continue;
            if (tickNumber % tickType.getDelay() != tickType.getDelayMod())
                continue;
            List<Block> registered = REGISTERED_BLOCKS.getOrDefault(tickType.getKey(), List.of());
            if (registered.isEmpty())
                continue;
            Stream<Block> blockStream = registered.stream().filter(BlockActorManager::isBlockLoaded);
            BlockTickEvent tickEvent = new BlockTickEvent(tickType, blockStream);
            StadtServer.getInstance().getServer().getPluginManager().callEvent(tickEvent);
        }
    }

    public static void executeTrigger(BlockTriggerActorType triggerType) {
        List<Block> registered = REGISTERED_BLOCKS.getOrDefault(triggerType.getKey(), List.of());
        if (registered.isEmpty())
            return;
        Stream<Block> blockStream = registered.stream().filter(BlockActorManager::isBlockLoaded);
        BlockTriggerEvent triggerEvent = new BlockTriggerEvent(triggerType, blockStream);
        StadtServer.getInstance().getServer().getPluginManager().callEvent(triggerEvent);
    }

    public static BlockTriggerActorType registerBlockTriggerType(@NotNull String key) {
        BlockTriggerActorType blockActorType = new BlockTriggerActorType(key);
        REGISTERED_ACTOR_TYPES.add(blockActorType);
        return blockActorType;
    }

    public static BlockTickActorType registerBlockTickType(@NotNull String key, int delay) {
        BlockTickActorType blockActorType = new BlockTickActorType(key, delay);
        REGISTERED_ACTOR_TYPES.add(blockActorType);
        return blockActorType;
    }

    public static void deleteBlockActorType(@NotNull BlockActorTypeBase blockActorType) {
        REGISTERED_ACTOR_TYPES.remove(blockActorType);
        REGISTERED_BLOCKS.remove(blockActorType.getKey());
    }

    public static void unregisterBlockActorTypes() {
        REGISTERED_ACTOR_TYPES.clear();
    }

    public static void registerBlock(@NotNull BlockActorTypeBase blockActorType, @NotNull Block block) {
        if (REGISTERED_BLOCKS.containsKey(blockActorType.getKey())) {
            List<Block> registered = REGISTERED_BLOCKS.get(blockActorType.getKey());
            registered.add(block);
        } else {
            List<Block> registered = new ArrayList<>();
            registered.add(block);
            REGISTERED_BLOCKS.put(blockActorType.getKey(), registered);
        }
    }

    public static void unregisterBlock(@NotNull BlockActorTypeBase blockActorType, @NotNull Block block) {
        if (REGISTERED_BLOCKS.containsKey(blockActorType.getKey())) {
            List<Block> registered = REGISTERED_BLOCKS.get(blockActorType.getKey());
            registered.remove(block);
            if (registered.isEmpty())
                REGISTERED_BLOCKS.remove(blockActorType.getKey());
        }
    }

    private static boolean isBlockLoaded(Block block) {
        return block.getWorld().isChunkLoaded(block.getX() >> 4, block.getZ() >> 4);
    }

    /* --- Persistence --- */

    public static void loadRegisteredBlocks() {
        File registeredBlockTicksFile = getRegisteredBlockTicksFile();
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
                getRegisteredBlockTicksFile().delete();
                FileWriter writer = new FileWriter(getRegisteredBlockTicksFile());
                writer.write(json);
                writer.close();
            } catch (IOException e) {
                StadtServer.LOGGER.info(e.getMessage());
            }
        });
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void saveRegisteredBlocks() {
        StadtServer.LOGGER.info("Saving registered block actors.");
        Gson gson = getGsonInstance();
        Map<String, Location[]> toPersist = new HashMap<>();
        for (Map.Entry<String, List<Block>> registered : REGISTERED_BLOCKS.entrySet()) {
            Location[] array = registered.getValue().stream().map(Block::getLocation).toArray(Location[]::new);
            toPersist.put(registered.getKey(), array);
        }
        try {
            getRegisteredBlockTicksFile().delete();
            FileWriter writer = new FileWriter(getRegisteredBlockTicksFile());
            writer.write(gson.toJson(toPersist));
            writer.close();
        } catch (IOException e) {
            StadtServer.LOGGER.info(e.getMessage());
        }
    }

    private static File getRegisteredBlockTicksFile() {
        return new File(StadtServer.getInstance().getDataFolder(), "ticking_blocks.json");
    }

    private static Gson getGsonInstance() {
        GsonBuilder builder = new GsonBuilder().setPrettyPrinting();
        builder.registerTypeAdapter(Location.class, new BlockLocationJSONAdapter());
        return builder.create();
    }
}
