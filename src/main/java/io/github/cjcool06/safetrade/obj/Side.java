package io.github.cjcool06.safetrade.obj;

import com.pixelmonmod.pixelmon.Pixelmon;
import io.github.cjcool06.safetrade.SafeTrade;
import io.github.cjcool06.safetrade.api.enums.InventoryType;
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

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public boolean isReady() {
        return ready;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public boolean isPaused() {
        return paused;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public Optional<Player> getPlayer() {
        return Sponge.getServer().getPlayer(sideOwnerUUID);
    }

    public Optional<User> getUser() {
        return Sponge.getServiceManager().provide(UserStorageService.class).get().get(sideOwnerUUID);
    }

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
                    player.openInventory(parentTrade.getOverviewInventory());
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
                player.openInventory(parentTrade.getOverviewInventory());
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

    public void sendMessage(Text text) {
        if (getPlayer().isPresent()) {
            getPlayer().get().sendMessage(text);
        }
    }

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
