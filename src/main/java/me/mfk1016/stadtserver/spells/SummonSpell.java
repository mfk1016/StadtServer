package me.mfk1016.stadtserver.spells;

import me.mfk1016.stadtserver.StadtServer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static me.mfk1016.stadtserver.util.Functions.stackEmpty;

public class SummonSpell extends CustomSpell {

    public SummonSpell() {
        super("Summon", "summon");
    }

    @Override
    public Level spellLevel() {
        return Level.COMMON;
    }

    @Override
    public int getMaxCharges() {
        return 8;
    }

    @Override
    public boolean canEnchantItem(@NotNull ItemStack item) {
        return item.getType() == Material.CLOCK;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onUseClock(PlayerInteractEvent event) {
        ItemStack clock = event.getItem();
        if (stackEmpty(clock) || SpellManager.getSpellCharges(clock, this) == 0)
            return;
        Block focus = event.getClickedBlock();
        if (focus == null || focus.getType() != Material.PLAYER_HEAD)
            return;
        World world = focus.getWorld();
        if (!focus.getRelative(BlockFace.UP).isEmpty() || !focus.getRelative(0, 2, 0).isEmpty())
            return;
        Skull skull = (Skull) focus.getState();
        OfflinePlayer offlinePlayer = skull.getOwningPlayer();
        if (offlinePlayer == null || !offlinePlayer.isOnline())
            return;
        Player target = Objects.requireNonNull(offlinePlayer.getPlayer());
        if (target.getWorld() != world)
            return;

        world.playSound(event.getPlayer().getLocation(), Sound.ENTITY_ILLUSIONER_CAST_SPELL, 4f, 1f);
        world.playSound(target.getLocation(), Sound.ENTITY_WITCH_CELEBRATE, 4f, 1f);
        SpellManager.useSpellCharge(clock, this);
        summonPlayer(target, focus.getRelative(BlockFace.UP));
    }

    public void summonPlayer(Player player, Block focus) {
        World world = focus.getWorld();
        BukkitRunnable runner = new BukkitRunnable() {

            int duration = 2;

            final Location spawnFocus = focus.getLocation().toCenterLocation();
            final Location spawnFocus2 = focus.getRelative(BlockFace.UP).getLocation().toCenterLocation();

            @Override
            public void run() {
                if (duration == 90)
                    world.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 4f, 1f);
                if (duration == 100) {
                    world.playSound(focus.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 4f, 1f);
                    player.teleport(spawnFocus);
                    this.cancel();
                }
                world.spawnParticle(Particle.PORTAL, spawnFocus, 2, 0.5, 0.4, 0.5, 0.01);
                world.spawnParticle(Particle.PORTAL, spawnFocus2, 2, 0.5, 0.4, 0.5, 0.01);
                world.spawnParticle(Particle.PORTAL, player.getLocation(), 2, 0.5, 0.4, 0.5, 0.01);
                world.spawnParticle(Particle.PORTAL, player.getLocation().clone().add(0, 1, 0), 2, 0.5, 0.4, 0.5, 0.01);
                duration += 2;
            }
        };
        runner.runTaskTimer(StadtServer.getInstance(), 1, 2);
    }

    private static NamespacedKey summonRecipeKey() {
        return new NamespacedKey(StadtServer.getInstance(), "spell_summon");
    }

    private static ShapedRecipe spellCraftingRecipe() {
        ItemStack result = new ItemStack(Material.COMPASS, 1);
        ShapedRecipe recipe = new ShapedRecipe(summonRecipeKey(), result);
        recipe.shape("-P-", "XCX", "-P-");
        recipe.setIngredient('P', Material.ENDER_PEARL);
        recipe.setIngredient('C', Material.COMPASS);
        recipe.setIngredient('X', new RecipeChoice.MaterialChoice(Material.POPPY, Material.DANDELION));
        return recipe;
    }

    @Override
    public List<Recipe> getRecipes() {
        List<Recipe> result = new ArrayList<>();
        result.add(spellCraftingRecipe());
        return result;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onCraftSummonSpell(PrepareItemCraftEvent event) {
        if (!(event.getRecipe() instanceof ShapedRecipe summonrec))
            return;
        if (!summonrec.getKey().equals(summonRecipeKey()))
            return;
        CraftingInventory crafter = event.getInventory();
        ItemStack compass = Objects.requireNonNull(crafter.getMatrix()[4]); // middle slot
        int charges = SpellManager.getSpellCharges(compass, this) + 1;
        if (charges > getMaxCharges()) {
            crafter.setResult(null);
            return;
        }
        ItemStack result = crafter.getResult();
        SpellManager.addSpell(result, this, charges);
        crafter.setResult(result);
    }
}
