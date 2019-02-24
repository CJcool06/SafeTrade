package io.github.cjcool06.safetrade.obj;

import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import io.github.cjcool06.safetrade.SafeTrade;
import io.github.cjcool06.safetrade.helpers.InventoryHelper;
import io.github.cjcool06.safetrade.utils.GsonUtils;
import io.github.cjcool06.safetrade.utils.Utils;
import net.minecraft.nbt.NBTTagCompound;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A Log represents the information saved by a {@link Trade} when it's successfully executed.
 *
 * This object is immutable.
 */
@ConfigSerializable
public class Log {
    @Setting
    private UUID uniqueID;
    @Setting
    private String timestamp;
    @Setting
    @Deprecated
    private List<String> jsonTexts;

    // Side
    @Setting
    private UUID participant;
    @Setting
    private List<String> sidePokemonStrings;
    @Setting
    private List<String> sideItemStrings;
    @Setting
    private Integer sideMoney;

    // Other Side
    @Setting
    private UUID otherParticipant;
    @Setting
    private List<String> otherSidePokemonStrings;
    @Setting
    private List<String> otherSideItemStrings;
    @Setting
    private Integer otherSideMoney;

    /**
     * Used for deserialisation purposes. Do NOT use this constructor
     */
    @Deprecated
    public Log() {}

    public Log(Trade trade) {
        uniqueID = UUID.randomUUID();
        timestamp = getFormatter().format(LocalDateTime.now().atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime());
        //this.jsonTexts = LogUtils.createContents(trade);

        // Side
        Side side = trade.getSides()[0];
        participant = side.sideOwnerUUID;
        sidePokemonStrings = serialisePokemon(side.vault.getAllPokemon());
        sideItemStrings = serialiseItemSnapshots(side.vault.getAllItems());
        sideMoney = side.vault.account.getBalance(SafeTrade.getEcoService().getDefaultCurrency()).intValue();

        // Other Side
        Side otherSide = trade.getSides()[1];
        otherParticipant = otherSide.sideOwnerUUID;
        otherSidePokemonStrings = serialisePokemon(otherSide.vault.getAllPokemon());
        otherSideItemStrings = serialiseItemSnapshots(otherSide.vault.getAllItems());
        otherSideMoney = side.vault.account.getBalance(SafeTrade.getEcoService().getDefaultCurrency()).intValue();
    }

    /**
     * Gets the {@link UUID} of the log.
     *
     * @return The unique id
     */
    public UUID getUniqueID() {
        return uniqueID;
    }

    /**
     * Gets the {@link LocalDateTime} of when the log was created.
     *
     * @return The timestamp
     */
    public LocalDateTime getTimestamp() {
        return LocalDateTime.parse(timestamp, getFormatter());
    }

    /**
     * Gets the first participant of the logged trade's {@link UUID}.
     *
     * @return The first participant's uuid
     */
    public UUID getParticipantUUID() {
        return participant;
    }

    /**
     * Gets the other participant of the logged trade's {@link UUID}.
     *
     * @return The other participant's uuid
     */
    public UUID getOtherParticipantUUID() {
        return otherParticipant;
    }

    /**
     * Gets the first participant of the logged trade as a {@link User}.
     *
     * @return The first participant
     */
    public User getParticipant() {
        return Utils.getUser(participant).get();
    }

    /**
     * Gets the other participant of the logged trade as a {@link User}.
     *
     * @return The other participant
     */
    public User getOtherParticipant() {
        return Utils.getUser(otherParticipant).get();
    }

    /**
     * Gets the first participant's logged {@link Pokemon}.
     *
     * @return The pokemon
     */
    public List<Pokemon> getSidesPokemon() {
        List<Pokemon> pokemon = new ArrayList<>();
        for (String pokemonStr : sidePokemonStrings) {
            pokemon.add(Pixelmon.pokemonFactory.create(GsonUtils.deserialize(pokemonStr)));
        }

        return pokemon;
    }

    /**
     * Gets the other participant's logged {@link Pokemon}.
     *
     * @return The pokemon
     */
    public List<Pokemon> getOtherSidesPokemon() {
        List<Pokemon> pokemon = new ArrayList<>();
        for (String pokemonStr : otherSidePokemonStrings) {
            pokemon.add(Pixelmon.pokemonFactory.create(GsonUtils.deserialize(pokemonStr)));
        }

        return pokemon;
    }

