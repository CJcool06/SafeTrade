package io.github.cjcool06.safetrade.obj;

import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import io.github.cjcool06.safetrade.SafeTrade;
import io.github.cjcool06.safetrade.api.enums.InventoryType;
import io.github.cjcool06.safetrade.api.enums.TradeResult;
import io.github.cjcool06.safetrade.api.enums.TradeState;
import io.github.cjcool06.safetrade.api.events.trade.StateChangedEvent;
import io.github.cjcool06.safetrade.api.events.trade.TradeCreationEvent;
import io.github.cjcool06.safetrade.api.events.trade.TradeEvent;
import io.github.cjcool06.safetrade.channels.TradeChannel;
import io.github.cjcool06.safetrade.helpers.InventoryHelper;
import io.github.cjcool06.safetrade.listeners.EvolutionListener;
import io.github.cjcool06.safetrade.trackers.Tracker;
import io.github.cjcool06.safetrade.utils.ItemUtils;
import io.github.cjcool06.safetrade.utils.LogUtils;
import net.minecraft.entity.player.EntityPlayerMP;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.format.TextColors;

import java.util.*;

/**
 * A Trade represents two participants attempting to trade Items, Pokemon, and Money with each other using an intuitive and real-time GUI.
 */
public class Trade {
    private final UUID id;
    private final Side[] sides;
    private final List<Player> viewers = new ArrayList<>();
    private final TradeChannel tradeChannel = new TradeChannel();
    private final Inventory tradeInventory;

    private TradeState state = TradeState.TRADING;
    private Inventory overviewInventory;

    public Trade(Player participant1, Player participant2) {
        this(UUID.randomUUID(), participant1, participant2);
    }

    private Trade(UUID id, Player participant1, Player participant2) {
        this.id = id;
        sides = new Side[]{new Side(this, participant1), new Side(this, participant2)};
        tradeChannel.addMember(participant1);
        tradeChannel.addMember(participant2);
        participant1.setMessageChannel(tradeChannel);
        participant2.setMessageChannel(tradeChannel);

        tradeInventory = Inventory.builder()
                .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(Text.of(TextColors.DARK_AQUA, "SafeTrade - Trade Safely!")))
                .property(InventoryDimension.PROPERTY_NAME, new InventoryDimension(9,6))
                .of(InventoryArchetypes.MENU_GRID)
                .listener(ClickInventoryEvent.class, this::handleClick)
                .listener(InteractInventoryEvent.Open.class, event -> InventoryHelper.handleOpen(this, event))
                .listener(InteractInventoryEvent.Close.class, event -> InventoryHelper.handleBasicClose(this, InventoryType.MAIN, event))
                .build(SafeTrade.getPlugin());
        reformatInventory();

        overviewInventory = InventoryHelper.buildAndGetOverviewInventory(this);

