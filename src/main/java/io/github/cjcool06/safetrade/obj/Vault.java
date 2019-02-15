package io.github.cjcool06.safetrade.obj;


import io.github.cjcool06.safetrade.SafeTrade;
import io.github.cjcool06.safetrade.api.enums.InventoryType;
import io.github.cjcool06.safetrade.api.enums.TradeState;
import io.github.cjcool06.safetrade.api.events.trade.TransactionEvent;
import io.github.cjcool06.safetrade.helpers.InventoryHelper;
import io.github.cjcool06.safetrade.utils.ItemUtils;
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
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.*;

/**
 * A Vault represents a side-specific storage, capable of holding Items, Pokemon, and Money that can be manipulated.
 */
public class Vault {
    public final Side side;
    public final Inventory itemStorage;
    public final Account account;
    private boolean locked = false;

    public Vault(Side side) {
        this.side = side;
        this.account = attemptAccountCreation();

        itemStorage = Inventory.builder()
                .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(Text.of(TextColors.DARK_AQUA, side.getPlayer().get().getName() + "'s Items")))
                .property(InventoryDimension.PROPERTY_NAME, new InventoryDimension(9,6))
                .of(InventoryArchetypes.MENU_GRID)
                .listener(ClickInventoryEvent.class, this::handleItemStorageClick)
                .listener(InteractInventoryEvent.Close.class, event -> InventoryHelper.handleBasicClose(side.parentTrade, InventoryType.ITEM, event))
                .listener(InteractInventoryEvent.Open.class, event -> InventoryHelper.handleOpen(side.parentTrade, event))
                .build(SafeTrade.getPlugin());

