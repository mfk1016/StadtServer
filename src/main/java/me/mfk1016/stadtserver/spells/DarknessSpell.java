package me.mfk1016.stadtserver.spells;

import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.logic.sorting.PluginCategories;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DarknessSpell extends CustomSpell {

    public DarknessSpell() {
        super("Darkness", "darkness");
    }

    @Override
    public Level spellLevel() {
        return Level.RARE;
    }

    @Override
    public List<Recipe> getRecipes() {
        return new ArrayList<>();
    }

    @Override
    public int getMaxCharges() {
        return 2;
    }

    @Override
    public boolean canEnchantItem(@NotNull ItemStack item) {
        return PluginCategories.isPickaxe(item.getType());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.SPECTATOR)
            return;
        ItemStack pickaxe = player.getInventory().getItemInMainHand();
        if (!PluginCategories.isPickaxe(pickaxe.getType()) || SpellManager.getSpellCharges(pickaxe, this) == 0)
            return;
        World world = player.getWorld();
        if (world.getEnvironment() != World.Environment.NORMAL || world.getTime() >= 12000)
            return;

        // Block must be glowstone and the sky must be visible (from the glowstone)
        Block focus = event.getBlock();
        if (focus.getRelative(BlockFace.UP).getLightFromSky() != 15 || focus.getType() != Material.GLOWSTONE)
            return;

        Location particleSpawn = focus.getLocation().toCenterLocation();
        world.playSound(particleSpawn, Sound.ENTITY_ILLUSIONER_CAST_SPELL, 4f, 1f);
        SpellManager.useSpellCharge(pickaxe, this);

        BukkitRunnable runner = new BukkitRunnable() {
            @Override
            public void run() {
                if (world.getTime() >= 18000) {
                    world.playSound(particleSpawn, Sound.ENTITY_WOLF_HOWL, 4f, 1f);
                    this.cancel();
                } else {
                    world.spawnParticle(Particle.FLAME, particleSpawn, 1, 0.4, 0.25, 0.4, 0.01);
                    world.setTime(Math.min(world.getTime() + 99, 18000));
                }
            }
        };
        runner.runTaskTimer(StadtServer.getInstance(), 1, 1);
    }
}
