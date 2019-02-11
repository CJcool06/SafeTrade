package io.github.cjcool06.safetrade.trackers;

import io.github.cjcool06.safetrade.managers.DataManager;
import io.github.cjcool06.safetrade.obj.PlayerStorage;
import io.github.cjcool06.safetrade.obj.Trade;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Tracker {
    private static final List<Trade> activeCache = new ArrayList<>();
    private static final List<PlayerStorage> storageCache = new ArrayList<>();
    //private static final List<Trade> endedCache = new ArrayList<>();

    public static void addActiveTrade(Trade trade) {
        activeCache.add(trade);
    }

    public static void removeActiveTrade(Trade trade) {
        activeCache.remove(trade);
    }

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

    public static List<PlayerStorage> getStorages() {
        return Collections.unmodifiableList(storageCache);
    }

    public static void addStorage(PlayerStorage storage) {
        storageCache.add(storage);
    }

    public static void removeStorage(PlayerStorage storage) {
        DataManager.deletePlayerStorageFile(storage);
        storageCache.remove(storage);
    }

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

    public static boolean hasStorage(User user) {
        for (PlayerStorage storage : storageCache) {
            if (storage.playerUUID.equals(user.getUniqueId())) {
                return true;
            }
        }
        return false;
    }

    public static Trade getActiveTrade(User participant) {
        for (Trade trade : activeCache) {
            if (trade.getSides()[0].sideOwnerUUID.equals(participant.getUniqueId()) || trade.getSides()[1].sideOwnerUUID.equals(participant.getUniqueId())) {
                return trade;
            }
        }
        return null;
    }

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
