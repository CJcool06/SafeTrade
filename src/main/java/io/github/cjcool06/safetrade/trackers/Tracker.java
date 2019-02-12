package io.github.cjcool06.safetrade.trackers;

import io.github.cjcool06.safetrade.managers.DataManager;
import io.github.cjcool06.safetrade.obj.PlayerStorage;
import io.github.cjcool06.safetrade.obj.Trade;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The Tracker holds and manages {@link Trade} and {@link PlayerStorage} caches.
 */
public final class Tracker {
    private static final List<Trade> activeCache = new ArrayList<>();
    private static final List<PlayerStorage> storageCache = new ArrayList<>();
    //private static final List<Trade> endedCache = new ArrayList<>();

    /**
     * Adds a {@link Trade} to the cache of active trades.
     *
     * @param trade The trade
     */
    public static void addActiveTrade(Trade trade) {
        activeCache.add(trade);
    }

    /**
     * Removes a {@link Trade} from the cache of active trades.
     *
     * @param trade The trade
     */
    public static void removeActiveTrade(Trade trade) {
        activeCache.remove(trade);
    }

    /**
     * Gets active {@link Trade}s cache.
     *
     * @return An unmodifiable list
     */
    public static List<Trade> getAllActiveTrades() {
        return Collections.unmodifiableList(activeCache);
    }

    /* todo: Future support for trade persistence
    public static void addEndedTrade(Trade trade, boolean saveFile) {
        if (saveFile) {
            DataManager.saveTrade(trade, DataManager.endedDir);
        }
        endedCache.add(trade);
    }

    public static void removeEndedTrade(Trade trade) {
        DataManager.deleteTradeFile(trade, DataManager.endedDir);
        endedCache.remove(trade);
    }

    public static List<Trade> getAllEndedTrades() {
        return Collections.unmodifiableList(endedCache);
    }*/

    /**
     * Gets the {@link PlayerStorage} cache.
     *
     * @return An unmodifiable list
     */
    public static List<PlayerStorage> getStorages() {
        return Collections.unmodifiableList(storageCache);
    }

    /**
     * Adds a {@link PlayerStorage} to the storages cache.
     *
     * @param storage The storage
     */
    public static void addStorage(PlayerStorage storage) {
        storageCache.add(storage);
    }

    /**
     * Removes a {@link PlayerStorage} from the storages cache.
     *
     * @param storage The storage
     */
    public static void removeStorage(PlayerStorage storage) {
        DataManager.deletePlayerStorageFile(storage);
        storageCache.remove(storage);
    }

    /**
     * Gets a {@link User}'s {@link PlayerStorage} if one is cached, otherwise creates a new one and caches it.
     *
     * @param user The user
     * @return A storage
     */
    public static PlayerStorage getOrCreateStorage(User user) {
        for (PlayerStorage storage : storageCache) {
            if (storage.playerUUID.equals(user.getUniqueId())) {
                return storage;
            }
        }
        PlayerStorage storage = new PlayerStorage(user);
        storageCache.add(storage);

        return storage;
    }

    /**
     * Gets whether the {@link User} has a {@link PlayerStorage} cached.
     *
     * @param user The user
     * @return True if storage is present
     */
    public static boolean hasStorage(User user) {
        for (PlayerStorage storage : storageCache) {
            if (storage.playerUUID.equals(user.getUniqueId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the trade of a {@link User} that is participating in a {@link Trade}, if present.
     *
     * @param participant The participant
     * @return The trade, if present.
     */
    public static Trade getActiveTrade(User participant) {
        for (Trade trade : activeCache) {
            if (trade.getSides()[0].sideOwnerUUID.equals(participant.getUniqueId()) || trade.getSides()[1].sideOwnerUUID.equals(participant.getUniqueId())) {
                return trade;
            }
        }
        return null;
    }

    /**
     * Gets the trade of a {@link Player} that is viewing a {@link Trade}, if present.
     *
     * @param viewer The viewer
     * @return The trade, if present.
     */
    public static Trade getActiveTradeOfViewer(Player viewer) {
        for (Trade trade : activeCache) {
            for (Player player : trade.getViewers()) {
                if (player.getUniqueId().equals(viewer.getUniqueId())) {
                    return trade;
                }
            }
        }
        return null;
    }
}
