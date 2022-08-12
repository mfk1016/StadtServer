package me.mfk1016.stadtserver.candlestore;

import com.google.gson.*;
import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.util.Keys;
import org.bukkit.Material;
import org.bukkit.block.Dispenser;
import org.bukkit.inventory.Inventory;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class CandleStoreManager {

    public static final String STORE_MEMBER_VIEW = "view";
    public static final String STORE_MEMBER_CHEST = "chest";
    public static final String STORE_MEMBER_EXPORT = "export";

    private static final HashMap<String, CandleStore> ALL_STORES = new HashMap<>();
    private static final HashMap<Inventory, CandleStore> VIEW_MAP = new HashMap<>();

    public static Optional<CandleStore> getStore(Dispenser dispenser) {
        PersistentDataContainer pdc = dispenser.getPersistentDataContainer();
        if (!pdc.has(Keys.CANDLE_STORE))
            return Optional.empty();
        String key = pdc.get(Keys.CANDLE_STORE, PersistentDataType.STRING);
        assert ALL_STORES.containsKey(key);
        return Optional.of(ALL_STORES.get(key));
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isStoreView(Inventory view) {
        return VIEW_MAP.containsKey(view);
    }

    public static CandleStore getStore(Inventory view) {
        return VIEW_MAP.get(view);
    }

    public static void addToStore(Dispenser dispenser, Dispenser partner, boolean hasChest, String memberType) {
        PersistentDataContainer pdc = dispenser.getPersistentDataContainer();
        String key = UUID.randomUUID().toString();
        if (partner == null) {
            while (ALL_STORES.containsKey(key))
                key = UUID.randomUUID().toString();
            CandleStore store = CandleStore.createStore(key, hasChest, dispenser.getLocation().toVector());
            VIEW_MAP.put(store.getView(), store);
            ALL_STORES.put(key, store);
        } else {
            CandleStore store = Objects.requireNonNull(getStore(partner).orElse(null));
            store.addMember(dispenser.getLocation().toVector(), hasChest);
            key = store.getKey();
        }
        pdc.set(Keys.CANDLE_STORE, PersistentDataType.STRING, key);
        pdc.set(Keys.CANDLE_STORE_MEMBER_TYPE, PersistentDataType.STRING, memberType);
        dispenser.update();
    }

    public static void updateStoreMember(Dispenser dispenser, boolean chestAdded, boolean chestRemoved, String memberType) {
        PersistentDataContainer pdc = dispenser.getPersistentDataContainer();
        if (!pdc.has(Keys.CANDLE_STORE))
            return;
        String key = pdc.get(Keys.CANDLE_STORE, PersistentDataType.STRING);
        assert ALL_STORES.containsKey(key);
        CandleStore store = ALL_STORES.get(key);
        store.updateMember(chestAdded, chestRemoved);
        pdc.set(Keys.CANDLE_STORE_MEMBER_TYPE, PersistentDataType.STRING, memberType);
        dispenser.update();
    }

    public static void deleteFromStore(Dispenser dispenser, boolean hasChest) {
        PersistentDataContainer pdc = dispenser.getPersistentDataContainer();
        if (!pdc.has(Keys.CANDLE_STORE))
            return;
        String key = pdc.get(Keys.CANDLE_STORE, PersistentDataType.STRING);
        assert ALL_STORES.containsKey(key);
        CandleStore store = ALL_STORES.get(key);
        if (store.getMemberCount() > 1) {
            store.deleteMember(dispenser.getLocation().toVector(), hasChest);
        } else {
            ALL_STORES.remove(key);
            VIEW_MAP.remove(store.getView());
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
                    VIEW_MAP.put(store.getView(), store);
                    ALL_STORES.put(store.getKey(), store);
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void saveStoresOnPluginStop() {
        try {
            Gson gson = getGsonInstance();
            getStoresFile().delete();
            CandleStore[] toPersist = ALL_STORES.values().toArray(new CandleStore[0]);
            String json = gson.toJson(toPersist);
            FileWriter writer = new FileWriter(getStoresFile());
            writer.write(json);
            writer.close();
            ALL_STORES.clear();
            VIEW_MAP.clear();
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
            String key = obj.get("key").getAsString();
            int memberCount = obj.get("member_count").getAsInt();
            int storageSlots = obj.get("storage_slots").getAsInt();
            int usedSlots = obj.get("used_slots").getAsInt();
            double centerX = obj.get("center_x").getAsDouble();
            double centerY = obj.get("center_y").getAsDouble();
            double centerZ = obj.get("center_z").getAsDouble();

            EnumMap<Material, Long> storage = new EnumMap<>(Material.class);
            JsonObject storageObj = obj.getAsJsonObject("storage");
            for (Map.Entry<String, JsonElement> elem : storageObj.entrySet()) {
                Material mat = Material.getMaterial(elem.getKey());
                long amount = elem.getValue().getAsLong();
                storage.put(mat, amount);
            }

            return new CandleStore(key, memberCount, storageSlots, usedSlots, storage, new Vector(centerX, centerY, centerZ));
        }
    }
}
