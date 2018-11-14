package io.github.cjcool06.safetrade.listeners;

import io.github.cjcool06.safetrade.SafeTrade;
import io.github.cjcool06.safetrade.managers.DataManager;
import io.github.cjcool06.safetrade.utils.Utils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;
import java.util.Iterator;

public class ConnectionListener {
    @Listener
    public void onJoin(ClientConnectionEvent.Join event) {
        Player player = event.getTargetEntity();
        if (DataManager.getFile(player).exists()) {
            Sponge.getScheduler().createTaskBuilder().execute(() -> {
                ArrayList<ItemStackSnapshot> storedItems = DataManager.getStoredItems(player);
                ArrayList<ItemStackSnapshot> removedItems = new ArrayList<>();
                Iterator<ItemStackSnapshot> iter = storedItems.iterator();
                DataManager.clearStoredItems(player);
                int airItems = 0;
                while (iter.hasNext()) {
                    ItemStackSnapshot snapshot = iter.next();
                    if (snapshot.createStack().getType().equals(ItemTypes.AIR)) {
                        airItems++;
                        iter.remove();
                    }
                    else if (Utils.giveItem(player, snapshot.createStack())) {
                        iter.remove();
                        removedItems.add(snapshot);
                    }
                }
                DataManager.storeItemSnapshots(player, storedItems);
                final int air = airItems;

                Sponge.getScheduler().createTaskBuilder()
                        .execute(() -> {
                            if (air > 0) {
                                player.sendMessage(Text.of(TextColors.GREEN, "SafeTrade has found and removed " + air + " bugged air items from your inventory."));
                            }
                            if (removedItems.size() > 0) {
                                player.sendMessage(Text.of(TextColors.GREEN, "SafeTrade has placed " + removedItems.size() + " items in to your inventory:"));
                                for (ItemStackSnapshot snapshot : removedItems) {
                                    player.sendMessage(Text.of(TextColors.GREEN, snapshot.getQuantity() + "x ", TextColors.AQUA, snapshot.getTranslation().get()));
                                }
                            }
                            if (storedItems.size() > 0) {
                                player.sendMessage(Text.of(TextColors.GOLD, "SafeTrade has " + storedItems.size() + " of your items in storage. " +
                                        "To obtain these items, relog with " + storedItems.size() + " free inventory spaces."));
                            }
                        })
                        .delayTicks(1).submit(SafeTrade.getPlugin());
            }).async().submit(SafeTrade.getPlugin());
        }
    }
}
