package me.mfk1016.stadtserver;

import com.comphenix.protocol.ProtocolLibrary;
import me.mfk1016.stadtserver.enchantments.WrenchEnchantment;
import me.mfk1016.stadtserver.listener.*;
import me.mfk1016.stadtserver.logic.DispenserDropperLogic;
import me.mfk1016.stadtserver.logic.MinecartLogic;
import me.mfk1016.stadtserver.rituals.RitualManager;
import me.mfk1016.stadtserver.util.Keys;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.inventory.SmithingRecipe;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.Random;
import java.util.logging.Logger;

public class StadtServer extends JavaPlugin {

    // Plugin wide stuff
    public static final Logger LOGGER = Logger.getLogger("Minecraft");
    public static final Random RANDOM = new Random();

    // Singleton Logic elements
    private final MinecartLogic minecartLogic = new MinecartLogic(this);
    private final DispenserDropperLogic dispenserDropperLogic = new DispenserDropperLogic(this);

    // Listeners
    private final MinecartListener minecartListener = new MinecartListener(this, minecartLogic);
    private final SmallFunctionsListener smallFunctionsListener = new SmallFunctionsListener(this, dispenserDropperLogic);
    private final SignPacketListener signPacketListener = new SignPacketListener(this, smallFunctionsListener);
    private final EnchantmentListener enchantmentListener = new EnchantmentListener(this);
    private final BossMobListener bossMobListener = new BossMobListener(this);
    private final DoorListener doorListener = new DoorListener(this);
    private final BrewingListener brewingListener = new BrewingListener(this);

    public static void broadcastSound(Block block, Sound sound, float volume, float pitch) {
        block.getWorld().playSound(block.getLocation(), sound, volume, pitch);
    }

    @Override
    public void onEnable() {

        LOGGER.info(getDescription().getName() + ": check configuration...");
        saveDefaultConfig();
        Keys.initialize(this);

        LOGGER.info(getDescription().getName() + ": register enchantments...");
        EnchantmentManager.onPluginEnable(this);

        LOGGER.info(getDescription().getName() + ": enable listeners...");
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(this.minecartListener, this);
        pm.registerEvents(this.smallFunctionsListener, this);
        pm.registerEvents(this.enchantmentListener, this);
        pm.registerEvents(this.bossMobListener, this);
        pm.registerEvents(this.doorListener, this);
        pm.registerEvents(this.brewingListener, this);
        ProtocolLibrary.getProtocolManager().addPacketListener(signPacketListener);

        LOGGER.info(getDescription().getName() + ": register recipes...");
        for (SmithingRecipe wrenchRecipe : WrenchEnchantment.getWrenchRecipes(this)) {
            Bukkit.addRecipe(wrenchRecipe);
        }
        RecipeManager.registerRecipes(this);

        Objects.requireNonNull(getCommand("stadtserver")).setExecutor(new StadtServerCommand(this));
        LOGGER.info(getDescription().getName() + " " + getDescription().getVersion() + " started.");
    }

    @Override
    public void onDisable() {
        RitualManager.onPluginDisable();
        ProtocolLibrary.getProtocolManager().removePacketListener(signPacketListener);
        EnchantmentManager.onPluginDisable();
        RecipeManager.unregisterBrewingRecipes();
        saveConfig();
        LOGGER.info(getDescription().getName() + " " + getDescription().getVersion() + " stopped.");
    }

}
