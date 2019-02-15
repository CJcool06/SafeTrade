package io.github.cjcool06.safetrade.api.events.trade;

import io.github.cjcool06.safetrade.obj.Vault;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;
import org.spongepowered.api.item.inventory.ItemStack;

public class TransactionEvent extends AbstractEvent {
    public final Vault vault;
    private final Cause cause;

    private TransactionEvent(Vault vault) {
        this.vault = vault;
        this.cause = Sponge.getCauseStackManager().getCurrentCause();
    }

    @Override
    public Cause getCause() {
        return cause;
    }

    public static class Item extends TransactionEvent {
        public final ItemStack itemStack;

        private Item(Vault vault, ItemStack itemStack) {
            super(vault);
            this.itemStack = itemStack;
        }

        public static class Add extends Item {

            private Add(Vault vault, ItemStack itemStack) {
                super(vault, itemStack);
            }

            public static class Pre extends Add implements Cancellable {
                private boolean cancelled = false;

                public Pre(Vault vault, ItemStack itemStack) {
                    super(vault, itemStack);
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
             * Posted after the item was successfully added.
             */
            public static class Success extends Add {
                public Success(Vault vault, ItemStack itemStack) {
                    super(vault, itemStack);
                }
            }

            /**
             * Posted after the item failed to be added.
             *
             * <p>If #Add is cancelled, this will be posted.</p>
             */
            public static class Fail extends Add {
                public Fail(Vault vault, ItemStack itemStack) {
                    super(vault, itemStack);
                }
            }
        }

        /**
         * Posted before the item is attempted to be removed.
         */
        public static class Remove extends Item {

            private Remove(Vault vault, ItemStack itemStack) {
                super(vault, itemStack);
            }

            public static class Pre extends Remove implements Cancellable {
                private boolean cancelled = false;

                public Pre(Vault vault, ItemStack itemStack) {
                    super(vault, itemStack);
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
             * Posted after the item was successfully removed.
             */
            public static class Success extends Remove {
                public Success(Vault vault, ItemStack itemStack) {
                    super(vault, itemStack);
                }
            }

            /**
             * Posted after the item failed to be removed.
             *
             * <p>If #Add is cancelled, this will be posted.</p>
             */
            public static class Fail extends Remove {
                public Fail(Vault vault, ItemStack itemStack) {
                    super(vault, itemStack);
                }
            }
        }
    }
}
