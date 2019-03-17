package io.github.cjcool06.safetrade.listeners;

import io.github.cjcool06.safetrade.SafeTrade;
import io.github.cjcool06.safetrade.api.events.trade.TradeEvent;
import io.github.cjcool06.safetrade.config.Config;
import io.github.cjcool06.safetrade.utils.Utils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class TradeExecutedListener {

    @SubscribeEvent
    public void onExecuted(TradeEvent.Executed.Success event) {
        if (Config.broadcastTradeOverviews) {
            SafeTrade.broadcast(Utils.getBroadcastOverview(event.trade));
        }
        else {
            event.trade.sendMessage(Utils.getSuccessMessage(event.trade));
        }
    }
}
