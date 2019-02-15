package io.github.cjcool06.safetrade.api.events.trade;

import io.github.cjcool06.safetrade.api.enums.TradeResult;
import io.github.cjcool06.safetrade.obj.Trade;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;

public class TradeEvent extends AbstractEvent {

    public final Trade trade;
    private final Cause cause;

    private TradeEvent(Trade trade) {
        this.trade = trade;
        this.cause = Sponge.getCauseStackManager().getCurrentCause();
    }

    @Override
    public Cause getCause() {
        return cause;
    }

    /**
     * Posted before the {@link Trade} is executed.
     */
    public static class Executing extends TradeEvent implements Cancellable {
        private boolean cancelled = false;

        public Executing(Trade trade) {
            super(trade);
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public void setCancelled(boolean cancel) {
            cancelled = cancel;
        }
    }

    public static class Executed extends TradeEvent {
        public final TradeResult result;

        private Executed(Trade trade, TradeResult result) {
            super(trade);
            this.result = result;
        }

        /**
         * Posted after the {@link Trade} is executed and was successful.
         */
        public static class SuccessfulTrade extends Executed {
            public SuccessfulTrade(Trade trade, TradeResult result) {
                super(trade, result);
            }
        }

        /**
         * Posted after the {@link Trade} is executed and was unsuccessful.
         */
        public static class UnsuccessfulTrade extends Executed {
            public UnsuccessfulTrade(Trade trade, TradeResult result) {
                super(trade, result);
            }
        }
    }

    /**
     * Posted after the {@link Trade} is cancelled.
     */
    public static class Cancelled extends TradeEvent {
        public Cancelled(Trade trade) {
            super(trade);
        }
    }
}
