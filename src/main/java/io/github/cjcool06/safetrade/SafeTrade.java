package io.github.cjcool06.safetrade;

import com.google.inject.Inject;
import io.github.cjcool06.safetrade.commands.TradeCommand;
import io.github.cjcool06.safetrade.config.Config;
import io.github.cjcool06.safetrade.listeners.ConnectionListener;
import io.github.cjcool06.safetrade.listeners.TradeCreationListener;
import io.github.cjcool06.safetrade.listeners.TradeExecutedEvent;
import io.github.cjcool06.safetrade.listeners.ViewerConnectionListener;
import io.github.cjcool06.safetrade.managers.DataManager;
import io.github.cjcool06.safetrade.obj.Trade;
import io.github.cjcool06.safetrade.trackers.Tracker;
import ninja.leaping.configurate.objectmapping.GuiceObjectMapperFactory;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.event.service.ChangeServiceProviderEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.economy.EconomyService;

import java.io.File;
import java.util.concurrent.TimeUnit;

@Plugin(id = SafeTrade.ID,
        name = SafeTrade.NAME,
        version = SafeTrade.VERSION,
        description = SafeTrade.DESCRIPTION,
        authors = SafeTrade.AUTHORS,
        dependencies = @Dependency(id = "pixelmon")
)
public class SafeTrade {
    public static final String ID = "safetrade";
    public static final String NAME = "SafeTrade";
    public static final String VERSION = "2.0.1-SPONGE";
    public static final String DESCRIPTION = "Trade Pokemon, Items, and Money safely";
    public static final String AUTHORS = "CJcool06";
    private static SafeTrade plugin;
    private EconomyService economyService = null;

    @Inject
    private GuiceObjectMapperFactory factory;

    @Inject
    private Logger logger;

    @Inject
    private PluginContainer container;

    @Inject
    @ConfigDir(sharedRoot = false)
    private File configDir;

    @Listener
    public void onPreInit(GamePreInitializationEvent event) {
        plugin = this;

        Sponge.getEventManager().registerListeners(this, new ConnectionListener());
        Sponge.getEventManager().registerListeners(this, new ViewerConnectionListener());
        Sponge.getEventManager().registerListeners(this, new TradeCreationListener());
        Sponge.getEventManager().registerListeners(this, new TradeExecutedEvent());

        Sponge.getCommandManager().register(this, TradeCommand.getSpec(), "safetrade");

        Sponge.getServiceManager()
                .getRegistration(EconomyService.class)
                .ifPresent(prov -> economyService = prov.getProvider());

        logger.info("Loading configs.");
        Config.load();
    }

    @Listener
    public void onInit(GameInitializationEvent event) {
        Sponge.getScheduler().createTaskBuilder()
                .execute(() -> {
                    if (Config.gcLogsEnabled) {
                        int num = DataManager.recycleLogs();
                        if (num > 0) {
                            logger.info("Garbage Collector >> Removed " + num + " old logs.");
                        }
                    }
                }).async().delay(5, TimeUnit.MINUTES).interval(1, TimeUnit.HOURS).submit(this);
    }

    // Data load will cause errors if loaded before this event
    @Listener
    public void onPostInit(GameStartingServerEvent event) {
        logger.info("Loading data...");
        DataManager.load();
        logger.info("Data loaded.");
        logger.info("Economy plugin: " + (economyService == null ? "Not Found" : "Found"));
        if (economyService == null) {
            logger.warn("No economy plugin was found. Shit's gonna break.");
        }
    }

    @Listener
    public void onGameStopping(GameStoppingServerEvent event) {
        logger.info("Executing shutdown tasks.");
        for (Trade trade : Tracker.getAllActiveTrades()) {
            trade.unloadToStorages();
        }
        DataManager.save();
        logger.info("Shutdown tasks completed.");
    }

    @Listener
    public void onReload(GameReloadEvent event) {
        logger.info("Reloading config...");
        Config.load();
        logger.info("Config reloaded.");
    }

    @Listener
    public void onChangeServiceProvider(ChangeServiceProviderEvent event) {
        if (event.getService().equals(EconomyService.class)) {
            economyService = (EconomyService) event.getNewProviderRegistration().getProvider();
        }
    }

    public static EconomyService getEcoService() {
        return getPlugin().economyService;
    }

    public static SafeTrade getPlugin() {
        return plugin;
    }

    public static Logger getLogger() {
        return plugin.logger;
    }

    public static GuiceObjectMapperFactory getFactory() {
        return plugin.factory;
    }
}
