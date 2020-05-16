package io.github.cjcool06.safetrade.helpers;

import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.PCStorage;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import io.github.cjcool06.safetrade.SafeTrade;
import io.github.cjcool06.safetrade.api.enums.InventoryType;
import io.github.cjcool06.safetrade.api.enums.PrefixType;
import io.github.cjcool06.safetrade.api.enums.TradeState;
import io.github.cjcool06.safetrade.api.events.trade.ConnectionEvent;
import io.github.cjcool06.safetrade.api.events.trade.InventoryChangeEvent;
import io.github.cjcool06.safetrade.api.events.trade.ViewerEvent;
import io.github.cjcool06.safetrade.obj.*;
import io.github.cjcool06.safetrade.trackers.Tracker;
import io.github.cjcool06.safetrade.utils.ItemUtils;
import io.github.cjcool06.safetrade.utils.Utils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.*;

/**
 * This class is to simply prevent these methods from unnecessarily cluttering up other classes
 */
public class InventoryHelper {

    //
    // Cooldowns
    //
    // A cooldown lasts 5 ticks (0.25 seconds)
    //

    private static List<UUID> clickingMainInv = new ArrayList<>();

    public static boolean hasCooldown(UUID uuid) {
        return clickingMainInv.contains(uuid);
    }

    public static void addCooldown(UUID uuid) {
        clickingMainInv.add(uuid);
        Sponge.getScheduler().createTaskBuilder().execute(() -> clickingMainInv.remove(uuid)).delayTicks(5).submit(SafeTrade.getPlugin());
    }

    //
    // Default open handler for main trade inventories
    //
    // Default close handler for main trade inventories
    //

    public static void handleOpen(Trade trade, InteractInventoryEvent.Open event) {
        event.getCause().first(Player.class).ifPresent(player -> {
            Optional<Side> optSide = trade.getSide(player.getUniqueId());
            // The player is a participant of a side of the trade.
            if (optSide.isPresent()) {
                Side side = optSide.get();
                // If the side is paused, it means that they are reconnecting to the trade.
                // If the side is not paused, it means they have been transferred from another trade-related inventory and is not considered as a trade connection
                if (side.isPaused()) {
                    if (SafeTrade.EVENT_BUS.post(new ConnectionEvent.Join.Pre(side))) {
                        event.setCancelled(true);
                        return;
                    }
                    side.setPaused(false);
                    SafeTrade.EVENT_BUS.post(new ConnectionEvent.Join.Post(side));
                    Sponge.getScheduler().createTaskBuilder().execute(trade::reformatInventory).delayTicks(1).submit(SafeTrade.getPlugin());
                }
                else {
                    Sponge.getScheduler().createTaskBuilder().execute(() -> SafeTrade.EVENT_BUS.post(new InventoryChangeEvent.Post(side))).delayTicks(1).submit(SafeTrade.getPlugin());
                }
            }
            // An unauthorized player is attempting to open the trade unexpectedly.
            // Adds them to the trade's viewers.
            // This will happen if the player doesn't come through Trade#addViewer
            else if (!trade.getViewers().contains(player)) {
                if (SafeTrade.EVENT_BUS.post(new ViewerEvent.Add.Pre(trade, player))) {
                    event.setCancelled(true);
                    return;
                }
                trade.addViewer(player, false);
                SafeTrade.EVENT_BUS.post(new ViewerEvent.Add.Post(trade, player));
            }
        });
    }

    public static void handleBasicClose(Trade trade, InventoryType inventoryType, InteractInventoryEvent.Close event) {
        event.getCause().first(Player.class).ifPresent(player -> {
            Optional<Side> optSide = trade.getSide(player.getUniqueId());
            // The player is a participant of a side of the trade.
            if (optSide.isPresent()) {
                Side side = optSide.get();
                // Side#changeInventory changes the current inventory
                // Therefore if the current inventory is for this inventory, the player is exiting the trade and not changing inventories
                // This will not work correctly if Player#changeInventory is used instead of Side#changeInventory
                // For example, if currentInventory equals MAIN and this inventoryType is NOT MAIN, the player is accessing the other side's inventories
                // This is also true if the InventoryType is NONE, which is the proper way to close a trade inventory.
                if (side.currentInventory.equals(inventoryType) || side.currentInventory.equals(InventoryType.NONE)) {
                    side.setReady(false);
                    side.setPaused(true);
                    side.currentInventory = InventoryType.NONE;
                    Sponge.getScheduler().createTaskBuilder().execute(side.parentTrade::reformatInventory).delayTicks(1).submit(SafeTrade.getPlugin());
                    SafeTrade.EVENT_BUS.post(new ConnectionEvent.Left(side));
                }
            }
            // Else, the player must be a viewer
            else {
                Sponge.getScheduler().createTaskBuilder().execute(() -> {
                    // If the viewer is not viewing an inventory after 1 tick, they have exited the trade.
                    // I have yet to test what happens if a viewer is FORCED to open an unrelated inventory. If I'd have to guess, this code block would not be executed
                    // as the player will have the unrelated inventory open, therefore the player will continue to be considered as a viewer.
                    // Just something to keep in mind.
                    if (!player.isViewingInventory()) {
                        if (SafeTrade.EVENT_BUS.post(new ViewerEvent.Remove.Pre(trade, player))) {
                            if (player.isOnline()) {
                                trade.getSides()[0].changeInventoryForViewer(player, inventoryType);
                            }
                        }
                        else {
                            trade.removeViewer(player, false);
                            SafeTrade.EVENT_BUS.post(new ViewerEvent.Remove.Post(trade, player));
                        }
                    }
                }).delayTicks(1).submit(SafeTrade.getPlugin());
            }
        });
    }

    /** Main {@link Trade} inventories: */

    //
    //  INVENTORIES
    //
    //  - Money (Main)
    //
    //
    //  CLICKERS
    //
    //  - Money (Main)
    //

    private static Map<UUID, Currency> currentCurrency = new HashMap<>();

    public static Inventory buildAndGetMoneyInventory(Side side) {
        Inventory inventory = Inventory.builder()
                .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(Text.of(TextColors.DARK_AQUA, side.getUser().get().getName() + "'s Money")))
                .property(InventoryDimension.PROPERTY_NAME, new InventoryDimension(9,3))
                .of(InventoryArchetypes.MENU_GRID)
                .listener(ClickInventoryEvent.class, event -> handleMoneyClick(side, event))
                .listener(InteractInventoryEvent.Close.class, event -> handleBasicClose(side.parentTrade, InventoryType.MONEY, event))
                .listener(InteractInventoryEvent.Open.class, event -> handleOpen(side.parentTrade, event))
                .build(SafeTrade.getPlugin());

        updateMoneyInventory(inventory, side);

