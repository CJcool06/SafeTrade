package io.github.cjcool06.safetrade.commands;

import com.google.common.collect.ImmutableMap;
import io.github.cjcool06.safetrade.SafeTrade;
import io.github.cjcool06.safetrade.api.enums.PrefixType;
import io.github.cjcool06.safetrade.obj.PlayerStorage;
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

public class StorageCommand implements CommandExecutor {
    public static CommandSpec getSpec() {
        return CommandSpec.builder()
                .description(Text.of("Opens a player's storage"))
                .permission("safetrade.common.storage.view")
                .arguments(
                        GenericArguments.optional(
                                GenericArguments.user(Text.of("target"))
                        ),
                        GenericArguments.optional(
                                GenericArguments.choices(Text.of("options"),
                                        ImmutableMap.<String, String>builder()
                                                .put("clear", "clear")
                                                .build())
                        ))
                .executor(new StorageCommand())
                .build();
    }

    public CommandResult execute(CommandSource src, CommandContext args) {
        User target = args.<User>getOne("target").isPresent() ? args.<User>getOne("target").get() : (User) src;
        PlayerStorage storage;

        if (args.<String>getOne("options").isPresent()) {
            String operation = args.<String>getOne("options").get();

            if (operation.equalsIgnoreCase("clear")) {
                if (!src.hasPermission("safetrade.admin.storage.clear")) {
                    SafeTrade.sendMessageToCommandSource(src, PrefixType.STORAGE, Text.of(TextColors.RED, "You do not have permission to clear storages."));
                    return CommandResult.success();
                }

                storage = Tracker.getStorage(target);
                if (storage != null && !storage.isEmpty()) {
                    storage.clearAll();
                    SafeTrade.sendMessageToCommandSource(src, PrefixType.STORAGE, Text.of(TextColors.GOLD, target.getName() + "'s", TextColors.GREEN, " storage has been cleared."));
                }
                else {
                    SafeTrade.sendMessageToCommandSource(src, PrefixType.STORAGE, Text.of(TextColors.GOLD, target.getName() + "'s", TextColors.RED, " storage is already empty."));
                }
            }

            return CommandResult.success();
        }

        else if (src instanceof Player) {
            Player player = (Player)src;

            if (!player.getUniqueId().equals(target.getUniqueId()) && !player.hasPermission("safetrade.admin.storage.view")) {
                SafeTrade.sendMessageToPlayer(player, PrefixType.STORAGE, Text.of(TextColors.RED, "You do not have permission to open another player's storage"));
            }

            storage = Tracker.getOrCreateStorage(target);
            storage.open(player);
            SafeTrade.sendMessageToCommandSource(src, PrefixType.STORAGE, Text.of(TextColors.GREEN, "Opening ", player.getUniqueId().equals(storage.getPlayerUUID()) ? Text.of("your") : Text.of(TextColors.GOLD, target.getName() + "'s"), TextColors.GREEN, " storage."));
        }
        else {
            SafeTrade.sendMessageToCommandSource(src, PrefixType.STORAGE, Text.of(TextColors.RED, "You must be a player to do that."));
        }

        return CommandResult.success();
    }
}
