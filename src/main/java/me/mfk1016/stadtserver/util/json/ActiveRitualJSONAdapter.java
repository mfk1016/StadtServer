package me.mfk1016.stadtserver.util.json;

import com.google.gson.*;
import me.mfk1016.stadtserver.ritual.RitualState;
import me.mfk1016.stadtserver.ritual.ActiveRitual;
import org.bukkit.Location;

import java.lang.reflect.Type;

public class ActiveRitualJSONAdapter implements JsonSerializer<ActiveRitual>, JsonDeserializer<ActiveRitual> {

    @Override
    public JsonElement serialize(ActiveRitual src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();
        obj.add("center", context.serialize(src.getCenter().getLocation(), Location.class));
        obj.add("tick", new JsonPrimitive(src.getTick()));
        obj.add("state", new JsonPrimitive(src.getState().ordinal()));
        obj.add("data", context.serialize(src.getData(), String[].class));
        return obj;
    }

    @Override
    public ActiveRitual deserialize(JsonElement element, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject obj = element.getAsJsonObject();
        Location centerLoc = context.deserialize(obj.get("center"), Location.class);
        int tick = obj.get("tick").getAsInt();
        RitualState state = RitualState.values()[obj.get("state").getAsInt()];
        String[] data = context.deserialize(obj.get("data"), String[].class);
        return new ActiveRitual(centerLoc.getBlock(), tick, state, data);
    }
}
