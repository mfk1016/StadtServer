package me.mfk1016.stadtserver.listener;

import com.destroystokyo.paper.event.inventory.PrepareResultEvent;
import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.enchantments.EnchantmentManager;
import me.mfk1016.stadtserver.enchantments.SmithingEnchantment;
import me.mfk1016.stadtserver.logic.AncientTome;
import me.mfk1016.stadtserver.logic.AnvilLogic;
import me.mfk1016.stadtserver.origin.loot.ChestEnchantmentOrigin;
import me.mfk1016.stadtserver.origin.loot.FishEnchantmentOrigin;
import me.mfk1016.stadtserver.origin.loot.LootOrigin;
import me.mfk1016.stadtserver.origin.loot.PiglinEnchantmentOrigin;
import me.mfk1016.stadtserver.origin.villager.VillagerOrigin;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
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

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static me.mfk1016.stadtserver.util.Functions.copyRecipe;

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
        Item catched = (Item) Objects.requireNonNull(event.getCaught());
        ItemStack catchedItem = catched.getItemStack();
        SmithingEnchantment.replaceMending(catchedItem, 4, 3);
        World world = event.getPlayer().getWorld();
        List<LootOrigin> origins = LootOrigin.match(FishEnchantmentOrigin.TYPE, Optional.of(catchedItem), world);
        if (origins.isEmpty())
            return;
        ItemStack result = origins.get(0).applyOrigin(Optional.of(catchedItem));
        catched.setItemStack(result);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onVillagerAquireTrade(VillagerAcquireTradeEvent event) {
        if (!(event.getEntity() instanceof Villager villager))
            return;

        MerchantRecipe recipe = event.getRecipe();
        ItemStack result = recipe.getResult();

        // Librarian items with mending are replaced with smithing 4
        if (EnchantmentManager.isEnchantedWith(result, Enchantment.MENDING)) {
            EnchantmentManager.disenchantItem(result, Enchantment.MENDING);
            EnchantmentManager.enchantItem(result, EnchantmentManager.SMITHING, 4);
            MerchantRecipe newRecipe = copyRecipe(recipe, result);
            event.setRecipe(newRecipe);
            return;
        }

        // Select the first possible trade matching the chance
        Optional<VillagerOrigin> origin = VillagerOrigin.match(villager, recipe);
        if (origin.isEmpty())
            return;
        event.setRecipe(origin.get().applyOrigin(villager, recipe));
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPiglinBarter(PiglinBarterEvent event) {
        World world = event.getEntity().getWorld();
        List<LootOrigin> origins = LootOrigin.match(PiglinEnchantmentOrigin.TYPE, Optional.empty(), world);
        if (origins.isEmpty())
            return;
        ItemStack result = origins.get(0).applyOrigin(Optional.empty());
        event.getOutcome().clear();
        event.getOutcome().add(result);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onLootGenerate(LootGenerateEvent event) {
        if (!(event.getInventoryHolder() instanceof Chest || event.getEntity() instanceof Minecart))
            return;

        for (ItemStack item : event.getLoot()) {
            SmithingEnchantment.replaceMending(item, 4, 4);
        }

        List<LootOrigin> origins = LootOrigin.match(ChestEnchantmentOrigin.TYPE, Optional.empty(), event.getWorld());
        for (LootOrigin origin : origins)
            event.getLoot().add(origin.applyOrigin(Optional.empty()));

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
