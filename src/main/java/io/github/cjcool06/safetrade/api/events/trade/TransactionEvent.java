package io.github.cjcool06.safetrade.api.events.trade;

import io.github.cjcool06.safetrade.obj.Vault;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.spongepowered.api.item.inventory.ItemStack;

public class TransactionEvent extends Event {
    public final Vault vault;

    private TransactionEvent(Vault vault) {
        this.vault = vault;
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

            @Cancelable
            public static class Pre extends Add {
                public Pre(Vault vault, ItemStack itemStack) {
                    super(vault, itemStack);
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

            @Cancelable
            public static class Pre extends Remove {
                public Pre(Vault vault, ItemStack itemStack) {
                    super(vault, itemStack);
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

    public static class Pokemon extends TransactionEvent {
        public final com.pixelmonmod.pixelmon.api.pokemon.Pokemon pokemon;

        private Pokemon(Vault vault, com.pixelmonmod.pixelmon.api.pokemon.Pokemon pokemon) {
            super(vault);
            this.pokemon = pokemon;
        }

        /**
         * Posted before the Pokemon is attempted to be added.
         */
        public static class Add extends Pokemon {

            private Add(Vault vault, com.pixelmonmod.pixelmon.api.pokemon.Pokemon pokemon) {
                super(vault, pokemon);
            }

            @Cancelable
            public static class Pre extends Add {
                public Pre(Vault vault, com.pixelmonmod.pixelmon.api.pokemon.Pokemon pokemon) {
                    super(vault, pokemon);
                }
            }

            /**
             * Posted after the Pokemon was successfully added.
             */
            public static class Success extends Add {
                public Success(Vault vault, com.pixelmonmod.pixelmon.api.pokemon.Pokemon pokemon) {
                    super(vault, pokemon);
                }
            }

            /**
             * Posted after the Pokemon failed to be added.
             *
             * <p>If #Add is cancelled, this will be posted.</p>
             */
            public static class Fail extends Add {
                public Fail(Vault vault, com.pixelmonmod.pixelmon.api.pokemon.Pokemon pokemon) {
                    super(vault, pokemon);
                }
            }
        }

        /**
         * Posted before the Pokemon is attempted to be removed.
         */
        public static class Remove extends Pokemon {

            private Remove(Vault vault, com.pixelmonmod.pixelmon.api.pokemon.Pokemon pokemon) {
                super(vault, pokemon);
            }

            @Cancelable
            public static class Pre extends Remove {
                public Pre(Vault vault, com.pixelmonmod.pixelmon.api.pokemon.Pokemon pokemon) {
                    super(vault, pokemon);
                }
            }

            /**
             * Posted after the Pokemon was successfully removed.
             */
            public static class Success extends Remove {
                public Success(Vault vault, com.pixelmonmod.pixelmon.api.pokemon.Pokemon pokemon) {
                    super(vault, pokemon);
                }
            }

            /**
             * Posted after the Pokemon failed to be removed.
             *
             * <p>If #Add is cancelled, this will be posted.</p>
             */
            public static class Fail extends Remove {
                public Fail(Vault vault, com.pixelmonmod.pixelmon.api.pokemon.Pokemon pokemon) {
                    super(vault, pokemon);
                }
            }
        }
    }
}
