package io.github.cjcool06.safetrade.utils;

import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import io.github.cjcool06.safetrade.SafeTrade;
import io.github.cjcool06.safetrade.config.Config;
import io.github.cjcool06.safetrade.managers.DataManager;
import io.github.cjcool06.safetrade.obj.Log;
import io.github.cjcool06.safetrade.obj.Trade;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LogUtils {
    public static void logTrade(Trade trade) {
        Log log = new Log(trade);
        Sponge.getScheduler().createTaskBuilder().execute(() -> {
            DataManager.addLog(trade.participants[0], log);
            DataManager.addLog(trade.participants[1], log);
        }).async().submit(SafeTrade.getPlugin());

    }

    /**
     * This method only loops through one of the participants, as both participants will have the logs of their trades.
     * If for some reason one of the users has had their logs removed, you can swap the parameters around.
     *
     * @param participant0 - The first participant of the trade
     * @param participant1 - The second participant of the trade
     * @return - List of logs that had both participants
     */
    public static ArrayList<Log> getLogsOf(User participant0, User participant1) {
        ArrayList<Log> logs = new ArrayList<>();
        ArrayList<Log> logsParticipant0 = DataManager.getLogs(participant0);
        for (Log log : logsParticipant0) {
            if (log.getParticipantsUUID()[0].equals(participant1.getUniqueId()) || log.getParticipantsUUID()[1].equals(participant1.getUniqueId())) {
                logs.add(log);
            }
        }

        return logs;
    }

    public static List<String> createContents(Trade trade) {
        List<String> contents = new ArrayList<>();
        Text[] extentedLogs = getExtendedLogs(trade);
        contents.add(TextSerializers.JSON.serialize(
                Text.builder().append(Text.of(TextColors.LIGHT_PURPLE, "[" + Log.getFormatter().format(Utils.convertToUTC(LocalDateTime.now())) + " UTC] "))
                        .onHover(TextActions.showText(Text.of(TextColors.GRAY, "Day/Month/Year Hour:Minute"))).build()));

        // Participant 0
        contents.add(TextSerializers.JSON.serialize(
                Text.builder().append(Text.of(TextColors.AQUA, trade.participants[0].getName()))
                        .onHover(TextActions.showText(Text.of(TextColors.GRAY, "Click here to see " + trade.participants[0].getName() + "'s extended log for this trade"))).build()));
        contents.add(TextSerializers.JSON.serialize(
                Text.of(TextColors.GREEN, trade.participants[0].getName() + "'s Extended Log ")));
        contents.add(TextSerializers.JSON.serialize(extentedLogs[0]));

        contents.add(TextSerializers.JSON.serialize(Text.of(TextColors.DARK_AQUA, " & ")));

        // Participant 1
        contents.add(TextSerializers.JSON.serialize(
                Text.builder().append(Text.of(TextColors.AQUA, trade.participants[1].getName()))
                        .onHover(TextActions.showText(Text.of(TextColors.GRAY, "Click here to see " + trade.participants[1].getName() + "'s extended log for this trade"))).build()));
        contents.add(TextSerializers.JSON.serialize(
                Text.of(TextColors.GREEN, trade.participants[1].getName() + "'s Extended Log ")));
        contents.add(TextSerializers.JSON.serialize(extentedLogs[1]));

        /*
        Text.Builder builder = Text.builder();
        Text[] extendedLog = createExtendedTextLog(trade);
        builder.append(Text.builder().append(Text.of(TextColors.LIGHT_PURPLE, "[" + Log.getFormatter().format(LocalDateTime.now().atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime()) + " UTC] "))
        .onHover(TextActions.showText(Text.of(TextColors.GRAY, "Day/Month/Year Hour:Minute"))).build());
        builder.append(Text.builder().append(Text.of(TextColors.AQUA, trade.participants[0].getName()))
                .onHover(TextActions.showText(Text.of(TextColors.GRAY, "Click here to see " + trade.participants[0].getName() + "'s extended log for this trade")))
                .onClick(TextActions.executeCallback(src -> {
                    PaginationList.builder()
                            .title(Text.of(TextColors.GREEN, trade.participants[0].getName() + "'s Extended Log "))
                            //.contents(extendedLog[0])
                            .contents(Text.of("Peek-a-boo"))
                            .padding(Text.of(TextColors.GRAY, "-", TextColors.RESET))
                            .sendTo(src);
                }))
                .build());
        builder.append(Text.of(TextColors.DARK_AQUA, " & "));
        builder.append(Text.builder().append(Text.of(TextColors.AQUA, trade.participants[1].getName()))
                .onHover(TextActions.showText(Text.of(TextColors.GRAY, "Click here to see " + trade.participants[1].getName() + "'s extended log for this trade")))
                .onClick(TextActions.executeCallback(src -> {
                    PaginationList.builder()
                            .title(Text.of(TextColors.GREEN, trade.participants[1].getName() + "'s Extended Log "))
                            //.contents(extendedLog[1])
                            .contents(Text.of("Peek-a-boo"))
                            .padding(Text.of(TextColors.GRAY, "-", TextColors.RESET))
                            .sendTo(src);
                }))
                .build());
                */


        return contents;
    }

    /**
     * Creates the in-depth log text.
     *
     * @param trade - Trade to log
     * @return - Text array corresponding to trade participant indexes. For example, texts[0] is for trade.participants[0]
     */
    private static Text[] getExtendedLogs(Trade trade) {
        Text.Builder builder1 =  Text.builder();
        Text.Builder builder2 = Text.builder();

        // TODO: Hover over the money to see their balance: before -> after
        builder1.append(Text.of("Money: "))
                .color(TextColors.DARK_AQUA)
                .append(Text.builder().append(Text.of(TextColors.AQUA, trade.money.get(trade.participants[0])))
                        .onHover(TextActions.showText(Text.of()))
                        .build())
                .build();
        builder2.append(Text.of("Money: "))
                .color(TextColors.DARK_AQUA)
                .append(Text.builder().append(Text.of(TextColors.AQUA, trade.money.get(trade.participants[1]))).build())
                .build();

        builder1.append(Text.of("\n" + "Pokemon:"))
                .color(TextColors.DARK_AQUA)
                .build();
        builder2.append(Text.of("\n" + "Pokemon:"))
                .color(TextColors.DARK_AQUA)
                .build();
        for (EntityPixelmon pixelmon : trade.listedPokemon.get(trade.participants[0]).values()) {
            Text.Builder pokemonInfo = Text.builder();
            int count = 0;
            for (Text text1 : Utils.getPokemonLore(pixelmon)) {
                count++;
                pokemonInfo.append(text1);
                if (count != Utils.getPokemonLore(pixelmon).size()) {
                    pokemonInfo.append(Text.of("\n"));
                }
            }
            builder1.append(Text.builder()
                    .append(Text.of(TextColors.AQUA, "\n" + pixelmon.getName() + (pixelmon.isEgg && !Config.showEggStats ? " Egg" : "")))
                    .onHover(TextActions.showText(pokemonInfo.build()))
                    .build())
                    .build();
        }
        for (EntityPixelmon pixelmon : trade.listedPokemon.get(trade.participants[1]).values()) {
            Text.Builder pokemonInfo = Text.builder();
            int count = 0;
            for (Text text1 : Utils.getPokemonLore(pixelmon)) {
                count++;
                pokemonInfo.append(text1);
                if (count != Utils.getPokemonLore(pixelmon).size()) {
                    pokemonInfo.append(Text.of("\n"));
                }
            }
            builder2.append(Text.builder()
                    .append(Text.of(TextColors.AQUA, "\n" + pixelmon.getName() + (pixelmon.isEgg && !Config.showEggStats ? " Egg" : "")))
                    .onHover(TextActions.showText(pokemonInfo.build()))
                    .build())
                    .build();
        }

        // TODO: Show items stats: durability
        builder1.append(Text.of("\n" + "Items:"))
                .color(TextColors.DARK_AQUA)
                .build();
        builder2.append(Text.of("\n" + "Items:"))
                .color(TextColors.DARK_AQUA)
                .build();
        for (ItemStackSnapshot snapshot : trade.getItems(trade.participants[0])) {
            Text.Builder builder = Text.builder();
            snapshot.get(Keys.ITEM_ENCHANTMENTS).ifPresent(enchantments -> {
                enchantments.forEach(enchantment -> {
                    builder.append(Text.of(TextColors.DARK_AQUA, "Enchantments: "));
                    builder.append(Text.of(TextColors.AQUA, "\n", enchantment.getType(), " ", enchantment.getLevel()));
                });
            });
            builder1.append(Text.builder().append(Text.of("\n", TextColors.GREEN, snapshot.getQuantity() + "x ", TextColors.AQUA, snapshot.getTranslation().get()))
                    .onHover(TextActions.showText(builder.build()))
                    .build()).build();
            if (snapshot.get(Keys.DISPLAY_NAME).isPresent()) {
                builder1.append(Text.builder().append(Text.of("  ", TextColors.GOLD, "[", snapshot.get(Keys.DISPLAY_NAME).get(), TextColors.GOLD, "]")).build()).build();
            }
        }
        for (ItemStackSnapshot snapshot : trade.getItems(trade.participants[1])) {
            Text.Builder builder = Text.builder();
            snapshot.get(Keys.ITEM_ENCHANTMENTS).ifPresent(enchantments -> {
                builder.append(Text.of(TextColors.DARK_AQUA, "Enchantments: "));
                enchantments.forEach(enchantment -> {
                    builder.append(Text.of(TextColors.AQUA, "\n", enchantment.getType(), " ", enchantment.getLevel()));
                });
            });
            builder2.append(Text.builder().append(Text.of("\n", TextColors.GREEN, snapshot.getQuantity() + "x ", TextColors.AQUA, snapshot.getTranslation().get()))
                    .onHover(TextActions.showText(builder.build()))
                    .build()).build();
            if (snapshot.get(Keys.DISPLAY_NAME).isPresent()) {
                builder2.append(Text.builder().append(Text.of("  ", TextColors.GOLD, "[", snapshot.get(Keys.DISPLAY_NAME).get(), TextColors.GOLD, "]")).build()).build();
            }
        }

        return new Text[]{builder1.build(), builder2.build()};
    }
}
