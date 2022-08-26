package me.mfk1016.stadtserver.candlestore;

import com.google.gson.*;
import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.ticklib.BlockActorManager;
import me.mfk1016.stadtserver.ticklib.BlockActorTypeBase;
import me.mfk1016.stadtserver.util.Keys;
import me.mfk1016.stadtserver.util.json.CandleStoreJSONAdapter;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;

import static me.mfk1016.stadtserver.util.Functions.undecoratedText;

public class CandleStoreManager {

    private static final HashMap<Long, CandleStore> ALL_STORES = new HashMap<>();

    public static Optional<CandleStore> getStore(Dispenser dispenser) {
        PersistentDataContainer pdc = dispenser.getPersistentDataContainer();
        if (!pdc.has(Keys.CANDLE_STORE))
            return Optional.empty();
        Long key = pdc.get(Keys.CANDLE_STORE, PersistentDataType.LONG);
        assert ALL_STORES.containsKey(key);
        return Optional.of(ALL_STORES.get(key));
    }

    public static Optional<CandleStore> getStore(BlockActorTypeBase blockActorType) {
        if (!blockActorType.getKey().startsWith(CandleStore.ACTOR_TYPE_PREFIX))
            return Optional.empty();
        long key = Long.parseLong(blockActorType.getKey().split(":")[1]);
        if (!ALL_STORES.containsKey(key))
            return Optional.empty();
        return Optional.of(ALL_STORES.get(key));
    }

    public static void createStore(@NotNull Dispenser dispenser, @NotNull CandleMemberType memberType) {
        PersistentDataContainer pdc = dispenser.getPersistentDataContainer();
        boolean hasChest = memberType == CandleMemberType.CHEST;
        Long nextKey = getNextStoreKey();
        CandleStore store = CandleStore.createStore(nextKey, hasChest, dispenser.getLocation().toVector());
        ALL_STORES.put(nextKey, store);
        pdc.set(Keys.CANDLE_STORE, PersistentDataType.LONG, nextKey);
        CandleMemberType.setMemberType(pdc, memberType);
        dispenser.customName(undecoratedText("Filter"));
        dispenser.update();
        updateBlockTrigger(store, dispenser.getBlock(), null, memberType);
    }

    public static void addToStore(@NotNull Dispenser dispenser, @NotNull Dispenser partner, @NotNull CandleMemberType memberType) {
        PersistentDataContainer pdc = dispenser.getPersistentDataContainer();
        CandleStore store = Objects.requireNonNull(getStore(partner).orElse(null));
        store.addMember(dispenser.getLocation(), memberType);
        pdc.set(Keys.CANDLE_STORE, PersistentDataType.LONG, store.getKey());
        CandleMemberType.setMemberType(pdc, memberType);
        dispenser.customName(undecoratedText("Filter"));
        dispenser.update();
        updateBlockTrigger(store, dispenser.getBlock(), null, memberType);
    }

    public static void updateStoreMember(Dispenser dispenser, CandleMemberType newType) {
        PersistentDataContainer pdc = dispenser.getPersistentDataContainer();
        if (!pdc.has(Keys.CANDLE_STORE))
            return;
        Long key = pdc.get(Keys.CANDLE_STORE, PersistentDataType.LONG);
        assert ALL_STORES.containsKey(key);
        CandleStore store = ALL_STORES.get(key);
        CandleMemberType oldType = CandleMemberType.getMemberType(pdc);
        store.updateMember(oldType, newType);
        CandleMemberType.setMemberType(pdc, newType);
        dispenser.customName(undecoratedText("Filter"));
        dispenser.update();
        updateBlockTrigger(store, dispenser.getBlock(), oldType, newType);
    }

    public static void deleteFromStore(Dispenser dispenser) {
        PersistentDataContainer pdc = dispenser.getPersistentDataContainer();
        if (!pdc.has(Keys.CANDLE_STORE))
            return;
        Long key = pdc.get(Keys.CANDLE_STORE, PersistentDataType.LONG);
        assert ALL_STORES.containsKey(key);
        CandleStore store = ALL_STORES.get(key);
        CandleMemberType memberType = CandleMemberType.getMemberType(pdc);
        if (store.getMemberCount() > 1) {
            store.deleteMember(dispenser.getLocation(), memberType);
        } else {
            BlockActorManager.deleteBlockActorType(store.getActor());
            ALL_STORES.remove(key);
        }
        pdc.remove(Keys.CANDLE_STORE);
        pdc.remove(Keys.CANDLE_STORE_MEMBER_TYPE);
        dispenser.customName(null);
        dispenser.update();
        updateBlockTrigger(store, dispenser.getBlock(), memberType, null);
    }

    /* --- Persistence --- */

    public static void loadStores() {
        File candleStoreFile = getStoresFile();
        if (!candleStoreFile.exists()) {
            StadtServer.getInstance().saveResource(candleStoreFile.getName(), false);
            return;
        }
        try {
            Gson gson = getGsonInstance();
            CandleStore[] loadedStores = gson.fromJson(new FileReader(candleStoreFile), CandleStore[].class);
            if (loadedStores != null) {
                ALL_STORES.clear();
                for (CandleStore store : loadedStores) {
                    ALL_STORES.put(store.getKey(), store);
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void saveStoresOnWorldSave() {
        Gson gson = getGsonInstance();
        CandleStore[] toPersist = ALL_STORES.values().toArray(new CandleStore[0]);
        String json = gson.toJson(toPersist);
        Bukkit.getServer().getScheduler().runTaskAsynchronously(StadtServer.getInstance(), () -> {
            try {
                getStoresFile().delete();
                FileWriter writer = new FileWriter(getStoresFile());
                writer.write(json);
                writer.close();
            } catch (IOException e) {
                StadtServer.LOGGER.info(e.getMessage());
            }
        });
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void saveStoresOnPluginStop() {
        Gson gson = getGsonInstance();
        CandleStore[] toPersist = ALL_STORES.values().toArray(new CandleStore[0]);
        String json = gson.toJson(toPersist);
        try {
            getStoresFile().delete();
            FileWriter writer = new FileWriter(getStoresFile());
            writer.write(json);
            writer.close();
            ALL_STORES.clear();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /* --- Helpers --- */

    private static Long getNextStoreKey() {
        Long nextKey = 0L;
        while (ALL_STORES.containsKey(nextKey))
            nextKey++;
        return nextKey;
    }

    private static void updateBlockTrigger(CandleStore store, Block block, CandleMemberType oldType, CandleMemberType newType) {
        if (oldType != null && CandleMemberType.isActorType(oldType))
            BlockActorManager.unregisterBlock(store.getActor(), block);
        else if (newType != null && CandleMemberType.isActorType(newType))
            BlockActorManager.registerBlock(store.getActor(), block);
    }

    private static File getStoresFile() {
        return new File(StadtServer.getInstance().getDataFolder(), "candle_stores.json");
    }

    private static Gson getGsonInstance() {
        GsonBuilder builder = new GsonBuilder().setPrettyPrinting();
        builder.registerTypeAdapter(CandleStore.class, new CandleStoreJSONAdapter());
        return builder.create();
    }
}
