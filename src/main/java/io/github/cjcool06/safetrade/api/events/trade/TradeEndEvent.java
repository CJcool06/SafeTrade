package io.github.cjcool06.safetrade.api.events.trade;

import io.github.cjcool06.safetrade.enums.TradeResult;
import io.github.cjcool06.safetrade.obj.Trade;
import net.minecraftforge.fml.common.eventhandler.Event;

public class TradeEndEvent extends Event {
    public final Trade trade;
    public final TradeResult result;

    public TradeEndEvent(Trade trade, TradeResult result) {
        this.trade = trade;
        this.result = result;
    }
}
