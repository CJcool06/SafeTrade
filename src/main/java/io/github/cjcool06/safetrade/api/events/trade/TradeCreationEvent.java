package io.github.cjcool06.safetrade.api.events.trade;

import io.github.cjcool06.safetrade.obj.Trade;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Posted when a new {@link Trade} has been created.
 *
 * <p>Specifically, it is fired at the end of the constructor.</p>
 */
public class TradeCreationEvent extends Event {

    public final Trade trade;

    public TradeCreationEvent(Trade trade) {
        this.trade = trade;
    }
}
