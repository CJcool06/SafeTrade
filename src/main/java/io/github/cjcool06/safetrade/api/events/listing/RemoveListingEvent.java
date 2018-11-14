package io.github.cjcool06.safetrade.api.events.listing;

import io.github.cjcool06.safetrade.api.enquiry.ListingBase;
import net.minecraftforge.fml.common.eventhandler.Event;

public class RemoveListingEvent extends Event {
    public final ListingBase listing;

    public RemoveListingEvent(ListingBase listing) {
        this.listing = listing;
    }
}
