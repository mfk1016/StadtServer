package me.mfk1016.stadtserver.spells;

import me.mfk1016.stadtserver.StadtServer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static me.mfk1016.stadtserver.util.Functions.stackEmpty;

public class DestructorSpell extends CustomSpell {

    public DestructorSpell() { super("Destructor", "destructor"); }

    @Override
    public Level spellLevel() { return Level.COMMON; }

    @Override
    public int getMaxCharges() { return 8; }

    @Override
    public boolean canEnchantItem(@NotNull ItemStack item) { return item.getType() == Material.BLAZE_ROD; }

    private static NamespacedKey destructorRecipeKey() {
        return new NamespacedKey(StadtServer.getInstance(), "spell_destructor");
    }

    private static ShapedRecipe spellCraftingRecipe() {
        ItemStack result = new ItemStack(Material.BLAZE_ROD, 1);
        ShapedRecipe recipe = new ShapedRecipe(destructorRecipeKey(), result);
        recipe.shape("-A-", "EBE", "-D-");
        recipe.setIngredient('A', Material.AMETHYST_SHARD);
        recipe.setIngredient('B', Material.BLAZE_ROD);
        recipe.setIngredient('E', Material.EMERALD);
        recipe.setIngredient('D', Material.DIAMOND);
        return recipe;
    }

    @Override
    public List<Recipe> getRecipes() {
        List<Recipe> result = new ArrayList<>();
        result.add(spellCraftingRecipe());
        return result;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onCraftDestructorSpell(PrepareItemCraftEvent event) {
        if (!(event.getRecipe() instanceof ShapedRecipe summonrec))
            return;
        if (!summonrec.getKey().equals(destructorRecipeKey()))
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

    @EventHandler(priority = EventPriority.NORMAL)
    public void onDestructorUse(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        ItemStack rod = event.getItem();
        if (stackEmpty(rod) || SpellManager.getSpellCharges(rod, this) == 0)
            return;
        Block target = event.getClickedBlock();
        if (target == null || target.getType() != Material.BEDROCK || rod.getType() != Material.BLAZE_ROD)
            return;
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.SPECTATOR)
            return;
        SpellManager.useSpellCharge(rod, this);
        new BukkitRunnable() {
            @Override
            public void run() {
                player.getWorld().playSound(player.getLocation(), Sound.ITEM_FIRECHARGE_USE, 4f, 1f);
                target.setType(Material.AIR);
                player.getWorld().spawnParticle(Particle.FLAME, target.getLocation().toCenterLocation(), 8,
                        0.5, 0.5, 0.5, 0.01);
            }
        }.runTaskLater(StadtServer.getInstance(), 1L);
    }
}
