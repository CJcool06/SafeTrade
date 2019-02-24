package io.github.cjcool06.safetrade.commands;

import io.github.cjcool06.safetrade.SafeTrade;
import io.github.cjcool06.safetrade.api.enums.InventoryType;
import io.github.cjcool06.safetrade.api.events.trade.ConnectionEvent;
import io.github.cjcool06.safetrade.obj.Side;
import io.github.cjcool06.safetrade.obj.Trade;
import io.github.cjcool06.safetrade.trackers.Tracker;
import io.github.cjcool06.safetrade.utils.Utils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TradeCommand implements CommandExecutor {
    private static HashMap<User, ArrayList<User>> tradeRequests = new HashMap<>();

    public static CommandSpec getSpec() {
        return CommandSpec.builder()
                .description(Text.of("Trade with another player"))
                .permission("safetrade.common.trade")
                .arguments(GenericArguments.optional(GenericArguments.player(Text.of("target"))))
                .executor(new TradeCommand())
                .child(OpenCommand.getSpec(), "open")
                .child(EndTradeCommand.getSpec(), "end")
                .child(StorageCommand.getSpec(), "storage")
                .child(LogsCommand.getSpec(), "logs")
                .child(ViewCommand.getSpec(), "view")
                //.child(TestCommand.getSpec(), "test")
                .child(ReloadCommand.getSpec(), "reload")
                .child(WikiCommand.getSpec(), "wiki")
                .build();
    }

    public CommandResult execute(CommandSource src, CommandContext args) {
        if (!args.<Player>getOne("target").isPresent()) {
            List<Text> contents = new ArrayList<>();

            contents.add(Text.of(TextColors.AQUA, "/safetrade <player>", TextColors.GRAY, " - ", TextColors.GRAY, "Request a SafeTrade"));
            contents.add(Text.of(TextColors.AQUA, "/safetrade open", TextColors.GRAY, " - ", TextColors.GRAY, "Open your current SafeTrade"));
            contents.add(Text.of(TextColors.AQUA, "/safetrade wiki", TextColors.GRAY, " - ", TextColors.GRAY, "Gives the wiki link"));
            contents.add(Text.of(TextColors.AQUA, "/safetrade end <player>", TextColors.GRAY, " - ", TextColors.GRAY, "Force end a SafeTrade"));
            contents.add(Text.of(TextColors.AQUA, "/safetrade view <player>", TextColors.GRAY, " - ", TextColors.GRAY, "View a player's SafeTrade"));
            contents.add(Text.of(TextColors.AQUA, "/safetrade logs <user> [other user]", TextColors.GRAY, " - ", TextColors.GRAY, "Browse a player's SafeTrade logs"));
            contents.add(Text.of(TextColors.AQUA, "/safetrade storage <user> <add | clear | list>", TextColors.GRAY, " - ", TextColors.GRAY, "Manipulate a player's SafeTrade storage"));
            contents.add(Text.of(TextColors.AQUA, "/safetrade reload", TextColors.GRAY, " - ", TextColors.GRAY, "Reloads config"));

            PaginationList.builder()
                    .title(Text.of(TextColors.GREEN, " SafeTrade "))
                    .contents(contents)
                    .padding(Text.of(TextColors.AQUA, "-", TextColors.RESET))
                    .sendTo(src);
            return CommandResult.success();
        }

        if (src instanceof Player) {
            Player player = (Player)src;
            Player target = args.<Player>getOne("target").get();

            if (player.equals(target)) {
                player.sendMessage(Text.of(Text.of(TextColors.RED, "You can't trade with yourself you banana.")));
            }
            else if (Tracker.getActiveTrade(player) != null) {
                player.sendMessage(Text.of(TextColors.RED, "You are already a participant in a SafeTrade."));
            }
            else if (Tracker.getActiveTrade(target) != null) {
                player.sendMessage(Text.of(TextColors.RED, "That player is currently SafeTrading with another player."));
            }
            else if (tradeRequests.containsKey(player) && tradeRequests.get(player).contains(target)) {
                player.sendMessage(Text.of(TextColors.RED, "There is already a SafeTrade request pending with that player. Requests expire after 2 minutes."));
            }
            // Catches if the requestee uses the command to trade instead of using the executable.
            else if (tradeRequests.containsKey(target) && tradeRequests.get(target).contains(player)) {
                acceptInvitation(target, player);
            }
            else {
                requestTrade(player, target);
            }
        }
        else {
            src.sendMessage(Text.of(TextColors.RED, "You must be a player to SafeTrade!"));
        }

        return CommandResult.success();
    }

    public static void requestTrade(Player requester, Player requestee) {
        requestee.sendMessage(Text.of(TextColors.DARK_AQUA, requester.getName(), TextColors.GRAY, " has requested a SafeTrade. ",
                Text.of(TextColors.GREEN, TextActions.executeCallback(dummySrc -> acceptInvitation(requester, requestee)), "[Accept]"),
                " ",
                Text.of(TextColors.RED, TextActions.executeCallback(dummySrc -> rejectInvitation(requester, requestee)), "[Decline]")));

        if (!tradeRequests.containsKey(requester)) {
            tradeRequests.put(requester, new ArrayList<>());
        }
        tradeRequests.get(requester).add(requestee);

        requester.sendMessage(Text.of(TextColors.GRAY, "SafeTrade request sent to ", TextColors.DARK_AQUA, requestee.getName(), TextColors.GRAY, "."));

        // Cancels request after 2 minutes
        Sponge.getScheduler().createTaskBuilder()
                .execute(() -> {
                    if (tradeRequests.containsKey(requester) && tradeRequests.get(requester).contains(requestee))
                        tradeRequests.get(requester).remove(requestee);
                })
                .delay(2, TimeUnit.MINUTES)
                .async()
                .submit(SafeTrade.getPlugin());
    }

    public static void rejectInvitation(Player requester, Player requestee) {
        if (tradeRequests.containsKey(requester) && tradeRequests.get(requester).contains(requestee)) {
            tradeRequests.get(requester).remove(requestee);
            requester.sendMessage(Text.of(TextColors.DARK_AQUA, requestee.getName(), TextColors.RED, " rejected your SafeTrade request."));
            requestee.sendMessage(Text.of(TextColors.GRAY, "Rejected ", TextColors.DARK_AQUA, requester.getName(), TextColors.GRAY, "'s SafeTrade request."));
        }
    }

    public static void acceptInvitation(Player requester, Player requestee) {
        if (tradeRequests.containsKey(requester) && tradeRequests.get(requester).contains(requestee)) {
            tradeRequests.get(requester).remove(requestee);
            if (Utils.isPlayerOccupied(requester)) {
                requester.sendMessage(Text.of(TextColors.RED, requestee.getName() + " has accepted your SafeTrade request, but you are otherwise occupied."));
                requestee.sendMessage(Text.of(TextColors.RED, "You have accepted " + requester.getName() + "'s SafeTrade request, but they are otherwise occupied."));
                return;
            }
            if (Utils.isPlayerOccupied(requestee)) {
                requester.sendMessage(Text.of(TextColors.RED, requestee.getName() + " has accepted your SafeTrade request, but they are otherwise occupied."));
                requestee.sendMessage(Text.of(TextColors.RED, "You have accepted " + requester.getName() + "'s SafeTrade request, but you are otherwise occupied."));
                return;
            }

            // The initial open needs to be like this, otherwise players will be flagged as paused unless they pause or close inv and resume.
            // This is because no player cause is given to the InteractInventoryEvent.Open event. Not sure why.
            Trade trade = new Trade(requester, requestee);
            Side side0 = trade.getSides()[0];
            Side side1 = trade.getSides()[1];
            side0.getPlayer().ifPresent(player -> {
                side0.setPaused(false);
                trade.reformatInventory();
                side0.changeInventory(InventoryType.MAIN);
                SafeTrade.EVENT_BUS.post(new ConnectionEvent.Join.Post(side0));
            });
            side1.getPlayer().ifPresent(player -> {
                side1.setPaused(false);
                trade.reformatInventory();
                side1.changeInventory(InventoryType.MAIN);
                SafeTrade.EVENT_BUS.post(new ConnectionEvent.Join.Post(side1));
            });
        }
    }
}
