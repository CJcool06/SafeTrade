package io.github.cjcool06.safetrade;

import com.google.inject.Inject;
import io.github.cjcool06.safetrade.api.enquiry.ListingBase;
import io.github.cjcool06.safetrade.api.enquiry.ListingRegistrar;
import io.github.cjcool06.safetrade.api.events.trade.TradeEndEvent;
import io.github.cjcool06.safetrade.commands.TradeCommand;
import io.github.cjcool06.safetrade.config.Config;
import io.github.cjcool06.safetrade.enums.TradeResult;
import io.github.cjcool06.safetrade.listeners.ChatListener;
import io.github.cjcool06.safetrade.listeners.ConnectionListener;
import io.github.cjcool06.safetrade.listings.ItemListing;
import io.github.cjcool06.safetrade.listings.PokemonListing;
import io.github.cjcool06.safetrade.managers.DataManager;
import io.github.cjcool06.safetrade.obj.Trade;
import net.minecraftforge.fml.common.eventhandler.EventBus;
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
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import java.io.File;
import java.util.ArrayList;
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
    public static final String VERSION = "1.1.6";
    public static final String DESCRIPTION = "Trade Pokemon and items safely";
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

        ListingRegistrar.register("pokemon", PokemonListing.class);
        ListingRegistrar.register("item", ItemListing.class);

        logger.info("Loading configs.");
        Config.load();
    }

    @Listener
    public void onInit(GameInitializationEvent event) {
        Sponge.getEventManager().registerListeners(this, new ConnectionListener());
        Sponge.getEventManager().registerListeners(this, new ChatListener());
        Sponge.getCommandManager().register(this, TradeCommand.getSpec(), "safetrade");

        Sponge.getServiceManager()
                .getRegistration(EconomyService.class)
                .ifPresent(prov -> economyService = prov.getProvider());

        logger.info("Economy plugin: " + (economyService == null ? "Not Found" : "Found"));
        if (economyService == null) {
            logger.warn("No economy plugin was found. Trades WILL break!");
        }

        // Logs GC
        Sponge.getScheduler().createTaskBuilder()
                .execute(() -> {
                    if (Config.gcLogsEnabled) {
                        int num = DataManager.recycleLogs();
                        if (num > 0) {
                            logger.info("Garbage Collector >> Removed " + num + " old logs.");
                        }
                    }
                }).async().delay(5, TimeUnit.MINUTES).interval(1, TimeUnit.HOURS).submit(this);

        // Listings GC
        Sponge.getScheduler().createTaskBuilder()
                .execute(() -> {
                    ArrayList<ListingBase> listings = new ArrayList<>(DataManager.getActiveListings());
                    for (ListingBase listing : listings) {
                        if (DataManager.hasListingExpired(listing)) {
                            listing.setExpired(true);
                            DataManager.removeListing(listing);
                            listing.getUser().getPlayer().ifPresent(player -> {
                                player.sendMessage(Text.builder()
                                        .append(Text.builder()
                                                .append(Text.of(TextColors.RED, "Your SafeTrade listing has expired."))
                                                .onHover(TextActions.showText(Text.joinWith(Text.of("\n"), listing.getDisplayLore())))
                                                .build())
                                        .append(Text.of(TextColors.GRAY, "  << Hover for info"))
                                        .build());
                            });

                        }
                    }
                }).async().interval(30, TimeUnit.SECONDS).submit(this);
    }

    // Data load will cause errors if loaded before this event
    @Listener
    public void onPostInit(GameStartingServerEvent event) {
        logger.info("Loading data...");
        DataManager.load();
        logger.info("Data loaded.");
    }

    @Listener
    public void onGameStopping(GameStoppingServerEvent event) {
        logger.info("Executing shutdown tasks.");
        for (Trade trade : DataManager.getActiveTrades()) {
            SafeTrade.EVENT_BUS.post(new TradeEndEvent(trade, TradeResult.CANCELLED));
            logger.warn("Force ending trade containing player " + trade.participants[0].getName() + " and " + trade.participants[1].getName());
            DataManager.storeItemSnapshots(trade.participants[0], trade.getItems(trade.participants[0]));
            DataManager.storeItemSnapshots(trade.participants[1], trade.getItems(trade.participants[1]));
        }
        DataManager.trimFiles();
    }

    @Listener
    public void onReload(GameReloadEvent event) {
        logger.info("Reloading config...");
        Config.load();
        logger.info("Config reloaded.");

        logger.info("Reloading data...");
        DataManager.getActiveListings().clear();
        DataManager.load();
        logger.info("Data loaded.");
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
