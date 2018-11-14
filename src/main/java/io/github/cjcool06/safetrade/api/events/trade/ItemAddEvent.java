package io.github.cjcool06.safetrade.api.events.trade;

import io.github.cjcool06.safetrade.obj.Trade;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

@Cancelable
public class ItemAddEvent extends Event {
    public final Trade trade;
    public final ItemStackSnapshot snapshot;

    public ItemAddEvent(Trade trade, ItemStackSnapshot snapshot) {
        this.trade = trade;
        this.snapshot = snapshot;
    }
}
