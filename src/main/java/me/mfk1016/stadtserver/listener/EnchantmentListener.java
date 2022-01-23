package me.mfk1016.stadtserver.listener;

import com.destroystokyo.paper.event.inventory.PrepareResultEvent;
import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.enchantments.EnchantmentManager;
import me.mfk1016.stadtserver.enchantments.SmithingEnchantment;
import me.mfk1016.stadtserver.logic.AncientTome;
import me.mfk1016.stadtserver.logic.AnvilLogic;
import me.mfk1016.stadtserver.origin.enchantment.FishBookOrigin;
import me.mfk1016.stadtserver.origin.enchantment.LootChestOrigin;
import me.mfk1016.stadtserver.origin.enchantment.PiglinTradeOrigin;
import me.mfk1016.stadtserver.origin.enchantment.VillagerTradeOrigin;
import me.mfk1016.stadtserver.util.Pair;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PiglinBarterEvent;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EnchantmentListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        AnvilLogic.fixAnvilResult(event);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPrepareGrindstone(PrepareResultEvent event) {
        AnvilLogic.fixGrindstoneResult(event);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onFishing(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH)
            return;
        ItemStack catchedItem = ((Item) Objects.requireNonNull(event.getCaught())).getItemStack();
        SmithingEnchantment.replaceMending(catchedItem, 4, 3);
        if (!(catchedItem.getItemMeta() instanceof EnchantmentStorageMeta))
            return;

        FishBookOrigin.matchOrigins().forEach((origin, level) ->
                EnchantmentManager.enchantItem(catchedItem, origin.getEnchantment(), level));
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onVillagerAquireTrade(VillagerAcquireTradeEvent event) {
        if (!(event.getEntity() instanceof Villager villager))
            return;

        MerchantRecipe recipe = event.getRecipe();
        ItemStack result = recipe.getResult();

        // Librarian items with mending are replaced with smithing 3
        boolean isMending = EnchantmentManager.isEnchantedWith(result, Enchantment.MENDING);
        if (isMending) {
            result = new ItemStack(Material.ENCHANTED_BOOK);
            EnchantmentManager.enchantItem(result, EnchantmentManager.SMITHING, 3);
            MerchantRecipe newRecipe = new MerchantRecipe(result, recipe.getUses(), recipe.getMaxUses(),
                    recipe.hasExperienceReward(), recipe.getVillagerExperience(),
                    recipe.getPriceMultiplier());
            newRecipe.setIngredients(recipe.getIngredients());
            event.setRecipe(newRecipe);
            return;
        }

        // Select the first possible trade matching the chance
        Pair<VillagerTradeOrigin, Integer> matched = VillagerTradeOrigin.matchOrigins(villager);
        if (matched != null) {
            result = new ItemStack(Material.ENCHANTED_BOOK);
            EnchantmentManager.enchantItem(result, matched._1.getEnchantment(), matched._2);
            MerchantRecipe newRecipe = new MerchantRecipe(result, recipe.getUses(), recipe.getMaxUses(),
                    recipe.hasExperienceReward(), recipe.getVillagerExperience(),
                    1f);
            ItemStack book = new ItemStack(Material.BOOK);
            ItemStack emeralds = new ItemStack(Material.EMERALD, matched._1.getCostForLevel(matched._2));
            newRecipe.setIngredients(List.of(book, emeralds));
            event.setRecipe(newRecipe);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPiglinBarter(PiglinBarterEvent event) {
        List<ItemStack> results = new ArrayList<>();
        PiglinTradeOrigin.matchOrigins().forEach((origin, level) -> {
            ItemStack toAdd = new ItemStack(Material.ENCHANTED_BOOK);
            EnchantmentManager.enchantItem(toAdd, origin.getEnchantment(), level);
            results.add(toAdd);
        });
        if (!results.isEmpty())
            event.getOutcome().add(results.get(StadtServer.RANDOM.nextInt(results.size())));
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onLootGenerate(LootGenerateEvent event) {
        if (!(event.getInventoryHolder() instanceof Chest || event.getEntity() instanceof Minecart))
            return;

        for (ItemStack item : event.getLoot()) {
            SmithingEnchantment.replaceMending(item, 4, 4);
        }

        LootChestOrigin.matchOrigins(event.getWorld()).forEach((origin, level) -> {
            ItemStack toAdd = new ItemStack(Material.ENCHANTED_BOOK);
            EnchantmentManager.enchantItem(toAdd, origin.getEnchantment(), level);
            event.getLoot().add(toAdd);
        });
        switch (event.getWorld().getEnvironment()) {
            case NETHER:
                if (StadtServer.RANDOM.nextInt(100) < 5)
                    event.getLoot().add(AncientTome.randomAncientTome());
                break;
            case THE_END:
                if (StadtServer.RANDOM.nextInt(100) < 25)
                    event.getLoot().add(AncientTome.randomAncientTome());
                break;
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerHitEntityWithBane(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player))
            return;
        if (!(event.getEntity() instanceof Creeper) && !(event.getEntity() instanceof Phantom))
            return;
        ItemStack weapon = player.getInventory().getItemInMainHand();
        if (!weapon.containsEnchantment(Enchantment.DAMAGE_ARTHROPODS))
            return;

        int level = weapon.getEnchantments().get(Enchantment.DAMAGE_ARTHROPODS);
        event.setDamage(event.getDamage() + (level * 2.5));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void tryAnvilInfinityWaterBucket(PrepareAnvilEvent event) {
        AnvilInventory anvil = event.getInventory();
        ItemStack target = anvil.getItem(0);
        ItemStack sacrifice = anvil.getItem(1);
        ItemStack result = event.getResult();

        // There must be an illegal state: target and sacrifice present but no result
        if (target == null || sacrifice == null || result != null)
            return;
        // The target must be a water bucket and the sacrifice must be a book with infinity
        if (target.getType() != Material.WATER_BUCKET || sacrifice.getType() != Material.ENCHANTED_BOOK)
            return;
        if (!EnchantmentManager.isEnchantedWith(sacrifice, Enchantment.ARROW_INFINITE))
            return;

        result = target.clone();
        result.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
        event.setResult(result);
        anvil.setRepairCost(5);
        AnvilLogic.doAnvilRename(event);
        anvil.setMaximumRepairCost(100);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onUseInfinityWaterBucket(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || !event.hasItem())
            return;
        ItemStack bucket = Objects.requireNonNull(event.getItem());
        if (bucket.getType() != Material.WATER_BUCKET || !bucket.containsEnchantment(Enchantment.ARROW_INFINITE))
            return;

        Block clicked = Objects.requireNonNull(event.getClickedBlock());
        Block target = clicked.getRelative(event.getBlockFace());
        target.setType(Material.WATER);
        target.getWorld().playSound(event.getPlayer().getLocation(), Sound.ITEM_BUCKET_EMPTY, 1f, 1f);
        event.setCancelled(true);
    }
}
