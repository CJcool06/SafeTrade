package io.github.cjcool06.safetrade.listeners;

import io.github.cjcool06.safetrade.SafeTrade;
import io.github.cjcool06.safetrade.obj.CommandWrapper;
import io.github.cjcool06.safetrade.obj.PlayerStorage;
import io.github.cjcool06.safetrade.trackers.Tracker;
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
        Sponge.getScheduler().createTaskBuilder().execute(() -> {
            Player player = event.getTargetEntity();
            PlayerStorage storage = Tracker.getOrCreateStorage(player);
            List<CommandWrapper> commandsExecuted = storage.executeCommands();
            List<ItemStackSnapshot> itemsGiven = storage.giveItems();

            if (commandsExecuted.size() > 0) {
                player.sendMessage(Text.of(TextColors.DARK_AQUA, "SafeTrade ", TextColors.GREEN, "executed ", TextColors.DARK_AQUA, commandsExecuted.size(), TextColors.GREEN, " commands on your login:"));
                for (CommandWrapper wrapper : commandsExecuted) {
                    player.sendMessage(Text.of(TextColors.GREEN, "- ", TextColors.AQUA, wrapper.cmd));
                }
            }
            if (itemsGiven.size() > 0) {
                player.sendMessage(Text.of(TextColors.DARK_AQUA, "SafeTrade ", TextColors.GREEN, "has placed ", TextColors.DARK_AQUA, itemsGiven.size(), TextColors.GREEN, " items in to your inventory:"));
                for (ItemStackSnapshot snapshot : itemsGiven) {
                    player.sendMessage(Text.of(TextColors.GREEN, snapshot.getQuantity() + "x ", TextColors.AQUA, snapshot.getTranslation().get()));
                }
            }

            if (storage.getCommands().size() > 0) {
                player.sendMessage(Text.of(TextColors.GREEN, "You have ", TextColors.DARK_AQUA, storage.getCommands().size(), TextColors.GREEN, " commands waiting to be executed."));
            }
            if (storage.getItems().size() > 0) {
                player.sendMessage(Text.of(TextColors.GREEN, "You have ", TextColors.DARK_AQUA, storage.getItems().size(), TextColors.GREEN, " items in your storage."));
            }
        }).delayTicks(1).submit(SafeTrade.getPlugin());
    }
}
