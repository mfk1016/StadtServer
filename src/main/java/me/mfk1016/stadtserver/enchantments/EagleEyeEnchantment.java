package me.mfk1016.stadtserver.enchantments;

import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.util.Keys;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

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
