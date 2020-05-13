package io.github.cjcool06.safetrade.commands;

import io.github.cjcool06.safetrade.SafeTrade;
import io.github.cjcool06.safetrade.api.enums.InventoryType;
import io.github.cjcool06.safetrade.api.enums.PrefixType;
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
                .child(EndCommand.getSpec(), "end")
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

            contents.add(Text.of(TextColors.AQUA, "/safetrade <player>", TextColors.GRAY, " - ", TextColors.GRAY, "Request/Accept a SafeTrade"));
            contents.add(Text.of(TextColors.AQUA, "/safetrade open", TextColors.GRAY, " - ", TextColors.GRAY, "Open your current SafeTrade"));
            contents.add(Text.of(TextColors.AQUA, "/safetrade wiki", TextColors.GRAY, " - ", TextColors.GRAY, "Gives the wiki link"));
            contents.add(Text.of(TextColors.AQUA, "/safetrade end <player>", TextColors.GRAY, " - ", TextColors.GRAY, "Force end a SafeTrade"));
            contents.add(Text.of(TextColors.AQUA, "/safetrade view <player>", TextColors.GRAY, " - ", TextColors.GRAY, "View a player's SafeTrade"));
            contents.add(Text.of(TextColors.AQUA, "/safetrade logs <user> [other user]", TextColors.GRAY, " - ", TextColors.GRAY, "Browse a player's SafeTrade logs"));
            contents.add(Text.of(TextColors.AQUA, "/safetrade storage <user> [clear]", TextColors.GRAY, " - ", TextColors.GRAY, "Open a user's SafeTrade storage"));
            contents.add(Text.of(TextColors.AQUA, "/safetrade reload", TextColors.GRAY, " - ", TextColors.GRAY, "Reloads the config"));

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
                SafeTrade.sendMessageToPlayer(player, PrefixType.SAFETRADE, Text.of(Text.of(TextColors.RED, "You can't trade with yourself you banana.")));
            }
            else if (Tracker.getActiveTrade(player) != null) {
                Side side = Tracker.getActiveTrade(player).getSide(player.getUniqueId()).get();
                SafeTrade.sendMessageToPlayer(player, PrefixType.SAFETRADE, Text.of(TextColors.RED, "You are already a participant in a SafeTrade.", "\n",
                        Text.of(TextColors.GOLD, TextActions.executeCallback(dummySrc -> Sponge.getCommandManager().process(side.getPlayer().get(), "safetrade open"))), "Click here to open your existing trade."));
            }
            else if (Tracker.getActiveTrade(target) != null) {
                SafeTrade.sendMessageToPlayer(player, PrefixType.SAFETRADE, Text.of(TextColors.RED, "That player is currently trading with another player."));
            }
            else if (tradeRequests.containsKey(player) && tradeRequests.get(player).contains(target)) {
                SafeTrade.sendMessageToPlayer(player, PrefixType.SAFETRADE, Text.of(TextColors.RED, "There is already a trade request pending with that player. Requests expire after 2 minutes."));
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
            SafeTrade.sendMessageToCommandSource(src, PrefixType.SAFETRADE, Text.of(TextColors.RED, "You must be a player to do that."));
        }

        return CommandResult.success();
    }

    public static void requestTrade(Player requester, Player requestee) {
        SafeTrade.sendMessageToPlayer(requestee, PrefixType.SAFETRADE, Text.of(TextColors.GOLD, requester.getName(), TextColors.GREEN, " has requested a trade. ",
                Text.of(TextColors.DARK_GREEN, TextActions.executeCallback(dummySrc -> acceptInvitation(requester, requestee)), "[Accept]"),
                " ",
                Text.of(TextColors.RED, TextActions.executeCallback(dummySrc -> rejectInvitation(requester, requestee)), "[Decline]")));

        if (!tradeRequests.containsKey(requester)) {
            tradeRequests.put(requester, new ArrayList<>());
        }
        tradeRequests.get(requester).add(requestee);

        SafeTrade.sendMessageToPlayer(requester, PrefixType.SAFETRADE, Text.of(TextColors.GREEN, "Trade request sent to ", TextColors.GOLD, requestee.getName(), TextColors.GREEN, "."));

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
            SafeTrade.sendMessageToPlayer(requester, PrefixType.SAFETRADE, Text.of(TextColors.GOLD, requestee.getName(), TextColors.RED, " rejected your trade request."));
            SafeTrade.sendMessageToPlayer(requestee, PrefixType.SAFETRADE, Text.of(TextColors.RED, "Rejected ", TextColors.GOLD, requester.getName() +  "'s ", TextColors.RED, " trade request."));
        }
    }

    public static void acceptInvitation(Player requester, Player requestee) {
        if (tradeRequests.containsKey(requester) && tradeRequests.get(requester).contains(requestee)) {
            tradeRequests.get(requester).remove(requestee);

            if (Utils.isPlayerOccupied(requester)) {
                SafeTrade.sendMessageToPlayer(requester, PrefixType.SAFETRADE, Text.of(TextColors.GOLD, requestee.getName(), TextColors.RED, " has accepted your trade request, but you are otherwise occupied."));
                SafeTrade.sendMessageToPlayer(requestee, PrefixType.SAFETRADE, Text.of(TextColors.RED, "You have accepted ", TextColors.GOLD, requester.getName() + "'s", TextColors.RED, " trade request, but they are otherwise occupied."));
                return;
            }
            if (Utils.isPlayerOccupied(requestee)) {
                requester.sendMessage(Text.of(TextColors.GOLD, requestee.getName(), TextColors.RED, " has accepted your trade request, but they are otherwise occupied."));
                requestee.sendMessage(Text.of(TextColors.RED, "You have accepted ", TextColors.GOLD, requester.getName() + "'s", TextColors.RED, " trade request, but you are otherwise occupied."));
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
