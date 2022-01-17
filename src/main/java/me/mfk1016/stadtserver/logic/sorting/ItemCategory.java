package me.mfk1016.stadtserver.logic.sorting;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ItemCategory {

    private final String name;
    private final String fullName;
    private Material[] materials;
    private final List<ItemCategory> subcategories = new ArrayList<>();

    public ItemCategory(@NotNull String name, @NotNull String namespace, @Nullable Material[] materials) {
        this.name = name;
        this.fullName = namespace.isBlank() ? name : namespace + "." + name;
        this.materials = materials;
    }

    public int cmp(Material a, Material b) {
        boolean memA, memB;
        for (var cat : subcategories) {
            memA = cat.isMember(a);
            memB = cat.isMember(b);
            if (memA && memB) {
                return cat.cmp(a, b);
            } else if (memA) {
                return -1;
            } else if (memB) {
                return 1;
            }
        }

        if (materials == null || materials.length == 0)
            return 0;
        int idxA = materials.length;
        int idxB = materials.length;
        for (int i = 0; i < materials.length; i++) {
            if (idxA <= i && idxB <= i)
                break;
            if (materials[i] == a)
                idxA = i;
            if (materials[i] == b)
                idxB = i;
        }
        return Integer.compare(idxA, idxB);
    }

    public void addSubCategory(@NotNull String name, @Nullable Material[] mats) {
        String[] chain = name.split("\\.");
        if (chain.length == 1) {
            if (!isSubCategory(name))
                subcategories.add(new ItemCategory(name, fullName, mats));
            else
                getSubCategory(name).setMaterials(mats);
        } else {
            String subname = chain[0];
            String leftName = name.substring(subname.length() + 1);
            if (!isSubCategory(subname))
                subcategories.add(new ItemCategory(subname, fullName, null));
            getSubCategory(subname).addSubCategory(leftName, mats);
        }
    }

    public boolean isSubCategory(String catname) {
        for (var cat : subcategories) {
            if (cat.name.equals(catname))
                return true;
        }
        return false;
    }

    public ItemCategory getSubCategory(String catname) {
        assert isSubCategory(catname);
        for (var cat : subcategories) {
            if (cat.name.equals(catname))
                return cat;
        }
        return this; // never reached
    }

    public boolean isMember(Material mat) {
        if (materials != null)
            for (var m : materials) {
                if (mat == m)
                    return true;
            }
        for (var cat : subcategories) {
            if (cat.isMember(mat))
                return true;
        }
        return false;
    }

    public void setMaterials(Material[] mats) {
        this.materials = mats;
    }
}
