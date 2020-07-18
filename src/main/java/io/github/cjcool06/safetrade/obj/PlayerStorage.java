package io.github.cjcool06.safetrade.obj;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.github.cjcool06.safetrade.SafeTrade;
import io.github.cjcool06.safetrade.api.enums.CommandType;
import io.github.cjcool06.safetrade.managers.DataManager;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryTransformations;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.service.user.UserStorageService;

import java.io.IOException;
import java.util.*;

// TODO: Make PlayerStorage GUI that admins can edit

/**
 * A PlayerStorage represents a storage for a {@link User} that holds {@link ItemStackSnapshot}s and {@link CommandWrapper}s.
 *
 * <p>PlayerStorage data persists across restarts.</p>
 */
public class PlayerStorage {
    public final UUID playerUUID;

    private final List<ItemStackSnapshot> items = new ArrayList<>();
    private final List<CommandWrapper> commands = new ArrayList<>();

    private boolean needsSaving = false;

    public PlayerStorage(User user) {
        this.playerUUID = user.getUniqueId();
    }

    private PlayerStorage(UUID uuid, List<ItemStackSnapshot> items, List<CommandWrapper> commands) {
        this.playerUUID = uuid;
        this.items.addAll(items);
        this.commands.addAll(commands);
    }

    /**
     * Attempts to get the {@link Player} of this storage.
     *
     * @return An {@link Optional}
     */
    public Optional<Player> getPlayer() {
        return Sponge.getServer().getPlayer(playerUUID);
    }

    /**
     * Attempts to get the {@link User} of this storage.
     *
     * @return An {@link Optional}
     */
    public Optional<User> getUser() {
        return Sponge.getServiceManager().provide(UserStorageService.class).get().get(playerUUID);
    }

    /**
     * Whether the storage needs to be saved
     *
     * @return True if needs to be saved
     */
    public boolean needsSaving() {
        return needsSaving;
    }

    /**
     * Gets all {@link ItemStackSnapshot}s in this storage.
     *
     * @return An unmodifiable list of items
     */
    public List<ItemStackSnapshot> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Adds an {@link ItemStackSnapshot} to this storage.
     *
     * @param snapshot The item
     * @return True if successfully added
     */
    public boolean addItem(ItemStackSnapshot snapshot) {
        needsSaving = true;
        return items.add(snapshot);
    }

    /**
     * Removes an {@link ItemStackSnapshot} from this storage.
     *
     * @param snapshot The item
     * @return True if successfully removed
     */
    public boolean removeItem(ItemStackSnapshot snapshot) {
        needsSaving = true;
        return items.remove(snapshot);
    }

    /**
     * Removes an {@link ItemStack} from this storage.
     *
     * @param itemStack The item
     * @return True if successfully removed
     */
    public boolean removeItem(ItemStack itemStack) {
        Iterator<ItemStackSnapshot> iter = items.iterator();
        while (iter.hasNext()) {
            ItemStack item = iter.next().createStack();
            if (item.equalTo(itemStack)) {
                iter.remove();
                needsSaving = true;
                return true;
            }
        }
        return false;
    }

    /**
     * Clears all items from this storage.
     */
    public void clearItems() {
        needsSaving = true;
        items.clear();
    }

    /**
     * Give the items in this storage to the storage owner and remove them.
     *
     * <p>If the player's {@link Inventory} becomes full it will halt and return.</p>
     *
     * @return The {@link ItemStackSnapshot}s that were successfully given
     */
    public List<ItemStackSnapshot> giveItems() {
        List<ItemStackSnapshot> successes = new ArrayList<>();
        Iterator<ItemStackSnapshot> iter = items.iterator();

        if (!getPlayer().isPresent()) {
            return successes;
        }

        while (iter.hasNext()) {
            ItemStackSnapshot snapshot = iter.next();
            Player player = getPlayer().get();
            PlayerInventory inv = (PlayerInventory)player.getInventory();
            Inventory prioritisedInv = inv.getMain().transform(InventoryTransformations.PLAYER_MAIN_HOTBAR_FIRST);

            if (prioritisedInv.offer(snapshot.createStack()).getType() == InventoryTransactionResult.Type.SUCCESS) {
                successes.add(snapshot);
                iter.remove();
                needsSaving = true;
            }
            else {
                // There will most likely be no space for any of the items left, so no point continuing the iteration.
                break;
            }
        }

        return successes;
    }

