package io.github.cjcool06.safetrade.helpers;

import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonSpec;
import com.pixelmonmod.pixelmon.api.storage.PCStorage;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import io.github.cjcool06.safetrade.SafeTrade;
import io.github.cjcool06.safetrade.api.enums.InventoryType;
import io.github.cjcool06.safetrade.api.enums.TradeState;
import io.github.cjcool06.safetrade.api.events.trade.ConnectionEvent;
import io.github.cjcool06.safetrade.api.events.trade.InventoryChangeEvent;
import io.github.cjcool06.safetrade.api.events.trade.ViewerEvent;
import io.github.cjcool06.safetrade.obj.Side;
import io.github.cjcool06.safetrade.obj.Trade;
import io.github.cjcool06.safetrade.utils.ItemUtils;
import io.github.cjcool06.safetrade.utils.Utils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.service.economy.Currency;
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
    public static HashMap<Side, Integer> currentPage = new HashMap<>();

    public static Inventory buildAndGetMoneyInventory(Side side) {
        Inventory inventory = Inventory.builder()
                .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(Text.of(TextColors.DARK_AQUA, side.getUser().get().getName() + "'s Money")))
                .property(InventoryDimension.PROPERTY_NAME, new InventoryDimension(9,3))
                .of(InventoryArchetypes.MENU_GRID)
                .listener(ClickInventoryEvent.class, event -> handleMoneyClick(side, event))
                .listener(InteractInventoryEvent.Close.class, event -> handleBasicClose(side.parentTrade, InventoryType.MONEY, event))
                .listener(InteractInventoryEvent.Open.class, event -> handleOpen(side.parentTrade, event))
                .build(SafeTrade.getPlugin());

        inventory.slots().forEach(slot -> {
            int i = slot.getProperty(SlotIndex.class, "slotindex").get().getValue();

            if (i == 1) {
                slot.set(ItemUtils.Money.getMoneyBars(1));
            }
            else if (i == 2) {
                slot.set(ItemUtils.Money.getMoneyBars(10));
            }
            else if (i == 3) {
                slot.set(ItemUtils.Money.getMoneyBars(100));
            }
            else if (i == 4) {
                slot.set(ItemUtils.Money.getMoneyBars(1000));
            }
            else if (i == 5) {
                slot.set(ItemUtils.Money.getMoneyBars(10000));
            }
            else if (i == 6) {
                slot.set(ItemUtils.Money.getMoneyBars(100000));
            }
            else if (i == 7) {
                slot.set(ItemUtils.Money.getMoneyBars(1000000));
            }
            else if (i == 18) {
                slot.set(ItemUtils.Other.getBackButton());
            }
            else if (i == 22) {
                slot.set(ItemUtils.Money.getTotalMoney(side));
            }
            else if (i <= 25) {
                slot.set(ItemUtils.Other.getFiller(DyeColors.GRAY));
            }
            else if (i == 26) {
                slot.set(ItemUtils.Money.getPlayersMoney(side));
            }
        });

        return inventory;
    }

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

    public static void handleOverviewClick(Trade trade, ClickInventoryEvent event) {
        event.setCancelled(true);
        event.getCause().first(Player.class).ifPresent(player -> {
            event.getTransactions().forEach(transaction -> {
                transaction.getSlot().getProperty(SlotIndex.class, "slotindex").ifPresent(slot -> {
                    ItemStack item = transaction.getOriginal().createStack();
                    trade.getSide(player.getUniqueId()).ifPresent(side -> {
                        Side otherSide = side.getOtherSide();

                        if (item.equalTo(ItemUtils.Overview.getConfirm())) {
                            side.setConfirmed(true);

                            if (otherSide.isConfirmed()) {
                                trade.executeTrade();
                                Sponge.getScheduler().createTaskBuilder().execute(() -> {
                                    side.getPlayer().ifPresent(Player::closeInventory);
                                    otherSide.getPlayer().ifPresent(Player::closeInventory);
                                }).delayTicks(1).submit(SafeTrade.getPlugin());
                            }
                            else {
                                Sponge.getScheduler().createTaskBuilder().execute(() -> updateOverview(event.getTargetInventory(), trade)).delayTicks(1).submit(SafeTrade.getPlugin());
                            }
                        }
                        else if (item.equalTo(ItemUtils.Overview.getCancel())) {
                            side.setConfirmed(false);
                            side.setReady(false);
                            otherSide.setConfirmed(false);
                            otherSide.setReady(false);
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
                    if (side.currentInventory == InventoryType.MAIN) {
                        Sponge.getScheduler().createTaskBuilder().execute(trade::reformatInventory).delayTicks(1).submit(SafeTrade.getPlugin());
                    }
                }
                else {
                    Sponge.getScheduler().createTaskBuilder().execute(() -> SafeTrade.EVENT_BUS.post(new InventoryChangeEvent.Post(side))).delayTicks(1).submit(SafeTrade.getPlugin());
                }
            }
            // An unauthorized player is attempting to open the trade unexpectedly
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
                // When currentInventory equals MAIN AND this inventoryType is NOT MAIN, the player is accessing the other side's inventories
                if (side.currentInventory.equals(inventoryType) || side.currentInventory.equals(InventoryType.NONE) || !(side.currentInventory.equals(InventoryType.MAIN) && !inventoryType.equals(InventoryType.MAIN))) {
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
                    // From what I can think of there is no inherent harm due to this happening, just something to keep in mind.
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


    public static void handleMoneyClick(Side side, ClickInventoryEvent event) {
        event.setCancelled(true);
        event.getCause().first(Player.class).ifPresent(player -> {
            event.getTransactions().forEach(transaction -> {
                transaction.getSlot().getProperty(SlotIndex.class, "slotindex").ifPresent(slot -> {
                    ItemStack item = transaction.getOriginal().createStack();

                    if (item.equalTo(ItemUtils.Other.getBackButton())) {
                        Sponge.getScheduler().createTaskBuilder().execute(() ->
                                side.changeInventoryForViewer(player, side.parentTrade.getState() == TradeState.WAITING_FOR_CONFIRMATION ? InventoryType.OVERVIEW : InventoryType.MAIN)
                        ).delayTicks(1).submit(SafeTrade.getPlugin());
                    }
                    // If player is not the in the side of this vault, or the if the vault is locked, or they didn't left/right click, nothing will happen.
                    // Checking for player is redundant when going through the main trade inventory, but I'll keep it here in-case a player somehow opens another side's money inventory
                    else if ((player.getUniqueId().equals(side.sideOwnerUUID) && !side.vault.isLocked() && (event instanceof ClickInventoryEvent.Primary || event instanceof ClickInventoryEvent.Secondary))
                            && side.parentTrade.getState() == TradeState.TRADING) {
                        for (int i = 1; i <= 1000000; i *= 10) {

                            if (item.equalTo(ItemUtils.Money.getMoneyBars(i))) {
                                Currency currency = SafeTrade.getEcoService().getDefaultCurrency();
                                // Left clicking = adding money to trade
                                if (event instanceof ClickInventoryEvent.Primary) {
                                    TransactionResult result = SafeTrade.getEcoService().getOrCreateAccount(side.getUser().get().getUniqueId()).get()
                                            .transfer(side.vault.account, currency, BigDecimal.valueOf(i), Cause.of(EventContext.empty(), SafeTrade.getPlugin()));

                                    if (result.getResult() == ResultType.SUCCESS) {
                                        side.sendMessage(Text.of(TextColors.GREEN, "Successfully added " + NumberFormat.getNumberInstance(Locale.US).format(i) + " ", currency.getPluralDisplayName(), " to the trade."));

                                        // Refreshes the total money and player money item
                                        event.getTargetInventory().slots().forEach(s -> {
                                            int index = s.getProperty(SlotIndex.class, "slotindex").get().getValue();
                                            if (index == 22) {
                                                s.set(ItemUtils.Money.getTotalMoney(side));
                                            }
                                            else if (index == 26) {
                                                s.set(ItemUtils.Money.getPlayersMoney(side));
                                            }
                                        });
                                        side.parentTrade.reformatInventory();
                                    } else {
                                        side.sendMessage(Text.of(TextColors.RED, "You do not have enough money."));
                                    }
                                }
                                // Right clicking = removing money from trade
                                else {
                                    if (i > side.vault.account.getBalance(currency).intValue()) {
                                        int val = side.vault.account.getBalance(currency).intValue();
                                        side.vault.account.transfer(SafeTrade.getEcoService().getOrCreateAccount(side.getUser().get().getUniqueId()).get(), currency, side.vault.account.getBalance(currency), Cause.of(EventContext.empty(), SafeTrade.getPlugin()));
                                        side.sendMessage(Text.of(TextColors.GREEN, "Successfully removed " + NumberFormat.getNumberInstance(Locale.US).format(val) + " ", currency.getPluralDisplayName(), " from the trade."));
                                        // Refreshes the total money and player money item
                                        event.getTargetInventory().slots().forEach(s -> {
                                            int index = s.getProperty(SlotIndex.class, "slotindex").get().getValue();
                                            if (index == 22) {
                                                s.set(ItemUtils.Money.getTotalMoney(side));
                                            }
                                            else if (index == 26) {
                                                s.set(ItemUtils.Money.getPlayersMoney(side));
                                            }
                                        });
                                        side.parentTrade.reformatInventory();
                                    }
                                    else {
                                        TransactionResult result = side.vault.account.transfer(SafeTrade.getEcoService().getOrCreateAccount(side.getUser().get().getUniqueId()).get(), currency, BigDecimal.valueOf(i), Cause.of(EventContext.empty(), SafeTrade.getPlugin()));

                                        if (result.getResult() == ResultType.SUCCESS) {
                                            side.sendMessage(Text.of(TextColors.GREEN, "Successfully removed " + NumberFormat.getNumberInstance(Locale.US).format(i) + " ", currency.getPluralDisplayName(), " from the trade."));
                                            // Refreshes the total money and player money item
                                            event.getTargetInventory().slots().forEach(s -> {
                                                int index = s.getProperty(SlotIndex.class, "slotindex").get().getValue();
                                                if (index == 22) {
                                                    s.set(ItemUtils.Money.getTotalMoney(side));
                                                }
                                                else if (index == 26) {
                                                    s.set(ItemUtils.Money.getPlayersMoney(side));
                                                }
                                            });
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

    public static void handlePCClick(Side side, ClickInventoryEvent event) {
        LinkedHashMap<ItemStack, Pokemon>[] pcArr = Utils.generatePCMaps(side);
        LinkedHashMap<ItemStack, Pokemon> partyMap = pcArr[0];
        LinkedHashMap<ItemStack, Pokemon> pcMap = pcArr[1];
        int page = currentPage.get(side);

        event.setCancelled(true);
        event.getCause().first(Player.class).ifPresent(player -> {
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

                                if (PokemonSpec.from("untradeable").matches(pokemon)) {
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
                                if (PokemonSpec.from("untradeable").matches(pokemon)) {
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
}
