package me.mfk1016.stadtserver.rituals.boss;

import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.listener.BossMobListener;
import me.mfk1016.stadtserver.rituals.BasicRitual;
import me.mfk1016.stadtserver.rituals.RitualManager;
import me.mfk1016.stadtserver.rituals.RitualState;
import me.mfk1016.stadtserver.util.BossName;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Objects;
import java.util.UUID;

public abstract class BossRitual extends BasicRitual {

    private final int spawnTime;
    private final String bossName;
    private final EntityType bossType;
    private final NamespacedKey bossBarKey;
    protected Location bossSpawnLocation;
    protected LivingEntity boss;
    private BossBar bossBar;
    private double bossMaxHealth;

    public BossRitual(Block focusBlock, int spawnTime, EntityType bossType) {
        super(focusBlock);
        this.spawnTime = spawnTime;
        this.bossType = bossType;
        bossName = BossName.randomName(4);
        bossBarKey = new NamespacedKey(StadtServer.getInstance(), UUID.randomUUID().toString());
    }

    @Override
    public void startRitual(Entity sacrifice) {
        if (!hasProperShape()) {
            cancelRitual();
            RitualManager.ritualStopped(this);
            return;
        }
        bossSpawnLocation = sacrifice.getLocation();
        bossBar = Bukkit.createBossBar(bossBarKey, bossName, BarColor.PINK, BarStyle.SOLID);
        for (Player player : sacrifice.getWorld().getNearbyPlayers(sacrifice.getLocation(), 64)) {
            bossBar.addPlayer(player);
        }
        bossBar.setProgress(0D);
        bossBar.setVisible(true);
        performRitual();
    }

    @Override
    public void onRitualCheck(int timer) {
        if (timer < spawnTime) {
            bossBar.setProgress((double) (timer + 1) / (double) spawnTime);
        } else if (timer == spawnTime) {
            boss = (LivingEntity) Objects.requireNonNull(bossSpawnLocation.getWorld()).spawnEntity(bossSpawnLocation, bossType);
            BossMobListener.createBoss(boss, 4, bossName);
            bossSpawnLocation.getWorld().strikeLightningEffect(bossSpawnLocation);
            AttributeInstance maxHealth = Objects.requireNonNull(boss.getAttribute(Attribute.GENERIC_MAX_HEALTH));
            bossMaxHealth = maxHealth.getBaseValue();
            bossBar.setProgress(1D);
            boss.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 12000, 1));
            boss.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 12000, 1));
            state = RitualState.RUNNING;
        } else {
            bossBar.setProgress(boss.getHealth() / bossMaxHealth);
            for (Player player : bossBar.getPlayers()) {
                if (!player.isOnline() || player.isDead() || player.getLocation().distance(boss.getLocation()) > 64)
                    bossBar.removePlayer(player);
            }

            if (boss.isDead()) {
                state = RitualState.SUCCESS;
            } else if (bossBar.getPlayers().isEmpty()) {
                state = RitualState.FAILURE;
            }
        }
    }

    private void deleteBossBar() {
        if (bossBar != null) {
            bossBar.setVisible(false);
            Bukkit.removeBossBar(bossBarKey);
        }
    }

    @Override
    public void onRitualSuccess() {
        deleteBossBar();
    }

    @Override
    public void onRitualFail() {
        if (boss != null)
            boss.remove();
        deleteBossBar();
    }

    @Override
    protected void onRitualCancel() {
        deleteBossBar();
    }
}