    /**
     * Gets the first participant's logged {@link ItemStackSnapshot}s.
     *
     * @return The items
     */
    @SuppressWarnings("all")
    public List<ItemStackSnapshot> getSidesItems() {
        List<ItemStackSnapshot> items = new ArrayList<>();
        for (String itemStr : sideItemStrings) {
            try {
                items.add(Sponge.getDataManager().deserialize(ItemStackSnapshot.class, DataFormats.JSON.read(itemStr)).get());
            } catch (Exception e) {
                continue;
            }
        }

        return items;
    }

    /**
     * Gets the first participant's logged {@link ItemStackSnapshot}s.
     *
     * @return The items
     */
    @SuppressWarnings("all")
    public List<ItemStackSnapshot> getOtherSidesItems() {
        List<ItemStackSnapshot> items = new ArrayList<>();
        for (String itemStr : otherSideItemStrings) {
            try {
                items.add(Sponge.getDataManager().deserialize(ItemStackSnapshot.class, DataFormats.JSON.read(itemStr)).get());
            } catch (Exception e) {
                continue;
            }
        }

        return items;
    }

    /**
     * Gets the first participant's logged money.
     *
     * @return The money
     */
    public Integer getSidesMoney() {
        return sideMoney;
    }

    /**
     * Gets the other participant's logged money.
     *
     * @return The money
     */
    public Integer getOtherSidesMoney() {
        return otherSideMoney;
    }

    /**
     * Gets the {@link Inventory} of this log.
     *
     * @return The inventory
     */
    public Inventory getInventory() {
        return InventoryHelper.buildAndGetLogInventory(this);
    }

    /**
     * Gets the texts that were saved in json.
     *
     * @return A list of the texts as strings
     */
    @Deprecated
    public List<String> getJsonTexts() {
        return new ArrayList<>(jsonTexts);
    }

    /**
     * Builds the log {@link Text} from the json texts.
     *
     * @return The log text
     */
    @Deprecated
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

    public Text getDisplayText() {
        return Text.builder()
                .append(Text.builder().append(Text.of(TextColors.LIGHT_PURPLE, "[" + Log.getFormatter().format(Utils.convertToUTC(LocalDateTime.now())) + " UTC] "))
                        .onHover(TextActions.showText(Text.of(TextColors.GRAY, "Day/Month/Year Hour:Minute"))).build())
                .append(Text.of(TextColors.AQUA, getParticipant().getName(), TextColors.DARK_AQUA, " & ", Text.of(TextColors.AQUA, getOtherParticipant().getName())))
                .onHover(TextActions.showText(Text.of(TextColors.GRAY, "Click here to see this trade's extended log")))
                .onClick(TextActions.executeCallback(src -> {
                    Player player = (Player)src;
                    Sponge.getScheduler().createTaskBuilder().execute(() -> player.openInventory(getInventory())).delayTicks(1).submit(SafeTrade.getPlugin());
                }))
                .build();
    }

    /**
     * Gets the {@link DateTimeFormatter} used for the timestamp.
     *
     * @return The format
     */
    public static DateTimeFormatter getFormatter() {
        return DateTimeFormatter.ofPattern("[dd/MM/yyyy HH:mm]");
    }

    @SuppressWarnings("all")
    private List<String> serialiseItemSnapshots(List<ItemStackSnapshot> items) {
        List<String> itemStrings = new ArrayList<>();
        for (ItemStackSnapshot snapshot : items) {
            try {
                itemStrings.add(DataFormats.JSON.write(snapshot.toContainer()));
            } catch (IOException ioe) {
                continue;
            }
        }

        return itemStrings;
    }

    private List<String> serialisePokemon(List<Pokemon> pokemons) {
        List<String> pokemonStrings = new ArrayList<>();
        for (Pokemon pokemon : pokemons) {
            pokemonStrings.add(GsonUtils.serialize(pokemon.writeToNBT(new NBTTagCompound())));
        }

        return pokemonStrings;
    }
}
