package me.mfk1016.stadtserver.util;

import me.mfk1016.stadtserver.StadtServer;
import org.bukkit.NamespacedKey;

public class Keys {

    // Configuration
    public static final String CONFIG_PLAYER_MESSAGE = "send_player_message";
    public static final String CONFIG_MAX_LOGS = "chop_max_logs";

    // Entitites

    // Contains the eagle eye level of the used bow/crossbow
    private static final String EAGLE_EYE_LEVEL_s = "eagle_level";
    public static NamespacedKey EAGLE_EYE_LEVEL;

    // Contains the position of the shooter for distance calculation
    private static final String EAGLE_EYE_VECTOR_s = "eagle_vector";
    public static NamespacedKey EAGLE_EYE_VECTOR;

    // Contains the boss level (boss only)
    private static final String IS_BOSS_s = "is_boss";
    public static NamespacedKey IS_BOSS;

    // Contains the attached cart
    private static final String CART_ATTACHED_s = "cart_attached";
    public static NamespacedKey CART_ATTACHED;

    // Contains X and Z coordiante of the last rail with booster/brake
    private static final String LAST_RAIL_X_s = "last_rail_x";
    public static NamespacedKey LAST_RAIL_X;
    private static final String LAST_RAIL_Z_s = "last_rail_z";
    public static NamespacedKey LAST_RAIL_Z;

    // Contains a reference to the master lorry (= the initial lorry)
    private static final String CART_MASTER_s = "cart_master";
    public static NamespacedKey CART_MASTER;

    // Contains a reference to the follower lorry (= the next lorry)
    private static final String CART_FOLLOWER_s = "cart_follower";
    public static NamespacedKey CART_FOLLOWER;

    // Blocks/Items

    // Set if a block is in wrenched state
    private static final String IS_WRENCHED_s = "is_wrenched";
    public static NamespacedKey IS_WRENCHED;
    // Set if an enchanted book is an ancient tome
    private static final String IS_ANCIENT_TOME_s = "is_ancient_tome";
    public static NamespacedKey IS_ANCIENT_TOME;
    // Set if a netherite item is repaired through a ritual
    private static final String IS_RITUAL_REPAIRED_s = "is_ritual_repaired";
    public static NamespacedKey IS_RITUAL_REPAIRED;
    // Set to determine a special potion
    private static final String SPECIAL_POTION_ID_s = "special_potion_id";
    public static NamespacedKey SPECIAL_POTION_ID;
    // Set if the given Barrel is a fluid barrel
    private static final String IS_FLUID_BARREL_s = "is_fluid_barrel";
    public static NamespacedKey IS_FLUID_BARREL;
    // Contains the liquid type of the barrel
    private static final String BARREL_LIQUID_TYPE_s = "barrel_liquid_type";
    public static NamespacedKey BARREL_LIQUID_TYPE;
    // Contains the amount of liquid in the barrel
    private static final String BARREL_LIQUID_AMOUNT_s = "barrel_liquid_amount";
    public static NamespacedKey BARREL_LIQUID_AMOUNT;

    // Candle Store
    private static final String CANDLE_STORE_s = "candle_store";
    public static NamespacedKey CANDLE_STORE;
    private static final String CANDLE_STORE_MEMBER_TYPE_s = "candle_store_member_type";
    public static NamespacedKey CANDLE_STORE_MEMBER_TYPE;


    public static void initialize() {
        StadtServer.getInstance().getConfig().addDefault(CONFIG_PLAYER_MESSAGE, true);
        StadtServer.getInstance().getConfig().addDefault(CONFIG_MAX_LOGS, 1600);

        EAGLE_EYE_LEVEL = new NamespacedKey(StadtServer.getInstance(), EAGLE_EYE_LEVEL_s);
        EAGLE_EYE_VECTOR = new NamespacedKey(StadtServer.getInstance(), EAGLE_EYE_VECTOR_s);
        IS_BOSS = new NamespacedKey(StadtServer.getInstance(), IS_BOSS_s);
        CART_ATTACHED = new NamespacedKey(StadtServer.getInstance(), CART_ATTACHED_s);
        LAST_RAIL_X = new NamespacedKey(StadtServer.getInstance(), LAST_RAIL_X_s);
        LAST_RAIL_Z = new NamespacedKey(StadtServer.getInstance(), LAST_RAIL_Z_s);
        CART_MASTER = new NamespacedKey(StadtServer.getInstance(), CART_MASTER_s);
        CART_FOLLOWER = new NamespacedKey(StadtServer.getInstance(), CART_FOLLOWER_s);

        IS_WRENCHED = new NamespacedKey(StadtServer.getInstance(), IS_WRENCHED_s);
        IS_ANCIENT_TOME = new NamespacedKey(StadtServer.getInstance(), IS_ANCIENT_TOME_s);
        IS_RITUAL_REPAIRED = new NamespacedKey(StadtServer.getInstance(), IS_RITUAL_REPAIRED_s);
        SPECIAL_POTION_ID = new NamespacedKey(StadtServer.getInstance(), SPECIAL_POTION_ID_s);
        IS_FLUID_BARREL = new NamespacedKey(StadtServer.getInstance(), IS_FLUID_BARREL_s);
        BARREL_LIQUID_TYPE = new NamespacedKey(StadtServer.getInstance(), BARREL_LIQUID_TYPE_s);
        BARREL_LIQUID_AMOUNT = new NamespacedKey(StadtServer.getInstance(), BARREL_LIQUID_AMOUNT_s);

        CANDLE_STORE = new NamespacedKey(StadtServer.getInstance(), CANDLE_STORE_s);
        CANDLE_STORE_MEMBER_TYPE = new NamespacedKey(StadtServer.getInstance(), CANDLE_STORE_MEMBER_TYPE_s);
    }
}
