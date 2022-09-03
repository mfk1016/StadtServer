package me.mfk1016.stadtserver;

import me.mfk1016.stadtserver.actor.BlockActorManager;
import me.mfk1016.stadtserver.brewing.PotionManager;
import me.mfk1016.stadtserver.brewing.PotionRecipeManager;
import me.mfk1016.stadtserver.candlestore.CandleStoreManager;
import me.mfk1016.stadtserver.enchantments.EnchantmentManager;
import me.mfk1016.stadtserver.listener.*;
import me.mfk1016.stadtserver.logic.sorting.CategoryManager;
import me.mfk1016.stadtserver.origin.OriginManager;
import me.mfk1016.stadtserver.ritual.RitualManager;
import me.mfk1016.stadtserver.ritual.type.boss.*;
import me.mfk1016.stadtserver.spells.SpellManager;
import me.mfk1016.stadtserver.util.Keys;
import me.mfk1016.stadtserver.util.library.BookManager;
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
        LOGGER.info(getDescription().getName() + ": load configuration...");
        saveDefaultConfig();
        Keys.initialize();
        CategoryManager.initialize(false);

        LOGGER.info(getDescription().getName() + ": load data...");
        BlockActorManager.loadRegisteredBlocks();
        RitualManager.onPluginEnable();
        CandleStoreManager.onPluginEnable();
        BookManager.onPluginEnable();

        LOGGER.info(getDescription().getName() + ": load logic...");
        EnchantmentManager.onPluginEnable();
        SpellManager.onPluginEnable();
        PotionManager.onPluginEnable();
        PotionRecipeManager.onPluginEnable();
        OriginManager.onPluginEnable(false);

        RitualManager.registerRitualType(new WitchRitualType());
        RitualManager.registerRitualType(new PillagerRitualType());
        RitualManager.registerRitualType(new VindicatorRitualType());
        RitualManager.registerRitualType(new EvokerRitualType());
        RitualManager.registerRitualType(new WitherSkeletonRitualType());

        LOGGER.info(getDescription().getName() + ": enable listeners...");
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new BlockActorManager(), this);
        pm.registerEvents(new RitualManager(), this);
        pm.registerEvents(new MinecartListener(), this);
        pm.registerEvents(new SmallFunctionsListener(), this);
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
        HandlerList.unregisterAll(this);
        EnchantmentManager.onPluginDisable();
        SpellManager.onPluginDisable();
        LOGGER.info(getDescription().getName() + ": save config and persist data...");
        CandleStoreManager.onPluginDisable();
        RitualManager.onPluginDisable();
        BlockActorManager.onPluginDisable();
        saveConfig();
        LOGGER.info(getDescription().getName() + " " + getDescription().getVersion() + " stopped.");
    }

}
