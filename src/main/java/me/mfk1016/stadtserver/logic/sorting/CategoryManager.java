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

public class CategoryManager {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static ItemCategory root = new ItemCategory("", "", null);

    public static int cmp(Material a, Material b) {
        return root.cmp(a, b);
    }

    public static void initialize(StadtServer plugin) {
        root = new ItemCategory("", "", null);
        File sortingConfig = new File(plugin.getDataFolder(), "sorting.json");
        if (!sortingConfig.exists())
            plugin.saveResource(sortingConfig.getName(), false);
        Type empMapType = new TypeToken<Map<String, List<String>>>() {
        }.getType();
        try {
            Map<String, List<String>> sortingData = gson.fromJson(new FileReader(sortingConfig), empMapType);
            // Validation
            List<String> order = sortingData.get("_order");
            for (String elem : order) {
                List<String> matnames = sortingData.get(elem);
                Material[] materials = new Material[matnames.size()];
                for (int i = 0; i < materials.length; i++) {
                    materials[i] = Material.matchMaterial(matnames.get(i));
                }
                root.addSubCategory(elem, materials);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
