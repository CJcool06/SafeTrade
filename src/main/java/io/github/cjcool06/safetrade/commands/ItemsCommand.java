package io.github.cjcool06.safetrade.commands;

import com.google.common.collect.ImmutableMap;
import io.github.cjcool06.safetrade.data.SafeTradeData;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

public class ItemsCommand implements CommandExecutor {
    public static CommandSpec getSpec() {
        return CommandSpec.builder()
                .description(Text.of("Manipulate a player's pending items"))
                .permission("safetrade.admin.items")
                .arguments(GenericArguments.choices(Text.of("options"),
                        ImmutableMap.<String, String>builder()
                                .put("add", "add")
                                .put("clear", "clear")
                                .put("list", "list")
                                .build()),
                        GenericArguments.string(Text.of("target")))
                .executor(new ItemsCommand())
                .build();
    }

    public CommandResult execute(CommandSource src, CommandContext args) {
        if (src instanceof Player) {
            Player player = (Player)src;
            String targetString = args.<String>getOne("target").get();
            Optional<User> user = Sponge.getServiceManager().provide(UserStorageService.class).get().get(targetString);
            String operation = args.<String>getOne("options").get();

            if (!user.isPresent()) {
                player.sendMessage(Text.of(TextColors.RED, "User could not be found."));
                return CommandResult.success();
            }
            SafeTradeData data;
            if (user.get().isOnline()) {
                data = user.get().getPlayer().get().get(SafeTradeData.class).orElse(new SafeTradeData());
            }
            else {
                data = user.get().get(SafeTradeData.class).orElse(new SafeTradeData());
            }

            if (operation.equalsIgnoreCase("add")) {
                Optional<ItemStack> optItem = player.getItemInHand(HandTypes.MAIN_HAND);
                if (!optItem.isPresent()) {
                    player.sendMessage(Text.of(TextColors.RED, "You must have the item you want to add in your hand."));
                    return CommandResult.success();
                }
                data.addPendingItem(optItem.get().createSnapshot());
                //player.get(SafeTradeData.class).get().addPendingItem(optItem.get().createSnapshot());   // Debug
                player.sendMessage(Text.of(TextColors.GREEN, "Successfully added item to " + user.get().getName() + "'s SafeTrade storage."));
            }
            else if (operation.equalsIgnoreCase("clear")) {
                data.clearPendingItems();
                player.sendMessage(Text.of(TextColors.GREEN, "Successfully cleared " + user.get().getName() + "'s SafeTrade storage."));
            }
            else if (operation.equalsIgnoreCase("list")) {
                player.sendMessage(Text.of(TextColors.GOLD, user.get().getName() + "'s SafeTrade storage:"));
                for (ItemStackSnapshot snapshot : data.getPendingItems().get()) {
                    player.sendMessage(Text.of(TextColors.GREEN, snapshot.getQuantity() + "x ", TextColors.AQUA, snapshot.getTranslation().get()));
                }
            }
        }
        else {
            src.sendMessage(Text.of(TextColors.RED, "You must be a player to do that."));
        }

        return CommandResult.success();
    }
}
