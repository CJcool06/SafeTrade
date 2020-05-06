package io.github.cjcool06.safetrade;

import com.google.inject.Inject;
import com.pixelmonmod.pixelmon.Pixelmon;
import io.github.cjcool06.safetrade.api.enums.PrefixType;
import io.github.cjcool06.safetrade.commands.TradeCommand;
import io.github.cjcool06.safetrade.config.Config;
import io.github.cjcool06.safetrade.listeners.*;
import io.github.cjcool06.safetrade.managers.DataManager;
import io.github.cjcool06.safetrade.obj.Trade;
import io.github.cjcool06.safetrade.trackers.Tracker;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import ninja.leaping.configurate.objectmapping.GuiceObjectMapperFactory;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.Player;
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
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;

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
    public static final String VERSION = "3.0.1";
    public static final String DESCRIPTION = "Trade Pokemon, Items, and Money safely";
    public static final String AUTHORS = "CJcool06";

    public static final EventBus EVENT_BUS = new EventBus();

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

        Pixelmon.EVENT_BUS.register(new EvolutionListener());

        EVENT_BUS.register(new TradeCreationListener());
        EVENT_BUS.register(new ViewerConnectionListener());
        EVENT_BUS.register(new TradeExecutedListener());
        EVENT_BUS.register(new TradeConnectionListener());
        logger.info("Listeners registered.");

        Sponge.getCommandManager().register(this, TradeCommand.getSpec(), "safetrade");
        logger.info("Commands registered.");

        Sponge.getServiceManager()
                .getRegistration(EconomyService.class)
                .ifPresent(prov -> economyService = prov.getProvider());

        logger.info("Economy service: " + (economyService != null ? "Found" : "Not Found"));
        if (economyService == null) {
            logger.warn("No economy service was found. Shit's gonna break.");
        }

        Config.load();
        logger.info("Config loaded.");
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
        DataManager.load();
        logger.info("Data loaded.");
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
        Config.load();
        logger.info("Config reloaded.");
    }

    @Listener
    public void onChangeServiceProvider(ChangeServiceProviderEvent event) {
        if (event.getService().equals(EconomyService.class)) {
            economyService = (EconomyService) event.getNewProviderRegistration().getProvider();
        }
    }

    /**
     * Gets sponge's current {@link EconomyService}.
     *
     * @return The service
     */
    public static EconomyService getEcoService() {
        return getPlugin().economyService;
    }

    /**
     * Gets the instance of this plugin.
     *
     * @return The instance
     */
    public static SafeTrade getPlugin() {
        return plugin;
    }

    /**
     * Gets the {@link Logger} of this plugin.
     *
     * @return The logger
     */
    public static Logger getLogger() {
        return plugin.logger;
    }

    /**
     * Gets the {@link GuiceObjectMapperFactory} of this plugin.
     *
     * @return The factory
     */
    public static GuiceObjectMapperFactory getFactory() {
        return plugin.factory;
    }

    /**
     * Sends a {@link Text} message to a {@link Player} adhering to SafeTrade's chat style.
     *
     * @param player The player to send to
     * @param prefixType The type of prefix to send the text with
     * @param text The message
     */
    public static void sendMessageToPlayer(Player player, PrefixType prefixType, Text text) {
        player.sendMessage(Text.of(prefixType.getPrefix(), text));
    }

    /**
     * Sends a {@link Text} message to a {@link Player} adhering to SafeTrade's chat style.
     *
     * <p>If the {@link CommandSource} is not a player, a prefix will not be sent.</p>
     *
     * @param src The source to send to
     * @param prefixType The type of prefix to send the text with
     * @param text The message
     */
    public static void sendMessageToCommandSource(CommandSource src, PrefixType prefixType, Text text) {
        if (src instanceof Player) {
            sendMessageToPlayer((Player)src, prefixType, text);
        }
        else {
            src.sendMessage(text);
        }
    }

    public static void sendMessageToAll(PrefixType prefixType, Text text) {
        MessageChannel.TO_ALL.send(Text.of(prefixType.getPrefix(), text));
    }
}
