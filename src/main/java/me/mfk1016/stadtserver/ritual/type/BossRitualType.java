package me.mfk1016.stadtserver.ritual.type;

import lombok.Getter;
import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.listener.BossMobListener;
import me.mfk1016.stadtserver.ritual.RitualState;
import me.mfk1016.stadtserver.ritual.ActiveRitual;
import me.mfk1016.stadtserver.ritual.RitualManager;
import me.mfk1016.stadtserver.ritual.RitualType;
import me.mfk1016.stadtserver.ritual.matcher.SacrificeRitual;
import me.mfk1016.stadtserver.util.BossName;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public abstract class BossRitualType extends RitualType implements SacrificeRitual {

    protected final HashMap<Block, BossRitualCache> BOSS_CACHE = new HashMap<>();

    @Getter
    private final EntityType bossType;
    @Getter
    private final int spawnTick;

    public BossRitualType(String key, Material focusMaterial, EntityType bossType, int spawnTick) {
        super("boss:" + key, focusMaterial, 10);
        this.bossType = bossType;
        this.spawnTick = spawnTick;
    }

    @Override
    public void updateRitualCache(ActiveRitual ritual) {
        // data is always correct
        String[] data = ritual.getData();
        BossRitualCache cache = BOSS_CACHE.getOrDefault(ritual.getCenter(), new BossRitualCache());
        if (cache.bossBarKey == null && data.length >= 1) {
            cache.bossBarKey = new NamespacedKey(StadtServer.getInstance(), data[0]);
        }
        if (cache.bossBarKey != null && cache.bossBar == null) {
            cache.bossBar = Bukkit.getBossBar(cache.bossBarKey);
        }
        if (cache.boss == null && data.length == 2) {
            cache.boss = (LivingEntity) ritual.getCenter().getWorld().getEntity(UUID.fromString(data[1]));
        }
        if (!BOSS_CACHE.containsKey(ritual.getCenter()))
            BOSS_CACHE.put(ritual.getCenter(), cache);
    }

    @Override
    public void onRitualSpawn(ActiveRitual ritual) {
        super.onRitualSpawn(ritual);
        BossRitualCache cache = new BossRitualCache();
        cache.bossBarKey = new NamespacedKey(StadtServer.getInstance(), UUID.randomUUID().toString());
        cache.bossBar = Bukkit.createBossBar(cache.bossBarKey, BossName.randomName(), BarColor.PINK, BarStyle.SOLID);
        for (Player player : ritual.getCenter().getWorld().getNearbyPlayers(ritual.getCenter().getLocation(), 64)) {
            cache.bossBar.addPlayer(player);
        }
        cache.bossBar.setProgress(0D);
        cache.bossBar.setVisible(true);
        BOSS_CACHE.put(ritual.getCenter(), cache);
        String[] data = { cache.bossBarKey.getKey() };
        ritual.setData(data);
    }

    @Override
    public void onRitualStop(ActiveRitual ritual, boolean success) {
        super.onRitualStop(ritual, success);
        BossRitualCache cache = BOSS_CACHE.get(ritual.getCenter());
        if (cache == null)
            return;
        if (cache.boss != null)
            cache.boss.remove();
        if (cache.bossBar != null)
            cache.bossBar.setVisible(false);
        if (cache.bossBarKey != null)
            Bukkit.removeBossBar(cache.bossBarKey);
        BOSS_CACHE.remove(ritual.getCenter());
    }

    @Override
    public void onRitualTick(ActiveRitual activeRitual) {
        BossRitualCache cache = Objects.requireNonNull(BOSS_CACHE.get(activeRitual.getCenter()));

        if (cache.bossBar == null || (activeRitual.getState() == RitualState.RUNNING && cache.boss == null)) {
            RitualManager.stopRitual(this, activeRitual, false);

        } else if (activeRitual.getTick() < spawnTick) {
            cache.bossBar.setProgress((double) (activeRitual.getTick() + 1) / (double) spawnTick);

        } else if (activeRitual.getTick() == spawnTick) {
            cache.bossBar.setProgress(1D);
            Location bossSpawnLocation = activeRitual.getCenter().getLocation().add(0, 1, 0);
            cache.boss = (LivingEntity) Objects.requireNonNull(bossSpawnLocation.getWorld()).spawnEntity(bossSpawnLocation, bossType);
            BossMobListener.createBoss(cache.boss, 4, cache.bossBar.getTitle());
            bossSpawnLocation.getWorld().strikeLightningEffect(bossSpawnLocation);
            activeRitual.setState(RitualState.RUNNING);
            String[] data = { activeRitual.getData()[0], cache.boss.getUniqueId().toString() };
            activeRitual.setData(data);

        } else {
            AttributeInstance maxHealth = Objects.requireNonNull(cache.boss.getAttribute(Attribute.GENERIC_MAX_HEALTH));
            cache.bossBar.setProgress(cache.boss.getHealth() / maxHealth.getBaseValue());
            for (Player player : cache.bossBar.getPlayers()) {
                if (!player.isOnline() || player.isDead() || player.getLocation().distance(cache.boss.getLocation()) > 64)
                    cache.bossBar.removePlayer(player);
            }
            if (cache.boss.isDead() || cache.bossBar.getPlayers().isEmpty())
                RitualManager.stopRitual(this, activeRitual, cache.boss.isDead());
        }
    }

    protected static class BossRitualCache {
        public NamespacedKey bossBarKey = null;
        public BossBar bossBar = null;
        public LivingEntity boss = null;
    }
}
