package me.mfk1016.stadtserver.logic.sorting;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.mfk1016.stadtserver.util.Pair;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class ItemCategory {

    private final String name;
    private final String fullName;
    private Predicate<Material> memberFunction;
    private final List<ItemCategory> subcategories = new ArrayList<>();

    public List<Material> toDump = new ArrayList<>();

    public ItemCategory(@NotNull String name, @NotNull String namespace, @Nullable Predicate<Material> memberFunction) {
        this.name = name;
        this.fullName = namespace.isBlank() ? name : namespace + "." + name;
        this.memberFunction = Objects.requireNonNullElseGet(memberFunction, () -> (m) -> false);
    }

    public String getName() {
        return name;
    }

    public String getFullName() {
        return fullName;
    }

    public boolean isLeaf() {
        return subcategories.isEmpty();
    }

    public ItemCategory matchCategory(Material mat) {
        for (var cat : subcategories) {
            if (cat.isMember(mat))
                return cat.matchCategory(mat);
        }
        return this;
    }

    public int cmp(Material a, Material b) {
        if (isLeaf())
            return 0;
        int aidx = subcategories.size();
        int bidx = aidx;
        for (int i = 0; i < subcategories.size(); i++) {
            if (aidx <= i && bidx <= i)
                break;
            ItemCategory subcat = subcategories.get(i);
            if (i < aidx && subcat.isMember(a))
                aidx = i;
            if (i < bidx && subcat.isMember(b))
                bidx = i;
        }
        int result = Integer.compare(aidx, bidx);
        if (result == 0 && aidx == subcategories.size())
            return 0;
        return result == 0 ? subcategories.get(aidx).cmp(a, b) : result;
    }

    public void addSubCategory(@NotNull String name, @Nullable Predicate<Material> memberFunction) {
        String[] chain = name.split("\\.");
        if (chain.length == 1) {
            if (!isSubCategory(name))
                subcategories.add(new ItemCategory(name, fullName, memberFunction));
            else
                getSubCategory(name).setMemberFunction(memberFunction);
        } else {
            String subname = chain[0];
            String leftName = name.substring(subname.length() + 1);
            if (!isSubCategory(subname))
                subcategories.add(new ItemCategory(subname, fullName, null));
            getSubCategory(subname).addSubCategory(leftName, memberFunction);
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
        if (memberFunction.test(mat))
            return true;
        for (var cat : subcategories) {
            if (cat.isMember(mat))
                return true;
        }
        return false;
    }

    public void setMemberFunction(Predicate<Material> memberFunction) {
        this.memberFunction = memberFunction;
    }

    public List<Pair<String, List<String>>> toJson() {
        List<Pair<String, List<String>>> result = new ArrayList<>();
        for (var cat : subcategories) {
            result.addAll(cat.toJson());
        }
        if (!toDump.isEmpty()) {
            List<String> matkeys = toDump.stream().map((mat) -> mat.getKey().toString()).toList();
            result.add(new Pair<>(fullName, matkeys));
        }
        return result;
    }
}
