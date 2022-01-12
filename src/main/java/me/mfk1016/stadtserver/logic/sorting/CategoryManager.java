package me.mfk1016.stadtserver.logic.sorting;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import me.mfk1016.stadtserver.StadtServer;
import org.bukkit.Material;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class CategoryManager {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static ItemCategory sortingRoot = new ItemCategory("", "", null);

    public static int sort(Material a, Material b) {
        return sortingRoot.cmp(a, b);
    }

    public static void initialize(StadtServer plugin, boolean setDefault) {
        sortingRoot = new ItemCategory("", "", null);
        File sortingConfig = new File(plugin.getDataFolder(), "sorting.json");
        if (!sortingConfig.exists()) {
            plugin.saveResource(sortingConfig.getName(), false);
        } else if (setDefault) {
            if (sortingConfig.delete())
                plugin.saveResource(sortingConfig.getName(), false);
        }
        Type empMapType = new TypeToken<Map<String, List<String>>>() {
        }.getType();
        try {
            Map<String, List<String>> sortingData = gson.fromJson(new FileReader(sortingConfig), empMapType);
            if (!sortingData.containsKey("_order")) {
                StadtServer.LOGGER.warning("Cannot load sorting.json: '_order' list missing");
                return;
            }
            List<String> order = sortingData.get("_order");
            for (String elem : order) {
                if (!sortingData.containsKey(elem)) {
                    StadtServer.LOGGER.warning("sorting.json: ignore missing category: " + elem);
                    continue;
                }
                List<String> matnames = sortingData.get(elem);
                Material[] materials = new Material[matnames.size()];
                for (int i = 0; i < materials.length; i++) {
                    Material mat = Material.matchMaterial(matnames.get(i));
                    if (mat != null)
                        materials[i] = mat;
                    else
                        StadtServer.LOGGER.warning("sorting.json: category " + elem + " ignored " + matnames.get(i));
                }
                sortingRoot.addSubCategory(elem, materials);
            }

        } catch (Exception e) {
            StadtServer.LOGGER.severe("sorting.json loading error: sorting categories not available");
            e.printStackTrace();
        }
    }
}
