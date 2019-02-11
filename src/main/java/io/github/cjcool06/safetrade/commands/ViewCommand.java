package io.github.cjcool06.safetrade.commands;

import io.github.cjcool06.safetrade.obj.Trade;
import io.github.cjcool06.safetrade.trackers.Tracker;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class ViewCommand implements CommandExecutor {

    public static CommandSpec getSpec() {
        return CommandSpec.builder()
                .description(Text.of("View an ongoing SafeTrade"))
                .permission("safetrade.admin.view")
                .executor(new OpenCommand())
                .arguments(GenericArguments.user(Text.of("target")))
                .build();
    }

    public CommandResult execute(CommandSource src, CommandContext args) {
        Player player = (Player)src;
        User user = args.<User>getOne("target").get();
        Trade trade = Tracker.getActiveTrade(user);

        if (trade != null) {
            trade.addViewer(player, true);
        }
        else {
            player.sendMessage(Text.of(TextColors.RED, user.getName() + " is not currently participating in a SafeTrade."));
        }

        return CommandResult.success();
    }
}
