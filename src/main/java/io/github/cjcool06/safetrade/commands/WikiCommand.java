package io.github.cjcool06.safetrade.commands;

import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.net.MalformedURLException;
import java.net.URL;

public class WikiCommand implements CommandExecutor {

    public static CommandSpec getSpec() {
        return CommandSpec.builder()
                .description(Text.of("Get the wiki"))
                .permission("safetrade.common.wiki")
                .executor(new WikiCommand())
                .build();
    }

    public CommandResult execute(CommandSource src, CommandContext args) {

        try {
            src.sendMessage(Text.builder().append(Text.of(TextColors.GOLD, TextStyles.BOLD, "Click me for wiki")).onClick(TextActions.openUrl(new URL("https://github.com/CJcool06/SafeTrade/wiki"))).build());
        } catch (MalformedURLException me) {
            src.sendMessage(Text.of(TextColors.RED, "A problem has occurred, please report this to an administrator."));
        }

        return CommandResult.success();
    }
}
