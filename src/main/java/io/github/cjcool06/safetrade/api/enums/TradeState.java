package io.github.cjcool06.safetrade.api.enums;

public enum TradeState {

    /**
     * The players involved in the trade are currently trading.
     */
    TRADING,

    /**
     * The trade conditions have been agreed upon and are awaiting confirmation to execute the trade.
     */
    WAITING_FOR_CONFIRMATION,

    /**
     * The trade is currently paused.
     */
    PAUSED,

    /**
     * The trade has ended.
     */
    ENDED
}
