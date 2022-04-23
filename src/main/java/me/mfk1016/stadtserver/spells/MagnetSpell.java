package me.mfk1016.stadtserver.spells;

import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.logic.sorting.PluginCategories;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static me.mfk1016.stadtserver.util.Functions.stackEmpty;

public class MagnetSpell extends CustomSpell {

    public MagnetSpell() {
        super("Magnet", "magnet");
    }

    @Override
    public @NotNull Level spellLevel() {
        return Level.RARE;
    }

    @Override
    public List<Recipe> getRecipes() {
        return new ArrayList<>();
    }

    @Override
    public int getMaxCharges() {
        return 4;
    }

    @Override
    public boolean canEnchantItem(@NotNull ItemStack item) {
        return PluginCategories.isAxe(item.getType());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onMagnetUse(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        ItemStack axe = event.getItem();
        if (stackEmpty(axe) || SpellManager.getSpellCharges(axe, this) == 0)
            return;
        if (event.getClickedBlock() == null || !PluginCategories.isAxe(axe.getType()))
            return;
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.SPECTATOR)
            return;
        Location playerBlockLoc = player.getLocation().getBlock().getRelative(BlockFace.DOWN).getLocation();
        if (!Objects.requireNonNull(event.getClickedBlock()).getLocation().equals(playerBlockLoc))
            return;

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ILLUSIONER_CAST_SPELL, 4f, 1f);
        SpellManager.useSpellCharge(axe, this);
        Iterator<Entity> targets = player.getNearbyEntities(24, 24, 24).iterator();
        World world = player.getWorld();
        BukkitRunnable runner = new BukkitRunnable() {
            @Override
            public void run() {
                if (!targets.hasNext()) {
                    this.cancel();
                } else if (targets.next() instanceof Item item) {
                    item.teleport(player.getLocation());
                    Location particleLoc = player.getLocation().clone().add(0, 1, 0);
                    world.spawnParticle(Particle.SOUL, particleLoc, 1, 1, 0, 1, 0.01);
                }
            }
        };
        runner.runTaskTimer(StadtServer.getInstance(), 1, 2);
    }
}