        return inventory;
    }

    private static void updateMoneyInventory(Inventory inventory, Side side) {
        Currency currency = currentCurrency.getOrDefault(side.sideOwnerUUID, SafeTrade.getEcoService().getDefaultCurrency());
        inventory.slots().forEach(slot -> {
            int i = slot.getProperty(SlotIndex.class, "slotindex").get().getValue();

            if (i == 1) {
                slot.set(ItemUtils.Money.getMoneyBars(currency, 1));
            }
            else if (i == 2) {
                slot.set(ItemUtils.Money.getMoneyBars(currency, 10));
            }
            else if (i == 3) {
                slot.set(ItemUtils.Money.getMoneyBars(currency, 100));
            }
            else if (i == 4) {
                slot.set(ItemUtils.Money.getMoneyBars(currency, 1000));
            }
            else if (i == 5) {
                slot.set(ItemUtils.Money.getMoneyBars(currency, 10000));
            }
            else if (i == 6) {
                slot.set(ItemUtils.Money.getMoneyBars(currency, 100000));
            }
            else if (i == 7) {
                slot.set(ItemUtils.Money.getMoneyBars(currency, 1000000));
            }
            else if (i == 18) {
                slot.set(ItemUtils.Other.getBackButton());
            }
            else if (i == 21) {
                slot.set(ItemUtils.Money.getTotalMoney(side));
            }
            else if (i == 23) {
                slot.set(ItemUtils.Money.getPlayersMoney(side, currency));
            }
            else if (i == 26) {
                slot.set(ItemUtils.Money.changeCurrency());
            }
            else if (i <= 26) {
                slot.set(ItemUtils.Other.getFiller(DyeColors.GRAY));
            }
        });
    }

    private static void handleMoneyClick(Side side, ClickInventoryEvent event) {
        event.setCancelled(true);
        event.getCause().first(Player.class).ifPresent(player -> {
            // Prevents players from clicking if they have a cooldown
            if (hasCooldown(player.getUniqueId())) {
                return;
            }
            addCooldown(player.getUniqueId());

            event.getTransactions().forEach(transaction -> {
                transaction.getSlot().getProperty(SlotIndex.class, "slotindex").ifPresent(slot -> {
                    ItemStack item = transaction.getOriginal().createStack();

                    if (item.equalTo(ItemUtils.Other.getBackButton())) {
                        Sponge.getScheduler().createTaskBuilder().execute(() -> {
                            side.changeInventoryForViewer(player, side.parentTrade.getState() == TradeState.WAITING_FOR_CONFIRMATION ? InventoryType.OVERVIEW : InventoryType.MAIN);
                            currentCurrency.remove(side.sideOwnerUUID);
                        }).delayTicks(1).submit(SafeTrade.getPlugin());
                    }
                    // If player is not the in the side of this vault, or the if the vault is locked, or they didn't left/right click, nothing will happen.
                    // Checking for player is redundant when going through the main trade inventory, but I'll keep it here in-case a player somehow opens another side's money inventory
                    else if ((player.getUniqueId().equals(side.sideOwnerUUID) && !side.vault.isLocked() && (event instanceof ClickInventoryEvent.Primary || event instanceof ClickInventoryEvent.Secondary))
                            && side.parentTrade.getState() == TradeState.TRADING) {

                        if (item.equalTo(ItemUtils.Money.changeCurrency())) {
                            Sponge.getScheduler().createTaskBuilder().execute(() -> player.openInventory(buildAndGetCurrenciesInventory(side, SafeTrade.getEcoService(), event.getTargetInventory()))).delayTicks(1).submit(SafeTrade.getPlugin());
                            return;
                        }

                        Currency currency = currentCurrency.getOrDefault(side.sideOwnerUUID, SafeTrade.getEcoService().getDefaultCurrency());

                        for (int i = 1; i <= 1000000; i *= 10) {

                            if (item.equalTo(ItemUtils.Money.getMoneyBars(currency, i))) {
                                // Left clicking = adding money to trade
                                if (event instanceof ClickInventoryEvent.Primary) {
                                    TransactionResult result = SafeTrade.getEcoService().getOrCreateAccount(side.getUser().get().getUniqueId()).get()
                                            .transfer(side.vault.account, currency, BigDecimal.valueOf(i), Cause.of(EventContext.empty(), SafeTrade.getPlugin()));

                                    if (result.getResult() == ResultType.SUCCESS) {
                                        side.sendMessage(Text.of(TextColors.GREEN, "Added " + NumberFormat.getNumberInstance(Locale.US).format(i) + " ", currency.getPluralDisplayName(), " to the trade."));
                                        Sponge.getScheduler().createTaskBuilder().execute(() -> updateMoneyInventory(event.getTargetInventory(), side)).delayTicks(1).submit(SafeTrade.getPlugin());
                                        side.parentTrade.reformatInventory();
                                    } else {
                                        side.sendMessage(Text.of(TextColors.RED, "You do not have enough ", currency.getPluralDisplayName(), "."));
                                    }
                                }
                                // Right clicking = removing money from trade
                                else {
                                    if (i > side.vault.account.getBalance(currency).intValue()) {
                                        int val = side.vault.account.getBalance(currency).intValue();
                                        side.vault.account.transfer(SafeTrade.getEcoService().getOrCreateAccount(side.getUser().get().getUniqueId()).get(), currency, side.vault.account.getBalance(currency), Cause.of(EventContext.empty(), SafeTrade.getPlugin()));
                                        side.sendMessage(Text.of(TextColors.GREEN, "Removed " + NumberFormat.getNumberInstance(Locale.US).format(val) + " ", currency.getPluralDisplayName(), " from the trade."));
                                        Sponge.getScheduler().createTaskBuilder().execute(() -> updateMoneyInventory(event.getTargetInventory(), side)).delayTicks(1).submit(SafeTrade.getPlugin());
                                        side.parentTrade.reformatInventory();
                                    }
                                    else {
                                        TransactionResult result = side.vault.account.transfer(SafeTrade.getEcoService().getOrCreateAccount(side.getUser().get().getUniqueId()).get(), currency, BigDecimal.valueOf(i), Cause.of(EventContext.empty(), SafeTrade.getPlugin()));

                                        if (result.getResult() == ResultType.SUCCESS) {
                                            side.sendMessage(Text.of(TextColors.GREEN, "Removed " + NumberFormat.getNumberInstance(Locale.US).format(i) + " ", currency.getPluralDisplayName(), " from the trade."));
                                            // Refreshes the total money and player money item
                                            Sponge.getScheduler().createTaskBuilder().execute(() -> updateMoneyInventory(event.getTargetInventory(), side)).delayTicks(1).submit(SafeTrade.getPlugin());
                                            side.parentTrade.reformatInventory();
                                        } else {
                                            side.sendMessage(Text.of(TextColors.RED, "There was an error removing " + NumberFormat.getNumberInstance(Locale.US).format(i) + " ", currency.getPluralDisplayName(), " from the trade."));
                                            side.sendMessage(Text.of(TextColors.RED, "Contact an administrator if this continues."));
                                        }
                                    }
                                }

                                break;
                            }
                        }
                    }
                });
            });
        });
    }

    //
    //  INVENTORIES
    //
    //  - Currencies
    //
    //
    //  CLICKERS
    //
    //  - Currencies
    //

    public static Inventory buildAndGetCurrenciesInventory(Side side, EconomyService economyService, Inventory parentMoneyInventory) {
        Set<Currency> currencies = economyService.getCurrencies();

        Inventory inventory = Inventory.builder()
                .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(Text.of(TextColors.DARK_AQUA, "Currencies")))
                .property(InventoryDimension.PROPERTY_NAME, new InventoryDimension(9,(currencies.size() / 9) + 1))
                .of(InventoryArchetypes.MENU_GRID)
                .listener(ClickInventoryEvent.class, event -> handleCurrenciesClick(side, event, parentMoneyInventory))
                .listener(InteractInventoryEvent.Close.class, event -> handleBasicClose(side.parentTrade, InventoryType.MONEY, event))
                .listener(InteractInventoryEvent.Open.class, event -> handleOpen(side.parentTrade, event))
                .build(SafeTrade.getPlugin());

        updateCurrenciesInventory(inventory, currencies);

        return inventory;
    }

    private static void updateCurrenciesInventory(Inventory inventory, Set<Currency> currencies) {
        Iterator<Currency> iter = currencies.iterator();
        inventory.slots().forEach(slot -> {
            if (iter.hasNext()) {
                slot.set(ItemUtils.Money.getCurrency(iter.next()));
            }
        });
    }

    private static void handleCurrenciesClick(Side side, ClickInventoryEvent event, Inventory parentMoneyInventory) {
        event.setCancelled(true);
        event.getCause().first(Player.class).ifPresent(player -> {
            // Prevents players from clicking if they have a cooldown
            if (hasCooldown(player.getUniqueId())) {
                return;
            }
            addCooldown(player.getUniqueId());

            event.getTransactions().forEach(transaction -> {
                ItemStack item = transaction.getOriginal().createStack();

                // If player is not the in the side of this vault, or the if the vault is locked, or they didn't left/right click, nothing will happen.
                // Checking for player is redundant when going through the main trade inventory, but I'll keep it here in-case a player somehow opens another side's money inventory
                if ((player.getUniqueId().equals(side.sideOwnerUUID) && !side.vault.isLocked() && (event instanceof ClickInventoryEvent.Primary || event instanceof ClickInventoryEvent.Secondary))
                        && side.parentTrade.getState() == TradeState.TRADING) {

                    for (Currency currency : SafeTrade.getEcoService().getCurrencies()) {
                        if (item.equalTo(ItemUtils.Money.getCurrency(currency))) {
                            currentCurrency.put(side.sideOwnerUUID, currency);

                            Sponge.getScheduler().createTaskBuilder().execute(() -> {
                                player.openInventory(buildAndGetMoneyInventory(side));
                            }).delayTicks(1).submit(SafeTrade.getPlugin());
                        }
                    }
                }
            });
        });
    }

    //
    //  INVENTORIES
    //
    //  - PC
    //
    //
    //  CLICKERS
    //
    //  - PC
    //

    private static HashMap<Side, Integer> currentPage = new HashMap<>();

    public static Inventory buildAndGetPCInventory(Side side) {
        Inventory inventory = Inventory.builder()
                .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(Text.of(TextColors.DARK_AQUA, "PC")))
                .property(InventoryDimension.PROPERTY_NAME, new InventoryDimension(9,6))
                .of(InventoryArchetypes.MENU_GRID)
                .listener(ClickInventoryEvent.class, event -> handlePCClick(side, event))
                .listener(InteractInventoryEvent.Close.class, event -> handleBasicClose(side.parentTrade, InventoryType.PC, event))
                .listener(InteractInventoryEvent.Open.class, event -> handleOpen(side.parentTrade, event))
                .build(SafeTrade.getPlugin());

        currentPage.put(side, 1);
        updatePC(inventory, side);

        return inventory;
    }

    private static void updatePC(Inventory inventory, Side side) {
        LinkedHashMap<ItemStack, Pokemon>[] pcArr = Utils.generatePCMaps(side);
        LinkedHashMap<ItemStack, Pokemon> partyMap = pcArr[0];
        LinkedHashMap<ItemStack, Pokemon> pcMap = pcArr[1];
        Iterator<ItemStack> partyIter = partyMap.keySet().iterator();
        Iterator<ItemStack> pcIter = pcMap.keySet().iterator();
        int page = currentPage.get(side);

        for (int j = 0; j < (page-1)*30 && pcIter.hasNext(); j++) {
            pcIter.next();
        }

        inventory.slots().forEach(slot -> {
            int i = slot.getProperty(SlotIndex.class, "slotindex").get().getValue();

            if ((i >= 3 && i <= 8) || (i >= 12 && i <= 17) || (i >= 21 && i <= 26) || (i >= 30 && i <= 35) || (i >= 39 && i <= 44)) {
                slot.set(pcIter.hasNext() ? pcIter.next() : ItemStack.empty());
            }
            else if (i == 9 || i == 10 || i == 18 || i == 19 || i == 27 || i == 28) {
                slot.set(partyIter.hasNext() ? partyIter.next() : ItemUtils.Other.getFiller(DyeColors.GRAY));
            }
            else if (i == 45) {
                slot.set(ItemUtils.Other.getBackButton());
            }
            else if (i == 48) {
                slot.set(ItemUtils.PC.getPreviousPage(page));
            }
            else if (i == 50) {
                slot.set(ItemUtils.PC.getNextPage(page));
            }
            else if (i <= 53) {
                slot.set(ItemUtils.Other.getFiller(DyeColors.GRAY));
            }
        });
    }

    private static void handlePCClick(Side side, ClickInventoryEvent event) {
        LinkedHashMap<ItemStack, Pokemon>[] pcArr = Utils.generatePCMaps(side);
        LinkedHashMap<ItemStack, Pokemon> partyMap = pcArr[0];
        LinkedHashMap<ItemStack, Pokemon> pcMap = pcArr[1];
        int page = currentPage.get(side);

        event.setCancelled(true);
        event.getCause().first(Player.class).ifPresent(player -> {
            // Prevents players from clicking if they have a cooldown
            if (hasCooldown(player.getUniqueId())) {
                return;
            }
            addCooldown(player.getUniqueId());

            event.getTransactions().forEach(transaction -> {
                transaction.getSlot().getProperty(SlotIndex.class, "slotindex").ifPresent(slot -> {
                    ItemStack item = transaction.getOriginal().createStack();

                    if (item.equalTo(ItemUtils.Other.getBackButton())) {
                        Sponge.getScheduler().createTaskBuilder().execute(() -> side.changeInventoryForViewer(player, InventoryType.POKEMON)).delayTicks(1).submit(SafeTrade.getPlugin());
                    }
                    // If player is not the in the side of this vault, or the if the vault is locked, or they didn't left/right click, nothing will happen.
                    // Checking for player is redundant when going through the main trade inventory, but I'll keep it here in-case a player somehow opens another side's pc inventory
                    else if ((player.getUniqueId().equals(side.sideOwnerUUID) && !side.vault.isLocked() && event instanceof ClickInventoryEvent.Primary)
                            && side.parentTrade.getState() == TradeState.TRADING) {
                        boolean continueChecks = true;
                        PlayerPartyStorage partyStorage = Pixelmon.storageManager.getParty(side.getUser().get().getUniqueId());
                        PCStorage pcStorage = Pixelmon.storageManager.getPCForPlayer(side.getUser().get().getUniqueId());

                        for (ItemStack itemStack : partyMap.keySet()) {
                            if (itemStack.equalTo(item)) {
                                Pokemon pokemon = partyMap.get(itemStack);

                                if (Utils.untradeable.matches(pokemon) || partyStorage.countPokemon() <= 1 || pokemon.isInRanch()) {
                                    return;
                                }
                                if (Utils.getAllPokemon(partyStorage).contains(pokemon)) {
                                    if (side.vault.addPokemon(pokemon)) {
                                        partyStorage.set(partyStorage.getPosition(pokemon), null);
                                        Sponge.getScheduler().createTaskBuilder().execute(() -> updatePC(event.getTargetInventory(), side)).delayTicks(1).submit(SafeTrade.getPlugin());
                                    }
                                }
                                continueChecks = false;
                                break;
                            }
                        }
                        for (ItemStack itemStack : pcMap.keySet()) {
                            if (!continueChecks) {
                                break;
                            }
                            if (itemStack.equalTo(item)) {
                                Pokemon pokemon = pcMap.get(itemStack);
                                if (Utils.untradeable.matches(pokemon) || pokemon.isInRanch()) {
                                    return;
                                }
                                List<Pokemon> pcPokemon = Utils.getAllPokemon(pcStorage);
                                if (pcPokemon.contains(pokemon)) {
                                    if (side.vault.addPokemon(pokemon)) {
                                        pcStorage.set(pcStorage.getPosition(pokemon), null);
                                        Sponge.getScheduler().createTaskBuilder().execute(() -> updatePC(event.getTargetInventory(), side)).delayTicks(1).submit(SafeTrade.getPlugin());
                                    }
                                }
                                continueChecks = false;
                                break;
                            }
                        }

                        if (continueChecks) {
                            if (item.equalTo(ItemUtils.PC.getNextPage(page))) {
                                if (page*30 >= pcMap.size()) {
                                    currentPage.put(side, 1);
                                }
                                else {
                                    currentPage.put(side, page+1);
                                }
                                Sponge.getScheduler().createTaskBuilder().execute(() -> updatePC(event.getTargetInventory(), side)).delayTicks(1).submit(SafeTrade.getPlugin());
                            }
                            else if (item.equalTo(ItemUtils.PC.getPreviousPage(page))) {
                                if (page > 1) {
                                    currentPage.put(side, page-1);
                                }
                                else {
                                    int num = pcMap.size() / 30;
                                    currentPage.put(side, num+1);
                                }
                                Sponge.getScheduler().createTaskBuilder().execute(() -> updatePC(event.getTargetInventory(), side)).delayTicks(1).submit(SafeTrade.getPlugin());
                            }
                        }
                    }
                });
            });
        });
    }

    //
    //  INVENTORIES
    //
    //  - Overview
    //
    //
    //  CLICKERS
    //
    //  - Overview
    //

    public static Inventory buildAndGetOverviewInventory(Trade trade) {
        Inventory inventory = Inventory.builder()
                .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(Text.of(TextColors.DARK_AQUA, "Trade Overview")))
                .property(InventoryDimension.PROPERTY_NAME, new InventoryDimension(9,3))
                .of(InventoryArchetypes.MENU_GRID)
                .listener(ClickInventoryEvent.class, event -> handleOverviewClick(trade, event))
                .listener(InteractInventoryEvent.Close.class, event -> handleBasicClose(trade, InventoryType.MONEY, event))
                .listener(InteractInventoryEvent.Open.class, event -> handleOpen(trade, event))
                .build(SafeTrade.getPlugin());

        updateOverview(inventory, trade);

        return inventory;
    }

    private static void updateOverview(Inventory inventory, Trade trade) {
        inventory.slots().forEach(slot -> {
            int i = slot.getProperty(SlotIndex.class, "slotindex").get().getValue();

            // Side 1
            if ((i >= 0 && i <= 3) || (i >= 18 && i <= 21)) {
                slot.set(ItemUtils.Overview.getConfirmationStatus(trade.getSides()[0]));
            }
            else if (i == 9) {
                slot.set(ItemUtils.Money.getTotalMoney(trade.getSides()[0]));
            }
            else if (i == 10) {
                slot.set(ItemUtils.Main.getItemStorage(trade.getSides()[0]));
            }
            else if (i == 11) {
                slot.set(ItemUtils.Main.getPokemonStorage(trade.getSides()[0]));
            }
            else if (i == 12) {
                slot.set(ItemUtils.Main.getHead(trade.getSides()[0]));
            }

            // Side 2
            else if ((i >= 5 && i <= 8) || (i >= 23 && i <= 26)) {
                slot.set(ItemUtils.Overview.getConfirmationStatus(trade.getSides()[1]));
            }
            else if (i == 14) {
                slot.set(ItemUtils.Main.getHead(trade.getSides()[1]));
            }
            else if (i == 15) {
                slot.set(ItemUtils.Main.getPokemonStorage(trade.getSides()[1]));
            }
            else if (i == 16) {
                slot.set(ItemUtils.Main.getItemStorage(trade.getSides()[1]));
            }
            else if (i == 17) {
                slot.set(ItemUtils.Money.getTotalMoney(trade.getSides()[1]));
            }

            // Other
            else if (i == 4) {
                slot.set(ItemUtils.Overview.getConfirm());
            }
            else if (i == 13) {
                slot.set(ItemUtils.Overview.getOverviewInfo());
            }
            else if (i == 22) {
                slot.set(ItemUtils.Overview.getCancel());
            }
        });
    }

    private static void handleOverviewClick(Trade trade, ClickInventoryEvent event) {
        event.setCancelled(true);
        if (trade.getState() !=  TradeState.WAITING_FOR_CONFIRMATION) {
            return;
        }
        event.getCause().first(Player.class).ifPresent(player -> {
            // Prevents players from clicking if they have a cooldown
            if (hasCooldown(player.getUniqueId())) {
                return;
            }
            addCooldown(player.getUniqueId());

            event.getTransactions().forEach(transaction -> {
                transaction.getSlot().getProperty(SlotIndex.class, "slotindex").ifPresent(slot -> {
                    ItemStack item = transaction.getOriginal().createStack();
                    trade.getSide(player.getUniqueId()).ifPresent(side -> {
                        Side otherSide = side.getOtherSide();

                        if (item.equalTo(ItemUtils.Overview.getConfirm())) {
                            side.setConfirmed(true);

                            if (otherSide.isConfirmed()) {
                                trade.executeTrade();
                            }
                            else {
                                Sponge.getScheduler().createTaskBuilder().execute(() -> updateOverview(event.getTargetInventory(), trade)).delayTicks(1).submit(SafeTrade.getPlugin());
                            }
                        }
                        else if (item.equalTo(ItemUtils.Overview.getCancel())) {
                            side.setConfirmed(false);
                            side.setReady(false);
                            side.vault.setLocked(false);
                            otherSide.setConfirmed(false);
                            otherSide.setReady(false);
                            otherSide.vault.setLocked(false);
                            trade.setState(TradeState.TRADING);

                            Sponge.getScheduler().createTaskBuilder().execute(trade::reformatInventory).delayTicks(1).submit(SafeTrade.getPlugin());
                            Sponge.getScheduler().createTaskBuilder().execute(() -> side.changeInventory(InventoryType.MAIN)).delayTicks(1).submit(SafeTrade.getPlugin());
                            Sponge.getScheduler().createTaskBuilder().execute(() -> otherSide.changeInventory(InventoryType.MAIN)).delayTicks(1).submit(SafeTrade.getPlugin());
                        }
                        else if (item.equalTo(ItemUtils.Main.getItemStorage(side))) {
                            Sponge.getScheduler().createTaskBuilder().execute(() -> side.changeInventoryForViewer(player, InventoryType.ITEM)).delayTicks(1).submit(SafeTrade.getPlugin());
                        }
                        else if (item.equalTo(ItemUtils.Main.getPokemonStorage(side))) {
                            Sponge.getScheduler().createTaskBuilder().execute(() -> side.changeInventoryForViewer(player, InventoryType.POKEMON)).delayTicks(1).submit(SafeTrade.getPlugin());
                        }
                        else if (item.equalTo(ItemUtils.Main.getItemStorage(otherSide))) {
                            Sponge.getScheduler().createTaskBuilder().execute(() -> otherSide.changeInventoryForViewer(player, InventoryType.ITEM)).delayTicks(1).submit(SafeTrade.getPlugin());
                        }
                        else if (item.equalTo(ItemUtils.Main.getPokemonStorage(otherSide))) {
                            Sponge.getScheduler().createTaskBuilder().execute(() -> otherSide.changeInventoryForViewer(player, InventoryType.POKEMON)).delayTicks(1).submit(SafeTrade.getPlugin());
                        }
                    });
                });
            });
        });
    }

    /** {@link Log} inventory shit */

    //
    //  INVENTORIES
    //
    //  - Log (Main)
    //
    //
    //  CLICKERS
    //
    //  - Log (Main)
    //

    public static Inventory buildAndGetLogInventory(Log log) {
        User user = log.getParticipant();
        User otherUser = log.getOtherParticipant();

        Inventory inventory = Inventory.builder()
                .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(Text.of(TextColors.DARK_AQUA, user.getName() + " & " + otherUser.getName() + "'s Trade Log")))
                .property(InventoryDimension.PROPERTY_NAME, new InventoryDimension(9,1))
                .of(InventoryArchetypes.MENU_GRID)
                .listener(ClickInventoryEvent.class, event -> handleLogClick(log, event))
                .build(SafeTrade.getPlugin());

        updateLogInventory(inventory, log);

        return inventory;
    }

    private static void updateLogInventory(Inventory inventory, Log log) {
        inventory.slots().forEach(slot -> {
            int i = slot.getProperty(SlotIndex.class, "slotindex").get().getValue();

            // First participant
            if (i == 0) {
                slot.set(ItemUtils.Logs.getMoney(log.getParticipant()));
            }
            else if (i == 1) {
                slot.set(ItemUtils.Logs.getItems(log.getParticipant()));
            }
            else if (i == 2) {
                slot.set(ItemUtils.Logs.getPokemon(log.getParticipant()));
            }
            else if (i == 3) {
                slot.set(ItemUtils.Logs.getHead(log.getParticipant()));
            }

            // Other participant
            else if (i == 5) {
                slot.set(ItemUtils.Logs.getHead(log.getOtherParticipant()));
            }
            else if (i == 6) {
                slot.set(ItemUtils.Logs.getPokemon(log.getOtherParticipant()));
            }
            else if (i == 7) {
                slot.set(ItemUtils.Logs.getItems(log.getOtherParticipant()));
            }
            else if (i == 8) {
                slot.set(ItemUtils.Logs.getMoney(log.getOtherParticipant()));
            }

            else if (i <= 8) {
                slot.set(ItemUtils.Other.getFiller(DyeColors.LIGHT_BLUE));
            }
        });
    }

    private static void handleLogClick(Log log, ClickInventoryEvent event) {
        event.setCancelled(true);
        event.getCause().first(Player.class).ifPresent(player -> {
            // Prevents players from clicking if they have a cooldown
            if (hasCooldown(player.getUniqueId())) {
                return;
            }
            addCooldown(player.getUniqueId());

            event.getTransactions().forEach(transaction -> {
                transaction.getSlot().getProperty(SlotIndex.class, "slotindex").ifPresent(slot -> {
                    ItemStack item = transaction.getOriginal().createStack();

                    if (item.equalTo(ItemUtils.Other.getBackButton())) {
                        Sponge.getScheduler().createTaskBuilder().execute(() -> player.openInventory(log.getInventory())).delayTicks(1).submit(SafeTrade.getPlugin());
                    }
                    else if (item.equalTo(ItemUtils.Logs.getItems(log.getParticipant()))) {
                        Sponge.getScheduler().createTaskBuilder().execute(() -> player.openInventory(getLogItemsInventory(log, log.getParticipant(), log.getSidesItems(), player.hasPermission("safetrade.admin.logs.interact.items"))))
                                .delayTicks(1).submit(SafeTrade.getPlugin());
                    }
                    else if (item.equalTo(ItemUtils.Logs.getItems(log.getOtherParticipant()))) {
                        Sponge.getScheduler().createTaskBuilder().execute(() -> player.openInventory(getLogItemsInventory(log, log.getOtherParticipant(), log.getOtherSidesItems(), player.hasPermission("safetrade.admin.logs.interact.items"))))
                                .delayTicks(1).submit(SafeTrade.getPlugin());
                    }
                    else if (item.equalTo(ItemUtils.Logs.getPokemon(log.getParticipant()))) {
                        Sponge.getScheduler().createTaskBuilder().execute(() -> player.openInventory(getLogPokemonInventory(log, log.getParticipant(), log.getSidesPokemon(), player.hasPermission("safetrade.admin.logs.interact.pokemon"))))
                                .delayTicks(1).submit(SafeTrade.getPlugin());
                    }
                    else if (item.equalTo(ItemUtils.Logs.getPokemon(log.getOtherParticipant()))) {
                        Sponge.getScheduler().createTaskBuilder().execute(() -> player.openInventory(getLogPokemonInventory(log, log.getOtherParticipant(), log.getOtherSidesPokemon(), player.hasPermission("safetrade.admin.logs.interact.pokemon"))))
                                .delayTicks(1).submit(SafeTrade.getPlugin());
                    }
                    else if (item.equalTo(ItemUtils.Logs.getMoney(log.getParticipant()))) {
                        Sponge.getScheduler().createTaskBuilder().execute(() -> player.openInventory(getLogMoneyInventory(log, log.getParticipant(), log.getSidesMoneyWrappers(), player.hasPermission("safetrade.admin.logs.interact.money"))))
                                .delayTicks(1).submit(SafeTrade.getPlugin());
                    }
                    else if (item.equalTo(ItemUtils.Logs.getMoney(log.getOtherParticipant()))) {
                        Sponge.getScheduler().createTaskBuilder().execute(() -> player.openInventory(getLogMoneyInventory(log, log.getOtherParticipant(), log.getSidesMoneyWrappers(), player.hasPermission("safetrade.admin.logs.interact.money"))))
                                .delayTicks(1).submit(SafeTrade.getPlugin());
                    }
                });
            });
        });
    }

    //
    //  INVENTORIES
    //
    //  - Log (Items)
    //
    //
    //  CLICKERS
    //
    //  - Log (Items)
    //

    private static Inventory getLogItemsInventory(Log log, User user, List<ItemStackSnapshot> items, boolean adminAccess) {
        List<ItemStack> itemsForClicking = new ArrayList<>();
        items.forEach(item -> itemsForClicking.add(item.createStack()));
        if (adminAccess) {
            itemsForClicking.forEach(item -> {
                List<Text> existingLore = item.get(Keys.ITEM_LORE).isPresent() ? item.get(Keys.ITEM_LORE).get() : new ArrayList<>();
                if (existingLore.size() != 0) {
                    existingLore.add(Text.of());
                }
                existingLore.add(Text.of(TextColors.GREEN, "Left-click to put this item in your SafeTrade storage"));
                existingLore.add(Text.of(TextColors.GOLD, "Right-click to put this item in " + user.getName() + "'s SafeTrade storage"));
                item.offer(Keys.ITEM_LORE, existingLore);
            });
        }

        Inventory inventory = Inventory.builder()
                .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(Text.of(TextColors.DARK_AQUA, user.getName() + "'s Traded Items")))
                .property(InventoryDimension.PROPERTY_NAME, new InventoryDimension(9,6))
                .of(InventoryArchetypes.MENU_GRID)
                .listener(ClickInventoryEvent.class, event -> handleLogItemsClick(log, user, items, itemsForClicking, event))
                .build(SafeTrade.getPlugin());

        updateLogItemsInventory(inventory, itemsForClicking);

        return inventory;
    }

    private static void updateLogItemsInventory(Inventory inventory, List<ItemStack> itemsForClicking) {
        Iterator<ItemStack> iter = itemsForClicking.iterator();

        inventory.slots().forEach(slot -> {
            int i = slot.getProperty(SlotIndex.class, "slotindex").get().getValue();

            if (i <= 35) {
                if (iter.hasNext()) {
                    slot.set(iter.next());
                }
            }
            else if (i == 45) {
                slot.set(ItemUtils.Other.getBackButton());
            }
            else if (i <= 53) {
                slot.set(ItemUtils.Other.getFiller(DyeColors.GRAY));
            }
        });
    }

    private static void handleLogItemsClick(Log log, User user, List<ItemStackSnapshot> actualItems, List<ItemStack> itemsForClicking, ClickInventoryEvent event) {
        event.setCancelled(true);
        event.getCause().first(Player.class).ifPresent(player -> {
            // Prevents players from clicking if they have a cooldown
            if (hasCooldown(player.getUniqueId())) {
                return;
            }
            addCooldown(player.getUniqueId());

            event.getTransactions().forEach(transaction -> {
                transaction.getSlot().getProperty(SlotIndex.class, "slotindex").ifPresent(slot -> {
                    ItemStack item = transaction.getOriginal().createStack();
                    PlayerStorage storage;

                    if (item.equalTo(ItemUtils.Other.getBackButton())) {
                        Sponge.getScheduler().createTaskBuilder().execute(() -> player.openInventory(log.getInventory())).delayTicks(1).submit(SafeTrade.getPlugin());
                    }

                    // If the player does not have interact permissions they will not be able to interact with the items of the log.
                    if (!player.hasPermission("safetrade.admin.logs.interact.items")) {
                        return;
                    }

                    if (event instanceof ClickInventoryEvent.Primary) {
                        storage = Tracker.getOrCreateStorage(player);
                    }
                    else if (event instanceof ClickInventoryEvent.Secondary) {
                        storage = Tracker.getOrCreateStorage(user);
                    }
                    else {
                        return;
                    }

                    for (ItemStack i : itemsForClicking) {
                        if (item.equalTo(i)) {
                            ItemStackSnapshot snapshot = actualItems.get(itemsForClicking.indexOf(i));
                            storage.addItem(snapshot);
                            SafeTrade.sendMessageToPlayer(player, PrefixType.STORAGE, Text.of(TextColors.GOLD, snapshot.getQuantity(), "x", snapshot.getType().getTranslation().get(), TextColors.GREEN, " was added to " + (storage.getUser().get().getUniqueId().equals(user.getUniqueId()) ? "your" : (user.getName() + "'s")) + " storage."));
                            break;
                        }
                    }
                });
            });
        });
    }

    //
    //  INVENTORIES
    //
    //  - Log (Pokemon)
    //
    //
    //  CLICKERS
    //
    //  - Log (Pokemon)
    //

    private static Inventory getLogPokemonInventory(Log log, User user, List<Pokemon> pokemon, boolean adminAccess) {
        List<ItemStack> pokemonItems = new ArrayList<>();
        pokemon.forEach(p -> pokemonItems.add(ItemUtils.Pokemon.getPokemonIcon(p)));
        if (adminAccess) {
            pokemonItems.forEach(item -> {
                List<Text> existingLore = item.get(Keys.ITEM_LORE).isPresent() ? item.get(Keys.ITEM_LORE).get() : new ArrayList<>();
                if (existingLore.size() != 0) {
                    existingLore.add(Text.of());
                }
                existingLore.add(Text.of(TextColors.GREEN, "Left-click to put this pokemon in your SafeTrade storage"));
                existingLore.add(Text.of(TextColors.GOLD, "Right-click to put this pokemon in " + user.getName() + "'s SafeTrade storage"));
                item.offer(Keys.ITEM_LORE, existingLore);
            });
        }

        Inventory inventory = Inventory.builder()
                .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(Text.of(TextColors.DARK_AQUA, user.getName() + "'s Traded Items")))
                .property(InventoryDimension.PROPERTY_NAME, new InventoryDimension(9,6))
                .of(InventoryArchetypes.MENU_GRID)
                .listener(ClickInventoryEvent.class, event -> handleLogPokemonClick(log, user, pokemon, pokemonItems, event))
                .build(SafeTrade.getPlugin());

        updateLogPokemonInventory(inventory, pokemonItems);

        return inventory;
    }

    private static void updateLogPokemonInventory(Inventory inventory, List<ItemStack> pokemonItems) {
        Iterator<ItemStack> iter = pokemonItems.iterator();

        inventory.slots().forEach(slot -> {
            int i = slot.getProperty(SlotIndex.class, "slotindex").get().getValue();

            if (i <= 35) {
                if (iter.hasNext()) {
                    slot.set(iter.next());
                }
            }
            else if (i == 45) {
                slot.set(ItemUtils.Other.getBackButton());
            }
            else if (i <= 53) {
                slot.set(ItemUtils.Other.getFiller(DyeColors.GRAY));
            }
        });
    }

    private static void handleLogPokemonClick(Log log, User user, List<Pokemon> pokemon, List<ItemStack> pokemonItems, ClickInventoryEvent event) {
        event.setCancelled(true);
        event.getCause().first(Player.class).ifPresent(player -> {
            // Prevents players from clicking if they have a cooldown
            if (hasCooldown(player.getUniqueId())) {
                return;
            }
            addCooldown(player.getUniqueId());

            event.getTransactions().forEach(transaction -> {
                transaction.getSlot().getProperty(SlotIndex.class, "slotindex").ifPresent(slot -> {
                    ItemStack item = transaction.getOriginal().createStack();
                    PlayerStorage storage;

                    if (item.equalTo(ItemUtils.Other.getBackButton())) {
                        Sponge.getScheduler().createTaskBuilder().execute(() -> player.openInventory(log.getInventory())).delayTicks(1).submit(SafeTrade.getPlugin());
                    }

                    // If the player does not have interact permissions they will not be able to interact with the pokemon of the log.
                    if (!player.hasPermission("safetrade.admin.logs.interact.pokemon")) {
                        return;
                    }

                    if (event instanceof ClickInventoryEvent.Primary) {
                        storage = Tracker.getOrCreateStorage(player);
                    }
                    else if (event instanceof ClickInventoryEvent.Secondary) {
                        storage = Tracker.getOrCreateStorage(user);
                    }
                    else {
                        return;
                    }

                    for (ItemStack i : pokemonItems) {
                        if (item.equalTo(i)) {
                            Pokemon p = pokemon.get(pokemonItems.indexOf(i));
                            storage.addPokemon(p);
                            SafeTrade.sendMessageToPlayer(player, PrefixType.STORAGE, Text.of(TextColors.GOLD, p.getDisplayName(), TextColors.GREEN, " was added to " + (storage.getUser().get().getUniqueId().equals(user.getUniqueId()) ? "your" : (user.getName() + "'s")) + " storage."));
                            break;
                        }
                    }
                });
            });
        });
    }

    //
    //  INVENTORIES
    //
    //  - Log (MoneyWrapper)
    //
    //
    //  CLICKERS
    //
    //  - Log (MoneyWrapper)
    //

    private static Inventory getLogMoneyInventory(Log log, User user, List<MoneyWrapper> moneyWrappers, boolean adminAccess) {
        List<ItemStack> itemStacks = new ArrayList<>();
        moneyWrappers.forEach(money -> itemStacks.add(ItemUtils.Money.getMoney(money)));
        if (adminAccess) {
            itemStacks.forEach(item -> {
                List<Text> existingLore = item.get(Keys.ITEM_LORE).isPresent() ? item.get(Keys.ITEM_LORE).get() : new ArrayList<>();
                if (existingLore.size() != 0) {
                    existingLore.add(Text.of());
                }
                existingLore.add(Text.of(TextColors.GREEN, "Left-click to put this item in your SafeTrade storage"));
                existingLore.add(Text.of(TextColors.GOLD, "Right-click to put this item in " + user.getName() + "'s SafeTrade storage"));
                item.offer(Keys.ITEM_LORE, existingLore);
            });
        }

        Inventory inventory = Inventory.builder()
                .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(Text.of(TextColors.DARK_AQUA, user.getName() + "'s Traded Money")))
                .property(InventoryDimension.PROPERTY_NAME, new InventoryDimension(9, 6))
                .of(InventoryArchetypes.MENU_GRID)
                .listener(ClickInventoryEvent.class, event -> handleLogMoneyClick(log, user, moneyWrappers, itemStacks, event))
                .build(SafeTrade.getPlugin());

        updateLogMoneyInventory(inventory, itemStacks);

        return inventory;
    }

    private static void updateLogMoneyInventory(Inventory inventory, List<ItemStack> moneyItems) {
        Iterator<ItemStack> iter = moneyItems.iterator();

        inventory.slots().forEach(slot -> {
            int i = slot.getProperty(SlotIndex.class, "slotindex").get().getValue();

            if (i <= 35) {
                if (iter.hasNext()) {
                    slot.set(iter.next());
                }
            } else if (i == 45) {
                slot.set(ItemUtils.Other.getBackButton());
            } else if (i <= 53) {
                slot.set(ItemUtils.Other.getFiller(DyeColors.GRAY));
            }
        });
    }

    private static void handleLogMoneyClick(Log log, User user, List<MoneyWrapper> moneyWrappers, List<ItemStack> moneyWrapperItems, ClickInventoryEvent event) {
        event.setCancelled(true);
        event.getCause().first(Player.class).ifPresent(player -> {
            // Prevents players from clicking if they have a cooldown
            if (hasCooldown(player.getUniqueId())) {
                return;
            }
            addCooldown(player.getUniqueId());

            event.getTransactions().forEach(transaction -> {
                transaction.getSlot().getProperty(SlotIndex.class, "slotindex").ifPresent(slot -> {
                    ItemStack item = transaction.getOriginal().createStack();
                    PlayerStorage storage;

                    if (item.equalTo(ItemUtils.Other.getBackButton())) {
                        Sponge.getScheduler().createTaskBuilder().execute(() -> player.openInventory(log.getInventory())).delayTicks(1).submit(SafeTrade.getPlugin());
                    }

                    // If the player does not have interact permissions they will not be able to interact with the money of the log.
                    if (!player.hasPermission("safetrade.admin.logs.interact.money")) {
                        return;
                    }

                    if (event instanceof ClickInventoryEvent.Primary) {
                        storage = Tracker.getOrCreateStorage(player);
                    }
                    else if (event instanceof ClickInventoryEvent.Secondary) {
                        storage = Tracker.getOrCreateStorage(user);
                    }
                    else {
                        return;
                    }

                    for (ItemStack i : moneyWrapperItems) {
                        if (item.equalTo(i)) {
                            MoneyWrapper moneyWrapper = moneyWrappers.get(moneyWrapperItems.indexOf(i));
                            storage.addMoney(moneyWrapper);
                            SafeTrade.sendMessageToPlayer(player, PrefixType.STORAGE, Text.of(TextColors.GOLD, moneyWrapper.getCurrency().getSymbol(), NumberFormat.getNumberInstance(Locale.US).format(moneyWrapper.getBalance().intValue()), TextColors.GREEN, " was added to " + (storage.getUser().get().getUniqueId().equals(user.getUniqueId()) ? "your" : (user.getName() + "'s")) + " storage."));
                            break;
                        }
                    }
                });
            });
        });
    }

    /** {@link PlayerStorage} inventories: */

    //
    //  INVENTORIES
    //
    //  - Storage (Main)
    //
    //
    //  CLICKERS
    //
    //  - Storage (Main)
    //

    public static Inventory buildAndGetStorageInventory(PlayerStorage storage) {
        Inventory inventory = Inventory.builder()
                .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(Text.of(TextColors.DARK_AQUA, storage.getUser().get().getName() + "'s SafeTrade Storage", TextColors.RED, " [ALPHA]")))
                .property(InventoryDimension.PROPERTY_NAME, new InventoryDimension(9,1))
                .of(InventoryArchetypes.MENU_GRID)
                .listener(ClickInventoryEvent.class, event -> handleStorageClick(storage, event))
                .build(SafeTrade.getPlugin());

        updateStorageInventory(inventory, storage);

        return inventory;
    }

    private static void updateStorageInventory(Inventory inventory, PlayerStorage storage) {
        inventory.slots().forEach(slot -> {
            int i = slot.getProperty(SlotIndex.class, "slotindex").get().getValue();

            if (i == 1) {
                slot.set(ItemUtils.Storage.getHead(storage));
            }
            else if (i == 2) {
                slot.set(ItemUtils.Storage.getAutoClaim(storage));
            }
            else if (i == 4) {
                slot.set(ItemUtils.Storage.getMoney(storage));
            }
            else if (i == 5) {
                slot.set(ItemUtils.Storage.getItems(storage));
            }
            else if (i == 6) {
                slot.set(ItemUtils.Storage.getPokemon(storage));
            }
            else if (i <= 8) {
                slot.set(ItemUtils.Other.getFiller(DyeColors.LIGHT_BLUE));
            }
        });
    }

    private static void handleStorageClick(PlayerStorage storage, ClickInventoryEvent event) {
        event.setCancelled(true);
        event.getCause().first(Player.class).ifPresent(player -> {
            // Prevents players from clicking if they have a cooldown
            if (hasCooldown(player.getUniqueId())) {
                return;
            }
            addCooldown(player.getUniqueId());

            event.getTransactions().forEach(transaction -> {
                transaction.getSlot().getProperty(SlotIndex.class, "slotindex").ifPresent(slot -> {
                    ItemStack item = transaction.getOriginal().createStack();

                    if (item.equalTo(ItemUtils.Other.getBackButton())) {
                        Sponge.getScheduler().createTaskBuilder().execute(() -> player.openInventory(storage.getInventory())).delayTicks(1).submit(SafeTrade.getPlugin());
                    }
                    else if (item.equalTo(ItemUtils.Storage.getAutoClaim(storage)) && (storage.getPlayerUUID().equals(player.getUniqueId()) || player.hasPermission("safetrade.admin.storage.interact.autoclaim"))) {
                        Sponge.getScheduler().createTaskBuilder().execute(() -> {
                            storage.setAutoGive(!storage.isAutoGiveEnabled()); // This doesn't need to be delayed but no harm
                            storage.giveItems().forEach(snapshot -> SafeTrade.sendMessageToPlayer(player, PrefixType.STORAGE, Text.of(TextColors.GOLD, snapshot.getQuantity(), "x ", snapshot.getType().getTranslation().get(), TextColors.GREEN, " was added to your inventory.")));
                            storage.givePokemon().forEach(pokemon -> SafeTrade.sendMessageToPlayer(player, PrefixType.STORAGE, Text.of(TextColors.GOLD, pokemon.getDisplayName(), TextColors.GREEN, " was added to your party/pc.")));
                            storage.giveMoney().forEach(money -> SafeTrade.sendMessageToPlayer(player, PrefixType.STORAGE, Text.of(TextColors.GOLD, money.getCurrency().getSymbol(), money.getBalance().intValue(), TextColors.GREEN, " was added to your bank account.")));
                            updateStorageInventory(event.getTargetInventory(), storage);
                        }).delayTicks(1).submit(SafeTrade.getPlugin());
                    }
                    else if (item.equalTo(ItemUtils.Storage.getItems(storage))) {
                        Sponge.getScheduler().createTaskBuilder().execute(() -> player.openInventory(getStorageItemsInventory(player, storage)))
                                .delayTicks(1).submit(SafeTrade.getPlugin());
                    }
                    else if (item.equalTo(ItemUtils.Storage.getPokemon(storage))) {
                        Sponge.getScheduler().createTaskBuilder().execute(() -> player.openInventory(getStoragePokemonInventory(player, storage)))
                                .delayTicks(1).submit(SafeTrade.getPlugin());
                    }
                    else if (item.equalTo(ItemUtils.Storage.getMoney(storage))) {
                        Sponge.getScheduler().createTaskBuilder().execute(() -> player.openInventory(getStorageMoneyInventory(player, storage)))
                                .delayTicks(1).submit(SafeTrade.getPlugin());
                    }
                });
            });
        });
    }

    //
    //  INVENTORIES
    //
    //  - Storage (Items)
    //
    //
    //  CLICKERS
    //
    //  - Storage (Items)
    //

    private static Inventory getStorageItemsInventory(Player player, PlayerStorage storage) {
        List<ItemStack> itemsForClicking = new ArrayList<>();
        storage.getItems().forEach(item -> itemsForClicking.add(item.createStack()));

        if (player.getUniqueId().equals(storage.getPlayerUUID())) {
            itemsForClicking.forEach(item -> {
                List<Text> existingLore = item.get(Keys.ITEM_LORE).isPresent() ? item.get(Keys.ITEM_LORE).get() : new ArrayList<>();
                if (existingLore.size() != 0) {
                    existingLore.add(Text.of());
                }
                existingLore.add(Text.of(TextColors.GREEN, "Left-click to claim this item to your inventory."));
                item.offer(Keys.ITEM_LORE, existingLore);
            });
        }
        else if (player.hasPermission("safetrade.admin.storage.interact.items")) {
            itemsForClicking.forEach(item -> {
                List<Text> existingLore = item.get(Keys.ITEM_LORE).isPresent() ? item.get(Keys.ITEM_LORE).get() : new ArrayList<>();
                if (existingLore.size() != 0) {
                    existingLore.add(Text.of());
                }
                existingLore.add(Text.of(TextColors.GREEN, "Left-click to add this item to your SafeTrade storage."));
                existingLore.add(Text.of(TextColors.RED, "Right-click to remove this item from " + storage.getUser().get().getName() + "'s storage."));
                item.offer(Keys.ITEM_LORE, existingLore);
            });
        }

        Inventory inventory = Inventory.builder()
                .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(Text.of(TextColors.DARK_AQUA, storage.getUser().get().getName() + "'s Item Storage")))
                .property(InventoryDimension.PROPERTY_NAME, new InventoryDimension(9,6))
                .of(InventoryArchetypes.MENU_GRID)
                .listener(ClickInventoryEvent.class, event -> handleStorageItemsClick(storage, itemsForClicking, event))
                .build(SafeTrade.getPlugin());

        updateStorageItemsInventory(inventory, itemsForClicking);

        return inventory;
    }

    private static void updateStorageItemsInventory(Inventory inventory, List<ItemStack> itemsForClicking) {
        Iterator<ItemStack> iter = itemsForClicking.iterator();

        inventory.slots().forEach(slot -> {
            int i = slot.getProperty(SlotIndex.class, "slotindex").get().getValue();

            if (i <= 35) {
                if (iter.hasNext()) {
                    slot.set(iter.next());
                }
            }
            else if (i == 45) {
                slot.set(ItemUtils.Other.getBackButton());
            }
            else if (i <= 53) {
                slot.set(ItemUtils.Other.getFiller(DyeColors.GRAY));
            }
        });
    }

    private static void handleStorageItemsClick(final PlayerStorage storage, List<ItemStack> itemsForClicking, ClickInventoryEvent event) {
        event.setCancelled(true);
        event.getCause().first(Player.class).ifPresent(player -> {
            // Prevents players from clicking if they have a cooldown
            if (hasCooldown(player.getUniqueId())) {
                return;
            }
            addCooldown(player.getUniqueId());

            event.getTransactions().forEach(transaction -> {
                transaction.getSlot().getProperty(SlotIndex.class, "slotindex").ifPresent(slot -> {
                    ItemStack item = transaction.getOriginal().createStack();
                    Iterator<ItemStack> iter = itemsForClicking.iterator();

                    if (item.equalTo(ItemUtils.Other.getBackButton())) {
                        Sponge.getScheduler().createTaskBuilder().execute(() -> player.openInventory(storage.getInventory())).delayTicks(1).submit(SafeTrade.getPlugin());
                    }
                    else if (player.getUniqueId().equals(storage.getPlayerUUID())) {
                        while (iter.hasNext()) {
                            ItemStack i = iter.next();

                            if (item.equalTo(i)) {
                                ItemStackSnapshot snapshot = storage.getItems().get(itemsForClicking.indexOf(i));

                                if (event instanceof ClickInventoryEvent.Primary) {
                                    if (Utils.giveItem(player, snapshot)) {
                                        storage.removeItem(snapshot);
                                        iter.remove();
                                        SafeTrade.sendMessageToPlayer(player, PrefixType.STORAGE, Text.of(TextColors.GOLD, snapshot.getQuantity(), "x ", snapshot.getType().getTranslation().get(), TextColors.GREEN, " was added to your inventory."));
                                        Sponge.getScheduler().createTaskBuilder().execute(() -> player.openInventory(getStorageItemsInventory(player, storage))).delayTicks(1).submit(SafeTrade.getPlugin());
                                    }
                                    else {
                                        SafeTrade.sendMessageToPlayer(player, PrefixType.STORAGE, Text.of(TextColors.RED, "Cannot claim item: Inventory is full."));
                                    }
                                }
                                break;
                            }
                        }
                    }
                    else if (player.hasPermission("safetrade.admin.storage.interact.items")) {
                        for (ItemStack i : itemsForClicking) {
                            if (item.equalTo(i)) {
                                ItemStackSnapshot snapshot = storage.getItems().get(itemsForClicking.indexOf(i));

                                if (event instanceof ClickInventoryEvent.Primary) {
                                    Tracker.getOrCreateStorage(player).addItem(snapshot);
                                    SafeTrade.sendMessageToPlayer(player, PrefixType.STORAGE, Text.of(TextColors.GOLD, snapshot.getQuantity(), "x ", snapshot.getType().getTranslation().get(), TextColors.GREEN, " was added to your storage."));
                                }
                                else if (event instanceof ClickInventoryEvent.Secondary) {
                                    storage.removeItem(snapshot);
                                    iter.remove();
                                    Sponge.getScheduler().createTaskBuilder().execute(() -> updateStorageItemsInventory(event.getTargetInventory(), itemsForClicking)).delayTicks(1).submit(SafeTrade.getPlugin());
                                    SafeTrade.sendMessageToPlayer(player, PrefixType.STORAGE, Text.of(TextColors.GOLD, snapshot.getQuantity(), "x ", snapshot.getType().getTranslation().get(), TextColors.GREEN, " was removed from " + storage.getUser().get().getName() + "'s storage."));
                                }
                                break;
                            }
                        }
                    }
                });
            });
        });
    }

    //
    //  INVENTORIES
    //
    //  - Storage (Pokemon)
    //
    //
    //  CLICKERS
    //
    //  - Storage (Pokemon)
    //

    private static Inventory getStoragePokemonInventory(Player player, PlayerStorage storage) {
        List<ItemStack> pokemonItems = new ArrayList<>();
        storage.getPokemons().forEach(p -> pokemonItems.add(ItemUtils.Pokemon.getPokemonIcon(p)));

        if (player.getUniqueId().equals(storage.getPlayerUUID())) {
            pokemonItems.forEach(item -> {
                List<Text> existingLore = item.get(Keys.ITEM_LORE).isPresent() ? item.get(Keys.ITEM_LORE).get() : new ArrayList<>();
                if (existingLore.size() != 0) {
                    existingLore.add(Text.of());
                }
                existingLore.add(Text.of(TextColors.GREEN, "Left-click to claim this Pokemon to your party/pc."));
                item.offer(Keys.ITEM_LORE, existingLore);
            });
        }
        else if (player.hasPermission("safetrade.admin.storage.interact.pokemon")) {
            pokemonItems.forEach(item -> {
                List<Text> existingLore = item.get(Keys.ITEM_LORE).isPresent() ? item.get(Keys.ITEM_LORE).get() : new ArrayList<>();
                if (existingLore.size() != 0) {
                    existingLore.add(Text.of());
                }
                existingLore.add(Text.of(TextColors.GREEN, "Left-click to add this Pokemon to your SafeTrade storage."));
                existingLore.add(Text.of(TextColors.RED, "Right-click to remove this Pokemon from " + storage.getUser().get().getName() + "'s storage."));
                item.offer(Keys.ITEM_LORE, existingLore);
            });
        }

        Inventory inventory = Inventory.builder()
                .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(Text.of(TextColors.DARK_AQUA, storage.getUser().get().getName() + "'s Pokemon Storage")))
                .property(InventoryDimension.PROPERTY_NAME, new InventoryDimension(9,6))
                .of(InventoryArchetypes.MENU_GRID)
                .listener(ClickInventoryEvent.class, event -> handleStoragePokemonClick(storage, pokemonItems, event))
                .build(SafeTrade.getPlugin());

        updateStoragePokemonInventory(inventory, pokemonItems);

        return inventory;
    }

    private static void updateStoragePokemonInventory(Inventory inventory, List<ItemStack> pokemonItems) {
        Iterator<ItemStack> iter = pokemonItems.iterator();

        inventory.slots().forEach(slot -> {
            int i = slot.getProperty(SlotIndex.class, "slotindex").get().getValue();

            if (i <= 35) {
                if (iter.hasNext()) {
                    slot.set(iter.next());
                }
            }
            else if (i == 45) {
                slot.set(ItemUtils.Other.getBackButton());
            }
            else if (i <= 53) {
                slot.set(ItemUtils.Other.getFiller(DyeColors.GRAY));
            }
        });
    }

    private static void handleStoragePokemonClick(PlayerStorage storage, List<ItemStack> pokemonItems, ClickInventoryEvent event) {
        event.setCancelled(true);
        event.getCause().first(Player.class).ifPresent(player -> {
            // Prevents players from clicking if they have a cooldown
            if (hasCooldown(player.getUniqueId())) {
                return;
            }
            addCooldown(player.getUniqueId());

            event.getTransactions().forEach(transaction -> {
                transaction.getSlot().getProperty(SlotIndex.class, "slotindex").ifPresent(slot -> {
                    ItemStack item = transaction.getOriginal().createStack();
                    Iterator<ItemStack> iter = pokemonItems.iterator();

                    if (item.equalTo(ItemUtils.Other.getBackButton())) {
                        Sponge.getScheduler().createTaskBuilder().execute(() -> player.openInventory(storage.getInventory())).delayTicks(1).submit(SafeTrade.getPlugin());
                    }
                    else if (player.getUniqueId().equals(storage.getPlayerUUID())) {
                        while (iter.hasNext()) {
                            ItemStack i = iter.next();

                            if (item.equalTo(i)) {
                                Pokemon p = storage.getPokemons().get(pokemonItems.indexOf(i));

                                if (event instanceof ClickInventoryEvent.Primary) {
                                    if (Pixelmon.storageManager.getParty(storage.getPlayerUUID()).add(p)) {
                                        storage.removePokemon(p);
                                        iter.remove();
                                        Sponge.getScheduler().createTaskBuilder().execute(() -> player.openInventory(getStoragePokemonInventory(player, storage))).delayTicks(1).submit(SafeTrade.getPlugin());
                                        SafeTrade.sendMessageToPlayer(player, PrefixType.STORAGE, Text.of(TextColors.GOLD, p.getDisplayName(), TextColors.GREEN, " was added to your party/pc."));
                                    }
                                    else {
                                        SafeTrade.sendMessageToPlayer(player, PrefixType.STORAGE, Text.of(TextColors.RED, "Cannot claim Pokemon: PC is full."));
                                    }
                                }
                                break;
                            }
                        }
                    }
                    else if (player.hasPermission("safetrade.admin.storage.interact.pokemon")) {
                        while (iter.hasNext()) {
                            ItemStack i = iter.next();

                            if (item.equalTo(i)) {
                                Pokemon p = storage.getPokemons().get(pokemonItems.indexOf(i));

                                if (event instanceof ClickInventoryEvent.Primary) {
                                    Tracker.getOrCreateStorage(player).addPokemon(p);
                                    SafeTrade.sendMessageToPlayer(player, PrefixType.STORAGE, Text.of(TextColors.GOLD, p.getDisplayName(), TextColors.GREEN, " was added to your storage."));
                                }
                                else if (event instanceof ClickInventoryEvent.Secondary) {
                                    storage.removePokemon(p);
                                    iter.remove();
                                    Sponge.getScheduler().createTaskBuilder().execute(() -> updateStoragePokemonInventory(event.getTargetInventory(), pokemonItems)).delayTicks(1).submit(SafeTrade.getPlugin());
                                    SafeTrade.sendMessageToPlayer(player, PrefixType.STORAGE, Text.of(TextColors.GOLD, p.getDisplayName(), TextColors.GREEN, " was removed from " + storage.getUser().get().getName() + "'s storage."));
                                }
                                break;
                            }
                        }
                    }
                });
            });
        });
    }

    //
    //  INVENTORIES
    //
    //  - Storage (Money)
    //
    //
    //  CLICKERS
    //
    //  - Storage (Money)
    //

    private static Inventory getStorageMoneyInventory(Player player, PlayerStorage storage) {
        List<ItemStack> moneyItems = new ArrayList<>();
        storage.getMoney().forEach(moneyWrapper -> moneyItems.add(ItemUtils.Money.getMoney(moneyWrapper)));

        if (player.getUniqueId().equals(storage.getPlayerUUID())) {
            moneyItems.forEach(item -> {
                List<Text> existingLore = item.get(Keys.ITEM_LORE).isPresent() ? item.get(Keys.ITEM_LORE).get() : new ArrayList<>();
                if (existingLore.size() != 0) {
                    existingLore.add(Text.of());
                }
                existingLore.add(Text.of(TextColors.GREEN, "Left-click to claim this money to your bank account."));
                item.offer(Keys.ITEM_LORE, existingLore);
            });
        }
        else if (player.hasPermission("safetrade.admin.storage.interact.money")) {
            moneyItems.forEach(item -> {
                List<Text> existingLore = item.get(Keys.ITEM_LORE).isPresent() ? item.get(Keys.ITEM_LORE).get() : new ArrayList<>();
                if (existingLore.size() != 0) {
                    existingLore.add(Text.of());
                }
                existingLore.add(Text.of(TextColors.GREEN, "Left-click to add this money to your SafeTrade storage."));
                existingLore.add(Text.of(TextColors.RED, "Right-click to remove this money from " + storage.getUser().get().getName() + "'s storage."));
                item.offer(Keys.ITEM_LORE, existingLore);
            });
        }

        Inventory inventory = Inventory.builder()
                .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(Text.of(TextColors.DARK_AQUA, storage.getUser().get().getName() + "'s Money Storage")))
                .property(InventoryDimension.PROPERTY_NAME, new InventoryDimension(9,6))
                .of(InventoryArchetypes.MENU_GRID)
                .listener(ClickInventoryEvent.class, event -> handleStorageMoneyClick(storage, moneyItems, event))
                .build(SafeTrade.getPlugin());

        updateStorageMoneyInventory(inventory, moneyItems);

        return inventory;
    }

    private static void updateStorageMoneyInventory(Inventory inventory, List<ItemStack> moneyItems) {
        Iterator<ItemStack> iter = moneyItems.iterator();

        inventory.slots().forEach(slot -> {
            int i = slot.getProperty(SlotIndex.class, "slotindex").get().getValue();

            if (i <= 35) {
                if (iter.hasNext()) {
                    slot.set(iter.next());
                }
            }
            else if (i == 45) {
                slot.set(ItemUtils.Other.getBackButton());
            }
            else if (i <= 53) {
                slot.set(ItemUtils.Other.getFiller(DyeColors.GRAY));
            }
        });
    }

    private static void handleStorageMoneyClick(PlayerStorage storage, List<ItemStack> moneyItems, ClickInventoryEvent event) {
        event.setCancelled(true);
        event.getCause().first(Player.class).ifPresent(player -> {
            // Prevents players from clicking if they have a cooldown
            if (hasCooldown(player.getUniqueId())) {
                return;
            }
            addCooldown(player.getUniqueId());

            event.getTransactions().forEach(transaction -> {
                transaction.getSlot().getProperty(SlotIndex.class, "slotindex").ifPresent(slot -> {
                    ItemStack item = transaction.getOriginal().createStack();
                    Iterator<ItemStack> iter = moneyItems.iterator();

                    if (item.equalTo(ItemUtils.Other.getBackButton())) {
                        Sponge.getScheduler().createTaskBuilder().execute(() -> player.openInventory(storage.getInventory())).delayTicks(1).submit(SafeTrade.getPlugin());
                    }
                    else if (player.getUniqueId().equals(storage.getPlayerUUID())) {
                        while (iter.hasNext()) {
                            ItemStack i = iter.next();

                            if (item.equalTo(i)) {
                                MoneyWrapper moneyWrapper = storage.getMoney().get(moneyItems.indexOf(i));

                                if (event instanceof ClickInventoryEvent.Primary) {
                                    if (moneyWrapper.transferBalance(SafeTrade.getEcoService().getOrCreateAccount(player.getUniqueId()).get()).getResult() == ResultType.SUCCESS) {
                                        storage.removeMoney(moneyWrapper);
                                        iter.remove();
                                        Sponge.getScheduler().createTaskBuilder().execute(() -> player.openInventory(getStorageMoneyInventory(player, storage))).delayTicks(1).submit(SafeTrade.getPlugin());
                                        SafeTrade.sendMessageToPlayer(player, PrefixType.STORAGE, Text.of(TextColors.GOLD, moneyWrapper.getCurrency().getSymbol(), moneyWrapper.getBalance().intValue(), TextColors.GREEN, " was added to your bank account."));
                                    }
                                    else {
                                        SafeTrade.sendMessageToPlayer(player, PrefixType.STORAGE, Text.of(TextColors.RED, "Cannot claim money: Unknown."));
                                    }
                                }
                                break;
                            }
                        }
                    }
                    else if (player.hasPermission("safetrade.admin.storage.interact.money")) {
                        while (iter.hasNext()) {
                            ItemStack i = iter.next();

                            if (item.equalTo(i)) {
                                MoneyWrapper moneyWrapper = storage.getMoney().get(moneyItems.indexOf(i));

                                if (event instanceof ClickInventoryEvent.Primary) {
                                    Tracker.getOrCreateStorage(player).addMoney(moneyWrapper);
                                    SafeTrade.sendMessageToPlayer(player, PrefixType.STORAGE, Text.of(TextColors.GOLD, moneyWrapper.getCurrency().getSymbol(), moneyWrapper.getBalance().intValue(), TextColors.GREEN, " was added to your storage."));
                                }
                                else if (event instanceof ClickInventoryEvent.Secondary) {
                                    storage.removeMoney(moneyWrapper);
                                    iter.remove();
                                    Sponge.getScheduler().createTaskBuilder().execute(() -> updateStorageMoneyInventory(event.getTargetInventory(), moneyItems)).delayTicks(1).submit(SafeTrade.getPlugin());
                                    SafeTrade.sendMessageToPlayer(player, PrefixType.STORAGE, Text.of(TextColors.GOLD, moneyWrapper.getCurrency().getSymbol(), moneyWrapper.getBalance().intValue(), TextColors.GREEN, " was removed from " + storage.getUser().get().getName() + "'s storage."));
                                }
                                break;
                            }
                        }
                    }
                });
            });
        });
    }

}
