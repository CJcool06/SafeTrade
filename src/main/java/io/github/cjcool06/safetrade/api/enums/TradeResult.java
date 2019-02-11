package io.github.cjcool06.safetrade.api.enums;

public enum TradeResult {
    /**
     * The trade was successfully executed.
     */
    SUCCESS,

    /**
     * The trade failed due to an expected condition.
     */
    FAILURE,

    /**
     * The trade was ended due to an unexpected condition.
     */
    ERROR,

    /**
     * The trade was cancelled.
     */
    CANCELLED
}
