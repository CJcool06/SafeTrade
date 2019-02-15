package io.github.cjcool06.safetrade.api.events.trade;

import io.github.cjcool06.safetrade.api.enums.InventoryType;
import io.github.cjcool06.safetrade.obj.Side;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;

public class InventoryChangeEvent extends AbstractEvent {

    public final Side side;
    private final Cause cause;

    private InventoryChangeEvent(Side side) {
        this.side = side;
        this.cause = Sponge.getCauseStackManager().getCurrentCause();
    }

    @Override
    public Cause getCause() {
        return cause;
    }

    /**
     * Posted before a {@link Side} changes inventory.
     */
    public static class Pre extends InventoryChangeEvent implements Cancellable {
        public final InventoryType newInventory;
        private boolean cancelled = false;

        public Pre(Side side, InventoryType newInventory) {
            super(side);
            this.newInventory = newInventory;
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public void setCancelled(boolean cancel) {
            cancelled = cancel;
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
