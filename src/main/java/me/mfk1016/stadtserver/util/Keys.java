package me.mfk1016.stadtserver.util;

import me.mfk1016.stadtserver.StadtServer;
import org.bukkit.NamespacedKey;

public class Keys {

    // Configuration
    public static final String CONFIG_PLAYER_MESSAGE = "send_player_message";
    public static final String CONFIG_MAX_LOGS = "chop_max_logs";
    public static final String CONFIG_CANDLE_STORE_CHEST_SLOTS = "candle_store.chest_slots";

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
    // Set if a netherite item is repaired through a ritual
    private static final String IS_RITUAL_REPAIRED_s = "is_ritual_repaired";
    // Set to determine a special potion
    private static final String SPECIAL_POTION_ID_s = "special_potion_id";
    // Set if the given Barrel is a fluid barrel
    private static final String IS_FLUID_BARREL_s = "is_fluid_barrel";
    // Contains the liquid type of the barrel
    private static final String BARREL_LIQUID_TYPE_s = "barrel_liquid_type";
    // Contains the amount of liquid in the barrel
    private static final String BARREL_LIQUID_AMOUNT_s = "barrel_liquid_amount";

    public static NamespacedKey IS_WRENCHED;
    public static NamespacedKey IS_ANCIENT_TOME;
    public static NamespacedKey IS_RITUAL_REPAIRED;
    public static NamespacedKey SPECIAL_POTION_ID;
    public static NamespacedKey IS_FLUID_BARREL;
    public static NamespacedKey BARREL_LIQUID_TYPE;
    public static NamespacedKey BARREL_LIQUID_AMOUNT;

    // Candle Store
    private static final String CANDLE_STORE_s = "candle_store";
    public static NamespacedKey CANDLE_STORE;
    private static final String CANDLE_STORE_MEMBER_TYPE_s = "candle_store_member_type";
    public static NamespacedKey CANDLE_STORE_MEMBER_TYPE;


    public static void initialize() {
        StadtServer.getInstance().getConfig().addDefault(CONFIG_PLAYER_MESSAGE, true);
        StadtServer.getInstance().getConfig().addDefault(CONFIG_MAX_LOGS, 1600);
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
