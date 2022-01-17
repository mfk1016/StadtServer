package me.mfk1016.stadtserver;

import me.mfk1016.stadtserver.enchantments.CustomEnchantment;
import me.mfk1016.stadtserver.listener.BossMobListener;
import me.mfk1016.stadtserver.logic.AncientTome;
import me.mfk1016.stadtserver.logic.sorting.CategoryManager;
import me.mfk1016.stadtserver.util.BossName;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class StadtServerCommand implements CommandExecutor {

    private final StadtServer plugin;

    public StadtServerCommand(StadtServer p) {
        plugin = p;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0)
            return false;
        String section = args[0];
        if (Objects.equals(section, "config")) {
            String path = args[1];
            boolean value = Boolean.parseBoolean(args[2]);
            if (plugin.getConfig().contains(path)) {
                plugin.getConfig().set(path, value);
                return true;
            }
        } else if (Objects.equals(section, "book")) {
            if (sender instanceof Player player) {
                return onCommandBook(player, args);
            }
        } else if (Objects.equals(section, "boss")) {
            if (sender instanceof Player player) {
                return onCommandBoss(player, args);
            }
        } else if (Objects.equals(section, "ancient")) {
            if (sender instanceof Player player) {
                return onCommandAncientTome(player);
            }
        } else if (Objects.equals(section, "sort")) {
            boolean setDefault = args.length == 2 && args[1].equals("reset");
            if (sender instanceof Player player) {
                return onCommandSorting(setDefault);
            }
        }
        return false;
    }

    private boolean onCommandBook(Player player, String[] args) {
        if (args.length < 2)
            return false;

        int level = 1;
        if (args.length > 2) {
            try {
                level = Integer.parseInt(args[2]);
            } catch (NumberFormatException ignored) {
            }
        }

        String enchStr = args[1].toLowerCase();
        CustomEnchantment enchantment;
        switch (enchStr) {
            case "chopping":
                enchantment = EnchantmentManager.CHOPPING;
                break;
            case "smithing":
                enchantment = EnchantmentManager.SMITHING;
                break;
            case "eagle_eye":
                enchantment = EnchantmentManager.EAGLE_EYE;
                break;
            case "farming":
                enchantment = EnchantmentManager.FARMING;
                break;
            case "sacrificial":
                enchantment = EnchantmentManager.SACRIFICIAL;
                break;
            default:
                return false;
        }
        level = Math.min(Math.max(enchantment.getStartLevel(), level), enchantment.getMaxLevel());

        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantmentManager.enchantItem(book, enchantment, level);
        player.getWorld().dropItem(player.getLocation(), book);
        return true;
    }

    private boolean onCommandBoss(Player player, String[] args) {
        if (args.length < 3)
            return false;
        EntityType targetType;
        int targetLevel = 1;
        try {
            targetType = EntityType.valueOf(args[1].toUpperCase());
        } catch (Exception ignored) {
            return false;
        }
        try {
            targetLevel = Integer.parseInt(args[2]);
        } catch (NumberFormatException ignored) {
        }

        if (!BossMobListener.isValidBossMobType(targetType))
            return false;

        LivingEntity mob = (LivingEntity) player.getWorld().spawnEntity(player.getLocation(), targetType);
        BossMobListener.createBoss(plugin, mob, targetLevel, BossName.randomName(targetLevel));
        return true;
    }

    private boolean onCommandAncientTome(Player player) {
        player.getWorld().dropItem(player.getLocation(), AncientTome.randomAncientTome());
        return true;
    }

    private boolean onCommandSorting(boolean setDefault) {
        CategoryManager.initialize(plugin, setDefault);
        return true;
    }
}