    /**
     * Gets all {@link CommandWrapper}s in this storage.
     *
     * @return An unmodifiable list of command wrappers
     */
    public List<CommandWrapper> getCommands() {
        return Collections.unmodifiableList(commands);
    }

    /**
     * Adds a {@link CommandWrapper} to this storage.
     *
     * @param commandWrapper The command wrapper
     * @return True if successfully added
     */
    public boolean addCommand(CommandWrapper commandWrapper) {
        needsSaving = true;
        return commands.add(commandWrapper);
    }

    /**
     * Removes a {@link CommandWrapper} from this storage.
     *
     * @param commandWrapper The command wrapper
     * @return True if successfully removed
     */
    public boolean removeCommand(CommandWrapper commandWrapper) {
        needsSaving = true;
        return commands.remove(commandWrapper);
    }

    /**
     * Clears all commands from this storage.
     */
    public void clearCommands() {
        needsSaving = true;
        commands.clear();
    }

    /**
     * Gets whether the storage is empty.
     *
     * @return True if empty
     */
    public boolean isEmpty() {
        return items.isEmpty() && commands.isEmpty();
    }

    /**
     * Executes all of the {@link CommandWrapper}s.
     *
     * <p>If the wrapper requires an online player and one isn't present, it will skip over it.</p>
     *
     * @return The commands executed
     */
    public List<CommandWrapper> executeCommands() {
        List<CommandWrapper> commandsExecuted = new ArrayList<>();
        Iterator<CommandWrapper> iter = commands.iterator();

        while (iter.hasNext()) {
            CommandWrapper wrapper = iter.next();
            if (wrapper.commandType == CommandType.CONSOLE) {
                wrapper.consoleExecute();
                iter.remove();
                needsSaving = true;
                commandsExecuted.add(wrapper);
            }
            else if (wrapper.commandType == CommandType.SUDO && getPlayer().isPresent()) {
                Player player = getPlayer().get();
                wrapper.sudoExecute(player);
                iter.remove();
                needsSaving = true;
                commandsExecuted.add(wrapper);
            }
        }

        return commandsExecuted;
    }

    /**
     * Save the storage to file.
     */
    public void save() {
        DataManager.savePlayerStorage(this);
        needsSaving = false;
    }

    /**
     * Serialise the storage in to a container ({@link JsonObject}) to be saved to file.
     *
     * @param jsonObject The container
     */
    public void toContainer(JsonObject jsonObject) {
        JsonArray itemsArr = new JsonArray();
        JsonArray pokemonsArr = new JsonArray();
        JsonArray commandsArr = new JsonArray();

        for (CommandWrapper wrapper : commands) {
            JsonObject cmdObj = new JsonObject();
            wrapper.toContainer(cmdObj);
            commandsArr.add(cmdObj);
        }

        for (ItemStackSnapshot snapshot : items) {
            try {
                itemsArr.add(DataFormats.JSON.write(snapshot.toContainer()));
            } catch (IOException ioe) {
                SafeTrade.getLogger().warn("Unable to serialise item. Details:  Type=" + snapshot.getType().getName() + "  Quantity=", snapshot.getQuantity());
            }
        }

        jsonObject.add("PlayerUUID", new JsonPrimitive(playerUUID.toString()));
        jsonObject.add("Commands", commandsArr);
        jsonObject.add("Pokemon", pokemonsArr);
        jsonObject.add("Items", itemsArr);
    }

    /**
     * De-serialise the storage from a container ({@link JsonObject}).
     *
     * @param jsonObject The container
     * @return The de-serialised storage
     */
    public static PlayerStorage fromContainer(JsonObject jsonObject) {
        try {
            UUID playerUUID = UUID.fromString(jsonObject.get("PlayerUUID").getAsString());
            List<ItemStackSnapshot> items = new ArrayList<>();
            List<CommandWrapper> commands = new ArrayList<>();

            for (JsonElement element : jsonObject.get("Commands").getAsJsonArray()) {
                CommandWrapper wrapper = CommandWrapper.fromContainer(element.getAsJsonObject());
                if (wrapper != null) {
                    commands.add(wrapper);
                }
            }

            for (JsonElement element : jsonObject.get("Items").getAsJsonArray()) {
                items.add(Sponge.getDataManager().deserialize(ItemStackSnapshot.class, DataFormats.JSON.read(element.getAsString())).get());
            }

            return new PlayerStorage(playerUUID, items, commands);
        } catch (Exception e) {
            SafeTrade.getLogger().warn("There was a problem deserialising a PlayerStorage from a container.");
            e.printStackTrace();
            return null;
        }
    }
}
