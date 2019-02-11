package io.github.cjcool06.safetrade.api.events.trade;

import io.github.cjcool06.safetrade.obj.Trade;
import io.github.cjcool06.safetrade.api.enums.TradeState;
import net.minecraftforge.fml.common.eventhandler.Event;

public class StateChangedEvent extends Event {

    public final Trade trade;
    public final TradeState oldState;
    public final TradeState newState;

    public StateChangedEvent(Trade trade, TradeState oldState, TradeState newState) {
        this.trade = trade;
        this.oldState = oldState;
        this.newState = newState;
    }
}
