package io.github.cjcool06.safetrade.listeners;

import io.github.cjcool06.safetrade.api.events.trade.TradeEvent;
import io.github.cjcool06.safetrade.utils.Utils;
import org.spongepowered.api.event.Listener;

public class TradeExecutedEvent {

    @Listener
    public void onExecuted(TradeEvent.Executed.SuccessfulTrade event) {
        event.trade.getSides()[0].sendMessage(Utils.getSuccessMessage(event.trade));
        event.trade.getSides()[1].sendMessage(Utils.getSuccessMessage(event.trade));
    }
}
