package io.github.cjcool06.safetrade.managers;

import com.google.common.reflect.TypeToken;
import io.github.cjcool06.safetrade.SafeTrade;
import io.github.cjcool06.safetrade.config.Config;
import io.github.cjcool06.safetrade.obj.Log;
import io.github.cjcool06.safetrade.obj.Trade;
import io.github.cjcool06.safetrade.utils.Utils;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

import java.io.File;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Any method related to reading/writing to files should be called asynchronously
 */
public class DataManager {
    private static final File dataDir = new File("config/safetrade/data");
    private static final ArrayList<Trade> activeTrades = new ArrayList<>();

    public static void load() {
        dataDir.mkdirs();
        for (File file : dataDir.listFiles()) {
            if (isFileEmpty(file)) {
                file.delete();
                continue;
            }
            try {
                // TODO
            } catch (Exception e) {
                SafeTrade.getLogger().error("Error reading file " + file.getName());
                SafeTrade.getLogger().error(e.getMessage());
            }
        }
    }

    public static void trimFiles() {
        for (File file : dataDir.listFiles()) {
            if (isFileEmpty(file)) {
                file.delete();
            }
        }
    }

    public static void addLog(User user, Log log) {
        File file = getFile(user);
        try {
            ConfigurationLoader<CommentedConfigurationNode> loader = getLoader(file);
            CommentedConfigurationNode node = loader.load(ConfigurationOptions.defaults().setObjectMapperFactory(SafeTrade.getFactory()));

            ArrayList<Log> logs = getLogs(user);
            logs.add(log);
            node.getNode("logs").setValue(new TypeToken<List<Log>>(){}, logs);
            loader.save(node);
        } catch (Exception e) {
            SafeTrade.getLogger().error("Error adding log. Offending file: " + file.getName());
            e.printStackTrace();
        }
    }

    public static void removeLog(User user, Log log) {
        File file = getFile(user);
        if (file.exists()) {
            try {
                ConfigurationLoader<CommentedConfigurationNode> loader = getLoader(file);
                CommentedConfigurationNode node = loader.load(ConfigurationOptions.defaults().setObjectMapperFactory(SafeTrade.getFactory()));
                ArrayList<Log> logs = getLogs(user);
                logs.removeIf(log1 -> log1.getUniqueID().equals(log.getUniqueID()));
                node.getNode("logs").setValue(new TypeToken<List<Log>>(){}, logs);
                loader.save(node);
            } catch (Exception e) {
                SafeTrade.getLogger().error("Error removing log. Offending file: " + file.getName());
                e.printStackTrace();
            }
        }
    }

    public static void clearLogs(User user) {
        File file = getFile(user);
        if (file.exists()) {
            try {
                ConfigurationLoader<CommentedConfigurationNode> loader = getLoader(file);
                CommentedConfigurationNode node = loader.load(ConfigurationOptions.defaults().setObjectMapperFactory(SafeTrade.getFactory()));
                node.getNode("logs").setValue(new TypeToken<List<Log>>(){}, new ArrayList<>());
            } catch (Exception e) {
                SafeTrade.getLogger().error("Error removing log. Offending file: " + file.getName());
                e.printStackTrace();
            }
        }
    }

    public static ArrayList<Log> getLogs(User user) {
        ArrayList<Log> logs = new ArrayList<>();
        File file = getFile(user);
        if (file.exists()) {
            try {
                ConfigurationLoader<CommentedConfigurationNode> loader = getLoader(file);
                CommentedConfigurationNode node = loader.load(ConfigurationOptions.defaults().setObjectMapperFactory(SafeTrade.getFactory()));
                if (node.getNode("logs").isVirtual()) {
                    return logs;
                }
                logs.addAll(node.getNode("logs").getList(new TypeToken<Log>(){}));
            } catch (Exception e) {
                SafeTrade.getLogger().error("Error deserializing log. Offending file: " + file.getName());
                e.printStackTrace();
            }
        }

        return logs;
    }

    public static void storeItem(User user, ItemStackSnapshot item) {
        File file = getFile(user);
        try {
            ConfigurationLoader<CommentedConfigurationNode> loader = getLoader(file);
            CommentedConfigurationNode node = loader.load();
            ArrayList<ItemStackSnapshot> items = getStoredItems(user);
            items.add(item);
            node.getNode("items").setValue(new TypeToken<List<ItemStackSnapshot>>(){}, items);
            loader.save(node);
        } catch (Exception e) {
            SafeTrade.getLogger().error("Error adding item to storage. Offending file: " + file.getName());
            e.printStackTrace();
        }
    }

    public static void storeItems(User user, List<ItemStack> items) {
        ArrayList<ItemStackSnapshot> snapshots = new ArrayList<>();
        for (ItemStack item : items) {
            snapshots.add(item.createSnapshot());
        }
        storeItemSnapshots(user, snapshots);
    }

    public static void storeItemSnapshots(User user, List<ItemStackSnapshot> items) {
        File file = getFile(user);
        try {
            ConfigurationLoader<CommentedConfigurationNode> loader = getLoader(file);
            CommentedConfigurationNode node = loader.load();
            ArrayList<ItemStackSnapshot> storedItems = getStoredItems(user);
            storedItems.addAll(items);
            node.getNode("items").setValue(new TypeToken<List<ItemStackSnapshot>>(){}, storedItems);
            loader.save(node);
        } catch (Exception e) {
            SafeTrade.getLogger().error("Error adding item snapshot to storage. Offending file: " + file.getName());
            e.printStackTrace();
        }
    }

