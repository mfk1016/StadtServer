package me.mfk1016.stadtserver.util.json;

import com.google.gson.*;
import me.mfk1016.stadtserver.util.library.LibraryBook;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.meta.BookMeta;

import java.lang.reflect.Type;

import static me.mfk1016.stadtserver.util.Functions.undecoratedText;

public class LibraryBookJSONAdapter implements JsonDeserializer<LibraryBook> {

    @Override
    public LibraryBook deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        String key = obj.get("key").getAsString();
        String title = obj.get("title").getAsString();
        String author = obj.get("author").getAsString();

        String[][] pages = context.deserialize(obj.get("pages"), String[][].class);
        assert pages.length <= 100;

        BookMeta meta = (BookMeta) Bukkit.getItemFactory().getItemMeta(Material.WRITTEN_BOOK);
        meta.setTitle(title);
        meta.setAuthor(author);

        for (String[] page : pages) {
            assert page.length <= 14;
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < page.length; i++) {
                assert page[i].length() <= 19;
                builder.append(page[i]);
                if (i != page.length - 1)
                    builder.append("\n");
            }
            meta.addPages(undecoratedText(builder.toString()));
        }

        return new LibraryBook(key, meta);
    }
}
