package io.github.cjcool06.safetrade.utils;

import io.github.cjcool06.safetrade.SafeTrade;
import io.github.cjcool06.safetrade.obj.Trade;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

public class Utils {
    public static Optional<User> getUser(UUID uuid) {
        return Sponge.getServiceManager().provide(UserStorageService.class).get().get(uuid);
    }

    public static LocalDateTime convertToUTC(LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }

    public static Text[] getTradeOverviewLore(Trade trade) {
        Text.Builder builder1 =  Text.builder();
        Text.Builder builder2 = Text.builder();

        builder1.append(Text.of("Money:"))
                .color(TextColors.DARK_AQUA)
                .append(Text.builder().append(Text.of(TextColors.AQUA, " ", trade.getSides()[0].vault.account.getBalance(SafeTrade.getEcoService().getDefaultCurrency()).intValue())).build())
                .build();
        builder2.append(Text.of("Money:"))
                .color(TextColors.DARK_AQUA)
                .append(Text.builder().append(Text.of(TextColors.AQUA, " ", trade.getSides()[1].vault.account.getBalance(SafeTrade.getEcoService().getDefaultCurrency()).intValue())).build())
                .build();

        builder1.append(Text.of("\n" + "Items:"))
                .color(TextColors.DARK_AQUA)
                .build();
        builder2.append(Text.of("\n" + "Items:"))
                .color(TextColors.DARK_AQUA)
                .build();
        for (ItemStackSnapshot snapshot : trade.getSides()[0].vault.getAllItems()) {
            builder1.append(Text.builder().append(Text.of("\n", TextColors.GREEN, snapshot.getQuantity() + "x ", TextColors.AQUA, snapshot.getTranslation().get()))
                    .build()).build();
            if (snapshot.get(Keys.DISPLAY_NAME).isPresent()) {
                builder1.append(Text.builder().append(Text.of("  ", TextColors.GOLD, "[", snapshot.get(Keys.DISPLAY_NAME).get(), TextColors.GOLD, "]")).build()).build();
            }
        }
        for (ItemStackSnapshot snapshot : trade.getSides()[1].vault.getAllItems()) {
            builder2.append(Text.builder().append(Text.of("\n", TextColors.GREEN, snapshot.getQuantity() + "x ", TextColors.AQUA, snapshot.getTranslation().get()))
                    .build()).build();
            if (snapshot.get(Keys.DISPLAY_NAME).isPresent()) {
                builder2.append(Text.builder().append(Text.of("  ", TextColors.GRAY, "[", TextColors.GOLD, snapshot.get(Keys.DISPLAY_NAME).get(), TextColors.GRAY, "]")).build()).build();
            }
        }

        return new Text[]{builder1.build(), builder2.build()};
    }

    public static Text getSuccessMessage(Trade trade) {
        Text[] texts = getTradeOverviewLore(trade);

        return Text.builder("SafeTrade Overview >> ")
                .color(TextColors.GREEN)
                .style(TextStyles.BOLD)
                .append(Text.builder().append(Text.of(TextColors.DARK_AQUA, trade.getSides()[0].getUser().get().getName()))
                        .onHover(TextActions.showText(texts[0]))
                        .build())
                .append(Text.builder().append(Text.of(TextColors.DARK_AQUA, " & "))
                        .build())
                .append(Text.builder().append(Text.of(TextColors.DARK_AQUA, trade.getSides()[1].getUser().get().getName()))
                        .onHover(TextActions.showText(texts[1]))
                        .build())
                .build();
    }
}
