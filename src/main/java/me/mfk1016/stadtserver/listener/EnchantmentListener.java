package me.mfk1016.stadtserver.listener;

import com.destroystokyo.paper.event.inventory.PrepareResultEvent;
import me.mfk1016.stadtserver.EnchantmentManager;
import me.mfk1016.stadtserver.StadtServer;
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
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static me.mfk1016.stadtserver.util.Functions.stackEmpty;

public class EnchantmentListener extends BasicListener {

    public EnchantmentListener(StadtServer p) {
        super(p);
    }

    /* --- ANVIL / GRINDSTONE --- */

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        AnvilInventory anvil = event.getInventory();
        ItemStack target = anvil.getItem(0);
        ItemStack sacrifice = anvil.getItem(1);

        if (stackEmpty(target))
            // No action without a target
            return;

        boolean isRenamed = false;
        boolean isRepaired = false;
        boolean isEnchanted = false;
        boolean isAncientTome = false;

        String newName = anvil.getRenameText();
        ItemMeta targetMeta = target.getItemMeta();
        if (targetMeta != null) {
            isRenamed = newName != null && !newName.isBlank() && !newName.equals(targetMeta.getDisplayName());
        }
        if (targetMeta instanceof Repairable && targetMeta instanceof Damageable damageable && !stackEmpty(sacrifice)) {
            isRepaired = damageable.hasDamage() && AnvilLogic.isRepairMaterial(target, sacrifice.getType());
        }
        if (!stackEmpty(sacrifice) && !EnchantmentManager.getItemEnchantments(sacrifice).isEmpty()) {
            isEnchanted = sacrifice.getType() == target.getType() || sacrifice.getType() == Material.ENCHANTED_BOOK;
            isAncientTome = target.getType() != Material.ENCHANTED_BOOK && AncientTome.isAncientTome(sacrifice);
        }
        if (!isRenamed && !isRepaired && !isEnchanted)
            return;

        anvil.setRepairCost(0);
        event.setResult(target.clone());
        if (isEnchanted) {
            boolean validResult = AnvilLogic.doAnvilEnchant(event);
            if (!validResult && !isRepaired && !isRenamed) {
                event.setResult(null);
                return;
            }
        }
        if (isRepaired)
            AnvilLogic.doAnvilRepair(event);
        if (isRenamed)
            AnvilLogic.doAnvilRename(event);

        // Add base costs and increase anvil penalty
        int baseCost = 0;
        if (isEnchanted && !isAncientTome) {
            Repairable targetRepairData = (Repairable) target.getItemMeta();
            baseCost += targetRepairData.getRepairCost();
            Repairable sacrificeRepairData = (Repairable) Objects.requireNonNull(sacrifice.getItemMeta());
            baseCost += sacrificeRepairData.getRepairCost();
        } else if (isRepaired) {
            Repairable targetRepairData = (Repairable) target.getItemMeta();
            baseCost += targetRepairData.getRepairCost();
        }
        anvil.setRepairCost(anvil.getRepairCost() + baseCost);
        ItemStack result = Objects.requireNonNull(event.getResult());
        if (!isAncientTome && (isEnchanted || isRepaired))
            AnvilLogic.increaseAnvilUses(result);
        event.setResult(result);

        if (isRepaired && !isEnchanted)
            SmithingEnchantment.tryApplySmithingEnchantment(event);
        anvil.setMaximumRepairCost(100);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPrepareGrindstone(PrepareResultEvent event) { EnchantmentManager.fixGrindstoneResult(event); }

    /* --- OBTAINING ENCHANTMENTS --- */

    @EventHandler(priority = EventPriority.NORMAL)
    public void onFishing(PlayerFishEvent event) {
        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            ItemStack catchedItem = ((Item) Objects.requireNonNull(event.getCaught())).getItemStack();

            SmithingEnchantment.replaceMending(catchedItem, 4, 3);

            if (!(catchedItem.getItemMeta() instanceof EnchantmentStorageMeta))
                return;
            FishBookOrigin.matchOrigins().forEach((origin, level) ->
                    EnchantmentManager.enchantItem(catchedItem, origin.getEnchantment(), level));
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onVillagerAquireTrade(VillagerAcquireTradeEvent event) {
        if (!(event.getEntity() instanceof Villager villager))
            return;
        if (villager.getProfession() != Villager.Profession.LIBRARIAN)
            return;

        MerchantRecipe recipe = event.getRecipe();
        ItemStack result = recipe.getResult();

        // Check the trade -> must be an enchanted book for emeralds
        if (result.getType() != Material.ENCHANTED_BOOK)
            return;
        int emeraldSlot = 0;
        if (recipe.getIngredients().get(1).getType() == Material.EMERALD) {
            emeraldSlot = 1;
        } else if (recipe.getIngredients().get(0).getType() != Material.EMERALD) {
            return;
        }

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
            newRecipe.setIngredients(recipe.getIngredients());
            newRecipe.getIngredients().get(emeraldSlot).setAmount(matched._1.getCostForLevel(StadtServer.RANDOM, matched._2));
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
        event.getOutcome().add(results.get(StadtServer.RANDOM.nextInt(results.size())));
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onLootGenerate(LootGenerateEvent event) {

        // This is for chests and minecarts with chests
        if (!(event.getInventoryHolder() instanceof Chest || event.getEntity() instanceof Minecart))
            return;

        for (ItemStack item : event.getLoot()) {
            // Replace Mending with smithing 4 on items and books
            SmithingEnchantment.replaceMending(item, 4, 4);
        }

        LootChestOrigin.matchOrigins(event.getWorld()).forEach((origin, level) -> {
            ItemStack toAdd = new ItemStack(Material.ENCHANTED_BOOK);
            EnchantmentManager.enchantItem(toAdd, origin.getEnchantment(), level);
            event.getLoot().add(toAdd);
        });

        // Ancient Tome: 5% Chance in the Nether, 25% Chance in the End
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

    /* --- BANE OF THE ATHROPODS BALANCE --- */

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerHitEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player))
            return;

        // Bane of the athropods should work on creepers and phantoms
        if (!(event.getEntity() instanceof Creeper) && !(event.getEntity() instanceof Phantom))
            return;

        ItemStack weapon = player.getInventory().getItemInMainHand();
        if (!weapon.containsEnchantment(Enchantment.DAMAGE_ARTHROPODS))
            return;

        int level = weapon.getEnchantments().get(Enchantment.DAMAGE_ARTHROPODS);
        event.setDamage(event.getDamage() + (level * 2.5));

    }

    /* --- WATER BUCKET WITH INFINITY --- */

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
        String possibleNewName = anvil.getRenameText();
        ItemMeta bucketMeta = Objects.requireNonNull(result.getItemMeta());
        // Fix a possible rename
        int renameCost = 0;
        if (possibleNewName != null && !possibleNewName.isBlank() && !possibleNewName.equals(bucketMeta.getDisplayName())) {
            bucketMeta.setDisplayName(possibleNewName);
            renameCost++;
        }
        result.setItemMeta(bucketMeta);
        // Enchant the bucket
        result.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
        event.setResult(result);

        // Set the repair cost
        anvil.setRepairCost(5 + renameCost);
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
