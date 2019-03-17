package io.github.cjcool06.safetrade.listeners;

import io.github.cjcool06.safetrade.api.events.trade.ViewerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class ViewerConnectionListener {

    @SubscribeEvent
    public void onViewer(ViewerEvent event) {
        if (event instanceof ViewerEvent.Add) {
            event.trade.sendChannelMessage(Text.of(TextColors.GOLD, event.viewer.getName(), TextColors.GREEN, " is viewing the trade."));
        }
        else if (event instanceof ViewerEvent.Remove) {
            event.trade.sendChannelMessage(Text.of(TextColors.GOLD, event.viewer.getName(), TextColors.RED, " is no longer viewing the trade."));
        }
    }
}