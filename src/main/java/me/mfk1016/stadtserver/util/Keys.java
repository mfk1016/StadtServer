package me.mfk1016.stadtserver.util;

import me.mfk1016.stadtserver.StadtServer;
import org.bukkit.NamespacedKey;

public class Keys {

    // Entitites: Strings

    // Contains the eagle eye level of the used bow/crossbow
    public static final String EAGLE_EYE_LEVEL = "eagle_level";

    // Contains the position of the shooter for distance calculation
    public static final String EAGLE_EYE_VECTOR = "eagle_vector";

    // Contains the boss level (boss only)
    public static final String IS_BOSS = "is_boss";

    // Contains the attached cart
    public static final String CART_ATTACHED = "cart_attached";

    // Contains X and Z coordiante of the last rail with booster/brake
    public static final String LAST_RAIL_X = "last_rail_x";
    public static final String LAST_RAIL_Z = "last_rail_z";

    // Contains a reference to the master lorry (= the initial lorry)
    public static final String CART_MASTER = "cart_master";

    // Contains a reference to the follower lorry (= the next lorry)
    public static final String CART_FOLLOWER = "cart_follower";

    // Blocks/Items: NamespacedKeys

    // Set if a block is in wrenched state
    private static final String IS_WRENCHED_s = "is_wrenched";
    // Set if an enchanted book is an ancient tome
    private static final String IS_ANCIENT_TOME_s = "is_ancient_tome";
    public static NamespacedKey IS_WRENCHED;
    public static NamespacedKey IS_ANCIENT_TOME;


    private static StadtServer plugin;

    public static void initialize(StadtServer p) {
        plugin = p;
        IS_WRENCHED = new NamespacedKey(plugin, IS_WRENCHED_s);
        IS_ANCIENT_TOME = new NamespacedKey(plugin, IS_ANCIENT_TOME_s);
    }
}
