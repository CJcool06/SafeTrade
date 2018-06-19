package io.github.cjcool06.safetrade.api.events.trade;

import io.github.cjcool06.safetrade.obj.Trade;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class TradeStartEvent extends Event {
    public final Trade trade;

    public TradeStartEvent(Trade trade) {
        this.trade = trade;
    }
}
