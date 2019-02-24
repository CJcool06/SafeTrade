package io.github.cjcool06.safetrade.utils;

import com.google.common.collect.Lists;
import io.github.cjcool06.safetrade.SafeTrade;
import io.github.cjcool06.safetrade.api.enums.TradeState;
import io.github.cjcool06.safetrade.obj.Side;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.RepresentedPlayerData;
import org.spongepowered.api.data.manipulator.mutable.SkullData;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.data.type.SkullTypes;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.text.NumberFormat;
import java.util.Locale;

public class ItemUtils {

    public static class Main {

        public static ItemStack getStateStatus(Side side) {
            ItemStack item = ItemStack.of(ItemTypes.STAINED_GLASS_PANE, 1);
            item.offer(Keys.DISPLAY_NAME, Text.of(TextColors.AQUA, side.getUser().get().getName() + "'s Trade Status"));
            if (side.isPaused()) {
                item.offer(Keys.DYE_COLOR, DyeColors.ORANGE);
                item.offer(Keys.ITEM_LORE, Lists.newArrayList(Text.of(TextColors.GRAY, "Current state: ", TextColors.GOLD, "Paused")));
            }
            else if (side.isReady()) {
                item.offer(Keys.DYE_COLOR, DyeColors.LIME);
                item.offer(Keys.ITEM_LORE, Lists.newArrayList(Text.of(TextColors.GRAY, "Current state: ", TextColors.GREEN, "Ready")));
            }
            else {
                item.offer(Keys.DYE_COLOR, DyeColors.RED);
                item.offer(Keys.ITEM_LORE, Lists.newArrayList(Text.of(TextColors.GRAY, "Current state: ", TextColors.RED, "Not Ready")));
            }
            return item;
        }

        public static ItemStack getQuit() {
            ItemStack item = ItemStack.of(ItemTypes.BARRIER, 1);
            item.offer(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "Quit"));
            item.offer(Keys.ITEM_LORE, Lists.newArrayList(Text.of(TextColors.GRAY, "End the trade and get your items and money back")));
            return item;
        }

        public static ItemStack getHead(Side side) {
            SkullData skullData = Sponge.getDataManager().getManipulatorBuilder(SkullData.class).get().create();
            skullData.set(Keys.SKULL_TYPE, SkullTypes.PLAYER);
            ItemStack itemStack = Sponge.getRegistry().createBuilder(ItemStack.Builder.class).itemType(ItemTypes.SKULL).itemData(skullData).build();
            RepresentedPlayerData skinData = Sponge.getDataManager().getManipulatorBuilder(RepresentedPlayerData.class).get().create();
            skinData.set(Keys.REPRESENTED_PLAYER, GameProfile.of(side.getUser().get().getUniqueId()));
            itemStack.offer(skinData);
            itemStack.offer(Keys.DISPLAY_NAME, Text.of(TextColors.DARK_AQUA, side.getUser().get().getName()));
            itemStack.offer(Keys.ITEM_LORE, Lists.newArrayList(Text.of(TextColors.GRAY, "This side of the trade holds the items and money that " +
                    side.getUser().get().getName() + " is willing to trade.")));
            return itemStack;
        }

