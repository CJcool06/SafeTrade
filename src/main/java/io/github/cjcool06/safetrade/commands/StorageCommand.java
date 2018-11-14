package io.github.cjcool06.safetrade.commands;

import com.google.common.collect.ImmutableMap;
import io.github.cjcool06.safetrade.SafeTrade;
import io.github.cjcool06.safetrade.managers.DataManager;
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

        if (src instanceof Player) {
            Player player = (Player)src;

            if (operation.equalsIgnoreCase("add")) {
                Optional<ItemStack> optItem = player.getItemInHand(HandTypes.MAIN_HAND);
                if (!optItem.isPresent()) {
                    player.sendMessage(Text.of(TextColors.RED, "You must have the item you want to add in your hand."));
                    return CommandResult.success();
                }
                Sponge.getScheduler().createTaskBuilder().execute(() -> {
                    DataManager.storeItem(user, optItem.get().createSnapshot());
                    player.sendMessage(Text.of(TextColors.GREEN, "Successfully added item to " + user.getName() + "'s SafeTrade storage."));
                }).async().submit(SafeTrade.getPlugin());
            }
        }

        if (operation.equalsIgnoreCase("clear")) {
            Sponge.getScheduler().createTaskBuilder().execute(() -> {
                DataManager.clearStoredItems(user);
                src.sendMessage(Text.of(TextColors.GREEN, "Successfully cleared " + user.getName() + "'s SafeTrade storage."));
            }).async().submit(SafeTrade.getPlugin());
        }
        else if (operation.equalsIgnoreCase("list")) {
            src.sendMessage(Text.of(TextColors.GOLD, user.getName() + "'s SafeTrade Storage:"));
            Sponge.getScheduler().createTaskBuilder().execute(() -> {
                for (ItemStackSnapshot snapshot : DataManager.getStoredItems(user)) {
                    src.sendMessage(Text.of(TextColors.GREEN, snapshot.getQuantity() + "x ", TextColors.AQUA, snapshot.getTranslation().get()));
                }
            }).async().submit(SafeTrade.getPlugin());
        }
        else {
            src.sendMessage(Text.of(TextColors.RED, "You must be a player to do that."));
        }

        return CommandResult.success();
    }
}
