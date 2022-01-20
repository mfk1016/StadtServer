package me.mfk1016.stadtserver.enchantments;

import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.origin.enchantment.*;
import me.mfk1016.stadtserver.util.Keys;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class EagleEyeEnchantment extends CustomEnchantment {

    public EagleEyeEnchantment() {
        super("Eagle Eye", "eagle_eye");
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    public int getStartLevel() {
        return 1;
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.BREAKABLE;
    }

    @Override
    public boolean isCursed() {
        return false;
    }

    @Override
    public boolean conflictsWith(Enchantment other) {
        return other.equals(Enchantment.ARROW_INFINITE) ||
                other.equals(Enchantment.MULTISHOT);
    }

    @Override
    public boolean canEnchantItem(ItemStack item) {
        return item.getType() == Material.BOW || item.getType() == Material.CROSSBOW;
    }

    @Override
    public int getAnvilCost(ItemStack sacrifice, int level) {
        if (sacrifice.getType() == Material.ENCHANTED_BOOK) {
            return level;
        } else {
            return 2 * level;
        }
    }

    @Override
    public Set<EnchantmentOrigin> getOrigins() {
        Set<EnchantmentOrigin> result = new HashSet<>();

        // Librarian: 3% at villager level 1+ for Eagle Eye I / II with 2/1 distribution
        // Fletcher: 7% at villager level 3+ for Eagle Eye II / III with 2/1 distribution
        int[] levelChancesLibrarian = {2, 1, 0, 0, 0};
        int[] levelChancesFletcher = {0, 2, 1, 0, 0};
        int[] baseCosts = {15, 30, 45, 60, 75};
        result.add(new VillagerTradeOrigin(this, 3, levelChancesLibrarian, Villager.Profession.LIBRARIAN, 1, baseCosts));
        result.add(new VillagerTradeOrigin(this, 7, levelChancesFletcher, Villager.Profession.FLETCHER, 3, baseCosts));

        // Piglin: 3% chance for Eagle Eye III
        int[] levelChancesPiglin = {0, 0, 1, 0, 0};
        result.add(new PiglinTradeOrigin(this, 3, levelChancesPiglin));

        // Loot chest: 10% chance in the nether/end for Eagle Eye IV
        int[] levelChancesLoot = {0, 0, 0, 1, 0};
        result.add(new LootChestOrigin(this, 10, levelChancesLoot, World.Environment.NETHER));
        result.add(new LootChestOrigin(this, 10, levelChancesLoot, World.Environment.THE_END));

        // Boss Skeleton: 10% chance for Eagle Eye III / IV with distribution 2/1
        int[] levelChancesBoss = {0, 0, 2, 1, 0};
        result.add(new BossMobBookOrigin(this, 10, levelChancesBoss, EntityType.SKELETON, 2, World.Environment.NORMAL));
        result.add(new BossMobBookOrigin(this, 10, levelChancesBoss, EntityType.SKELETON, 2, World.Environment.NETHER));

        return result;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBowShoot(EntityShootBowEvent event) {
        ItemStack bow = event.getBow();
        if (bow == null || !EnchantmentManager.isEnchantedWith(bow, this))
            return;

        int level = bow.getEnchantmentLevel(this);
        Entity projectile = event.getProjectile();
        projectile.setMetadata(Keys.EAGLE_EYE_LEVEL, new FixedMetadataValue(StadtServer.getInstance(), level));
        Entity shooter = event.getEntity();
        projectile.setMetadata(Keys.EAGLE_EYE_VECTOR, new FixedMetadataValue(StadtServer.getInstance(), shooter.getLocation().toVector()));
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onProjectileHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Arrow))
            return;

        AbstractArrow projectile = (Arrow) event.getDamager();
        if (!projectile.hasMetadata(Keys.EAGLE_EYE_LEVEL))
            return;

        int eagleEyeLevel = getMetaEagleEyeLevel(projectile);
        Entity target = event.getEntity();
        double distance = target.getLocation().toVector().distance(getMetaEagleEyeVector(projectile));
        double effect = (distance - 30D) * eagleEyeLevel;
        double itemFactor = (projectile.isShotFromCrossbow() && effect > 0D) ? 1.25D : 1D;
        effect = Math.max(-25D, Math.min(effect, 100D)) * itemFactor / 50D;
        double damageEffect = Math.round(event.getDamage() * effect) / 2D;
        event.setDamage(event.getDamage() + damageEffect);
    }


    private int getMetaEagleEyeLevel(AbstractArrow arrow) {
        try {
            MetadataValue val = arrow.getMetadata(Keys.EAGLE_EYE_LEVEL).get(0);
            return val.asInt();
        } catch (Exception e) {
            return 0;
        }
    }

    private Vector getMetaEagleEyeVector(AbstractArrow arrow) {
        try {
            MetadataValue val = arrow.getMetadata(Keys.EAGLE_EYE_VECTOR).get(0);
            return (Vector) val.value();
        } catch (Exception e) {
            return new Vector();
        }
    }
}
