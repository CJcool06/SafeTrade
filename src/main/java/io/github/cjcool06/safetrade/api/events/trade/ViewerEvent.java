package io.github.cjcool06.safetrade.api.events.trade;

import io.github.cjcool06.safetrade.obj.Trade;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;

public class ViewerEvent extends AbstractEvent {

    public final Trade trade;
    public final Player viewer;
    private final Cause cause;

    private ViewerEvent(Trade trade, Player viewer) {
        this.trade = trade;
        this.viewer = viewer;
        this.cause = Sponge.getCauseStackManager().getCurrentCause();
    }

    @Override
    public Cause getCause() {
        return cause;
    }

    public static class Add extends ViewerEvent {

        private Add(Trade trade, Player player) {
            super(trade, player);
        }

        /**
         * Posted before the viewer is added to the {@link Trade}.
         */
        public static class Pre extends Add implements Cancellable {
            private boolean cancelled = false;

            public Pre(Trade trade, Player player) {
                super(trade, player);
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
         * Posted after the viewer is added to the {@link Trade}.
         */
        public static class Post extends Add {
            public Post(Trade trade, Player player) {
                super(trade, player);
            }
        }
    }

    public static class Remove extends ViewerEvent {

        private Remove(Trade trade, Player player) {
            super(trade, player);
        }

        /**
         * Posted before the viewer is removed from the {@link Trade}.
         */
        public static class Pre extends Remove implements Cancellable {
            private boolean cancelled = false;

            public Pre(Trade trade, Player player) {
                super(trade, player);
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
         * Posted after the viewer is removed from the {@link Trade}.
         */
        public static class Post extends Remove {
            public Post(Trade trade, Player player) {
                super(trade, player);
            }
        }
    }
}
