package io.github.cjcool06.safetrade.utils;

import io.github.cjcool06.safetrade.data.SafeTradeData;
import io.github.cjcool06.safetrade.obj.Trade;
import org.spongepowered.api.entity.living.player.User;

public class LogManager {
    public static void logTrade(Trade trade) {

    }

    public static void removeLog(User user, int index) {

    }

    public static void getLogs(User user) {
        SafeTradeData data;
        if (user.isOnline()) {
            data = user.getPlayer().get().getOrCreate(SafeTradeData.class).get();
        }
        else {
            data = user.getOrCreate(SafeTradeData.class).get();
        }
    }
}
