package me.mfk1016.stadtserver;

import me.mfk1016.stadtserver.candlestore.CandleStoreUtils;
import me.mfk1016.stadtserver.enchantments.CustomEnchantment;
import me.mfk1016.stadtserver.enchantments.EnchantmentManager;
import me.mfk1016.stadtserver.listener.BossMobListener;
import me.mfk1016.stadtserver.logic.AncientTome;
import me.mfk1016.stadtserver.logic.sorting.CategoryManager;
import me.mfk1016.stadtserver.origin.OriginManager;
import me.mfk1016.stadtserver.spells.CustomSpell;
import me.mfk1016.stadtserver.spells.SpellManager;
import me.mfk1016.stadtserver.util.BossName;
import me.mfk1016.stadtserver.util.library.BookManager;
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

import static me.mfk1016.stadtserver.util.Functions.stackEmpty;

public class StadtServerCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0)
            return false;
        String section = args[0];
        if (Objects.equals(section, "config")) {
            if (sender instanceof Player player) {
                String toDo = args[1];
                if (toDo.equals("reload")) {
                    StadtServer.getInstance().reloadConfig();
                    CategoryManager.initialize(true);
                    OriginManager.onPluginEnable(true);
                    player.sendMessage("Configuration reloaded.");
                    return true;
                } else if (toDo.equals("reset")) {
                    StadtServer.getInstance().saveResource("config.yml", true);
                    StadtServer.getInstance().saveResource("sorting.json", true);
                    StadtServer.getInstance().saveResource("origins.json", true);
                    StadtServer.getInstance().reloadConfig();
                    CategoryManager.initialize(true);
                    OriginManager.onPluginEnable(true);
                    player.sendMessage("Default configuration dumped and reloaded.");
                    return true;
                }
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
                onCommandAncientTome(player);
                return true;
            }
        } else if (Objects.equals(section, "spell")) {
            if (sender instanceof Player player) {
                return onCommandSpell(player, args);
            }
        } else if (Objects.equals(section, "candle_tool")) {
            if (sender instanceof Player player) {
                onCommandCandleTool(player);
                return true;
            }
        } else if (Objects.equals(section, "test_book")) {
            if (sender instanceof Player player) {
                onCommandTestBook(player);
                return true;
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
        player.sendMessage("Book of '" + enchStr + "' dropped.");
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
        BossMobListener.createBoss(mob, targetLevel, BossName.randomName());
        player.sendMessage("Boss spawned.");
        return true;
    }

    private void onCommandAncientTome(Player player) {
        player.getWorld().dropItem(player.getLocation(), AncientTome.randomAncientTome());
        player.sendMessage("Ancient tome dropped.");
    }

    private boolean onCommandSpell(Player player, String[] args) {
        if (args.length < 2)
            return false;

        int charges = 1;
        if (args.length > 2) {
            try {
                charges = Integer.parseInt(args[2]);
            } catch (NumberFormatException ignored) {
            }
        }

        String enchStr = args[1].toLowerCase();
        CustomSpell spell;
        switch (enchStr) {
            case "darkness":
                spell = SpellManager.DARKNESS;
                break;
            case "summon":
                spell = SpellManager.SUMMON;
                break;
            case "magnet":
                spell = SpellManager.MAGNET;
                break;
            default:
                return false;
        }
        charges = Math.min(Math.max(spell.getStartLevel(), charges), spell.getMaxLevel());

        ItemStack target = player.getInventory().getItemInMainHand();
        if (stackEmpty(target)) {
            target = new ItemStack(Material.STICK);
            player.getInventory().setItemInMainHand(target);
        }
        SpellManager.addSpell(target, spell, charges);
        player.sendMessage("Spell '" + enchStr + "' added.");
        return true;
    }

    private void onCommandCandleTool(Player player) {
        player.getWorld().dropItem(player.getLocation(), CandleStoreUtils.getCandleTool());
        player.sendMessage("Candle tool created.");
    }

    private void onCommandTestBook(Player player) {
        player.getInventory().addItem(BookManager.createBook("ritual:boss:sheep_witch"));
    }
}
