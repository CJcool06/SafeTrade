package io.github.cjcool06.safetrade.api.events.trade;

import io.github.cjcool06.safetrade.api.enums.TradeState;
import io.github.cjcool06.safetrade.obj.Trade;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;

/**
 * Posted after the {@link Trade}'s state is changed.
 */
public class StateChangedEvent extends AbstractEvent {

    public final Trade trade;
    public final TradeState oldState;
    public final TradeState newState;
    private final Cause cause;

    public StateChangedEvent(Trade trade, TradeState oldState, TradeState newState) {
        this.trade = trade;
        this.oldState = oldState;
        this.newState = newState;
        this.cause = Sponge.getCauseStackManager().getCurrentCause();
    }

    @Override
    public Cause getCause() {
        return cause;
    }
}
