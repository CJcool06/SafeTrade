package io.github.cjcool06.safetrade.commands;

import com.google.common.collect.Lists;
import io.github.cjcool06.safetrade.SafeTrade;
import io.github.cjcool06.safetrade.api.enums.PrefixType;
import io.github.cjcool06.safetrade.managers.DataManager;
import io.github.cjcool06.safetrade.obj.Log;
import io.github.cjcool06.safetrade.utils.LogUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class LogsCommand implements CommandExecutor {

    public static CommandSpec getSpec() {
        return CommandSpec.builder()
                .description(Text.of("List player logs"))
                .permission("safetrade.admin.logs.view")
                .arguments(
                        GenericArguments.user(Text.of("target")),
                        GenericArguments.optional(GenericArguments.user(Text.of("target2"))))
                .executor(new LogsCommand())
                .build();
    }

    public CommandResult execute(CommandSource src, CommandContext args) {
        User target = args.<User>getOne("target").get();
        User target2 = args.<User>getOne("target2").isPresent() ? args.<User>getOne("target2").get().equals(target) ? null : args.<User>getOne("target2").get() : null;

        SafeTrade.sendMessageToCommandSource(src, PrefixType.LOG, Text.of(TextColors.GRAY, "Getting logs, please wait..."));

        showLogs(src, target, target2);

        return CommandResult.success();
    }

    public static void showLogs(CommandSource src, User target, @Nullable User target2) {
        // Async
        Sponge.getScheduler().createTaskBuilder().execute(() -> {
            List<Text> contents = new ArrayList<>();
            ArrayList<Log> logs = target2 == null ? DataManager.getLogs(target) : LogUtils.getLogsOf(target, target2);
            for (Log log : logs) {

                // If the player has the permission "safetrade.admin.logs.delete" they will be able to delete logs.

                // Legacy logs
                if (log.getParticipantUUID() == null) {
                    contents.add(Text.builder().append(
                            src.hasPermission("safetrade.admin.logs.delete") ?
                                    Text.builder().append(Text.of(TextColors.RED, "[", TextColors.DARK_RED, "-", TextColors.RED, "] "))
                                            .onHover(TextActions.showText(Text.of(TextColors.GRAY, "Click to delete log")))
                                            .onClick(TextActions.executeCallback(dummySrc -> {
                                                DataManager.removeLog(target, log);
                                                showLogs(src, target, target2);
                                            })).build()
                                    : Text.of()
                    ).append(log.getText()).build());
                }

                // Current logs
                else {
                    contents.add(
                            Text.builder().append(
                                src.hasPermission("safetrade.admin.logs.delete") ?
                                Text.builder().append(Text.of(TextColors.RED, "[", TextColors.DARK_RED, "-", TextColors.RED, "] "))
                                .onHover(TextActions.showText(Text.of(TextColors.GRAY, "Click to delete log")))
                                .onClick(TextActions.executeCallback(dummySrc -> {
                                    DataManager.removeLog(target, log);
                                    showLogs(src, target, target2);
                                })).build()
                                : Text.of()
                            ).append(log.getDisplayText()).build());
                }
            }
            List<Text> reversedContents = Lists.reverse(contents);

            PaginationList.builder()
                    .title(Text.of(" ", TextColors.GREEN, target.getName() + "'s" + (target2 != null ? (" & " + target2.getName()+ "'s") : "") + " Logs "))
                    .contents(reversedContents)
                    .padding(Text.of(TextColors.GRAY, "-", TextColors.RESET))
                    .sendTo(src);
        }).async().delayTicks(1).submit(SafeTrade.getPlugin());
    }
}
