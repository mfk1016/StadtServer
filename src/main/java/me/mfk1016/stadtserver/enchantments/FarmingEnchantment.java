package me.mfk1016.stadtserver.enchantments;

import me.mfk1016.stadtserver.logic.sorting.PluginCategories;
import me.mfk1016.stadtserver.util.CropData;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    @EventHandler(priority = EventPriority.NORMAL)
    public void onHarvestMushroomBlock(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block target = event.getBlock();
        ItemStack hoe = player.getInventory().getItemInMainHand();
        if (!PluginCategories.isHoe(hoe.getType()) || !PluginCategories.isMushroomBlock(target.getType()))
            return;
        int farmingLevel = hoe.getEnchantments().getOrDefault(this, 0);
        if (farmingLevel == 0)
            return;
        if (event.getPlayer().isSneaking())
            farmingLevel = 1;

        int unbreakingFactor = hoe.getEnchantments().getOrDefault(Enchantment.DURABILITY, 0) + 1;
        List<Block> toHarvest = mushroomTargets(target, farmingLevel - 1);
        for (Block block : toHarvest) {
            block.breakNaturally(hoe);
        }
        if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
            int damage = toHarvest.size() / unbreakingFactor;
            Damageable hoeData = (Damageable) Objects.requireNonNull(hoe.getItemMeta());
            hoeData.setDamage(hoeData.getDamage() + damage);
            if (hoeData.getDamage() < hoe.getType().getMaxDurability())
                hoe.setItemMeta(hoeData);
            else
                player.getInventory().setItemInMainHand(null);
        }
        target.getWorld().playSound(target.getLocation(), Sound.BLOCK_WART_BLOCK_BREAK, 1f, 1f);
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onHoeUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack hoe = player.getInventory().getItemInMainHand();
        if (!PluginCategories.isHoe(hoe.getType()) || event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        int farmingLevel = hoe.getEnchantments().getOrDefault(this, 0);
        Block target = event.getClickedBlock();
        if (farmingLevel == 0 || target == null || !target.getRelative(BlockFace.UP).getType().isAir())
            return;

        if (event.getPlayer().isSneaking())
            farmingLevel = 1;
        boolean result = switch (target.getType()) {
            case GRASS_BLOCK, DIRT, DIRT_PATH -> onTillFarmland(player, hoe, target, farmingLevel);
            case WHEAT, POTATOES, CARROTS, BEETROOTS, NETHER_WART ->
                    onReplantNormalCrop(player, hoe, target, farmingLevel);
            case FARMLAND, SOUL_SAND -> onPlantNormalCrop(player, hoe, target, farmingLevel);
            default -> false;
        };
        if (result)
            event.setCancelled(true);
    }

    @SuppressWarnings("SameReturnValue")
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

    private List<Block> mushroomTargets(Block target, int farmingLevel) {
        List<Block> result = new ArrayList<>();
        int yLimit = farmingLevel * 2 + 1;
        result.add(target);
        for (int x = -farmingLevel; x <= farmingLevel; x++) {
            for (int y = 0; y < yLimit; y++) {
                for (int z = -farmingLevel; z <= farmingLevel; z++) {
                    if (x == 0 && y == 0 && z == 0)
                        continue;
                    Block toAdd = target.getRelative(x, y, z);
                    if (PluginCategories.isMushroomBlock(toAdd.getType()))
                        result.add(toAdd);
                }
            }
        }
        return result;
    }
}
