package me.mfk1016.stadtserver;

import me.mfk1016.stadtserver.brewing.PotionManager;
import me.mfk1016.stadtserver.brewing.PotionRecipeManager;
import me.mfk1016.stadtserver.candlestore.CandleStoreManager;
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

        LOGGER.info(getDescription().getName() + ": load candle stores...");
        CandleStoreManager.loadStores();
        LOGGER.info(getDescription().getName() + ": register enchantments...");
        EnchantmentManager.onPluginEnable();
        LOGGER.info(getDescription().getName() + ": register spells...");
        SpellManager.onPluginEnable();
        LOGGER.info(getDescription().getName() + ": register potions...");
        PotionManager.initialize();
        PotionRecipeManager.initialize();
        LOGGER.info(getDescription().getName() + ": enable origins...");
        OriginManager.initialize(false);

        LOGGER.info(getDescription().getName() + ": enable listeners...");
        SmallFunctionsListener smallFunctionsListener = new SmallFunctionsListener();
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
        pm.registerEvents(new BarrelListener(), this);
        pm.registerEvents(new CandleStoreListener(), this);

        LOGGER.info(getDescription().getName() + ": register recipes...");
        RecipeManager.registerRecipes();

        Objects.requireNonNull(getCommand("stadtserver")).setExecutor(new StadtServerCommand());
        LOGGER.info(getDescription().getName() + " " + getDescription().getVersion() + " started.");
    }

    @Override
    public void onDisable() {
        LOGGER.info(getDescription().getName() + ": stop managers and handlers...");
        RitualManager.onPluginDisable();
        HandlerList.unregisterAll(this);
        EnchantmentManager.onPluginDisable();
        SpellManager.onPluginDisable();
        LOGGER.info(getDescription().getName() + ": save config and candle stores...");
        saveConfig();
        CandleStoreManager.saveStoresOnPluginStop();
        LOGGER.info(getDescription().getName() + " " + getDescription().getVersion() + " stopped.");
    }

}
