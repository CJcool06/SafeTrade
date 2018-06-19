package io.github.cjcool06.safetrade.listeners;

import io.github.cjcool06.safetrade.SafeTrade;
import io.github.cjcool06.safetrade.data.SafeTradeData;
import io.github.cjcool06.safetrade.obj.Trade;
import io.github.cjcool06.safetrade.utils.Utils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.List;

public class ConnectionListener {
    @Listener
    public void onJoin(ClientConnectionEvent.Join event) {
        Player player = event.getTargetEntity();
        if (player.get(SafeTradeData.class).isPresent()) {
            SafeTradeData data = player.get(SafeTradeData.class).get();
            // TODO: Iterator
            //Iterator<ItemStackSnapshot> iter = data.getPendingItems().get().iterator();
            List<ItemStackSnapshot> items = data.getPendingItems().get();
            data.clearPendingItems();
            Utils.giveItems(player, items, false);
            if ((items.size() - data.getPendingItems().size()) != 0) {
                Sponge.getScheduler().createTaskBuilder()
                        .execute(() -> {
                            player.sendMessage(Text.of(TextColors.GREEN, "SafeTrade has placed " + (items.size() - data.getPendingItems().size()) + " items in to your inventory:"));
                            outerloop:
                            for (ItemStackSnapshot snapshot : items) {
                                for (ItemStackSnapshot snapshot1 : data.getPendingItems().get()) {
                                    // Was not successfully placed in inventory
                                    if (snapshot.createStack().equalTo(snapshot1.createStack())) {
                                        continue outerloop;
                                    }
                                }
                                player.sendMessage(Text.of(TextColors.GREEN, snapshot.getQuantity() + "x ", TextColors.AQUA, snapshot.getTranslation().get()));
                            }
                        })
                        .delayTicks(1).submit(SafeTrade.getPlugin());
            }
            if (data.getPendingItems().size() != 0) {
                Sponge.getScheduler().createTaskBuilder()
                        .execute(() -> player.sendMessage(Text.of(TextColors.GOLD, "SafeTrade has " + data.getPendingItems().size() + " of your items in storage. " +
                                "To obtain these items, relog with " + data.getPendingItems().size() + " free inventory spaces.")))
                        .delayTicks(1).submit(SafeTrade.getPlugin());
            }
        }
        else {
            player.offer(new SafeTradeData());
        }
    }

    @Listener
    public void onLeave(ClientConnectionEvent.Disconnect event) {
        Player player = event.getTargetEntity();
        Trade trade = SafeTrade.getTrade(player);
        if (trade != null) {
            SafeTradeData data = player.get(SafeTradeData.class).orElse(new SafeTradeData());
            data.addPendingItems(trade.getItems(player));
            player.offer(data);
        }
    }
}
