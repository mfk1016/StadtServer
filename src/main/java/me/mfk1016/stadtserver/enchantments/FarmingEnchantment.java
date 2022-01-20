package me.mfk1016.stadtserver.enchantments;

import me.mfk1016.stadtserver.logic.sorting.PluginCategories;
import me.mfk1016.stadtserver.origin.enchantment.*;
import me.mfk1016.stadtserver.util.CropData;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static me.mfk1016.stadtserver.util.Functions.stackEmpty;


public class FarmingEnchantment extends CustomEnchantment {

    public FarmingEnchantment() {
        super("Farming", "farming");
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public int getStartLevel() {
        return 1;
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TOOL;
    }

    @Override
    public boolean isCursed() {
        return false;
    }

    @Override
    public boolean conflictsWith(Enchantment other) {
        return other.equals(Enchantment.SILK_TOUCH) || other.equals(Enchantment.LUCK);
    }

    @Override
    public boolean canEnchantItem(@NotNull ItemStack item) {
        return PluginCategories.isHoe(item.getType());
    }

    @Override
    public int getAnvilCost(ItemStack sacrifice, int level) {
        if (sacrifice.getType() == Material.ENCHANTED_BOOK) {
            return level;
        } else {
            return 2 * level;
        }
    }

    @Override
    public Set<EnchantmentOrigin> getOrigins() {
        Set<EnchantmentOrigin> result = new HashSet<>();

        // Fishing: 15% for Farming I / II, distributed 3/2 when getting an enchanted book
        int[] levelChancesFish = {3, 2, 0};
        result.add(new FishBookOrigin(this, 15, levelChancesFish));

        // Librarian: 3% at villager level 1+ for Farming I / II with 3/2 distribution
        // Farmer: 7% at villager level 3+ for Farming III
        int[] levelChancesLibrarian = {3, 2, 0};
        int[] levelChancesFarmer = {0, 0, 1};
        int[] baseCosts = {10, 20, 30};
        result.add(new VillagerTradeOrigin(this, 3, levelChancesLibrarian, Villager.Profession.LIBRARIAN, 1, baseCosts));
        result.add(new VillagerTradeOrigin(this, 7, levelChancesFarmer, Villager.Profession.FARMER, 3, baseCosts));

        // Loot chest: 5% chance in the overworld for Farming II / III with 2/1 distribution
        int[] levelChancesLoot = {0, 2, 1};
        result.add(new LootChestOrigin(this, 5, levelChancesLoot, World.Environment.NORMAL));

        // Boss Zombie: 10% chance in the overworld for Farming III
        int[] levelChancesBoss = {0, 0, 1};
        result.add(new BossMobBookOrigin(this, 10, levelChancesBoss, EntityType.ZOMBIE, 1, World.Environment.NORMAL));
        result.add(new BossMobBookOrigin(this, 10, levelChancesBoss, EntityType.ZOMBIE_VILLAGER, 1, World.Environment.NORMAL));

        return result;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onHoeUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack hoe = player.getInventory().getItemInMainHand();
        if (stackEmpty(hoe) || !PluginCategories.isHoe(hoe.getType()) || event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        int farmingLevel = hoe.getEnchantments().getOrDefault(this, 0);
        Block target = event.getClickedBlock();
        if (farmingLevel == 0 || target == null || !target.getRelative(BlockFace.UP).getType().isAir())
            return;

        if (event.getPlayer().isSneaking())
            farmingLevel = 1;
        boolean result = switch (target.getType()) {
            case GRASS_BLOCK, DIRT, DIRT_PATH -> onTillFarmland(player, hoe, target, farmingLevel);
            case WHEAT, POTATOES, CARROTS, BEETROOTS -> onReplantNormalCrop(player, hoe, target, farmingLevel);
            case FARMLAND, SOUL_SAND -> onPlantNormalCrop(player, hoe, target, farmingLevel);
            default -> false;
        };
        if (result)
            event.setCancelled(true);
    }

    private boolean onTillFarmland(Player player, ItemStack hoe, Block target, int farmingLevel) {
        int unbreakingFactor = hoe.getEnchantments().getOrDefault(Enchantment.DURABILITY, 0) + 1;
        List<Block> toTill = farmingTargets(target, farmingLevel - 1, true);
        for (Block block : toTill) {
            block.setType(Material.FARMLAND);
        }
        if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
            int damage = toTill.size() / unbreakingFactor;
            Damageable hoeData = (Damageable) Objects.requireNonNull(hoe.getItemMeta());
            hoeData.setDamage(hoeData.getDamage() + damage);
            if (hoeData.getDamage() < hoe.getType().getMaxDurability())
                hoe.setItemMeta(hoeData);
            else
                player.getInventory().setItemInMainHand(null);
        }
        target.getWorld().playSound(target.getLocation(), Sound.ITEM_HOE_TILL, 1f, 1f);
        return true;
    }

    private boolean onReplantNormalCrop(Player player, ItemStack hoe, Block target, int farmingLevel) {
        int unbreakingFactor = hoe.getEnchantments().getOrDefault(Enchantment.DURABILITY, 0) + 1;
        List<Block> toReplant = farmingTargets(target, farmingLevel - 1, false);
        Material cropType = toReplant.get(0).getType();
        int replanted = 0;
        for (Block block : toReplant) {
            Ageable blockData = (Ageable) block.getBlockData();
            if (blockData.getAge() == blockData.getMaximumAge()) {
                block.breakNaturally(hoe);
                block.setType(cropType);
                replanted++;
            }
        }
        if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
            int damage = replanted / unbreakingFactor;
            Damageable hoeData = (Damageable) Objects.requireNonNull(hoe.getItemMeta());
            hoeData.setDamage(hoeData.getDamage() + damage);
            if (hoeData.getDamage() < hoe.getType().getMaxDurability())
                hoe.setItemMeta(hoeData);
            else
                player.getInventory().setItemInMainHand(null);
        }
        if (replanted != 0)
            target.getWorld().playSound(target.getLocation(), Sound.BLOCK_CROP_BREAK, 1f, 1f);
        return replanted != 0;
    }

