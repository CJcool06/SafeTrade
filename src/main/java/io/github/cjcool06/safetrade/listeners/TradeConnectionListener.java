package io.github.cjcool06.safetrade.listeners;

import io.github.cjcool06.safetrade.api.enums.TradeState;
import io.github.cjcool06.safetrade.api.events.trade.ConnectionEvent;
import io.github.cjcool06.safetrade.obj.Side;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

public class TradeConnectionListener {

    @SubscribeEvent
    public void onLeave(ConnectionEvent.Left event) {
        Side side = event.side;
        if (!side.parentTrade.getState().equals(TradeState.ENDED)) {
            event.side.sendMessage(Text.builder().append(Text.of(TextColors.GOLD, "You can resume the trade by clicking here or typing /safetrade open"))
                    .onClick(TextActions.executeCallback(dummySrc -> Sponge.getCommandManager().process(side.getPlayer().get(), "safetrade open"))).build());
        }
    }
}
