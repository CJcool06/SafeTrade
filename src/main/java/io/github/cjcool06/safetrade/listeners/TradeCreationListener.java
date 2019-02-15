package io.github.cjcool06.safetrade.listeners;

import io.github.cjcool06.safetrade.api.events.trade.TradeCreationEvent;
import io.github.cjcool06.safetrade.obj.Trade;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import java.net.MalformedURLException;
import java.net.URL;

public class TradeCreationListener {

    @Listener
    public void onHandshake(TradeCreationEvent event) {
        Trade trade = event.trade;
        trade.sendMessage(Text.of(TextColors.GREEN, "Trade channel initialised."));
        try {
            trade.sendMessage(Text.builder().append(Text.of(TextColors.GOLD, "If you're unsure about this chat or how to conduct a SafeTrade, click here.")).onClick(TextActions.openUrl(new URL("https://github.com/CJcool06/SafeTrade/wiki"))).build());
        } catch (MalformedURLException mue) {
            trade.sendMessage(Text.of(TextColors.GOLD, "If you're unsure about this chat or how to conduct a SafeTrade, type /safetrade wiki"));
        }
    }
}
