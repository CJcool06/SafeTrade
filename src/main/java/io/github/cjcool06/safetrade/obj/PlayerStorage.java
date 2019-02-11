package io.github.cjcool06.safetrade.obj;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import io.github.cjcool06.safetrade.SafeTrade;
import io.github.cjcool06.safetrade.api.enums.CommandType;
import io.github.cjcool06.safetrade.managers.DataManager;
import io.github.cjcool06.safetrade.utils.GsonUtils;
import net.minecraft.nbt.NBTTagCompound;
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

public class PlayerStorage {
    public final UUID playerUUID;

    private final List<ItemStackSnapshot> items = new ArrayList<>();
    private final List<Pokemon> pokemons = new ArrayList<>();
    private final List<CommandWrapper> commands = new ArrayList<>();

    private boolean needsSaving = false;

    public PlayerStorage(User user) {
        this.playerUUID = user.getUniqueId();
    }

    private PlayerStorage(UUID uuid, List<ItemStackSnapshot> items, List<Pokemon> pokemons, List<CommandWrapper> commands) {
        this.playerUUID = uuid;
        this.items.addAll(items);
        this.pokemons.addAll(pokemons);
        this.commands.addAll(commands);
    }

    public Optional<Player> getPlayer() {
        return Sponge.getServer().getPlayer(playerUUID);
    }

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

    // Items
    public List<ItemStackSnapshot> getItems() {
        return Collections.unmodifiableList(items);
    }

    public boolean addItem(ItemStackSnapshot snapshot) {
        needsSaving = true;
        return items.add(snapshot);
    }

    public boolean removeItem(ItemStackSnapshot snapshot) {
        needsSaving = true;
        return items.remove(snapshot);
    }

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

    public void clearItems() {
        needsSaving = true;
        items.clear();
    }

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

    // Pokemon
    public List<Pokemon> getPokemons() {
        return Collections.unmodifiableList(pokemons);
    }

    public boolean addPokemon(Pokemon pokemon) {
        needsSaving = true;
        return pokemons.add(pokemon);
    }

    public boolean removePokemon(Pokemon pokemon) {
        needsSaving = true;
        return pokemons.remove(pokemon);
    }

    public void clearPokemon() {
        needsSaving = true;
        pokemons.clear();
    }

    /**
     * Puts as many of the {@link Pokemon} in this storage in to the player's {@link com.pixelmonmod.pixelmon.api.storage.PokemonStorage}
     * and removes those {@link Pokemon} from this storage.
     *
     * <p>If the player's {@link com.pixelmonmod.pixelmon.api.storage.PokemonStorage} becomes full it will halt and return.</p>
     *
     * @return A list of the {@link Pokemon} that were successfully added to the player's {@link com.pixelmonmod.pixelmon.api.storage.PokemonStorage}
     */
    public List<Pokemon> givePokemon() {
        List<Pokemon> successes = new ArrayList<>();
        PlayerPartyStorage partyStorage = Pixelmon.storageManager.getParty(playerUUID);
        Iterator<Pokemon> iter = pokemons.iterator();

        while (iter.hasNext()) {
            Pokemon pokemon = iter.next();
            if (partyStorage.add(pokemon)) {
                successes.add(pokemon);
                iter.remove();
                needsSaving = true;
            }
            else {
                // There are no more spaces in the player's pokemon storage, so no point continuing the iteration.
                break;
            }
        }

        return successes;
    }

    // Commands
    public List<CommandWrapper> getCommands() {
        return Collections.unmodifiableList(commands);
    }

    public boolean addCommand(CommandWrapper commandWrapper) {
        needsSaving = true;
        return commands.add(commandWrapper);
    }

    public boolean removeCommand(CommandWrapper commandWrapper) {
        needsSaving = true;
        return commands.remove(commandWrapper);
    }

    public void clearCommands() {
        needsSaving = true;
        commands.clear();
    }

    public boolean isEmpty() {
        return items.isEmpty() && pokemons.isEmpty() && commands.isEmpty();
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

    public void save() {
        DataManager.savePlayerStorage(this);
        needsSaving = false;
    }

    public void toContainer(JsonObject jsonObject) {
        JsonArray itemsArr = new JsonArray();
        JsonArray pokemonsArr = new JsonArray();
        JsonArray commandsArr = new JsonArray();

        for (CommandWrapper wrapper : commands) {
            JsonObject cmdObj = new JsonObject();
            wrapper.toContainer(cmdObj);
            commandsArr.add(cmdObj);
        }

        for (Pokemon pokemon : pokemons) {
            pokemonsArr.add(GsonUtils.serialize(pokemon.writeToNBT(new NBTTagCompound())));
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

    public static PlayerStorage fromContainer(JsonObject jsonObject) {
        try {
            UUID playerUUID = UUID.fromString(jsonObject.get("PlayerUUID").getAsString());
            List<ItemStackSnapshot> items = new ArrayList<>();
            List<Pokemon> pokemons = new ArrayList<>();
            List<CommandWrapper> commands = new ArrayList<>();

            for (JsonElement element : jsonObject.get("Commands").getAsJsonArray()) {
                CommandWrapper wrapper = CommandWrapper.fromContainer(element.getAsJsonObject());
                if (wrapper != null) {
                    commands.add(CommandWrapper.fromContainer(element.getAsJsonObject()));
                }
            }

            for (JsonElement element : jsonObject.get("Pokemon").getAsJsonArray()) {
                pokemons.add(Pixelmon.pokemonFactory.create(GsonUtils.deserialize(element.getAsString())));
            }

            for (JsonElement element : jsonObject.get("Items").getAsJsonArray()) {
                items.add(Sponge.getDataManager().deserialize(ItemStackSnapshot.class, DataFormats.JSON.read(element.getAsString())).get());
            }

            return new PlayerStorage(playerUUID, items, pokemons, commands);
        } catch (Exception e) {
            SafeTrade.getLogger().warn("There was a problem deserialising a PlayerStorage from a container.");
            e.printStackTrace();
            return null;
        }
    }
}
