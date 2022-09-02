package me.mfk1016.stadtserver.brewing;

import io.papermc.paper.potion.PotionMix;
import me.mfk1016.stadtserver.brewing.recipe.AdvancedPotionRecipe;
import me.mfk1016.stadtserver.brewing.recipe.BarrelRecipe;
import me.mfk1016.stadtserver.brewing.recipe.barrel.BarrelBeerRecipe;
import me.mfk1016.stadtserver.brewing.recipe.potion.*;
import me.mfk1016.stadtserver.util.Keys;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PotionRecipeManager implements Listener {

    private static final List<AdvancedPotionRecipe> recipes = new ArrayList<>();
    private static final List<BarrelRecipe> barrelRecipes = new ArrayList<>();

    public static void onPluginEnable() {
        recipes.clear();

        recipes.add(new SplashPotionRecipe());
        recipes.add(new LingerPotionRecipe());
        recipes.add(MultiPotionRecipe.customExtend());
        recipes.add(MultiPotionRecipe.customAmplify());
        recipes.add(MultiPotionRecipe.customCorrupt());

        recipes.add(new SinglePotionRecipe("base_stamina", Material.GLOW_BERRIES, "awkward", "haste:1_1"));
        recipes.add(new SinglePotionRecipe("base_shielding", Material.DIAMOND, "awkward", "resistance:1_1"));
        recipes.add(new WarpedFungusRecipe());
        recipes.add(new AmethystShardRecipe());

        recipes.add(new SpecialPotionRecipe("yeast_normal", Material.BROWN_MUSHROOM,
                new ItemStack(Material.HONEY_BOTTLE), "yeast_normal"));
        recipes.add(new SpecialPotionRecipe("yeast_nether", Material.CRIMSON_FUNGUS,
                new ItemStack(Material.HONEY_BOTTLE), "yeast_nether"));
        recipes.add(new SpecialPotionRecipe("yeast_end", Material.CHORUS_FRUIT,
                new ItemStack(Material.HONEY_BOTTLE), "yeast_end"));

        for (var recipe : recipes) {
            for (PotionMix mix : recipe.buildBaseRecipes()) {
                Bukkit.getServer().getPotionBrewer().removePotionMix(mix.getKey());
                Bukkit.getServer().getPotionBrewer().addPotionMix(mix);
            }
        }

        barrelRecipes.clear();
        barrelRecipes.add(new BarrelBeerRecipe("yeast_normal", Material.APPLE, "weizen_hell"));
        barrelRecipes.add(new BarrelBeerRecipe("yeast_end", Material.VINE, "bier_hell"));
        barrelRecipes.add(new BarrelBeerRecipe("yeast_nether", Material.POPPY, "weizen_dunkel"));
        barrelRecipes.add(new BarrelBeerRecipe("yeast_normal", Material.BEETROOT_SEEDS, "bier_dunkel"));
        barrelRecipes.add(new BarrelBeerRecipe("yeast_end", Material.FLOWERING_AZALEA, "bock_hell"));
        barrelRecipes.add(new BarrelBeerRecipe("yeast_nether", Material.GRASS, "bock_dunkel"));
    }

    public static Optional<BarrelRecipe> matchBarrelRecipe(Inventory barrel) {
        for (BarrelRecipe recipe : barrelRecipes) {
            if (recipe.isMatched(barrel))
                return Optional.of(recipe);
        }
        return Optional.empty();
    }


    @EventHandler(priority = EventPriority.NORMAL)
    public void onAdvancedBrewFinished(BrewEvent event) {
        for (AdvancedPotionRecipe recipe : recipes)
            if (recipe.isIngredientMatched(event)) {
                recipe.applyAdvancedRecipe(event);
                recipe.applyRandomOutput(event);
            }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onCustomLingerSplash(LingeringPotionSplashEvent event) {
        ItemStack input = event.getEntity().getItem();
        if (input.getType() != Material.LINGERING_POTION)
            return;
        if (PotionManager.matchCustomPotion(input).isEmpty())
            return;
        // Fix the linger potion effect duration to be equivalent to the correct potion description
        for (PotionEffect e : event.getAreaEffectCloud().getCustomEffects()) {
            if (e.getDuration() > 10) {
                PotionEffect fixed = new PotionEffect(e.getType(), e.getDuration() / 4, e.getAmplifier());
                event.getAreaEffectCloud().addCustomEffect(fixed, true);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDrinkSpecialPotion(PlayerItemConsumeEvent event) {
        ItemMeta meta = event.getItem().getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (!pdc.has(Keys.SPECIAL_POTION_ID))
            return;
        String specialPotionID = pdc.get(Keys.SPECIAL_POTION_ID, PersistentDataType.STRING);
        assert PotionManager.SPECIAL_POTION_TYPE.containsKey(specialPotionID);
        SpecialPotionType type = PotionManager.SPECIAL_POTION_TYPE.get(specialPotionID);
        type.onPlayerDrink(event.getPlayer());
    }
}
