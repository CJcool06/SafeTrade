package io.github.cjcool06.safetrade.commands;

import io.github.cjcool06.safetrade.managers.DataManager;
import io.github.cjcool06.safetrade.obj.Trade;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class EndTradeCommand implements CommandExecutor {
    public static CommandSpec getSpec() {
        return CommandSpec.builder()
                .description(Text.of("End a trade"))
                .permission("safetrade.admin.end")
                .arguments(GenericArguments.player(Text.of("target")))
                .executor(new EndTradeCommand())
                .build();
    }

    public CommandResult execute(CommandSource src, CommandContext args) {
        Player target = args.<Player>getOne("target").get();
        Trade trade = DataManager.getTrade(target);

        if (trade == null) {
            src.sendMessage(Text.of(TextColors.RED, "That player is not in a trade."));
            return CommandResult.success();
        }
        trade.forceEnd();
        src.sendMessage(Text.of(TextColors.GREEN, "Cancelled " + target.getName() + "'s safe trade."));
        trade.sendMessage(Text.of(TextColors.GRAY, "Trade force ended by " + src.getName() + "."));

        return CommandResult.success();
    }
}
