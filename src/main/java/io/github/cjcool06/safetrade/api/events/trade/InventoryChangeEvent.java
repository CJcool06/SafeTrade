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

    @Cancelable
    public static class Pre extends InventoryChangeEvent {
        public final InventoryType newInventory;

        public Pre(Side side, InventoryType newInventory) {
            super(side);
            this.newInventory = newInventory;
        }
    }

    public static class Post extends InventoryChangeEvent {

        public Post(Side side) {
            super(side);
        }
    }
}
