package me.mfk1016.stadtserver.util.library;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.util.json.LibraryBookJSONAdapter;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;

public class BookManager {

    private static final HashMap<String, LibraryBook> LIBRARY = new HashMap<>();

    public static ItemStack createBook(@NotNull String key) {
        assert LIBRARY.containsKey(key);
        return LIBRARY.get(key).createBook();
    }

    public static void onPluginEnable() {
        File libraryFile = getLibraryFile();
        StadtServer.getInstance().saveResource(libraryFile.getName(), true);
        try {
            Gson gson = getGsonInstance();
            LibraryBook[] loadedBooks = gson.fromJson(new FileReader(libraryFile), LibraryBook[].class);
            if (loadedBooks != null) {
                LIBRARY.clear();
                for (LibraryBook book : loadedBooks) {
                    LIBRARY.put(book.getKey(), book);
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static File getLibraryFile() {
        return new File(StadtServer.getInstance().getDataFolder(), "library.json");
    }

    private static Gson getGsonInstance() {
        GsonBuilder builder = new GsonBuilder().setPrettyPrinting();
        builder.registerTypeAdapter(LibraryBook.class, new LibraryBookJSONAdapter());
        return builder.create();
    }

}
