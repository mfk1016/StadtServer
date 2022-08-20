package me.mfk1016.stadtserver.enchantments;

import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.logic.sorting.PluginCategories;
import org.bukkit.Axis;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.*;
import org.bukkit.block.data.type.Slab;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.SmithingInventory;
import org.bukkit.inventory.meta.Damageable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static me.mfk1016.stadtserver.util.Functions.stackEmpty;

public class TrowelEnchantment extends CustomEnchantment {

    public TrowelEnchantment() {
        super("Trowel", "trowel");
    }

    @Override
    public int getMaxLevel() {
        return 1;
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
    public boolean conflictsWith(@NotNull Enchantment other) {
        return other.equals(EnchantmentManager.WRENCH);
    }

    @Override
    public boolean canEnchantItem(ItemStack item) {
        return PluginCategories.isShovel(item.getType());
    }

    @Override
    public int getAnvilCost(ItemStack sacrifice, int level) {
        return 1;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerTrowelBlock(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getItem() == null || event.useInteractedBlock() == Event.Result.DENY)
            return;
        if (!EnchantmentManager.isEnchantedWith(event.getItem(), this))
            return;

        Block target = Objects.requireNonNull(event.getClickedBlock()).getRelative(event.getBlockFace());
        if (!isValidTarget(target))
            return;
        boolean isWater = target.getType() == Material.WATER;

        Player player = event.getPlayer();
        PlayerInventory inventory = player.getInventory();
        List<Integer> hotbarSlots = getValidHotbarSlots(inventory);
        if (hotbarSlots.isEmpty())
            return;
        int slot = hotbarSlots.get(StadtServer.RANDOM.nextInt(hotbarSlots.size()));
        ItemStack toPlace = Objects.requireNonNull(inventory.getItem(slot));
        boolean placeUpside = player.getLocation().getBlockY() < target.getLocation().getBlockY() - 1;
        target.setType(toPlace.getType());
        BlockData targetData = target.getBlockData();
        if (targetData instanceof Orientable orientable) {
            List<Axis> axes = orientable.getAxes().stream().toList();
            orientable.setAxis(axes.get(StadtServer.RANDOM.nextInt(axes.size())));
        }
        if (targetData instanceof Directional directional) {
            List<BlockFace> faces = directional.getFaces().stream().toList();
            directional.setFacing(faces.get(StadtServer.RANDOM.nextInt(faces.size())));
        }
        if (targetData instanceof Bisected bisected) {
            bisected.setHalf(placeUpside ? Bisected.Half.TOP : Bisected.Half.BOTTOM);
        }
        if (targetData instanceof Slab slab) {
            slab.setType(placeUpside ? Slab.Type.TOP : Slab.Type.BOTTOM);
        }
        if (targetData instanceof Waterlogged waterlogged) {
            waterlogged.setWaterlogged(isWater);
        }
        target.setBlockData(targetData);

        target.getWorld().playSound(target.getLocation(), Sound.BLOCK_GRASS_PLACE, 1f, 1f);
        if (player.getGameMode() == GameMode.SURVIVAL) {
            toPlace.setAmount(toPlace.getAmount() - 1);
            toPlace = toPlace.getAmount() == 0 ? null : toPlace;
            inventory.setItem(slot, toPlace);
            damageTrowel(event.getItem());
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onTrowelRecipe(PrepareSmithingEvent event) {
        SmithingInventory inventory = Objects.requireNonNull(event.getInventory());
        ItemStack base = inventory.getItem(0);
        ItemStack additive = inventory.getItem(1);
        ItemStack result = event.getResult();
        if (base == null || additive == null || additive.getType() != Material.GOLD_INGOT || result == null)
            return;
        if (EnchantmentManager.isEnchantedWith(base, EnchantmentManager.WRENCH)) {
            event.setResult(null);
            return;
        }
        if (!EnchantmentManager.isEnchantedWith(base, this)) {
            EnchantmentManager.enchantItem(result, this, 1);
            event.setResult(result);
        } else {
            event.setResult(null);
        }
    }

    private static boolean isValidTarget(Block block) {
        if (!block.isEmpty() && block.getType() != Material.WATER)
            return false;
        Collection<Entity> entities = block.getWorld().getNearbyEntities(block.getBoundingBox());
        for (Entity entity : entities) {
            if (!(entity instanceof Item)) return false; // a non-item entity is in the way
        }
        return true; // there are entities in the way, but all of them are items
    }

    private static final BlockFace[] horizontalFaces = {
            BlockFace.WEST, BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH
    };

    private static List<Integer> getValidHotbarSlots(PlayerInventory inventory) {
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i <= 8; i++) {
            ItemStack item = inventory.getItem(i);
            if (stackEmpty(item))
                continue;
            Material mat = Objects.requireNonNull(item.getType());
            String matName = mat.name();
            if (!mat.isBlock() || matName.contains("TORCH") || mat == Material.LANTERN || mat == Material.SOUL_LANTERN)
                continue;
            if (matName.contains("BUTTON") || matName.contains("ITEM_FRAME") || mat == Material.PAINTING)
                continue;
            if (matName.contains("_BED") || matName.contains("BANNER") || mat == Material.LEVER || matName.contains("_DOOR"))
                continue;
            result.add(i);
        }
        return result;
    }

    private static void damageTrowel(ItemStack trowel) {
        Map<Enchantment, Integer> enchants = EnchantmentManager.getItemEnchantments(trowel);
        int unbreakingFactor = enchants.getOrDefault(Enchantment.DURABILITY, 0) + 1;
        if (StadtServer.RANDOM.nextInt(unbreakingFactor) == 0) {
            Damageable trowelDamage = (Damageable) trowel.getItemMeta();
            trowelDamage.setDamage(trowelDamage.getDamage() + 1);
            trowel.setItemMeta(trowelDamage);
        }
    }
}
