package io.github.cjcool06.safetrade;

import com.google.inject.Inject;
import io.github.cjcool06.safetrade.api.events.trade.TradeEndEvent;
import io.github.cjcool06.safetrade.commands.TradeCommand;
import io.github.cjcool06.safetrade.data.ImmutableSafeTradeData;
import io.github.cjcool06.safetrade.data.SafeTradeData;
import io.github.cjcool06.safetrade.data.SafeTradeDataManipulatorBuilder;
import io.github.cjcool06.safetrade.data.SafeTradeKeys;
import io.github.cjcool06.safetrade.listeners.ConnectionListener;
import io.github.cjcool06.safetrade.obj.Trade;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.event.service.ChangeServiceProviderEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.economy.EconomyService;

import java.io.File;
import java.util.ArrayList;

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
    public static final String VERSION = "1.0.0";
    public static final String DESCRIPTION = "Trade Pokemon and items safely";
    public static final String AUTHORS = "CJcool06";
    public static final EventBus EVENT_BUS = new EventBus();
    public static final ArrayList<Trade> activeTrades = new ArrayList<>();
    private static SafeTrade plugin;
    private EconomyService economyService = null;

    @Inject
    private Logger logger;

    @Inject
    PluginContainer container;

    @Inject
    @ConfigDir(sharedRoot = false)
    private File configDir;

    @Listener
    public void onPreInit(GamePreInitializationEvent event) {
        plugin = this;
        SafeTradeKeys.init();

        DataRegistration.builder()
                .dataName("SafeTrade Data")
                .manipulatorId("safetrade_data")
                .dataClass(SafeTradeData.class)
                .immutableClass(ImmutableSafeTradeData.class)
                .builder(new SafeTradeDataManipulatorBuilder())
                .buildAndRegister(container);
    }

    @Listener
    public void onInit(GameInitializationEvent event) {
        Sponge.getEventManager().registerListeners(this, new ConnectionListener());
        Sponge.getCommandManager().register(this, TradeCommand.getSpec(), "safetrade");

        Sponge.getServiceManager()
                .getRegistration(EconomyService.class)
                .ifPresent(prov -> economyService = prov.getProvider());

        logger.info("Economy plugin: " + (economyService == null ? "Not Found" : "Found"));
        if (economyService == null) {
            logger.warn("No economy plugin was found. Trades WILL break!");
        }
    }

    @Listener
    public void onGameStopping(GameStoppingServerEvent event) {
        logger.warn("Preparing shut down.");
        for (Trade trade : activeTrades) {
            SafeTrade.EVENT_BUS.post(new TradeEndEvent(trade));
            logger.warn("Ending trade.");
            SafeTradeData data = trade.participants[0].get(SafeTradeData.class).orElse(new SafeTradeData());
            data.addPendingItems(trade.getItems(trade.participants[0]));
            trade.participants[0].offer(data);
            data = trade.participants[1].get(SafeTradeData.class).orElse(new SafeTradeData());
            data.addPendingItems(trade.getItems(trade.participants[1]));
            trade.participants[1].offer(data);
            trade.participants[0].get(SafeTradeData.class).get().getPendingItems().get().forEach(snapshot -> logger.warn(trade.participants[0].getName() + ": " + snapshot.getTranslation().get()));
            trade.participants[1].get(SafeTradeData.class).get().getPendingItems().get().forEach(snapshot -> logger.warn(trade.participants[1].getName() + ": " + snapshot.getTranslation().get()));
        }
        // Prevents ConnectionListener#onJoin from being called
        activeTrades.clear();
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

    public static Trade getTrade(Player player) {
        for (Trade trade : activeTrades) {
            if (trade.hasPlayer(player)) {
                return trade;
            }
        }

        return null;
    }
}
