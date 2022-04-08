package me.mfk1016.stadtserver;

import com.comphenix.protocol.ProtocolLibrary;
import me.mfk1016.stadtserver.brewing.PotionRecipeManager;
import me.mfk1016.stadtserver.brewing.PotionManager;
import me.mfk1016.stadtserver.enchantments.EnchantmentManager;
import me.mfk1016.stadtserver.listener.*;
import me.mfk1016.stadtserver.logic.sorting.CategoryManager;
import me.mfk1016.stadtserver.origin.OriginManager;
import me.mfk1016.stadtserver.rituals.RitualManager;
import me.mfk1016.stadtserver.spells.SpellManager;
import me.mfk1016.stadtserver.util.Keys;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.Random;
import java.util.logging.Logger;

public class StadtServer extends JavaPlugin {

    public static final Logger LOGGER = Logger.getLogger("Minecraft");
    public static final Random RANDOM = new Random();
    private static StadtServer plugin = null;
    private SignPacketListener signPacketListener = null;

    public static StadtServer getInstance() {
        return plugin;
    }

    @Override
    public void onEnable() {

        plugin = this;
        LOGGER.info(getDescription().getName() + ": check configuration...");
        saveDefaultConfig();
        Keys.initialize();
        CategoryManager.initialize(false);

        LOGGER.info(getDescription().getName() + ": register enchantments...");
        EnchantmentManager.onPluginEnable();
        LOGGER.info(getDescription().getName() + ": register spells...");
        SpellManager.onPluginEnable();
        LOGGER.info(getDescription().getName() + ": register potions...");
        PotionManager.initialize(false);
        PotionRecipeManager.initialize();
        LOGGER.info(getDescription().getName() + ": enable origins...");
        OriginManager.initialize(false);

        LOGGER.info(getDescription().getName() + ": enable listeners...");
        SmallFunctionsListener smallFunctionsListener = new SmallFunctionsListener();
        signPacketListener = new SignPacketListener(smallFunctionsListener);
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new MinecartListener(), this);
        pm.registerEvents(smallFunctionsListener, this);
        pm.registerEvents(new EnchantmentListener(), this);
        pm.registerEvents(new SpellListener(), this);
        pm.registerEvents(new BossMobListener(), this);
        pm.registerEvents(new DoorListener(), this);
        pm.registerEvents(new PotionRecipeManager(), this);
        pm.registerEvents(new TreeListener(), this);
        pm.registerEvents(new VineListener(), this);
        ProtocolLibrary.getProtocolManager().addPacketListener(signPacketListener);

        LOGGER.info(getDescription().getName() + ": register recipes...");
        RecipeManager.registerRecipes();

        Objects.requireNonNull(getCommand("stadtserver")).setExecutor(new StadtServerCommand());
        LOGGER.info(getDescription().getName() + " " + getDescription().getVersion() + " started.");
    }

    @Override
    public void onDisable() {
        RitualManager.onPluginDisable();
        HandlerList.unregisterAll(this);
        if (signPacketListener != null)
            ProtocolLibrary.getProtocolManager().removePacketListener(signPacketListener);
        signPacketListener = null;
        EnchantmentManager.onPluginDisable();
        SpellManager.onPluginDisable();
        //RecipeManager.unregisterBrewingRecipes();
        saveConfig();
        LOGGER.info(getDescription().getName() + " " + getDescription().getVersion() + " stopped.");
    }

}
