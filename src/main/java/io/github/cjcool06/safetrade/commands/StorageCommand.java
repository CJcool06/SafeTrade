package io.github.cjcool06.safetrade.commands;

import com.google.common.collect.ImmutableMap;
import io.github.cjcool06.safetrade.obj.CommandWrapper;
import io.github.cjcool06.safetrade.obj.PlayerStorage;
import io.github.cjcool06.safetrade.trackers.Tracker;
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
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

public class StorageCommand implements CommandExecutor {
    public static CommandSpec getSpec() {
        return CommandSpec.builder()
                .description(Text.of("Manipulate a player's storage"))
                .permission("safetrade.admin.storage")
                .arguments(
                        GenericArguments.user(Text.of("target")),
                        GenericArguments.choices(Text.of("options"),
                        ImmutableMap.<String, String>builder()
                                .put("add", "add")
                                .put("clear", "clear")
                                .put("list", "list")
                                .build()))
                .executor(new StorageCommand())
                .build();
    }

    public CommandResult execute(CommandSource src, CommandContext args) {
        User user = args.<User>getOne("target").get();
        String operation = args.<String>getOne("options").get();
        PlayerStorage storage = Tracker.getOrCreateStorage(user);

        if (src instanceof Player) {
            Player player = (Player)src;

            if (operation.equalsIgnoreCase("add")) {
                Optional<ItemStack> optItem = player.getItemInHand(HandTypes.MAIN_HAND);
                if (!optItem.isPresent()) {
                    player.sendMessage(Text.of(TextColors.RED, "You must have the item you want to add in your hand."));
                    return CommandResult.success();
                }
                storage.addItem(optItem.get().createSnapshot());
                player.sendMessage(Text.of(TextColors.GREEN, "Successfully added item to " + user.getName() + "'s SafeTrade storage."));
            }
        }

        if (operation.equalsIgnoreCase("clear")) {
            storage.clearItems();
            src.sendMessage(Text.of(TextColors.GREEN, "Successfully cleared " + user.getName() + "'s SafeTrade storage."));
        }
        else if (operation.equalsIgnoreCase("list")) {
            src.sendMessage(Text.of(TextColors.GOLD, user.getName() + "'s SafeTrade Storage:"));
            src.sendMessage(Text.of());

            for (ItemStackSnapshot snapshot : storage.getItems()) {
                src.sendMessage(Text.of(TextColors.GREEN, snapshot.getQuantity() + "x ", TextColors.AQUA, snapshot.getTranslation().get()));
            }
            src.sendMessage(Text.of());

            for (CommandWrapper wrapper : storage.getCommands()) {
                src.sendMessage(Text.of(TextColors.AQUA, wrapper.cmd));
            }
            src.sendMessage(Text.of());

        }
        else {
            src.sendMessage(Text.of(TextColors.RED, "You must be a player to do that."));
        }

        return CommandResult.success();
    }
}
