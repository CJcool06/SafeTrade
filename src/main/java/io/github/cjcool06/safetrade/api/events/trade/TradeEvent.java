package io.github.cjcool06.safetrade.api.events.trade;

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
        public final Trade.Result tradeResult;

        private Executed(Trade.Result tradeResult) {
            super(tradeResult.getTrade());
            this.tradeResult = tradeResult;
        }

        /**
         * Posted after the {@link Trade} is executed and was successful.
         */
        public static class Success extends Executed {
            public Success(Trade.Result tradeResult) {
                super(tradeResult);
            }
        }

        /**
         * Posted after the {@link Trade} is executed and was unsuccessful.
         */
        public static class Fail extends Executed {
            public Fail(Trade.Result tradeResult) {
                super(tradeResult);
            }
        }
    }

    /**
     * Posted after the {@link Trade} is cancelled.
     */
    public static class Cancelled extends TradeEvent {
        public final Trade.Result tradeResult;

        public Cancelled(Trade.Result tradeResult) {
            super(tradeResult.getTrade());
            this.tradeResult = tradeResult;
        }
    }
}
