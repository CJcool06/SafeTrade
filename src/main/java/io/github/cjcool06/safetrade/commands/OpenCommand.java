package io.github.cjcool06.safetrade.commands;

import io.github.cjcool06.safetrade.api.enums.InventoryType;
import io.github.cjcool06.safetrade.obj.Trade;
import io.github.cjcool06.safetrade.trackers.Tracker;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class OpenCommand implements CommandExecutor {

    public static CommandSpec getSpec() {
        return CommandSpec.builder()
                .description(Text.of("Open a SafeTrade"))
                .permission("safetrade.common.open")
                .executor(new OpenCommand())
                .build();
    }

    public CommandResult execute(CommandSource src, CommandContext args) {
        Player player = (Player)src;
        Trade trade = Tracker.getActiveTrade(player);

        if (trade != null) {
            trade.getSide(player.getUniqueId()).ifPresent(side -> side.changeInventory(InventoryType.MAIN));
        }
        else {
            player.sendMessage(Text.of(TextColors.RED, "You are not currently participating in a SafeTrade."));
        }

        return CommandResult.success();
    }
}
