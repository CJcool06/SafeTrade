package io.github.cjcool06.safetrade.commands;

import com.google.common.collect.Lists;
import io.github.cjcool06.safetrade.SafeTrade;
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
                .permission("safetrade.admin.logs")
                .arguments(
                        GenericArguments.user(Text.of("target")),
                        GenericArguments.optional(GenericArguments.user(Text.of("target2"))))
                .executor(new LogsCommand())
                .build();
    }

    public CommandResult execute(CommandSource src, CommandContext args) {
        User target = args.<User>getOne("target").get();
        User target2 = args.<User>getOne("target2").isPresent() ? args.<User>getOne("target2").get().equals(target) ? null : args.<User>getOne("target2").get() : null;
        src.sendMessage(Text.of(TextColors.GRAY, "Grabbing SafeTrade logs, please wait..."));
        showLogs(src, target, target2);
        return CommandResult.success();
    }

    public static void showLogs(CommandSource src, User target, @Nullable User target2) {
        Sponge.getScheduler().createTaskBuilder().execute(() -> {
            List<Text> contents = new ArrayList<>();
            ArrayList<Log> logs = target2 == null ? DataManager.getLogs(target) : LogUtils.getLogsOf(target, target2);
            for (Log log : logs) {
                // Legacy logs
                if (log.getParticipantUUID() == null) {
                    contents.add(Text.builder().append(Text.builder().append(Text.of(TextColors.RED, "[", TextColors.DARK_RED, "-", TextColors.RED, "] "))
                            .onHover(TextActions.showText(Text.of(TextColors.GRAY, "Click to delete log")))
                            .onClick(TextActions.executeCallback(dummySrc -> {
                                DataManager.removeLog(target, log);
                                showLogs(src, target, target2);
                            })).build())
                            .append(log.getText()).build());
                }
                // Current logs
                else {
                    contents.add(Text.builder().append(Text.builder().append(Text.of(TextColors.RED, "[", TextColors.DARK_RED, "-", TextColors.RED, "] "))
                            .onHover(TextActions.showText(Text.of(TextColors.GRAY, "Click to delete log")))
                            .onClick(TextActions.executeCallback(dummySrc -> {
                                DataManager.removeLog(target, log);
                                showLogs(src, target, target2);
                            })).build())
                            .append(log.getDisplayText()).build());
                }
            }
            List<Text> reverseContents = Lists.reverse(contents);

            PaginationList.builder()
                    .title(Text.of(TextColors.GREEN, target.getName() + (target2 != null ? (" & " + target2.getName()) : "") + "'s Logs "))
                    .contents(reverseContents)
                    .padding(Text.of(TextColors.GRAY, "-", TextColors.RESET))
                    .sendTo(src);
        }).async().delayTicks(1).submit(SafeTrade.getPlugin());
    }
}
