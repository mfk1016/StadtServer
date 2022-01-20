package me.mfk1016.stadtserver.rituals;

import me.mfk1016.stadtserver.rituals.boss.EvokerBossRitual;
import me.mfk1016.stadtserver.rituals.boss.VindicatorPillagerBossRitual;
import me.mfk1016.stadtserver.rituals.boss.WitchBossRitual;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.HashSet;
import java.util.Set;

public class RitualManager {

    private static final Set<BasicRitual> active_rituals = new HashSet<>();

    public static boolean tryRitual(LivingEntity sacrifice, Block focus) {

        BasicRitual ritual = null;
        if (focus.getType() == Material.LAPIS_BLOCK) {
            if (sacrifice.getType() == EntityType.SHEEP) {
                ritual = new WitchBossRitual(focus);
            } else if (sacrifice.getType() == EntityType.PIG) {
                ritual = new VindicatorPillagerBossRitual(focus, EntityType.VINDICATOR);
            } else if (sacrifice.getType() == EntityType.COW) {
                ritual = new VindicatorPillagerBossRitual(focus, EntityType.PILLAGER);
            } else if (sacrifice.getType() == EntityType.VILLAGER) {
                ritual = new EvokerBossRitual(focus);
            }
        }

        if (ritual != null) {
            active_rituals.add(ritual);
            ritual.startRitual(sacrifice);
            return true;
        } else {
            return false;
        }

    }

    public static void ritualStopped(BasicRitual ritual) {
        active_rituals.remove(ritual);
    }

    public static void onPluginDisable() {
        for (var ritual : active_rituals) {
            ritual.cancelRitual();
        }
        active_rituals.clear();
    }

}
