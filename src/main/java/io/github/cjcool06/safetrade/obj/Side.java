package io.github.cjcool06.safetrade.obj;

import com.pixelmonmod.pixelmon.Pixelmon;
import io.github.cjcool06.safetrade.SafeTrade;
import io.github.cjcool06.safetrade.api.enums.InventoryType;
import io.github.cjcool06.safetrade.api.enums.PrefixType;
import io.github.cjcool06.safetrade.api.events.trade.InventoryChangeEvent;
import io.github.cjcool06.safetrade.helpers.InventoryHelper;
import io.github.cjcool06.safetrade.utils.Utils;
import net.minecraft.entity.player.EntityPlayerMP;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.title.Title;

import java.util.Optional;
import java.util.UUID;

/**
 * A Side represents one of the two sides of a trade.
 */
public class Side {
    public final UUID sideOwnerUUID;
    public final Trade parentTrade;
    public final Vault vault;

    private boolean ready = false;
    private boolean paused = true;
    private boolean confirmed = false;

    public InventoryType currentInventory = InventoryType.NONE;

    public Side(Trade parentTrade, User sideOwner) {
        this.sideOwnerUUID = sideOwner.getUniqueId();
        this.parentTrade = parentTrade;
        this.vault = new Vault(this);
    }

    /**
     * Sets whether the side is considered ready to trade.
     *
     * @param ready True if ready
     */
    public void setReady(boolean ready) {
        this.ready = ready;
    }

    /**
     * Gets whether the side is considered ready to trade.
     *
     * @return True if ready
     */
    public boolean isReady() {
        return ready;
    }

    /**
     * Sets whether the side is considered paused.
     *
     * @param paused True if paused
     */
    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    /**
     * Gets whether the side is considered paused.
     *
     * @return True if paused
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     * Gets whether the side has confirmed the trade.
     *
     * @return True if confirmed
     */
    public boolean isConfirmed() {
        return confirmed;
    }

    /**
     * Sets whether the side has confirmed the trade.
     *
     * @param confirmed True if confirmed
     */
    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    /**
     * Attempts to get the {@link Player} of this side.
     *
     * @return An {@link Optional}
     */
    public Optional<Player> getPlayer() {
        return Sponge.getServer().getPlayer(sideOwnerUUID);
    }

    /**
     * Attempts to get the {@link User} of this side.
     *
     * @return An {@link Optional}
     */
    public Optional<User> getUser() {
        return Sponge.getServiceManager().provide(UserStorageService.class).get().get(sideOwnerUUID);
    }

    /**
     * Gets the other side of the {@link Trade}.
     *
     * @return The other side
     */
    public Side getOtherSide() {
        return parentTrade.getSides()[0].equals(this) ? parentTrade.getSides()[1] : parentTrade.getSides()[0];
    }

    /**
     * Opens and inventory from this side for this side.
     *
     * <p>This should be used at all times, as opposed to Player#openInventory, as the currentInventory needs to stay up-to-date.</p>
     *
     * @param inventoryType The new inventory
     */
    public void changeInventory(InventoryType inventoryType) {
        if (SafeTrade.EVENT_BUS.post(new InventoryChangeEvent.Pre(this, inventoryType))) {
            return;
        }
        if (getPlayer().isPresent()) {
            Player player = getPlayer().get();
            Utils.recallAllPokemon(Pixelmon.storageManager.getParty((EntityPlayerMP)player));
            switch (inventoryType) {
                case MAIN:
                    currentInventory = InventoryType.MAIN;
                    player.openInventory(parentTrade.getTradeInventory());
                    break;
                case OVERVIEW:
                    currentInventory = InventoryType.OVERVIEW;
                    player.openInventory(InventoryHelper.buildAndGetOverviewInventory(parentTrade));
                    break;
                case POKEMON:
                    currentInventory = InventoryType.POKEMON;
                    player.openInventory(vault.pokemonStorage);
                    break;
                case ITEM:
                    currentInventory = InventoryType.ITEM;
                    player.openInventory(vault.itemStorage);
                    break;
                case MONEY:
                    currentInventory = InventoryType.MONEY;
                    player.openInventory(InventoryHelper.buildAndGetMoneyInventory(this));
                    break;
                case PC:
                    currentInventory = InventoryType.PC;
                    player.openInventory(InventoryHelper.buildAndGetPCInventory(this));
                    break;
                case NONE:
                    ready = false;
                    paused = true;
                    currentInventory = InventoryType.NONE;
                    player.closeInventory();
                    break;
            }
        }
    }

    /**
     * Opens an inventory from this side for a {@link Player}.
     *
     * @param inventoryType The new inventory
     */
    public void changeInventoryForViewer(Player player, InventoryType inventoryType) {
        Utils.recallAllPokemon(Pixelmon.storageManager.getParty((EntityPlayerMP)player));
        switch (inventoryType) {
            case MAIN:
                player.openInventory(parentTrade.getTradeInventory());
                break;
            case OVERVIEW:
                player.openInventory(InventoryHelper.buildAndGetOverviewInventory(parentTrade));
                break;
            case POKEMON:
                player.openInventory(vault.pokemonStorage);
                break;
            case ITEM:
                player.openInventory(vault.itemStorage);
                break;
            case MONEY:
                player.openInventory(InventoryHelper.buildAndGetMoneyInventory(this));
                break;
            case PC:
                player.openInventory(InventoryHelper.buildAndGetPCInventory(this));
                break;
        }
    }

    /**
     * Sends a {@link Text} to the player occupying this side.
     *
     * @param text The message
     */
    public void sendMessage(Text text) {
        if (getPlayer().isPresent()) {
            SafeTrade.sendMessageToPlayer(getPlayer().get(), PrefixType.SAFETRADE, text);
        }
    }

    /**
     * Sends a {@link Title} to the player occupying this side.
     *
     * @param title The title
     * @param subtitle The subtitle
     */
    public void sendTitle(Text title, Text subtitle) {
        if (getPlayer().isPresent()) {
            Title t = Title.builder()
                    .title(title)
                    .subtitle(subtitle)
                    .fadeIn(10)
                    .stay(70)
                    .fadeOut(10)
                    .build();

            getPlayer().get().sendTitle(t);
        }
    }
}
