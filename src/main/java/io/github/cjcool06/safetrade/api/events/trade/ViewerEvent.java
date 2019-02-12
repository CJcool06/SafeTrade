package io.github.cjcool06.safetrade.api.events.trade;

import io.github.cjcool06.safetrade.obj.Trade;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.spongepowered.api.entity.living.player.Player;

public class ViewerEvent extends Event {

    public final Trade trade;
    public final Player viewer;

    private ViewerEvent(Trade trade, Player viewer) {
        this.trade = trade;
        this.viewer = viewer;
    }

    public static class Add extends ViewerEvent {

        private Add(Trade trade, Player player) {
            super(trade, player);
        }

        /**
         * Posted before the viewer is added to the {@link Trade}.
         */
        @Cancelable
        public static class Pre extends Add {
            public Pre(Trade trade, Player player) {
                super(trade, player);
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
        @Cancelable
        public static class Pre extends Remove {
            public Pre(Trade trade, Player player) {
                super(trade, player);
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
