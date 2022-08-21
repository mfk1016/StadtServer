package me.mfk1016.stadtserver.util.json;

import com.google.gson.*;
import me.mfk1016.stadtserver.candlestore.CandleStore;
import org.bukkit.Material;
import org.bukkit.util.Vector;

import java.lang.reflect.Type;
import java.util.EnumMap;
import java.util.Map;

public class CandleStoreJSONAdapter implements JsonSerializer<CandleStore>, JsonDeserializer<CandleStore> {

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
