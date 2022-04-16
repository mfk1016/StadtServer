package me.mfk1016.stadtserver.listener;

import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.logic.AncientTome;
import me.mfk1016.stadtserver.origin.mob.MobOrigin;
import me.mfk1016.stadtserver.util.BossName;
import me.mfk1016.stadtserver.util.Keys;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Objects;

public class BossMobListener implements Listener {

    public static boolean isValidBossMobType(EntityType type) {
        return switch (type) {
            case BLAZE, CREEPER, DROWNED, ENDERMAN, EVOKER, GUARDIAN, HUSK, PIGLIN, PIGLIN_BRUTE, PILLAGER, SHULKER,
                    SKELETON, SPIDER, STRAY, VINDICATOR, WITCH, WITHER_SKELETON, ZOMBIE,
                    ZOMBIE_VILLAGER, ZOMBIFIED_PIGLIN -> true;
            default -> false;
        };
    }

    public static void createBoss(LivingEntity mob, int level, String bossName) {
        mob.setMetadata(Keys.IS_BOSS, new FixedMetadataValue(StadtServer.getInstance(), level));

        // Boss health (in base max health): 2 / 3 / 4 / 5
        AttributeInstance maxHealth = Objects.requireNonNull(mob.getAttribute(Attribute.GENERIC_MAX_HEALTH));
        maxHealth.setBaseValue(maxHealth.getBaseValue() * (level + 1));
        mob.setHealth(mob.getHealth() * (level + 1));

        // Name the boss
        mob.setCustomName(bossName);
        mob.setCustomNameVisible(true);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBossSpawn(CreatureSpawnEvent event) {
        // No boss mobs from spawner
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER)
            return;

        LivingEntity mob = event.getEntity();
        Location loc = event.getLocation();
        if (!isValidBossMobType(mob.getType()))
            return;

        int bossChance = 8;
        if (mob.getWorld().getEnvironment() == World.Environment.NORMAL) {
            // Overworld: 5% chance, 10% below 20, 15% below -20
            bossChance = loc.getBlockY() < -20 ? 15 : (loc.getBlockY() < 20 ? 10 : 5);
        } else if (mob.getWorld().getEnvironment() == World.Environment.NETHER) {
            // Nether: 8% chance, no boss mobs above the nether roof
            bossChance = loc.getBlockY() > 125 ? 0 : bossChance;
        } else {
            // End: 8% chance, bo boss mobs while the dragon is alive
            if (Math.abs(loc.getBlockX()) < 1000 && Math.abs(loc.getBlockZ()) < 1000)
                for (LivingEntity e : mob.getWorld().getLivingEntities()) {
                    if (e instanceof EnderDragon) {
                        bossChance = 0;
                        break;
                    }
                }
        }
        if (bossChance == 0 || StadtServer.RANDOM.nextInt(100) > bossChance)
            return;

        // Boss level: 50% / 35% / 15% chance for level 1 - 3
        int rand = StadtServer.RANDOM.nextInt(100);
        int bossLevel = rand < 15 ? 3 : (rand < 50 ? 2 : 1);
        createBoss(mob, bossLevel, BossName.randomName(bossLevel));
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBossHit(EntityDamageByEntityEvent event) {
        if (!event.getDamager().hasMetadata(Keys.IS_BOSS))
            return;

        LivingEntity mob = (LivingEntity) event.getDamager();
        // Boss Damage (in base damage): 1,5 / 2 / 2,5 / 3
        int bossLevel = mob.getMetadata(Keys.IS_BOSS).get(0).asInt();
        double damageFactor = 1 + ((double) (bossLevel) / 2);
        Difficulty difficulty = mob.getWorld().getDifficulty();
        if (difficulty == Difficulty.NORMAL) {
            damageFactor *= 0.9D;
        } else if (difficulty == Difficulty.HARD) {
            damageFactor *= 0.75D;
        }
        event.setDamage(event.getDamage() * damageFactor);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBossProjectileHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Projectile projectile))
            return;
        if (!(projectile.getShooter() instanceof LivingEntity shooter))
            return;
        if (!shooter.hasMetadata(Keys.IS_BOSS))
            return;
        // Boss Damage (in base damage): 1,5 / 2 / 2,5 / 3
        int bossLevel = shooter.getMetadata(Keys.IS_BOSS).get(0).asInt();
        double damageFactor = 1 + ((double) (bossLevel) / 2);
        event.setDamage(event.getDamage() * damageFactor);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onCreeperBossExplode(EntityExplodeEvent event) {
        if (!(event.getEntity() instanceof Creeper creeper))
            return;
        if (!(event.getEntity().hasMetadata(Keys.IS_BOSS)))
            return;
        float bossLevel = creeper.getMetadata(Keys.IS_BOSS).get(0).asInt();
        float power = 3F + bossLevel / 2;
        if (creeper.isPowered())
            power += 3F;
        event.setCancelled(true);
        creeper.getWorld().createExplosion(creeper.getLocation(), power, false, true);
        creeper.remove();
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBossSunlightBurn(EntityCombustEvent event) {
        if (!event.getEntity().hasMetadata(Keys.IS_BOSS))
            return;
        if (event.getEntity().getWorld().getEnvironment() != World.Environment.NORMAL)
            return;
        if (event.getEntity().getLocation().getBlock().getLightFromSky() != 15)
            return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBossDeath(EntityDeathEvent event) {
        if (!event.getEntity().hasMetadata(Keys.IS_BOSS))
            return;
        int bossLevel = event.getEntity().getMetadata(Keys.IS_BOSS).get(0).asInt();
        if (event.getDroppedExp() == 0)
            return; // Not death by player

        event.setDroppedExp(event.getDroppedExp() * (5 * bossLevel));
        for (ItemStack drop : event.getDrops()) {
            if (drop.getMaxStackSize() > 1)
                drop.setAmount(drop.getAmount() * (bossLevel + 1));
        }

        MobOrigin.match(event.getEntity()).forEach((origin) -> {
            event.getDrops().add(origin.applyOrigin());
        });

        // Ancient Tome: 25% at level 3, 100% at level 4
        if (bossLevel == 4 || (bossLevel == 3 && StadtServer.RANDOM.nextInt(100) < 25))
            event.getDrops().add(AncientTome.randomAncientTome());
    }

    private static final EntityType[] spawnerWeights = {
            EntityType.ZOMBIE, EntityType.ZOMBIE,
            EntityType.SKELETON, EntityType.SKELETON,
            EntityType.SPIDER, EntityType.SPIDER,
            EntityType.CAVE_SPIDER, EntityType.CAVE_SPIDER,
            EntityType.BLAZE, EntityType.BLAZE,
            EntityType.SILVERFISH, EntityType.SILVERFISH,
            EntityType.CREEPER,
            EntityType.SLIME,
            EntityType.PILLAGER,
            EntityType.WITCH,
            EntityType.SHEEP, EntityType.SHEEP
    };

    @EventHandler(priority = EventPriority.NORMAL)
    public void onSpawnerPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR)
            return;
        Block spawner = event.getBlock();
        if (spawner.getType() != Material.SPAWNER)
            return;

        CreatureSpawner spawnerState = (CreatureSpawner) spawner.getState();
        EntityType targetType = spawnerWeights[StadtServer.RANDOM.nextInt(spawnerWeights.length)];
        spawnerState.setSpawnedType(targetType);
        spawnerState.update(true);
    }
}
