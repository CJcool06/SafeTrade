package io.github.cjcool06.safetrade.api.events.trade;

import io.github.cjcool06.safetrade.obj.Side;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;

public class ConnectionEvent extends AbstractEvent {

    public final Side side;
    private final Cause cause;

    private ConnectionEvent(Side side) {
        this.side = side;
        this.cause = Sponge.getCauseStackManager().getCurrentCause();
    }

    @Override
    public Cause getCause() {
        return cause;
    }


    public static class Join extends ConnectionEvent {

        private Join(Side side) {
            super(side);
        }

        /**
         * Posted before the {@link Side} connects to the trade inventory.
         */
        public static class Pre extends Join implements Cancellable {
            private boolean cancelled = false;

            public Pre(Side side) {
                super(side);
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
         * Posted after the {@link Side} connects to the trade inventory.
         */
        public static class Post extends Join {
            public Post(Side side) {
                super(side);
            }
        }
    }

    /**
     * Posted when a {@link Side} closes the trade inventory.
     */
    public static class Left extends ConnectionEvent {
        public Left(Side side) {
            super(side);
        }
    }
}
