package me.mfk1016.stadtserver.util;

import me.mfk1016.stadtserver.StadtServer;
import org.bukkit.NamespacedKey;

public class Keys {

    // Configuration
    public static final String CONFIG_PLAYER_MESSAGE = "send_player_message";
    public static final String CONFIG_MAX_LOGS = "chop_max_logs";

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


    public static void initialize(StadtServer p) {
        p.getConfig().addDefault(CONFIG_PLAYER_MESSAGE, true);
        p.getConfig().addDefault(CONFIG_MAX_LOGS, 2400);
        IS_WRENCHED = new NamespacedKey(p, IS_WRENCHED_s);
        IS_ANCIENT_TOME = new NamespacedKey(p, IS_ANCIENT_TOME_s);
    }
}
