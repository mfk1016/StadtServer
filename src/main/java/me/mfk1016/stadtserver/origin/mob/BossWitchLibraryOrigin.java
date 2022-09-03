package me.mfk1016.stadtserver.origin.mob;

import com.google.gson.JsonObject;
import me.mfk1016.stadtserver.util.library.BookManager;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import static me.mfk1016.stadtserver.util.Functions.stackEmpty;

public class BossWitchLibraryOrigin extends BossMobOrigin {

    private final ItemStack book;

    public BossWitchLibraryOrigin(int chance, int minlevel, ItemStack book) {
        super(chance, EntityType.WITCH, minlevel, World.Environment.NORMAL);
        this.book = book;
        assert !stackEmpty(book);
    }

    @Override
    public ItemStack applyOrigin() {
        return book.clone();
    }

    public static BossWitchLibraryOrigin fromJson(JsonObject object) {
        int chance = object.get("chance").getAsInt();
        int level = object.get("minlevel").getAsInt();
        String key = object.get("key").getAsString();
        ItemStack item = BookManager.createBook(key);
        return new BossWitchLibraryOrigin(chance, level, item);
    }
}
