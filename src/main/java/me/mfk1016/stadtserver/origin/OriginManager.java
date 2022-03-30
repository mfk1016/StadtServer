package me.mfk1016.stadtserver.origin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.origin.villager.VillagerOrigin;

import java.io.File;
import java.io.FileReader;

public class OriginManager {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void initialize(boolean setDefault) {
        File originsConfig = new File(StadtServer.getInstance().getDataFolder(), "origins.json");
        if (!originsConfig.exists()) {
            StadtServer.getInstance().saveResource(originsConfig.getName(), false);
        } else if (setDefault) {
            if (originsConfig.delete())
                StadtServer.getInstance().saveResource(originsConfig.getName(), false);
        }
        try {
            JsonObject object = gson.fromJson(new FileReader(originsConfig), JsonObject.class);
            VillagerOrigin.initialize(object, gson);
        } catch (Exception e) {
            StadtServer.LOGGER.severe("sorting.json loading error: sorting categories not available");
            e.printStackTrace();
        }
    }

}