        Tracker.addActiveTrade(this);
        SafeTrade.EVENT_BUS.post(new TradeCreationEvent(this));
    }

    /**
     * Gets the {@link UUID} identifier of the trade
     *
     * @return The ID
     */
    public UUID getId() {
        return id;
    }

    /**
     * Gets the current {@link TradeState} of the trade.
     *
     * @return The state of the trade
     */
    public TradeState getState() {
        return state;
    }

    /**
     * Gets the {@link TradeChannel} of the trade.
     *
     * @return The channel
     */
    public TradeChannel getChannel() {
        return tradeChannel;
    }

    public Inventory getTradeInventory() {
        return tradeInventory;
    }

    public Inventory getOverviewInventory() {
        return overviewInventory;
    }

    /**
     * Gets a list of participants of the trade as a {@link User} object.
     *
     * @return The participants
     */
    public List<User> getParticipants() {
        List<User> participants = new ArrayList<>();
        if (sides[0].getUser().isPresent()) {
            participants.add(sides[0].getUser().get());
        }
        if (sides[1].getUser().isPresent()) {
            participants.add(sides[1].getUser().get());
        }

        return participants;
    }

    /**
     * Gets an immutable list of players currently viewing the trade.
     *
     * @return The viewers
     */
    public List<Player> getViewers() {
        return Collections.unmodifiableList(viewers);
    }

    /**
     * Pause and un-pause the trade.
     *
     * <p>If the trade is executing or ended, the trade cannot be paused or un-paused.</p>
     *
     * @param state True to pause, false to un-pause
     */
    public void setState(TradeState state) {
        if (this.state != state) {
            TradeState oldState = this.state;
            this.state = state;
            SafeTrade.EVENT_BUS.post(new StateChangedEvent(this, oldState, state));
        }
    }

    /**
     * Immediately executes the trade.
     *
     * <p>The players cannot cancel the trade.</p>
     */
    public TradeResult executeTrade() {
        if (SafeTrade.EVENT_BUS.post(new TradeEvent.Executing(this))) {
            return TradeResult.CANCELLED;
        }
        LogUtils.logTrade(this);
        Tracker.removeActiveTrade(this);
        return handleTrade();
    }

    /**
     * Handles the trade execution.
     *
     * @return The result
     */
    private TradeResult handleTrade() {
        Side side0 = getSides()[0];
        Side side1 = getSides()[1];

        PlayerStorage storage0 = Tracker.getOrCreateStorage(side0.getUser().get());
        PlayerStorage storage1 = Tracker.getOrCreateStorage(side1.getUser().get());

        side0.vault.unloadToStorage(storage1);
        side1.vault.unloadToStorage(storage0);

        side0.getPlayer().ifPresent(player -> {
            storage0.giveItems();
            storage0.givePokemon().forEach(pokemon -> {
                // Requires tick delay otherwise the player will become glitched
                Sponge.getScheduler().createTaskBuilder().execute(() -> {
                    EntityPixelmon pixelmon = pokemon.getOrSpawnPixelmon((EntityPlayerMP)player);
                    if (pixelmon.testTradeEvolution(EnumSpecies.Abomasnow)) {
                        EvolutionListener.ongoingEvolutions.add(pixelmon.getUniqueID());
                    }
                    else {
                        pixelmon.unloadEntity();
                    }
                }).delayTicks(20).submit(SafeTrade.getPlugin());
            });
            player.setMessageChannel(MessageChannel.TO_ALL);
        });
        side1.getPlayer().ifPresent(player -> {
            storage1.giveItems();
            storage1.givePokemon().forEach(pokemon -> {
                // Requires tick delay otherwise the player will become glitched
                Sponge.getScheduler().createTaskBuilder().execute(() -> {
                    EntityPixelmon pixelmon = pokemon.getOrSpawnPixelmon((EntityPlayerMP)player);
                    if (pixelmon.testTradeEvolution(EnumSpecies.Abomasnow)) {
                        EvolutionListener.ongoingEvolutions.add(pixelmon.getUniqueID());
                    }
                    else {
                        pixelmon.unloadEntity();
                    }
                }).delayTicks(20).submit(SafeTrade.getPlugin());
            });
            player.setMessageChannel(MessageChannel.TO_ALL);
        });
        SafeTrade.EVENT_BUS.post(new TradeEvent.Executed.SuccessfulTrade(this, TradeResult.SUCCESS));

        return TradeResult.SUCCESS;
    }

    /**
     * Forces the trade to close and return all items, money, and Pokemon that are being held by the trade.
     */
    public TradeResult forceEnd() {
        unloadToStorages();
        tradeChannel.clearMembers();

        Tracker.removeActiveTrade(this);
        Sponge.getScheduler().createTaskBuilder().execute(() -> {
            sides[0].getPlayer().ifPresent(player -> {
                player.closeInventory();
                PlayerStorage storage = Tracker.getOrCreateStorage(player);
                storage.giveItems();
                storage.givePokemon();
                player.setMessageChannel(MessageChannel.TO_ALL);
            });
            sides[1].getPlayer().ifPresent(player -> {
                player.closeInventory();
                PlayerStorage storage = Tracker.getOrCreateStorage(player);
                storage.giveItems();
                storage.givePokemon();
                player.setMessageChannel(MessageChannel.TO_ALL);
            });
        }).delayTicks(1).submit(SafeTrade.getPlugin());

        SafeTrade.EVENT_BUS.post(new TradeEvent.Cancelled(this));
        return TradeResult.CANCELLED;
    }

    /**
     * Moves all possessions in the trade (Pokemon, items, money) in to the respective participant's {@link PlayerStorage} and bank account.
     *
     * <p>This is useful when a trade is cancelled or the server is stopping.</p>
     */
    public void unloadToStorages() {
        for (Side side : sides) {
            PlayerStorage storage = Tracker.getOrCreateStorage(side.getUser().get());
            side.vault.unloadToStorage(storage);
        }
    }

    /**
     * Adds a viewer to the trade.
     *
     * <p>Opens the trade for a {@link Player} to view, although they cannot interact with the trade.</p>
     *
     * @param player The player
     * @param openInventory Whether to open the player's inventory
     */
    public void addViewer(Player player, boolean openInventory) {
        viewers.removeIf(p -> p.getUniqueId().equals(player.getUniqueId()));
        viewers.add(player);
        if (openInventory) {
            Sponge.getScheduler().createTaskBuilder().execute(() -> player.openInventory(tradeInventory)).delayTicks(1).submit(SafeTrade.getPlugin());
        }
    }

    /**
     * Removes a viewer from the trade.
     *
     * <p>Closes the trade for the {@link Player}.</p>
     *
     * @param player The player
     * @param closeInventory Whether to close the player's inventory
     */
    public void removeViewer(Player player, boolean closeInventory) {
        // Delaying by 1 tick prevents nasty errors if the method is called when listening to InteractInventoryEvent#Close,
        // as it would be attempting to close the inventory... while closing the inventory. lol
        viewers.removeIf(p -> p.getUniqueId().equals(player.getUniqueId()));
        if (closeInventory) {
            Sponge.getScheduler().createTaskBuilder().execute(player::closeInventory).delayTicks(1).submit(SafeTrade.getPlugin());
        }
    }

    /**
     * Gets a cloned array of the sides of the trade.
     *
     * @return The sides
     */
    public Side[] getSides() {
        return sides.clone();
    }

    /**
     * Gets the side of a participant, if present.
     *
     * @param uuid The {@link UUID} of the user
     * @return The side
     */
    public Optional<Side> getSide(UUID uuid) {
        if (sides[0].sideOwnerUUID.equals(uuid)) {
            return Optional.of(sides[0]);
        }
        else if (sides[1].sideOwnerUUID.equals(uuid)) {
            return Optional.of(sides[1]);
        }
        else {
            return Optional.empty();
        }
    }

    /**
     * Sends a {@link Text} message to the trade's {@link TradeChannel}.
     *
     * @param text The message
     */
    public void sendMessage(Text text) {
        tradeChannel.send(text);
    }

    private void handleClick(ClickInventoryEvent event) {
        event.setCancelled(true);
        event.getCause().first(Player.class).ifPresent(player -> {
            event.getTransactions().forEach(transaction -> {
                ItemStack item = transaction.getOriginal().createStack();
                Optional<Side> optSide = getSide(player.getUniqueId());

                // Only players in a side can use these buttons
                if (optSide.isPresent()) {
                    Side side = optSide.get();
                    Side otherSide = optSide.get().getOtherSide();

                    if (item.equalTo(ItemUtils.Main.getReady()) && state == TradeState.TRADING) {
                        side.setReady(true);
                        side.vault.setLocked(true);
                        Sponge.getScheduler().createTaskBuilder().execute(() -> {
                            if (side.getOtherSide().isReady() && !side.getOtherSide().isPaused()) {
                                setState(TradeState.WAITING_FOR_CONFIRMATION);
                                side.changeInventory(InventoryType.OVERVIEW);
                                side.getOtherSide().changeInventory(InventoryType.OVERVIEW);
                            }
                            else {
                                reformatInventory();
                            }
                        }).delayTicks(1).submit(SafeTrade.getPlugin());
                    }
                    else if (item.equalTo(ItemUtils.Main.getNotReady()) && state == TradeState.TRADING) {
                        side.setReady(false);
                        side.vault.setLocked(false);
                        Sponge.getScheduler().createTaskBuilder().execute(this::reformatInventory).delayTicks(1).submit(SafeTrade.getPlugin());
                    }
                    else if (item.equalTo(ItemUtils.Main.getPause()) && state == TradeState.TRADING) {
                        side.changeInventory(InventoryType.NONE);
                        Sponge.getScheduler().createTaskBuilder().execute(this::reformatInventory).delayTicks(1).submit(SafeTrade.getPlugin());
                    }
                    else if (item.equalTo(ItemUtils.Main.getQuit()) && state == TradeState.TRADING) {
                        Sponge.getScheduler().createTaskBuilder().execute(this::forceEnd).delayTicks(1).submit(SafeTrade.getPlugin());
                    }
                    else if (item.equalTo(ItemUtils.Main.getMoneyStorage(side))) {
                        Sponge.getScheduler().createTaskBuilder().execute(() -> side.changeInventory(InventoryType.MONEY)).delayTicks(1).submit(SafeTrade.getPlugin());
                    }
                    else if (item.equalTo(ItemUtils.Main.getItemStorage(side))) {
                        Sponge.getScheduler().createTaskBuilder().execute(() -> side.changeInventory(InventoryType.ITEM)).delayTicks(1).submit(SafeTrade.getPlugin());
                    }
                    else if (item.equalTo(ItemUtils.Main.getPokemonStorage(side))) {
                        Sponge.getScheduler().createTaskBuilder().execute(() -> side.changeInventory(InventoryType.POKEMON)).delayTicks(1).submit(SafeTrade.getPlugin());
                    }
                    else if (item.equalTo(ItemUtils.Main.getItemStorage(otherSide))) {
                        Sponge.getScheduler().createTaskBuilder().execute(() -> otherSide.changeInventoryForViewer(player, InventoryType.ITEM)).delayTicks(1).submit(SafeTrade.getPlugin());
                    }
                    else if (item.equalTo(ItemUtils.Main.getPokemonStorage(otherSide))) {
                        Sponge.getScheduler().createTaskBuilder().execute(() -> otherSide.changeInventoryForViewer(player, InventoryType.POKEMON)).delayTicks(1).submit(SafeTrade.getPlugin());
                    }
                }
                // Viewers can use these buttons
                else {
                    // Side 1
                    if (item.equalTo(ItemUtils.Main.getItemStorage(sides[0]))) {
                        Sponge.getScheduler().createTaskBuilder().execute(() -> sides[0].changeInventoryForViewer(player, InventoryType.ITEM)).delayTicks(1).submit(SafeTrade.getPlugin());
                    }
                    else if (item.equalTo(ItemUtils.Main.getPokemonStorage(sides[0]))) {
                        Sponge.getScheduler().createTaskBuilder().execute(() -> sides[0].changeInventoryForViewer(player, InventoryType.POKEMON)).delayTicks(1).submit(SafeTrade.getPlugin());
                    }

                    // Side 2
                    else if (item.equalTo(ItemUtils.Main.getItemStorage(sides[1]))) {
                        Sponge.getScheduler().createTaskBuilder().execute(() -> sides[1].changeInventoryForViewer(player, InventoryType.ITEM)).delayTicks(1).submit(SafeTrade.getPlugin());
                    }
                    else if (item.equalTo(ItemUtils.Main.getPokemonStorage(sides[1]))) {
                        Sponge.getScheduler().createTaskBuilder().execute(() -> sides[1].changeInventoryForViewer(player, InventoryType.POKEMON)).delayTicks(1).submit(SafeTrade.getPlugin());
                    }
                }
            });
        });
    }

    /**
     * Reformats the inventory.
     */
    public void reformatInventory() {
        tradeInventory.slots().forEach(slot -> {
            int i = slot.getProperty(SlotIndex.class, "slotindex").get().getValue();

            // Side 1
            // Status border
            if ((i >= 0 && i <= 3) || i == 9 || i == 18 || i == 27 || i ==36 || (i >= 45 && i <= 47)) {
                slot.set(ItemUtils.Main.getStateStatus(sides[0]));
            }
            // Head
            else if (i == 11) {
                slot.set(ItemUtils.Main.getHead(sides[0]));
            }
            // Money storage
            else if (i == 20) {
                slot.set(ItemUtils.Main.getMoneyStorage(sides[0]));
            }
            // Item storage
            else if (i == 28) {
                slot.set(ItemUtils.Main.getItemStorage(sides[0]));
            }
            // Pokemon storage
            else if (i == 30) {
                slot.set(ItemUtils.Main.getPokemonStorage(sides[0]));
            }

            // Side 2
            // Status border
            else if ((i >= 5 && i <= 8) || i == 17 || i == 26 || i == 35 || i == 44 || (i >= 51 && i <= 53)) {
                slot.set(ItemUtils.Main.getStateStatus(sides[1]));
            }
            // Head
            else if (i == 15) {
                slot.set(ItemUtils.Main.getHead(sides[1]));
            }
            // Money storage
            else if (i == 24) {
                slot.set(ItemUtils.Main.getMoneyStorage(sides[1]));
            }
            // Item storage
            else if (i == 32) {
                slot.set(ItemUtils.Main.getItemStorage(sides[1]));
            }
            // Pokemon storage
            else if (i == 34) {
                slot.set(ItemUtils.Main.getPokemonStorage(sides[1]));
            }

            // Rest
            // Quit item
            else if (i == 4) {
                slot.set(ItemUtils.Main.getQuit());
            }
            // Middle border (Currently is filler)
            else if (i == 13 || i == 22 || i == 31 || i == 40) {
                slot.set(ItemUtils.Other.getFiller(DyeColors.BLACK));
            }
            // Ready
            else if (i == 48) {
                slot.set(ItemUtils.Main.getReady());
            }
            // Not ready
            else if (i == 50) {
                slot.set(ItemUtils.Main.getNotReady());
            }
            // Pause
            else if (i == 49) {
                slot.set(ItemUtils.Main.getPause());
            }
            // Filler
            else if (i == 10 || i == 12 || i == 14 || i == 16 || i == 19 || i == 21 || i == 23 || i == 25 || i == 29 || i == 33 || (i >= 37 && i <= 39) || (i >= 41 && i <= 43)) {
                slot.set(ItemUtils.Other.getFiller(DyeColors.BLACK));
            }
        });
    }
}
