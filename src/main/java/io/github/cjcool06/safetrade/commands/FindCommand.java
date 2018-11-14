package io.github.cjcool06.safetrade.commands;

import io.github.cjcool06.safetrade.guis.OptionsGUI;
import io.github.cjcool06.safetrade.listeners.ChatListener;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

public class FindCommand implements CommandExecutor {

    public static CommandSpec getSpec() {
        return CommandSpec.builder()
                .description(Text.of("Inquire about trading an element"))
                .permission("safetrade.common.find.gui")
                .arguments(GenericArguments.optional(GenericArguments.remainingJoinedStrings(Text.of("args"))))
                .executor(new FindCommand())
                .build();
    }

    public CommandResult execute(CommandSource src, CommandContext args) {

        if (src instanceof Player) {
            Player player = (Player)src;
            ChatListener.optionsGUISListening.removeIf(optionsGUI -> optionsGUI.getPlayer().getUniqueId().equals(player.getUniqueId()));
            ChatListener.listingsGUISListening.removeIf(listingsGUI -> listingsGUI.player.getUniqueId().equals(player.getUniqueId()));
            new OptionsGUI(player);
        }
        else {
            src.sendMessage(Text.of("You have nothing to trade though..."));
        }
        return CommandResult.success();
    }
}
