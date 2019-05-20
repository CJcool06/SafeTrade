package io.github.cjcool06.safetrade.commands;

import io.github.cjcool06.safetrade.SafeTrade;
import io.github.cjcool06.safetrade.api.enums.PrefixType;
import io.github.cjcool06.safetrade.config.Config;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class ReloadCommand implements CommandExecutor {

    public static CommandSpec getSpec() {
        return CommandSpec.builder()
                .description(Text.of("Reloads config"))
                .permission("safetrade.admin.reload")
                .executor(new ReloadCommand())
                .build();
    }

    public CommandResult execute(CommandSource src, CommandContext args) {
        Config.load();
        SafeTrade.sendMessageToCommandSource(src, PrefixType.CONFIG, Text.of(TextColors.GRAY, "Config reloaded."));

        return CommandResult.success();
    }
}
