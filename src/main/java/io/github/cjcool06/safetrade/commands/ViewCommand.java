package io.github.cjcool06.safetrade.commands;

import io.github.cjcool06.safetrade.SafeTrade;
import io.github.cjcool06.safetrade.api.enums.PrefixType;
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
        if (src instanceof Player) {
            User user = args.<User>getOne("target").get();
            Trade trade = Tracker.getActiveTrade(user);

            if (trade != null) {
                trade.addViewer((Player)src, true);
                SafeTrade.sendMessageToPlayer((Player)src, PrefixType.SAFETRADE, Text.of(TextColors.GREEN, "Opening trade between ", TextColors.GOLD, trade.getSides()[0].getUser().get().getName(), TextColors.GREEN, " & ", TextColors.GOLD, trade.getSides()[1].getUser().get().getName(), TextColors.GREEN, "."));
            } else {
                SafeTrade.sendMessageToPlayer((Player)src, PrefixType.SAFETRADE, Text.of(TextColors.GOLD, user.getName(), TextColors.RED, " is not currently participating in a SafeTrade."));
            }
        }
        else {
            SafeTrade.sendMessageToCommandSource(src, PrefixType.SAFETRADE, Text.of(TextColors.RED, "You must be a player to do that."));
        }

        return CommandResult.success();
    }
}