        public static ItemStack getMoneyStorage(Side side) {
            Currency currency = SafeTrade.getEcoService().getDefaultCurrency();
            ItemStack item = ItemStack.of(ItemTypes.GOLD_BLOCK, 1);
            item.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, currency.getSymbol(), side.vault.account.getBalance(currency)));
            if (side.parentTrade.getState().equals(TradeState.TRADING)) {
                item.offer(Keys.ITEM_LORE, Lists.newArrayList(Text.of(TextColors.GRAY, "Click to change the amount of ", currency.getPluralDisplayName(), " to trade"),
                        Text.of(TextColors.GOLD, "Only " + side.getUser().get().getName() + " can do this")));
            }
            return item;
        }

        public static ItemStack getItemStorage(Side side) {
            ItemStack item = ItemStack.of(ItemTypes.CHEST, 1);
            item.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Items"));
            item.offer(Keys.ITEM_LORE, Lists.newArrayList(Text.of(TextColors.GRAY, "Click to view the items that " + side.getUser().get().getName() + " wants to trade")));
            return item;
        }

        public static ItemStack getReady() {
            ItemStack item = ItemStack.of(ItemTypes.DYE, 1);
            item.offer(Keys.DYE_COLOR, DyeColors.LIME);
            item.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GREEN, "Ready"));
            item.offer(Keys.ITEM_LORE, Lists.newArrayList(Text.of(TextColors.GRAY, "Flag yourself as ready")));
            return item;
        }

        public static ItemStack getNotReady() {
            ItemStack item = ItemStack.of(ItemTypes.DYE, 1);
            item.offer(Keys.DYE_COLOR, DyeColors.RED);
            item.offer(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "Not Ready"));
            item.offer(Keys.ITEM_LORE, Lists.newArrayList(Text.of(TextColors.GRAY, "Flag yourself as not ready")));
            return item;
        }

        public static ItemStack getPause() {
            ItemStack item = ItemStack.of(ItemTypes.DYE, 1);
            item.offer(Keys.DYE_COLOR, DyeColors.ORANGE);
            item.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Pause"));
            item.offer(Keys.ITEM_LORE, Lists.newArrayList(Text.of(TextColors.GRAY, "Flag yourself as paused")));
            return item;
        }
    }

    public static class Money {

        public static ItemStack getTotalMoney(Side side) {
            Currency currency = SafeTrade.getEcoService().getDefaultCurrency();
            ItemStack item = ItemStack.of(ItemTypes.GOLD_BLOCK, 1);
            item.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, currency.getSymbol(), side.vault.account.getBalance(currency).intValue()));
            item.offer(Keys.ITEM_LORE, Lists.newArrayList(Text.of(TextColors.GRAY, "This money is safely stored until the trade comes to an end.")));
            return item;
        }

        public static ItemStack getPlayersMoney(Side side) {
            Currency currency = SafeTrade.getEcoService().getDefaultCurrency();
            ItemStack item = ItemStack.of(ItemTypes.DIAMOND_ORE, 1);
            item.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GREEN, currency.getSymbol(), SafeTrade.getEcoService().getOrCreateAccount(side.getUser().get().getUniqueId()).get().getBalance(currency).intValue()));
            item.offer(Keys.ITEM_LORE, Lists.newArrayList(Text.of(TextColors.GRAY, "This is the total number of ", currency.getPluralDisplayName(), " you have.")));
            return item;
        }

        public static ItemStack getMoneyBars(int amount) {
            Currency currency = SafeTrade.getEcoService().getDefaultCurrency();
            ItemStack item = ItemStack.of(ItemTypes.GOLD_INGOT, 1);
            item.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, currency.getSymbol(), NumberFormat.getNumberInstance(Locale.US).format(amount)));
            item.offer(Keys.ITEM_LORE, Lists.newArrayList(
                    Text.of(TextColors.GREEN, "Left-Click: ", TextColors.GRAY, "Adds ", currency.getPluralDisplayName()),
                    Text.of(TextColors.RED, "Right-Click: ", TextColors.GRAY, "Removes ", currency.getPluralDisplayName())
            ));
            return item;
        }
    }

    public static class Overview {

        public static ItemStack getConfirmationStatus(Side side) {
            ItemStack item = ItemStack.of(ItemTypes.STAINED_GLASS_PANE, 1);
            item.offer(Keys.DISPLAY_NAME, Text.of(TextColors.AQUA, side.getUser().get().getName() + "'s Confirmation Status"));
            if (side.isConfirmed()) {
                item.offer(Keys.DYE_COLOR, DyeColors.LIME);
                item.offer(Keys.ITEM_LORE, Lists.newArrayList(Text.of(TextColors.GRAY, "Current status: ", TextColors.GREEN, "Ready")));
            }
            else {
                item.offer(Keys.DYE_COLOR, DyeColors.RED);
                item.offer(Keys.ITEM_LORE, Lists.newArrayList(Text.of(TextColors.GRAY, "Current status: ", TextColors.RED, "Not Ready")));
            }
            return item;
        }

        public static ItemStack getConfirm() {
            ItemStack item = ItemStack.of(ItemTypes.DYE, 1);
            item.offer(Keys.DYE_COLOR, DyeColors.GREEN);
            item.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GREEN, "Confirm"));
            item.offer(Keys.ITEM_LORE, Lists.newArrayList(
                    Text.of(TextColors.GOLD, "Confirm you are happy with the trade.")));
            return item;
        }

        public static ItemStack getCancel() {
            ItemStack item = ItemStack.of(ItemTypes.DYE, 1);
            item.offer(Keys.DYE_COLOR, DyeColors.YELLOW);
            item.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Cancel"));
            item.offer(Keys.ITEM_LORE, Lists.newArrayList(Text.of(TextColors.GREEN, "Go back and renegotiate the trade.")));
            return item;
        }

        public static ItemStack getOverviewInfo() {
            ItemStack item = ItemStack.of(ItemTypes.PAPER, 1);
            item.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "What is the trade overview?"));
            item.offer(Keys.ITEM_LORE, Lists.newArrayList(Text.of(TextColors.GREEN, "The trade overview allows you to browse the trade and make sure "),
                    Text.of(TextColors.GREEN, "that you are happy."),
                    Text.of(),
                    Text.of(TextColors.DARK_GREEN, "During this time you are unable to change anything about the trade."),
                    Text.of(),
                    Text.of(TextColors.GRAY, "The trade will execute once both players have confirmed."),
                    Text.of(TextColors.RED, "There is no reverting this!")));
            return item;
        }
    }

    // Yeah yeah, I know this shit is kinda redundant
    public static class Logs {

        public static ItemStack getMoney(User user, int money) {
            Currency currency = SafeTrade.getEcoService().getDefaultCurrency();
            ItemStack item = ItemStack.of(ItemTypes.GOLD_BLOCK, 1);
            item.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, currency.getSymbol(), money));
            item.offer(Keys.ITEM_LORE, Lists.newArrayList(Text.of(TextColors.GRAY, "The amount of ", currency.getPluralDisplayName(), " " + user.getName() + " traded")));
            return item;
        }

        public static ItemStack getItems(User user) {
            ItemStack item = ItemStack.of(ItemTypes.CHEST, 1);
            item.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Items"));
            item.offer(Keys.ITEM_LORE, Lists.newArrayList(Text.of(TextColors.GRAY, "Click to view the items that " + user.getName() + " traded")));
            return item;
        }

        public static ItemStack getHead(User user) {
            SkullData skullData = Sponge.getDataManager().getManipulatorBuilder(SkullData.class).get().create();
            skullData.set(Keys.SKULL_TYPE, SkullTypes.PLAYER);
            ItemStack itemStack = Sponge.getRegistry().createBuilder(ItemStack.Builder.class).itemType(ItemTypes.SKULL).itemData(skullData).build();
            RepresentedPlayerData skinData = Sponge.getDataManager().getManipulatorBuilder(RepresentedPlayerData.class).get().create();
            skinData.set(Keys.REPRESENTED_PLAYER, GameProfile.of(user.getUniqueId()));
            itemStack.offer(skinData);
            itemStack.offer(Keys.DISPLAY_NAME, Text.of(TextColors.DARK_AQUA, user.getName()));
            itemStack.offer(Keys.ITEM_LORE, Lists.newArrayList(Text.of(TextColors.GRAY, "This side of the trade holds the Items, Money, and Pokemon that " +
                    user.getName() + " traded")));
            return itemStack;
        }
    }

    public static class Other {

        public static ItemStack getBackButton() {
            ItemStack itemStack = ItemStack.of(ItemTypes.DYE, 1);
            itemStack.offer(Keys.DYE_COLOR, DyeColors.RED);
            itemStack.offer(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "Back"));
            itemStack.offer(Keys.ITEM_LORE, Lists.newArrayList(Text.of(TextColors.GRAY, "Return back to the main trade gui")));
            return itemStack;
        }

        public static ItemStack getFiller(DyeColor color) {
            ItemStack background = ItemStack.of(ItemTypes.STAINED_GLASS_PANE, 1);
            background.offer(Keys.DYE_COLOR, color);
            return background;
        }
    }
}
