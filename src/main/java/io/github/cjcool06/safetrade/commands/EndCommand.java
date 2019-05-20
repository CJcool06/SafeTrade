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
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class EndCommand implements CommandExecutor {
    public static CommandSpec getSpec() {
        return CommandSpec.builder()
                .description(Text.of("End a trade"))
                .permission("safetrade.admin.end")
                .arguments(GenericArguments.optional(GenericArguments.player(Text.of("target"))))
                .executor(new EndCommand())
                .build();
    }

    public CommandResult execute(CommandSource src, CommandContext args) {
        Trade trade;
        if (!args.<Player>getOne("target").isPresent()) {
            if (src instanceof Player) {
                trade = Tracker.getActiveTrade((Player) src);

                if (trade != null) {
                    trade.sendMessage(Text.of(TextColors.GRAY, "Trade ended by " + src.getName() + "."));
                    trade.forceEnd();
                } else {
                    SafeTrade.sendMessageToPlayer((Player) src, PrefixType.SAFETRADE, Text.of(TextColors.RED, "You are not currently participating in a trade."));
                }
            }
            else {
                SafeTrade.sendMessageToCommandSource(src, PrefixType.SAFETRADE, Text.of(TextColors.RED, "You must be a player to do that."));
            }
        }
        else {
            Player target = args.<Player>getOne("target").get();
            trade = Tracker.getActiveTrade(target);

            if (trade == null) {
                SafeTrade.sendMessageToCommandSource(src, PrefixType.SAFETRADE, Text.of(TextColors.RED, "That player is not currently participating in a trade."));
                return CommandResult.success();
            }
            trade.sendChannelMessage(Text.of(TextColors.GRAY, "Trade force ended by " + src.getName() + "."));
            trade.forceEnd();

            SafeTrade.sendMessageToCommandSource(src, PrefixType.SAFETRADE, Text.of(
                    TextColors.GREEN, "Force ended ", TextColors.GOLD, trade.getSides()[0].getUser().get().getName() + "'s & " +
                            trade.getSides()[1].getUser().get().getName(), TextColors.GREEN, "'s SafeTrade."
            ));
        }

        return CommandResult.success();
    }
}
