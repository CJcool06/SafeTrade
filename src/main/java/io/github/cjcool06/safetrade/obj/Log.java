package io.github.cjcool06.safetrade.obj;

import io.github.cjcool06.safetrade.utils.LogUtils;
import io.github.cjcool06.safetrade.utils.Utils;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ConfigSerializable
public class Log {
    @Setting
    private UUID uniqueID;
    @Setting
    private String timestamp;
    @Setting
    private UUID participant0;
    @Setting
    private UUID participant1;
    @Setting
    private List<String> jsonTexts;
    //private Text text;

    /**
     * Used for deserialisation purposes. Do NOT use this constructor
     */
    @Deprecated
    public Log() {}

    public Log(Trade trade) {
        this.uniqueID = UUID.randomUUID();
        this.timestamp = getFormatter().format(LocalDateTime.now().atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime());
        this.participant0 = trade.getSides()[0].sideOwnerUUID;
        this.participant1 = trade.getSides()[1].sideOwnerUUID;
        this.jsonTexts = LogUtils.createContents(trade);
    }

    public UUID getUniqueID() {
        return uniqueID;
    }

    public LocalDateTime getTimestamp() {
        return LocalDateTime.parse(timestamp, getFormatter());
    }

    public UUID[] getParticipantsUUID() {
        return new UUID[]{participant0, participant1};
    }

    public User[] getParticipants() {
        return new User[]{Utils.getUser(participant0).isPresent() ? Utils.getUser(participant0).get() : null, Utils.getUser(participant1).isPresent() ? Utils.getUser(participant1).get() : null};
    }

    public List<String> getJsonTexts() {
        return new ArrayList<>(jsonTexts);
    }

    public Text getText() {
        Text.Builder builder = Text.builder();
        builder.append(TextSerializers.JSON.deserialize(jsonTexts.get(0)));
        builder.append(TextSerializers.JSON.deserialize(jsonTexts.get(1)).toBuilder()
                .onClick(TextActions.executeCallback(src -> {
                    PaginationList.builder()
                            .title(TextSerializers.JSON.deserialize(jsonTexts.get(2)))
                            .contents(TextSerializers.JSON.deserialize(jsonTexts.get(3)))
                            .padding(Text.of(TextColors.GRAY, "-", TextColors.RESET))
                            .sendTo(src);
                }))
                .build());
        builder.append(TextSerializers.JSON.deserialize(jsonTexts.get(4)));
        builder.append(TextSerializers.JSON.deserialize(jsonTexts.get(5)).toBuilder()
                .onClick(TextActions.executeCallback(src -> {
                    PaginationList.builder()
                            .title(TextSerializers.JSON.deserialize(jsonTexts.get(6)))
                            .contents(TextSerializers.JSON.deserialize(jsonTexts.get(7)))
                            .padding(Text.of(TextColors.GRAY, "-", TextColors.RESET))
                            .sendTo(src);
                }))
                .build());


        return builder.build();
    }

    public static DateTimeFormatter getFormatter() {
        return DateTimeFormatter.ofPattern("[dd/MM/yyyy HH:mm]");
    }
}
