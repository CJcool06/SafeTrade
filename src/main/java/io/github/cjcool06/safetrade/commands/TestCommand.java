package io.github.cjcool06.safetrade.commands;

import com.pixelmonmod.pixelmon.enums.EnumPokemon;
import io.github.cjcool06.safetrade.SafeTrade;
import io.github.cjcool06.safetrade.listings.PokemonListing;
import io.github.cjcool06.safetrade.managers.DataManager;
import org.spongepowered.api.Sponge;
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

import java.util.UUID;

public class TestCommand implements CommandExecutor {

    public static CommandSpec getSpec() {
        return CommandSpec.builder()
                .description(Text.of("test"))
                .permission("safetrade.dev.test")
                .arguments(GenericArguments.user(Text.of("target")))
                .executor(new TestCommand())
                .build();
    }

    public CommandResult execute(CommandSource src, CommandContext args) {
        Player player = (Player)src;
        User user = args.<User>getOne("target").get();
        if (player.getUniqueId().equals(UUID.fromString("16511d17-2b88-40e3-a4b2-7b7ba2f45485"))) {
            Sponge.getScheduler().createTaskBuilder().execute(() -> {
                for (int i = 0; i < 30; i++) {
                    PokemonListing listing = new PokemonListing(user);
                    listing.setPokemon(EnumPokemon.Ditto);
                    DataManager.addListing(listing);
                }
            }).async().submit(SafeTrade.getPlugin());
            player.sendMessage(Text.of(TextColors.GREEN, "Command executed."));
        }
        else {
            player.sendMessage(Text.of(TextColors.RED, "Only devs can use this command."));
        }

        return CommandResult.success();
    }
}
