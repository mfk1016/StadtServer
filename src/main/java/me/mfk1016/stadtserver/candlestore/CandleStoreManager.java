package me.mfk1016.stadtserver.candlestore;

import com.google.gson.*;
import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.util.Keys;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Dispenser;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class CandleStoreManager {

    private static final HashMap<Long, CandleStore> ALL_STORES = new HashMap<>();

    private static Long NEXT_KEY = 0L;

    public static Optional<CandleStore> getStore(Dispenser dispenser) {
        PersistentDataContainer pdc = dispenser.getPersistentDataContainer();
        if (!pdc.has(Keys.CANDLE_STORE))
            return Optional.empty();
        Long key = pdc.get(Keys.CANDLE_STORE, PersistentDataType.LONG);
        assert ALL_STORES.containsKey(key);
        return Optional.of(ALL_STORES.get(key));
    }

    public static void createStore(@NotNull Dispenser dispenser, @NotNull CandleMemberType memberType) {
        PersistentDataContainer pdc = dispenser.getPersistentDataContainer();
        boolean hasChest = memberType == CandleMemberType.CHEST;
        CandleStore store = CandleStore.createStore(NEXT_KEY, hasChest, dispenser.getLocation().toVector());
        ALL_STORES.put(NEXT_KEY, store);
        pdc.set(Keys.CANDLE_STORE, PersistentDataType.LONG, NEXT_KEY);
        CandleMemberType.setMemberType(pdc, memberType);
        dispenser.update();
        NEXT_KEY = NEXT_KEY + 1;
    }

    public static void addToStore(@NotNull Dispenser dispenser, @NotNull Dispenser partner, @NotNull CandleMemberType memberType) {
        PersistentDataContainer pdc = dispenser.getPersistentDataContainer();
        CandleStore store = Objects.requireNonNull(getStore(partner).orElse(null));
        store.addMember(dispenser.getLocation().toVector(), memberType);
        pdc.set(Keys.CANDLE_STORE, PersistentDataType.LONG, store.getKey());
        CandleMemberType.setMemberType(pdc, memberType);
        dispenser.update();
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
        dispenser.update();
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
            store.deleteMember(dispenser.getLocation().toVector(), memberType);
        } else {
            ALL_STORES.remove(key);
        }
        pdc.remove(Keys.CANDLE_STORE);
        pdc.remove(Keys.CANDLE_STORE_MEMBER_TYPE);
        dispenser.update();
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
                    if (store.getKey() >= NEXT_KEY)
                        NEXT_KEY = store.getKey() + 1;
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

    private static File getStoresFile() {
        return new File(StadtServer.getInstance().getDataFolder(), "candle_stores.json");
    }

    private static Gson getGsonInstance() {
        GsonBuilder builder = new GsonBuilder().setPrettyPrinting();
        builder.registerTypeAdapter(CandleStore.class, new CandleStoreJSONAdapter());
        return builder.create();
    }

    private static class CandleStoreJSONAdapter implements JsonSerializer<CandleStore>, JsonDeserializer<CandleStore> {

        @Override
        public JsonElement serialize(CandleStore store, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.add("key", new JsonPrimitive(store.getKey()));
            obj.add("member_count", new JsonPrimitive(store.getMemberCount()));
            obj.add("storage_slots", new JsonPrimitive(store.getStorageSlots()));
            obj.add("used_slots", new JsonPrimitive(store.getUsedSlots()));
            obj.add("center_x", new JsonPrimitive(store.getCenterLocation().getX()));
            obj.add("center_y", new JsonPrimitive(store.getCenterLocation().getY()));
            obj.add("center_z", new JsonPrimitive(store.getCenterLocation().getZ()));
            JsonObject storage = new JsonObject();
            for (Map.Entry<Material, Long> elem : store.getStorage().entrySet()) {
                storage.add(elem.getKey().name(), new JsonPrimitive(elem.getValue()));
            }
            obj.add("storage", storage);
            return obj;
        }

        @Override
        public CandleStore deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = (JsonObject) json;
            Long key = obj.get("key").getAsLong();
            int memberCount = obj.get("member_count").getAsInt();
            int storageSlots = obj.get("storage_slots").getAsInt();
            int usedSlots = obj.get("used_slots").getAsInt();
            double centerX = obj.get("center_x").getAsDouble();
            double centerY = obj.get("center_y").getAsDouble();
            double centerZ = obj.get("center_z").getAsDouble();
            Vector centerLocation = new Vector(centerX, centerY, centerZ);

            EnumMap<Material, Long> storage = new EnumMap<>(Material.class);
            JsonObject storageObj = obj.getAsJsonObject("storage");
            for (Map.Entry<String, JsonElement> elem : storageObj.entrySet()) {
                Material mat = Material.getMaterial(elem.getKey());
                long amount = elem.getValue().getAsLong();
                storage.put(mat, amount);
            }

            return new CandleStore(key, memberCount, storageSlots, usedSlots, storage, centerLocation);
        }
    }
}
