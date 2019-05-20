package io.github.cjcool06.safetrade.listeners;

import io.github.cjcool06.safetrade.SafeTrade;
import io.github.cjcool06.safetrade.api.enums.PrefixType;
import io.github.cjcool06.safetrade.api.events.trade.TradeEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;

public class TradeExecutedListener {


    @SubscribeEvent
    public void onExecuted(TradeEvent.Executed.Success event) {

        // Sends a log notification to every online player with the "safetrade.admin.overview" permission node.
        for (Player player : Sponge.getServer().getOnlinePlayers()) {
            if (player.hasPermission("safetrade.admin.overview")) {
                SafeTrade.sendMessageToPlayer(player, PrefixType.LOG, event.tradeResult.getTradeLog().getDisplayText());
            }
        }

        // Sends the overview to the trade participants, if online.
        for (User user : event.trade.getParticipants()) {
            user.getPlayer().ifPresent(player -> SafeTrade.sendMessageToPlayer(player, PrefixType.OVERVIEW, event.tradeResult.getTradeLog().getDisplayText()));
        }
    }
}
