package me.mfk1016.stadtserver.util.json;

import com.google.gson.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.lang.reflect.Type;

public class BlockLocationJSONAdapter implements JsonSerializer<Location>, JsonDeserializer<Location> {

    @Override
    public JsonElement serialize(Location location, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();
        if (location.getWorld() != null) {
            obj.add("world", new JsonPrimitive(location.getWorld().getName()));
        }
        obj.add("x", new JsonPrimitive(location.getBlockX()));
        obj.add("y", new JsonPrimitive(location.getBlockY()));
        obj.add("z", new JsonPrimitive(location.getBlockZ()));
        return obj;
    }

    @Override
    public Location deserialize(JsonElement element, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject obj = element.getAsJsonObject();
        World world = null;
        if (obj.has("world")) {
            world = Bukkit.getWorld(obj.get("world").getAsString());
            if (world == null) {
                throw new IllegalArgumentException("unknown world");
            }
        }
        int x = obj.get("x").getAsInt();
        int y = obj.get("y").getAsInt();
        int z = obj.get("z").getAsInt();
        return new Location(world, x, y, z);
    }
}
