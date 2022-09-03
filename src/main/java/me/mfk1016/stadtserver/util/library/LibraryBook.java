package me.mfk1016.stadtserver.util.library;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

@RequiredArgsConstructor
public class LibraryBook {

    @Getter
    private final String key;

    private final BookMeta meta;

    public ItemStack createBook() {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        book.setItemMeta(meta.clone());
        return book;
    }
}
