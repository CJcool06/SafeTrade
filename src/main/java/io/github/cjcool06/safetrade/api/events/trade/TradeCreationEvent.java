package io.github.cjcool06.safetrade.api.events.trade;

import io.github.cjcool06.safetrade.obj.Trade;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;

/**
 * Posted when a new {@link Trade} has been created.
 *
 * <p>Specifically, it is fired at the end of the constructor.</p>
 */
public class TradeCreationEvent extends AbstractEvent {

    public final Trade trade;
    private final Cause cause;

    public TradeCreationEvent(Trade trade) {
        this.trade = trade;
        this.cause = Sponge.getCauseStackManager().getCurrentCause();
    }

    @Override
    public Cause getCause() {
        return cause;
    }
}
