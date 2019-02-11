package io.github.cjcool06.safetrade.api.events.trade;

import io.github.cjcool06.safetrade.obj.Side;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

public class ConnectionEvent extends Event {

    public final Side side;

    private ConnectionEvent(Side side) {
        this.side = side;
    }

    public static class Join extends ConnectionEvent {

        private Join(Side side) {
            super(side);
        }

        @Cancelable
        public static class Pre extends Join {
            public Pre(Side side) {
                super(side);
            }
        }

        public static class Post extends Join {
            public Post(Side side) {
                super(side);
            }
        }
    }


    public static class Left extends ConnectionEvent {
        public Left(Side side) {
            super(side);
        }
    }
}
