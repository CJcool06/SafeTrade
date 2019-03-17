package io.github.cjcool06.safetrade.listeners;

import io.github.cjcool06.safetrade.api.events.trade.TradeCreationEvent;
import io.github.cjcool06.safetrade.obj.Trade;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import java.net.MalformedURLException;
import java.net.URL;

public class TradeCreationListener {

    @SubscribeEvent
    public void onHandshake(TradeCreationEvent event) {
        Trade trade = event.trade;
        trade.sendChannelMessage(Text.of(TextColors.GREEN, "Trade channel initialised."));
        try {
            trade.sendChannelMessage(Text.builder().append(Text.of(TextColors.GOLD, "If you're unsure about this chat or how to conduct a SafeTrade, click here.")).onClick(TextActions.openUrl(new URL("https://github.com/CJcool06/SafeTrade/wiki"))).build());
        } catch (MalformedURLException mue) {
            trade.sendChannelMessage(Text.of(TextColors.GOLD, "If you're unsure about this chat or how to conduct a SafeTrade, type /safetrade wiki"));
        }
    }
}
