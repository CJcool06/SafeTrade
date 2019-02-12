package io.github.cjcool06.safetrade.api.events.trade;

import io.github.cjcool06.safetrade.api.enums.InventoryType;
import io.github.cjcool06.safetrade.obj.Side;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

public class InventoryChangeEvent extends Event {

    public final Side side;

    private InventoryChangeEvent(Side side) {
        this.side = side;
    }

    /**
     * Posted before a {@link Side} changes inventory.
     */
    @Cancelable
    public static class Pre extends InventoryChangeEvent {
        public final InventoryType newInventory;

        public Pre(Side side, InventoryType newInventory) {
            super(side);
            this.newInventory = newInventory;
        }
    }

    /**
     * Posted after a {@link Side} changes inventory.
     */
    public static class Post extends InventoryChangeEvent {

        public Post(Side side) {
            super(side);
        }
    }
}
