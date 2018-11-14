package io.github.cjcool06.safetrade.api.condition;

import io.github.cjcool06.safetrade.api.enquiry.ListingBase;

public interface Condition {
    boolean passes(ListingBase listing);
}
