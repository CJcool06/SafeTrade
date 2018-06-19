package io.github.cjcool06.safetrade.utils;

import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import io.github.cjcool06.safetrade.SafeTrade;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;
import java.util.List;

public class ItemUtils {
    // Items
    public static ItemStack getStatusItem(DyeColor color, int amount) {
        ItemStack accept = ItemStack.of(ItemTypes.DYE, amount);
        accept.offer(Keys.DYE_COLOR, color);
        return accept;
    }

    public static ItemStack getPokemonIcon(EntityPixelmon pokemon) {
        //String star = pokemon.getIsShiny() ? "\u2605" : "";
        List<Text> lore = new ArrayList<>(Utils.getPokemonLore(pokemon));
        ItemStack pokemonIcon = Utils.getPicture(pokemon);
        pokemonIcon.offer(Keys.DISPLAY_NAME, Text.of(TextColors.LIGHT_PURPLE, pokemon.getName()/*, TextColors.GOLD, star*/));
        pokemonIcon.offer(Keys.ITEM_LORE, lore);

        return pokemonIcon;
    }

    public static ItemStack getInfoItem(Text name, List<Text> lore, DyeColor color) {
        ItemStack info = ItemStack.of(ItemTypes.WOOL, 1);
        info.offer(Keys.DYE_COLOR, color);
        info.offer(Keys.DISPLAY_NAME, name);
        info.offer(Keys.ITEM_LORE, lore);
        return info;
    }

    // Clickable Items
    public static ItemStack getSlotButton(int slot) {
        ItemStack item = ItemStack.of(Sponge.getRegistry().getType(ItemType.class, "pixelmon:master_ball").get(), slot);
        item.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Slot " + slot));
        return item;
    }

    /*
    public static ItemStack getUpdateButton() {
        ItemStack update = ItemStack.of(ItemTypes.DYE, 1);
        update.offer(Keys.DYE_COLOR, DyeColors.ORANGE);
        update.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Request Update"));
        return update;
    }*/

    public static ItemStack getAcceptButton() {
        ItemStack accept = ItemStack.of(ItemTypes.DYE, 1);
        accept.offer(Keys.DYE_COLOR, DyeColors.GREEN);
        accept.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GREEN, "Accept Trade"));
        return accept;
    }

    public static ItemStack getCancelButton() {
        ItemStack cancel = ItemStack.of(ItemTypes.DYE, 1);
        cancel.offer(Keys.DYE_COLOR, DyeColors.RED);
        cancel.offer(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "Cancel Trade"));
        return cancel;
    }

    public static ItemStack getResetMoneyButton() {
        ItemStack reset = ItemStack.of(ItemTypes.DYE, 1);
        reset.offer(Keys.DYE_COLOR, DyeColors.ORANGE);
        reset.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Reset Money"));
        return reset;
    }

    public static ItemStack getExitButton() {
        ItemStack close = ItemStack.of(ItemTypes.BARRIER, 1);
        close.offer(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "Exit Trade"));
        return close;
    }

    public static ItemStack getMoneyButton(int moneyAmount) {
        ItemStack money = ItemStack.of(ItemTypes.GOLD_INGOT, 1);
        money.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GREEN, SafeTrade.getEcoService().getDefaultCurrency().getSymbol(), moneyAmount));
        return money;
    }
/*
    public static ItemStack getMoney1Button() {
        ItemStack money = ItemStack.of(ItemTypes.GOLD_INGOT, 1);
        money.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GREEN, "1 Money"));
        return money;
    }

    public static ItemStack getMoney10Button() {
        ItemStack money = ItemStack.of(ItemTypes.GOLD_INGOT, 1);
        money.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GREEN, "10 Moneys"));
        return money;
    }

    public static ItemStack getMoney100Button() {
        ItemStack money = ItemStack.of(ItemTypes.GOLD_INGOT, 1);
        money.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GREEN, "100 Moneys"));
        return money;
    }

    public static ItemStack getMoney1000Button() {
        ItemStack money = ItemStack.of(ItemTypes.GOLD_INGOT, 1);
        money.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GREEN, "1000 Moneys"));
        return money;
    }

    public static ItemStack getMoney10000Button() {
        ItemStack money = ItemStack.of(ItemTypes.GOLD_INGOT, 1);
        money.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GREEN, "10,000 Moneys"));
        return money;
    }

    public static ItemStack getMoney100000Button() {
        ItemStack money = ItemStack.of(ItemTypes.GOLD_INGOT, 1);
        money.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GREEN, "100,000 Moneys"));
        return money;
    }
    public static ItemStack getMoney1000000Button() {
        ItemStack money = ItemStack.of(ItemTypes.GOLD_INGOT, 1);
        money.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GREEN, "1,000,000 Moneys"));
        return money;
    }
    */
}
