package me.mfk1016.stadtserver.enchantments;

import me.mfk1016.stadtserver.EnchantmentManager;
import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.logic.MaterialTypes;
import me.mfk1016.stadtserver.logic.wrench.WrenchAction;
import me.mfk1016.stadtserver.logic.wrench.WrenchActionStateChange;
import me.mfk1016.stadtserver.origin.enchantment.EnchantmentOrigin;
import me.mfk1016.stadtserver.util.Keys;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.SmithingInventory;
import org.bukkit.inventory.SmithingRecipe;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class WrenchEnchantment extends CustomEnchantment {

    private static final List<SmithingRecipe> wrenchRecipes = new ArrayList<>();

    public WrenchEnchantment(StadtServer plugin) {
        super(plugin, "Wrench", "wrench");
    }

    public static boolean isWrenched(TileState state) {
        PersistentDataContainer pdc = state.getPersistentDataContainer();
        int result = pdc.getOrDefault(Keys.IS_WRENCHED, PersistentDataType.INTEGER, 0);
        return result == 1;
    }

    public static List<SmithingRecipe> getWrenchRecipes(StadtServer plugin) {

        if (wrenchRecipes.size() != 0)
            return wrenchRecipes;

        RecipeChoice additive = new RecipeChoice.ExactChoice(new ItemStack(Material.COPPER_INGOT));
        for (Material shovelMaterial : Arrays.asList(Material.WOODEN_SHOVEL,
                Material.STONE_SHOVEL,
                Material.IRON_SHOVEL,
                Material.GOLDEN_SHOVEL,
                Material.DIAMOND_SHOVEL,
                Material.NETHERITE_SHOVEL)) {
            NamespacedKey recipeKey = new NamespacedKey(plugin, "wrenchRecipe_" + shovelMaterial.name());
            ItemStack shovel = new ItemStack(shovelMaterial);
            RecipeChoice base = new RecipeChoice.MaterialChoice(shovelMaterial);
            wrenchRecipes.add(new SmithingRecipe(recipeKey, shovel, base, additive));
        }
        return wrenchRecipes;
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
        return false;
    }

    @Override
    public boolean canEnchantItem(ItemStack item) {
        return MaterialTypes.isShovel(item.getType());
    }

    @Override
    public int getAnvilCost(ItemStack sacrifice, int level) {
        return 1;
    }

    @Override
    public Set<EnchantmentOrigin> getOrigins() {
        return new HashSet<>();
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerWrenchBlock(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getItem() == null)
            return;
        if (!EnchantmentManager.isEnchantedWith(event.getItem(), this))
            return;

        Player player = event.getPlayer();
        Block target = Objects.requireNonNull(event.getClickedBlock());
        WrenchAction action = WrenchAction.actionFactory(target, plugin);
        if (action instanceof WrenchActionStateChange)
            StadtServer.broadcastSound(target, Sound.BLOCK_LEVER_CLICK, 0.5F, 1);
        action.onWrenchBlock(player, target, event);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onWrenchRecipe(PrepareSmithingEvent event) {
        SmithingInventory inventory = Objects.requireNonNull(event.getInventory());
        ItemStack base = inventory.getItem(0);
        ItemStack additive = inventory.getItem(1);
        ItemStack result = event.getResult();
        if (base == null || additive == null || additive.getType() != Material.COPPER_INGOT || result == null)
            return;
        if (!EnchantmentManager.isEnchantedWith(base, this)) {
            EnchantmentManager.enchantItem(result, this, 1);
            event.setResult(result);
        } else {
            event.setResult(null);
        }
    }
}