        formatItemInventory();
    }

    /**
     * Gets whether the vault is locked.
     *
     * @return True if locked
     */
    public boolean isLocked() {
        return locked;
    }

    /**
     * Sets whether the vault is locked.
     *
     * @param locked True if locked
     */
    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    /**
     * Moves all possessions in the trade (Pokemon, items, money) in to the respective {@link PlayerStorage} and bank account.
     *
     * <p>This is useful when a trade is cancelled or the server is stopping.</p>
     */
    public void unloadToStorage(PlayerStorage storage) {
        getAllItems().forEach(storage::addItem);

        Currency currency = SafeTrade.getEcoService().getDefaultCurrency();
        account.transfer(SafeTrade.getEcoService().getOrCreateAccount(storage.playerUUID).get(), currency, account.getBalance(currency), Cause.of(EventContext.empty(), SafeTrade.getPlugin()));
    }

    /**
     * Attempts to add an {@link ItemStack} to the items inventory.
     *
     * @param item The item
     * @return True if the item was successfully added to the inventory, false if not
     */
    public boolean addItem(ItemStack item) {
        if (Sponge.getEventManager().post(new TransactionEvent.Item.Add.Pre(this, item))) {
            return false;
        }
        Iterator<Inventory> iter = itemStorage.slots().iterator();

        while (iter.hasNext()) {
            Inventory slot = iter.next();
            int ind = slot.getProperty(SlotIndex.class, "slotindex").get().getValue();
            // Last 2 rows are static clickable items
            if (ind >= 36) {
                break;
            }
            if (!slot.peek().isPresent()) {
                InventoryTransactionResult result = slot.set(item);
                if (result.getType().equals(InventoryTransactionResult.Type.SUCCESS)) {
                    Sponge.getEventManager().post(new TransactionEvent.Item.Add.Success(this, item));
                    return true;
                }
            }
        }

        Sponge.getEventManager().post(new TransactionEvent.Item.Add.Fail(this, item));
        return false;
    }

    /**
     * Attempts to add an {@link ItemStack} to a specific slot in the items inventory.
     *
     * <p>The index must not be greater than 35.</p>
     *
     * @param index The slot index
     * @param item The item
     * @return True if the item was successfully added to the inventory, false if not
     */
    public boolean addItem(int index, ItemStack item) {
        if (Sponge.getEventManager().post(new TransactionEvent.Item.Add.Pre(this, item))) {
            return false;
        }
        if (index >= 36) {
            return false;
        }
        Iterator<Inventory> iter = itemStorage.slots().iterator();

        while (iter.hasNext()) {
            Inventory slot = iter.next();
            if (slot.getProperty(SlotIndex.class, "slotindex").get().getValue() == index) {
                if (!slot.peek().isPresent()) {
                    InventoryTransactionResult result = slot.set(item);
                    if (result.getType().equals(InventoryTransactionResult.Type.SUCCESS)) {
                        Sponge.getEventManager().post(new TransactionEvent.Item.Add.Success(this, item));
                        return true;
                    }
                }
                else {
                    break;
                }
            }
        }

        Sponge.getEventManager().post(new TransactionEvent.Item.Add.Fail(this, item));
        return false;
    }

    /**
     * Removes an item from the items inventory.
     *
     * @param item The item
     * @return True if the item was successfully removed from the inventory, false if not
     */
    public boolean removeItem(ItemStack item) {
        if (Sponge.getEventManager().post(new TransactionEvent.Item.Remove.Pre(this, item))) {
            return false;
        }
        Iterator<Inventory> iter = itemStorage.slots().iterator();

        while (iter.hasNext()) {
            Inventory slot = iter.next();
            int ind = slot.getProperty(SlotIndex.class, "slotindex").get().getValue();
            if (ind >= 36) {
                break;
            }
            if (slot.peek().isPresent() && slot.contains(item)) {
                slot.poll();
                Sponge.getEventManager().post(new TransactionEvent.Item.Remove.Success(this, item));
                return true;
            }
        }

        Sponge.getEventManager().post(new TransactionEvent.Item.Remove.Fail(this, item));
        return false;
    }

    /**
     * Removes an item in a specific index from the items inventory.
     *
     * <p>The index must not be greater than 35.</p>
     *
     * @param index The index
     * @return True if the item was successfully removed from the inventory, false if not
     */
    public boolean removeItem(int index) {
        if (index >= 36) {
            return false;
        }
        Iterator<Inventory> iter = itemStorage.slots().iterator();

        while (iter.hasNext()) {
            Inventory slot = iter.next();
            if (slot.getProperty(SlotIndex.class, "slotindex").get().getValue() == index) {
                if (slot.peek().isPresent()) {
                    ItemStack item = slot.peek().get();
                    if (Sponge.getEventManager().post(new TransactionEvent.Item.Remove.Pre(this, item))) {
                        return false;
                    }
                    else {
                        slot.poll();
                        Sponge.getEventManager().post(new TransactionEvent.Item.Remove.Success(this, item));
                    }
                }
                break;
            }
        }

        return false;
    }

    /**
     * Swaps the positions of two {@link ItemStack} in the items inventory.
     *
     * <p>One of the indexes can be empty and it will still work.</p>
     *
     * <p>The indexes must not be greater than 35.</p>
     *
     * @param index1 The first index
     * @param index2 The second index
     */
    public void swapItems(int index1, int index2) {
        if (index1 >= 36 || index2 >= 36) {
            return;
        }
        Iterator<Inventory> iter = itemStorage.slots().iterator();
        ItemStack item1 = null;
        ItemStack item2 = null;

        // Polls (gets and removes) the items from the relevant slots.
        while (iter.hasNext()) {
            Inventory slot = iter.next();
            int ind = slot.getProperty(SlotIndex.class, "slotindex").get().getValue();
            if (ind == index1) {
                if (slot.peek().isPresent()) {
                    item1 = slot.poll().get();
                }
            }
            else if (ind == index2) {
                if (slot.peek().isPresent()) {
                    item2 = slot.poll().get();
                }
            }
            // Exit condition to prevent unnecessary iterations
            else if (ind > index1 && ind > index2) {
                break;
            }
        }

        iter = itemStorage.slots().iterator();

        // Sets the previously polled items in the opposite slot they were in
        while (iter.hasNext()) {
            Inventory slot = iter.next();
            int ind = slot.getProperty(SlotIndex.class, "slotindex").get().getValue();
            if (ind == index1) {
                if (item2 != null) {
                    slot.set(item2);
                }
            }
            else if (ind == index2) {
                if (item1 != null) {
                    slot.set(item1);
                }
            }
            // Exit condition to prevent unnecessary iterations
            else if (ind > index1 && ind > index2) {
                break;
            }
        }
    }

    /**
     * Gets all the {@link ItemStackSnapshot}s in the vault.
     *
     * @return A list of items
     */
    public List<ItemStackSnapshot> getAllItems() {
        List<ItemStackSnapshot> items = new ArrayList<>();

        itemStorage.slots().forEach(slot -> {
            int ind = slot.getProperty(SlotIndex.class, "slotindex").get().getValue();
            if (ind <= 35 && slot.peek().isPresent()) {
                items.add(slot.peek().get().createSnapshot());
            }
        });

        return items;
    }

    /**
     * Clears the Items, Pokemon, and Money from the vault.
     */
    public void clear() {
        itemStorage.clear();
        account.resetBalance(SafeTrade.getEcoService().getDefaultCurrency(), Cause.of(EventContext.empty(), SafeTrade.getPlugin()));
    }

    /**
     * If an account creation fails, it will retry with a different {@link UUID}.
     *
     * <p>After 100 fails the trade will be force ended and will return null</p>
     *
     * @return An {@link Account}, if possible
     */
    private Account attemptAccountCreation() {
        for (int i = 0; i <= 100; i++) {
            Optional<Account> optAcc = SafeTrade.getEcoService().getOrCreateAccount(UUID.randomUUID().toString());
            if (optAcc.isPresent()) {
                return optAcc.get();
            }
        }
        Sponge.getScheduler().createTaskBuilder().execute(() -> {
            side.parentTrade.sendMessage(Text.of(TextColors.RED, "Account creation failed."));
            side.parentTrade.sendMessage(Text.of(TextColors.RED, "Force ending trade to prevent further errors."));
            side.parentTrade.sendMessage(Text.of(TextColors.RED, "If this keeps happening, please report it to an administrator."));
            side.parentTrade.forceEnd();
        }).delayTicks(1).submit(SafeTrade.getPlugin());

        return null;
    }

    private void formatItemInventory() {
        //36 -> 53
        itemStorage.slots().forEach(slot -> {
            int ind = slot.getProperty(SlotIndex.class, "slotindex").get().getValue();
            if (ind == 49) {
                slot.set(ItemUtils.Other.getBackButton());
            }
            else if (ind >= 36 && ind <= 53) {
                slot.set(ItemUtils.Other.getFiller(DyeColors.CYAN));
            }
        });
    }

    private void handleItemStorageClick(ClickInventoryEvent event) {
        event.getCause().first(Player.class).ifPresent(player -> {
            event.getTransactions().forEach(transaction -> {
                transaction.getSlot().getProperty(SlotIndex.class, "slotindex").ifPresent(slot -> {
                    ItemStack item = transaction.getOriginal().createStack();

                    // The player is clicking out-of-bounds of the storage, and is hence clicking a static item/button
                    if (slot.getValue() >= 36 && slot.getValue() <= 53) {
                        event.setCancelled(true);
                        if (item.equalTo(ItemUtils.Other.getBackButton())) {
                            Sponge.getScheduler().createTaskBuilder().execute(() ->
                                    side.changeInventoryForViewer(player, side.parentTrade.getState() == TradeState.WAITING_FOR_CONFIRMATION ? InventoryType.OVERVIEW : InventoryType.MAIN)
                            ).delayTicks(1).submit(SafeTrade.getPlugin());
                        }
                    }

                    // If player is not the in the side of this vault, or the if the vault is locked, or the trade is not in the trading state, they can't manipulate anything
                    else if (!player.getUniqueId().equals(side.sideOwnerUUID) || locked || side.parentTrade.getState() != TradeState.TRADING) {
                        event.setCancelled(true);
                    }

                    // The player is adding/removing items to/from the storage
                    else if (slot.getValue() <= 35) {
                        // If true, the player is attempting to remove an item from the vault's item storage
                        if (itemStorage.contains(transaction.getOriginal().createStack())) {
                            if (Sponge.getEventManager().post(new TransactionEvent.Item.Remove.Pre(side.vault, transaction.getOriginal().createStack()))) {
                                event.setCancelled(true);
                            }
                            else {
                                Sponge.getScheduler().createTaskBuilder().execute((() -> Sponge.getEventManager().post(new TransactionEvent.Item.Remove.Success(this, item)))).delayTicks(1).submit(SafeTrade.getPlugin());
                            }
                        }
                        // Else, the player is attempting to add an item to the vault's item storage
                        else {
                            if (Sponge.getEventManager().post(new TransactionEvent.Item.Add.Pre(side.vault, transaction.getOriginal().createStack()))) {
                                event.setCancelled(true);
                            }
                            else {
                                Sponge.getScheduler().createTaskBuilder().execute((() -> Sponge.getEventManager().post(new TransactionEvent.Item.Add.Success(this, item)))).delayTicks(1).submit(SafeTrade.getPlugin());
                            }
                        }
                    }
                });
            });
        });
    }
}