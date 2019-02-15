package io.github.cjcool06.safetrade.api.events.trade;

import io.github.cjcool06.safetrade.api.enums.TradeResult;
import io.github.cjcool06.safetrade.obj.Trade;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

public class TradeEvent extends Event {

    public final Trade trade;

    private TradeEvent(Trade trade) {
        this.trade = trade;
    }

    /**
     * Posted before the {@link Trade} is executed.
     */
    @Cancelable
    public static class Executing extends TradeEvent {
        public Executing(Trade trade) {
            super(trade);
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