    private boolean onPlantNormalCrop(Player player, ItemStack hoe, Block target, int farmingLevel) {
        int unbreakingFactor = hoe.getEnchantments().getOrDefault(Enchantment.DURABILITY, 0) + 1;
        if (!target.getRelative(BlockFace.UP).isEmpty())
            return false;
        List<Block> toPlant = farmingTargets(target, farmingLevel - 1, false);

        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (stackEmpty(offHand) || !CropData.isPlantable(offHand.getType()) || !CropData.isFarmland(offHand.getType(), target.getType()))
            return false;

        Material cropType = CropData.cropOfSeed(offHand.getType());
        int planted = 0;
        for (Block block : toPlant) {
            Block realTarget = block.getRelative(BlockFace.UP);
            if (!realTarget.isEmpty())
                continue;
            if (planted == offHand.getAmount())
                break;
            realTarget.setType(cropType);
            planted++;
        }
        offHand.setAmount(offHand.getAmount() - planted);
        if (offHand.getAmount() == 0)
            offHand = null;
        player.getInventory().setItemInOffHand(offHand);

        if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
            int damage = planted / unbreakingFactor;
            Damageable hoeData = (Damageable) Objects.requireNonNull(hoe.getItemMeta());
            hoeData.setDamage(hoeData.getDamage() + damage);
            if (hoeData.getDamage() < hoe.getType().getMaxDurability())
                hoe.setItemMeta(hoeData);
            else
                player.getInventory().setItemInMainHand(null);
        }
        if (planted != 0)
            target.getWorld().playSound(target.getLocation(), Sound.ITEM_CROP_PLANT, 1f, 1f);
        return planted != 0;
    }

    private List<Block> farmingTargets(Block clicked, int farmingLevel, boolean belowAir) {
        List<Block> result = new ArrayList<>();
        result.add(clicked);
        for (int x = -farmingLevel; x <= farmingLevel; x++) {
            for (int z = -farmingLevel; z <= farmingLevel; z++) {
                if (x == 0 && z == 0)
                    continue;
                Block toAdd = clicked.getRelative(x, 0, z);
                if (toAdd.getType() == clicked.getType() && (!belowAir || toAdd.getRelative(BlockFace.UP).getType().isAir()))
                    result.add(toAdd);
            }
        }
        return result;
    }
}