    // TODO: Doesn't work - figure out a way to fix this eventually
    /*
    public static void removeItem(User user, ItemStackSnapshot item) {
        File file = getFile(user);
        if (file.exists()) {
            try {
                ConfigurationLoader<CommentedConfigurationNode> loader = getLoader(file);
                CommentedConfigurationNode node = loader.load();
                ArrayList<ItemStackSnapshot> storedItems = getStoredItems(user);
                Iterator<ItemStackSnapshot> iter = storedItems.iterator();
                while (iter.hasNext()) {
                    if (iter.next().createStack().equalTo(item.createStack())) {
                        iter.remove();
                        break;
                    }
                }
                node.getNode("items").setValue(new TypeToken<List<ItemStackSnapshot>>(){}, storedItems);
                loader.save(node);
            } catch (Exception e) {
                SafeTrade.getLogger().error("Error removing item from storage. Offending file: " + file.getName());
                e.printStackTrace();
            }
        }
    }*/

    public static void clearStoredItems(User user) {
        File file = getFile(user);
        if (file.exists()) {
            try {
                ConfigurationLoader<CommentedConfigurationNode> loader = getLoader(file);
                CommentedConfigurationNode node = loader.load();
                node.getNode("items").setValue(new TypeToken<List<ItemStackSnapshot>>(){}, new ArrayList<>());
                loader.save(node);
            } catch (Exception e) {
                SafeTrade.getLogger().error("Error clearing item storage. Offending file: " + file.getName());
                e.printStackTrace();
            }
        }
    }

    public static ArrayList<ItemStackSnapshot> getStoredItems(User user) {
        ArrayList<ItemStackSnapshot> items = new ArrayList<>();
        File file = getFile(user);
        if (file.exists()) {
            try {
                ConfigurationLoader<CommentedConfigurationNode> loader = getLoader(file);
                CommentedConfigurationNode node = loader.load();
                if (node.getNode("items").isVirtual()) {
                    return items;
                }
                items.addAll(node.getNode("items").getList(new TypeToken<ItemStackSnapshot>(){}));
            } catch (Exception e) {
                SafeTrade.getLogger().error("Error retrieving items from storage. Offending file: " + file.getName());
                e.printStackTrace();
            }
        }

        return items;
    }

    public static int recycleLogs() {
        int count = 0;
        for (File file : dataDir.listFiles()) {
            ArrayList<Log> logs = new ArrayList<>();
            if (file.exists()) {
                try {
                    ConfigurationLoader<CommentedConfigurationNode> loader = getLoader(file);
                    CommentedConfigurationNode node = loader.load(ConfigurationOptions.defaults().setObjectMapperFactory(SafeTrade.getFactory()));
                    if (node.getNode("logs").isVirtual()) {
                        break;
                    }
                    logs.addAll(node.getNode("logs").getList(new TypeToken<Log>(){}));
                } catch (Exception e) {
                    SafeTrade.getLogger().error("Error deserialising log. Offending file: " + file.getName());
                    e.printStackTrace();
                }
            }

            boolean needsRewrite = false;
            Iterator<Log> iter = logs.iterator();
            while (iter.hasNext()) {
                Log log = iter.next();
                long daysBetween = ChronoUnit.DAYS.between(log.getTimestamp(), Utils.convertToUTC(LocalDateTime.now()));
                if (daysBetween >= Config.gcLogsExpiryTime) {
                    iter.remove();
                    needsRewrite = true;
                    count++;
                }
            }
            if (needsRewrite) {
                try {
                    ConfigurationLoader<CommentedConfigurationNode> loader = getLoader(file);
                    CommentedConfigurationNode node = loader.load(ConfigurationOptions.defaults().setObjectMapperFactory(SafeTrade.getFactory()));
                    node.getNode("logs").setValue(new TypeToken<List<Log>>(){}, logs);
                    loader.save(node);
                } catch (Exception e) {
                    SafeTrade.getLogger().error("Error recycling logs. Offending file: " + file.getName());
                    e.printStackTrace();
                }
            }
        }

        return count;
    }

    public static ArrayList<Trade> getActiveTrades() {
        return new ArrayList<>(activeTrades);
    }

    public static Trade getTrade(Player player) {
        for (Trade trade : activeTrades) {
            if (trade.hasPlayer(player)) {
                return trade;
            }
        }

        return null;
    }

    public static void addTrade(Trade trade) {
        activeTrades.add(trade);
    }

    public static void removeTrade(Trade trade) {
        activeTrades.remove(trade);
    }


    public static ConfigurationLoader<CommentedConfigurationNode> getLoader(File file) {
        return HoconConfigurationLoader.builder().setFile(file).build();
    }

    public static File getFile(User user) {
        return new File(dataDir, user.getUniqueId() + ".hocon");
    }

    private static boolean isFileEmpty(File file) {
        try {
            CommentedConfigurationNode node = getLoader(file).load();
            return node.getNode("listings").getChildrenList().isEmpty() && node.getNode("logs").getChildrenList().isEmpty() && node.getNode("items").getChildrenList().isEmpty();
        } catch (Exception e) {
            SafeTrade.getLogger().error("Unable to load file to check if empty: " + file.getName());
            return false;
        }
    }
}
