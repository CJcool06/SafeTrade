package io.github.cjcool06.safetrade.obj;


import com.google.common.collect.Lists;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import io.github.cjcool06.safetrade.SafeTrade;
import io.github.cjcool06.safetrade.api.enums.InventoryType;
import io.github.cjcool06.safetrade.api.enums.PrefixType;
import io.github.cjcool06.safetrade.api.enums.TradeState;
import io.github.cjcool06.safetrade.api.events.trade.TransactionEvent;
import io.github.cjcool06.safetrade.helpers.InventoryHelper;
import io.github.cjcool06.safetrade.trackers.Tracker;
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

import java.math.BigDecimal;
import java.util.*;

/**
 * A Vault represents a side-specific storage, capable of holding Items, Pokemon, and Money that can be manipulated.
 */
public class Vault {
    public final Side side;
    public final Inventory itemStorage;
    public final Inventory pokemonStorage;
    public final Account account;
    private final Map<Integer, Pokemon> entityStorage = new HashMap<>();
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

        pokemonStorage = Inventory.builder()
                .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(Text.of(TextColors.DARK_AQUA, side.getPlayer().get().getName() + "'s Pokemon")))
                .property(InventoryDimension.PROPERTY_NAME, new InventoryDimension(9,6))
                .of(InventoryArchetypes.MENU_GRID)
                .listener(ClickInventoryEvent.class, this::handlePokemonStorageClick)
                .listener(InteractInventoryEvent.Close.class, event -> InventoryHelper.handleBasicClose(side.parentTrade, InventoryType.POKEMON, event))
                .listener(InteractInventoryEvent.Open.class, event -> InventoryHelper.handleOpen(side.parentTrade, event))
                .build(SafeTrade.getPlugin());

        formatItemInventory();
        formatPokemonInventory();
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
     * Moves all possessions in the trade (Pokemon, items, money) in to the respective {@link PlayerStorage}.
     */
    public void unloadToStorage(PlayerStorage storage) {
        storage.addItems(getAllItems());
        storage.addPokemon(getAllPokemon());
        storage.addMoney(getAllMoney());
    }

    /**
     * Converts all money in the vault's {@link Account} to {@link MoneyWrapper}s.
     *
     * @return The wrappers
     */
    public List<MoneyWrapper> getAllMoney() {
        List<MoneyWrapper> wrappers = new ArrayList<>();

        for (Currency currency : SafeTrade.getEcoService().getCurrencies()) {
            BigDecimal balance = account.getBalance(currency);
            if (balance.longValue() != 0) {
                wrappers.add(new MoneyWrapper(currency, balance));
            }
        }

        return wrappers;
    }

    /**
     * Attempts to add an {@link ItemStack} to the items inventory.
     *
     * @param item The item
     * @return True if the item was successfully added to the inventory, false if not
     */
    public boolean addItem(ItemStack item) {
        if (SafeTrade.EVENT_BUS.post(new TransactionEvent.Item.Add.Pre(this, item))) {
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
                    SafeTrade.EVENT_BUS.post(new TransactionEvent.Item.Add.Success(this, item));
                    return true;
                }
            }
        }

        SafeTrade.EVENT_BUS.post(new TransactionEvent.Item.Add.Fail(this, item));
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
        if (SafeTrade.EVENT_BUS.post(new TransactionEvent.Item.Add.Pre(this, item))) {
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
                        SafeTrade.EVENT_BUS.post(new TransactionEvent.Item.Add.Success(this, item));
                        return true;
                    }
                }
                else {
                    break;
                }
            }
        }

        SafeTrade.EVENT_BUS.post(new TransactionEvent.Item.Add.Fail(this, item));
        return false;
    }

    /**
     * Removes an item from the items inventory.
     *
     * @param item The item
     * @return True if the item was successfully removed from the inventory, false if not
     */
    public boolean removeItem(ItemStack item) {
        if (SafeTrade.EVENT_BUS.post(new TransactionEvent.Item.Remove.Pre(this, item))) {
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
                SafeTrade.EVENT_BUS.post(new TransactionEvent.Item.Remove.Success(this, item));
                return true;
            }
        }

        SafeTrade.EVENT_BUS.post(new TransactionEvent.Item.Remove.Fail(this, item));
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
                    if (SafeTrade.EVENT_BUS.post(new TransactionEvent.Item.Remove.Pre(this, item))) {
                        return false;
                    }
                    else {
                        slot.poll();
                        SafeTrade.EVENT_BUS.post(new TransactionEvent.Item.Remove.Success(this, item));
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
     * Attempts to add an {@link EntityPixelmon} to the pokemon inventory.
     *
     * @param pokemon
     * @return True if the Pokemon was successfully added to the inventory, false if not
     */
    public boolean addPokemon(Pokemon pokemon) {
        if (SafeTrade.EVENT_BUS.post(new TransactionEvent.Pokemon.Add.Pre(this, pokemon))) {
            return false;
        }
        ItemStack item = ItemUtils.Pokemon.getPokemonIcon(pokemon);
        Iterator<Inventory> iter = pokemonStorage.slots().iterator();

        while (iter.hasNext()) {
            Inventory slot = iter.next();
            int ind = slot.getProperty(SlotIndex.class, "slotindex").get().getValue();
            // Last 2 rows are static clickable items
            if (ind >= 36) {
                break;
            }
            if (!slot.peek().isPresent()) {
                slot.set(item);
                entityStorage.put(ind, pokemon);
                SafeTrade.EVENT_BUS.post(new TransactionEvent.Pokemon.Add.Success(this, pokemon));
                return true;
            }
        }

        SafeTrade.EVENT_BUS.post(new TransactionEvent.Pokemon.Add.Fail(this, pokemon));
        return false;
    }

    /**
     * Attempts to add an {@link EntityPixelmon} to a specific slot in the pokemon inventory.
     *
     * <p>The index must not be greater than 35.</p>
     *
     * @param pokemon The pokemon
     * @param index The index
     * @return True if the Pokemon was successfully added to the inventory, false if not
     */
    public boolean addPokemon(Pokemon pokemon, int index) {
        if (SafeTrade.EVENT_BUS.post(new TransactionEvent.Pokemon.Add.Pre(this, pokemon))) {
            return false;
        }
        ItemStack item = ItemUtils.Pokemon.getPokemonIcon(pokemon);
        Iterator<Inventory> iter = pokemonStorage.slots().iterator();

        while (iter.hasNext()) {
            Inventory slot = iter.next();
            int ind = slot.getProperty(SlotIndex.class, "slotindex").get().getValue();
            // Last 2 rows are static clickable items
            if (ind >= 36) {
                break;
            }
            if (ind == index) {
                if (!slot.peek().isPresent()) {
                    slot.set(item);
                    entityStorage.put(ind, pokemon);
                    SafeTrade.EVENT_BUS.post(new TransactionEvent.Pokemon.Add.Success(this, pokemon));
                    return true;
                }
                break;
            }
        }

        SafeTrade.EVENT_BUS.post(new TransactionEvent.Pokemon.Add.Fail(this, pokemon));
        return false;
    }

    /**
     * Gets the inventory index of a {@link Pokemon} in the vault.
     *
     * @param pokemon The pokemon
     * @return The index, null if none
     */
    public Integer getIndex(Pokemon pokemon) {
        for (Integer index : entityStorage.keySet()) {
            if (entityStorage.get(index).equals(pokemon)) {
                return index;
            }
        }

        return null;
    }

    /**
     * Removes an {@link EntityPixelmon} from the pokemon inventory.
     *
     * @param pokemon The pokemon
     * @return True if the Pokemon was successfully removed from the inventory, false if not
     */
    public boolean removePokemon(Pokemon pokemon) {
        if (SafeTrade.EVENT_BUS.post(new TransactionEvent.Pokemon.Remove.Pre(this, pokemon))) {
            return false;
        }
        ItemStack item = ItemUtils.Pokemon.getPokemonIcon(pokemon);
        Iterator<Inventory> iter = pokemonStorage.slots().iterator();

        while (iter.hasNext()) {
            Inventory slot = iter.next();
            int ind = slot.getProperty(SlotIndex.class, "slotindex").get().getValue();
            if (ind >= 36) {
                break;
            }
            if (slot.peek().isPresent()) {
                if (slot.peek().get().equalTo(item)) {
                    slot.poll();
                    entityStorage.remove(ind);
                    SafeTrade.EVENT_BUS.post(new TransactionEvent.Pokemon.Remove.Success(this, pokemon));
                    return true;
                }
            }
        }

        SafeTrade.EVENT_BUS.post(new TransactionEvent.Pokemon.Remove.Fail(this, pokemon));
        return false;
    }

    /**
     * Removes an {@link EntityPixelmon} at a specific index from the pokemon inventory.
     *
     * <p>The index must not be greater than 35.</p>
     *
     * @param index The index
     * @return True if the Pokemon was successfully removed from the inventory, false if not
     */
    public boolean removePokemon(int index) {
        if (index >= 36) {
            return false;
        }
        Iterator<Inventory> iter = pokemonStorage.slots().iterator();

        while (iter.hasNext()) {
            Inventory slot = iter.next();
            int ind = slot.getProperty(SlotIndex.class, "slotindex").get().getValue();
            if (ind == index) {
                if (slot.peek().isPresent() && entityStorage.containsKey(ind)) {
                    Pokemon pokemon = entityStorage.get(ind);
                    if (SafeTrade.EVENT_BUS.post(new TransactionEvent.Pokemon.Remove.Pre(this, pokemon))) {
                        return false;
                    }
                    slot.poll();
                    entityStorage.remove(ind);
                    SafeTrade.EVENT_BUS.post(new TransactionEvent.Pokemon.Remove.Success(this, pokemon));
                    return true;
                }
                else if (entityStorage.containsKey(ind)) {
                    Pokemon pokemon = entityStorage.get(ind);
                    if (SafeTrade.EVENT_BUS.post(new TransactionEvent.Pokemon.Remove.Pre(this, pokemon))) {
                        return false;
                    }
                    entityStorage.remove(ind);
                    SafeTrade.EVENT_BUS.post(new TransactionEvent.Pokemon.Remove.Success(this, pokemon));
                    return true;
                }
                break;
            }
        }

        return false;
    }

    /**
     * Swaps the positions of two {@link Pokemon} in the pokemon inventory.
     *
     * <p>One of the indexes can be empty and it will still work.</p>
     *
     * <p>The indexes must not be greater than 35.</p>
     *
     * @param index1 The first index
     * @param index2 The second index
     */
    public void swapPokemon(int index1, int index2) {
        if (index1 >= 36 || index2 >= 36) {
            return;
        }
        Iterator<Inventory> iter = pokemonStorage.slots().iterator();
        ItemStack item1 = null;
        ItemStack item2 = null;
        Pokemon pokemon1 = null;
        Pokemon pokemon2 = null;

        // Polls (gets and removes) the items from the relevant slots.
        while (iter.hasNext()) {
            Inventory slot = iter.next();
            int ind = slot.getProperty(SlotIndex.class, "slotindex").get().getValue();
            if (ind == index1) {
                if (slot.peek().isPresent()) {
                    item1 = slot.poll().get();
                    pokemon1 = entityStorage.get(ind);
                }
            }
            else if (ind == index2) {
                if (slot.peek().isPresent()) {
                    item2 = slot.poll().get();
                    pokemon2 = entityStorage.get(ind);
                }
            }
            // Exit condition to prevent unnecessary iterations
            else if (ind > index1 && ind > index2) {
                break;
            }
        }

        iter = pokemonStorage.slots().iterator();

        // Sets the previously polled items in the opposite slot they were in
        while (iter.hasNext()) {
            Inventory slot = iter.next();
            int ind = slot.getProperty(SlotIndex.class, "slotindex").get().getValue();
            if (ind == index1) {
                if (item2 != null) {
                    slot.set(item2);
                    entityStorage.put(ind, pokemon2);
                }
            }
            else if (ind == index2) {
                if (item1 != null) {
                    slot.set(item1);
                    entityStorage.put(ind, pokemon1);
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
     * Gets all the {@link Pokemon} in the vault.
     *
     * @return A list of pokemon
     */
    public List<Pokemon> getAllPokemon() {
        return Lists.newArrayList(entityStorage.values());
    }

    /**
     * Clears the Items, Pokemon, and Money from the vault.
     */
    public void clear() {
        itemStorage.clear();
        pokemonStorage.clear();
        entityStorage.clear();
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
                optAcc.get().resetBalances(Cause.of(EventContext.empty(), this));
                return optAcc.get();
            }
        }
        Sponge.getScheduler().createTaskBuilder().execute(() -> {
            side.parentTrade.sendChannelMessage(Text.of(TextColors.RED, "Account creation failed."));
            side.parentTrade.sendChannelMessage(Text.of(TextColors.RED, "Force ending trade to prevent further errors."));
            side.parentTrade.sendChannelMessage(Text.of(TextColors.RED, "If this keeps happening, please report it to an administrator."));
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

    private void formatPokemonInventory() {
        //36 -> 53
        pokemonStorage.slots().forEach(slot -> {
            int ind = slot.getProperty(SlotIndex.class, "slotindex").get().getValue();
            if (ind == 45) {
                slot.set(ItemUtils.Other.getBackButton());
            }
            else if (ind == 49) {
                slot.set(ItemUtils.Pokemon.getPC());
            }
            else if ((ind >= 36 && ind <= 53)) {
                slot.set(ItemUtils.Other.getFiller(DyeColors.CYAN));
            }
        });
    }

    private void handleItemStorageClick(ClickInventoryEvent event) {
        event.getCause().first(Player.class).ifPresent(player -> {
            // Prevents players from clicking if they have a cooldown
            if (InventoryHelper.hasCooldown(player.getUniqueId())) {
                return;
            }
            InventoryHelper.addCooldown(player.getUniqueId());

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
                            if (SafeTrade.EVENT_BUS.post(new TransactionEvent.Item.Remove.Pre(side.vault, transaction.getOriginal().createStack()))) {
                                event.setCancelled(true);
                            }
                            else {
                                Sponge.getScheduler().createTaskBuilder().execute((() -> SafeTrade.EVENT_BUS.post(new TransactionEvent.Item.Remove.Success(this, item)))).delayTicks(1).submit(SafeTrade.getPlugin());
                            }
                        }
                        // Else, the player is attempting to add an item to the vault's item storage
                        else {
                            if (SafeTrade.EVENT_BUS.post(new TransactionEvent.Item.Add.Pre(side.vault, transaction.getOriginal().createStack()))) {
                                event.setCancelled(true);
                            }
                            else {
                                Sponge.getScheduler().createTaskBuilder().execute((() -> SafeTrade.EVENT_BUS.post(new TransactionEvent.Item.Add.Success(this, item)))).delayTicks(1).submit(SafeTrade.getPlugin());
                            }
                        }
                    }
                });
            });
        });
    }

    private void handlePokemonStorageClick(ClickInventoryEvent event) {
        event.setCancelled(true);

        event.getCause().first(Player.class).ifPresent(player -> {
            // Prevents players from clicking if they have a cooldown
            if (InventoryHelper.hasCooldown(player.getUniqueId())) {
                return;
            }
            InventoryHelper.addCooldown(player.getUniqueId());

            event.getTransactions().forEach(transaction -> {
                transaction.getSlot().getProperty(SlotIndex.class, "slotindex").ifPresent(slot -> {
                    ItemStack item = transaction.getOriginal().createStack();
                    int index = transaction.getSlot().getProperty(SlotIndex.class, "slotindex").get().getValue();

                    if (item.equalTo(ItemUtils.Other.getBackButton())) {
                        Sponge.getScheduler().createTaskBuilder().execute(() ->
                                side.changeInventoryForViewer(player, side.parentTrade.getState() == TradeState.WAITING_FOR_CONFIRMATION ? InventoryType.OVERVIEW : InventoryType.MAIN)
                        ).delayTicks(1).submit(SafeTrade.getPlugin());
                    }

                    // If player is not the in the side of this vault, or the if the vault is locked, or they didn't left/right click, nothing will happen.
                    // Checking for player is redundant when going through the main trade inventory, but I'll keep it here in-case a player somehow opens another side's pc inventory
                    else if ((player.getUniqueId().equals(side.sideOwnerUUID) && !side.vault.isLocked() && (event instanceof ClickInventoryEvent.Primary || event instanceof ClickInventoryEvent.Secondary))
                            && side.parentTrade.getState() == TradeState.TRADING) {

                        if (item.equalTo(ItemUtils.Pokemon.getPC())) {
                            Sponge.getScheduler().createTaskBuilder().execute(() -> side.changeInventory(InventoryType.PC)).delayTicks(1).submit(SafeTrade.getPlugin());
                        }
                        else if (entityStorage.containsKey(index)) {
                            if (event instanceof ClickInventoryEvent.Secondary) {
                                Pokemon pokemon = entityStorage.get(index);
                                // Need scheduler to wait for the click to cancel, otherwise the slot won't recognise there's an itemstack
                                Sponge.getScheduler().createTaskBuilder().execute(() -> {
                                    if (removePokemon(index)) {
                                        if (Pixelmon.storageManager.getParty(player.getUniqueId()).add(pokemon)) {
                                            SafeTrade.sendMessageToPlayer(player, PrefixType.SAFETRADE, Text.of(TextColors.GREEN, pokemon.getDisplayName() + " has been added to your party/pc"));
                                        }
                                        // uh oh, something went wrong!
                                        else {
                                            Tracker.getOrCreateStorage(player).addPokemon(pokemon);
                                            SafeTrade.sendMessageToPlayer(player, PrefixType.SAFETRADE, Text.of(TextColors.RED, "Uh oh, something went wrong! Your Pokemon was put in to your SafeTrade storage for safe-keeping. " +
                                                    "Reconnect to receive everything in your storage."));
                                        }
                                    }
                                }).delayTicks(1).submit(SafeTrade.getPlugin());
                            }
                        }
                    }
                });
            });
        });
    }
}