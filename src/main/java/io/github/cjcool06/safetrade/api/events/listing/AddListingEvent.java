package io.github.cjcool06.safetrade.api.events.listing;

import io.github.cjcool06.safetrade.api.enquiry.ListingBase;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class AddListingEvent extends Event {
    public final ListingBase listing;

    public AddListingEvent(ListingBase listing) {
        this.listing = listing;
    }
}