package me.mfk1016.stadtserver.enchantments;

import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.logic.sorting.PluginCategories;
import me.mfk1016.stadtserver.ritual.matcher.SacrificeRitual;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static me.mfk1016.stadtserver.util.Functions.stackEmpty;

public class SacrificialEnchantment extends CustomEnchantment {

    public SacrificialEnchantment() {
        super("Sacrificial", "sacrificial");
    }

    @Override
    public int getMaxLevel() {
        return 1;
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
    public boolean conflictsWith(@NotNull Enchantment other) {
        return false;
    }

    @Override
    public boolean canEnchantItem(@NotNull ItemStack item) {
        return PluginCategories.isSword(item.getType()) || PluginCategories.isAxe(item.getType());
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
    public void onPlayerSacrifice(PlayerDeathEvent event) {
        Player sacrifice = event.getEntity();
        Player killer = sacrifice.getKiller();
        if (killer == null) {
            if (!(sacrifice.getLastDamageCause() instanceof EntityDamageByEntityEvent deathHitEvent))
                return;
            if (!(deathHitEvent.getDamager() instanceof Player player))
                return;
            killer = player;
        }

        ItemStack weapon = killer.getInventory().getItemInMainHand();
        if (stackEmpty(weapon) || !EnchantmentManager.isEnchantedWith(weapon, this))
            return;

        ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) Objects.requireNonNull(playerHead.getItemMeta());
        meta.setOwningPlayer(sacrifice);
        playerHead.setItemMeta(meta);
        event.getDrops().add(playerHead);
        event.deathMessage(Component.text(properDeathMessage(sacrifice, killer)));
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onMonsterDeath(EntityDeathEvent event) {
        LivingEntity sacrifice = event.getEntity();
        Material headType = getMobHead(sacrifice.getType());
        if (headType == null)
            return;
        Player killer = sacrifice.getKiller();
        if (killer == null) {
            if (!(sacrifice.getLastDamageCause() instanceof EntityDamageByEntityEvent deathHitEvent))
                return;
            if (!(deathHitEvent.getDamager() instanceof Player player))
                return;
            killer = player;
        }

        ItemStack weapon = killer.getInventory().getItemInMainHand();
        if (stackEmpty(weapon) || !EnchantmentManager.isEnchantedWith(weapon, this))
            return;

        // 10% Chance to drop a skull
        if (StadtServer.RANDOM.nextInt(100) > 10)
            return;
        ItemStack head = new ItemStack(headType);
        event.getDrops().add(head);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onHitAnimal(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Animals) && !(event.getEntity() instanceof Villager))
            return;
        if (!(event.getDamager() instanceof Player player))
            return;

        ItemStack weapon = player.getInventory().getItemInMainHand();
        if (stackEmpty(weapon) || !EnchantmentManager.isEnchantedWith(weapon, this))
            return;

        // Set the raw damage to max
        event.setDamage(100D);

        // Ritual start
        LivingEntity sacrifice = (LivingEntity) event.getEntity();
        Block block = event.getEntity().getLocation().getBlock();
        while (block.isEmpty() && block.getLocation().getBlockY() > 0) {
            block = block.getRelative(BlockFace.DOWN);
        }
        Block finalBlock = block;
        SacrificeRitual.matchRitualType(block, sacrifice).ifPresent(ritualType -> {
            me.mfk1016.stadtserver.ritual.RitualManager.spawnRitual(ritualType, finalBlock);
            sacrifice.remove();
            event.setCancelled(true);
        });
    }

    private String properDeathMessage(Player sacrifice, Player killer) {
        String message = " was sacrificed by ";
        return sacrifice.getName() + message + killer.getName();
    }

    private Material getMobHead(EntityType type) {
        return switch (type) {
            case SKELETON -> Material.SKELETON_SKULL;
            case ZOMBIE -> Material.ZOMBIE_HEAD;
            case CREEPER -> Material.CREEPER_HEAD;
            default -> null;
        };
    }
}
