package io.github.cjcool06.safetrade.managers;

import com.google.common.reflect.TypeToken;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.cjcool06.safetrade.SafeTrade;
import io.github.cjcool06.safetrade.config.Config;
import io.github.cjcool06.safetrade.obj.Log;
import io.github.cjcool06.safetrade.obj.PlayerStorage;
import io.github.cjcool06.safetrade.trackers.Tracker;
import io.github.cjcool06.safetrade.utils.Utils;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.entity.living.player.User;

import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * DataManager handles all data saving/loading
 */
public final class DataManager {
    public static final File dataDir = new File("config/safetrade/data");
    //public static final File activeDir = new File("config/safetrade/data/active");
    //public static final File endedDir = new File("config/safetrade/data/inactive");
    public static final File storageDir = new File("config/safetrade/data/player-storage");
    public static final File logsDir = new File("config/safetrade/data/logs");

    /**
     * Loads {@link PlayerStorage}s from files.
     */
    public static void load() {
        dataDir.mkdirs();
        storageDir.mkdirs();
        logsDir.mkdirs();
        JsonParser parser = new JsonParser();
        File currentFile = null;

        try {
            List<File> emptyFiles = new ArrayList<>();

            for (File file : storageDir.listFiles()) {
                currentFile = file;
                JsonObject jsonObject = (JsonObject)parser.parse(new FileReader(file));
                PlayerStorage storage = PlayerStorage.fromContainer(jsonObject);

                // If the PlayerStorage is empty and the GC is enabled, the file will be deleted and not added to the cache
                if (storage.isEmpty() && Config.gcStoragesEnabled) {
                    emptyFiles.add(file);
                }
                else {
                    Tracker.addStorage(storage);
                }
            }
            for (File emptyFile : emptyFiles) {
                emptyFile.delete();
            }

            /* todo: Future support for trade persistence

            for (File file : activeDir.listFiles()) {
                currentFile = file;
                JsonObject jsonObject = (JsonObject)parser.parse(new FileReader(file));
                Trade trade = Trade.fromContainer(jsonObject);
                Tracker.addActiveTrade(trade);
            }
            for (File file : endedDir.listFiles()) {
                currentFile = file;
                JsonObject jsonObject = (JsonObject)parser.parse(new FileReader(file));
                Trade trade = Trade.fromContainer(jsonObject);
                Tracker.addEndedTrade(trade, false);
            }*/
        } catch (Exception e) {
            SafeTrade.getLogger().error("Error reading file: " + (currentFile != null ? currentFile.getName() : "None"));
            SafeTrade.getLogger().error(e.getMessage());
        }
    }

    /**
     * Saves all {@link PlayerStorage}s to files.
     */
    public static void save() {
        // todo: Future support for trade persistence
        /*
        for (Trade trade : Tracker.getAllActiveTrades()) {
            saveTrade(trade, activeDir);
        }*/
        for (PlayerStorage storage : Tracker.getStorages()) {
            if (storage.needSaving()) {
                storage.save();
            }
        }
    }

    /**
     * Saves the {@link PlayerStorage} to file.
     *
     * @param storage The storage
     */
    public static void savePlayerStorage(PlayerStorage storage) {
        JsonObject storageObject = new JsonObject();
        storage.toContainer(storageObject);

        try {
            File file = new File(storageDir, storage.playerUUID + ".json");
            PrintWriter pw = new PrintWriter(file);
            String objString = new GsonBuilder().setPrettyPrinting().create().toJson(storageObject);
            pw.print(objString);
            pw.flush();
            pw.close();
        } catch (Exception e) {
            SafeTrade.getLogger().warn("Error saving the PlayerStorage of a player:  UUID=" + storage.playerUUID.toString());
            e.printStackTrace();
        }
    }

    // todo: Future support for trade persistence
    /*
    public static void saveTrade(Trade trade, File dir) {
        JsonObject tradeObj = new JsonObject();
        trade.toContainer(tradeObj);

        try {
            File file = new File(dir, trade.getId() + ".json");
            PrintWriter pw = new PrintWriter(file);
            String objString = new GsonBuilder().setPrettyPrinting().create().toJson(tradeObj);
            pw.print(objString);
            pw.flush();
            pw.close();
        } catch (Exception e) {
            SafeTrade.getLogger().warn("Error saving a Trade:  ID=" + trade.getId());
            e.printStackTrace();
        }
    }*/

    /**
     * Deletes the file associated with the {@link PlayerStorage}.
     *
     * @param storage The storage
     */
    public static void deletePlayerStorageFile(PlayerStorage storage) {
        new File(storageDir, storage.playerUUID + ".json").delete();
    }

    // todo: Future support for trade persistence
    /*
    public static void deleteTradeFile(Trade trade, File dir) {
        new File(dir, trade.getId() + ".json").delete();
    }*/

    /**
     * Adds a {@link Log} to the {@link User}'s file.
     *
     * @param user The user
     * @param log The log
     */
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

    /**
     * Removes a {@link Log} from the {@link User}'s file.
     *
     * @param user The user
     * @param log The log
     */
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

    /**
     * Clears all {@link Log}s from the {@link User}'s file.
     *
     * @param user The user
     */
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

    /**
     * Gets all {@link Log}s from the {@link User}'s file.
     *
     * @param user The user
     * @return A list of logs
     */
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

    /**
     * Handles deletion of {@link Log}s if they meet a certain age threshold specified in {@link Config}
     *
     * @return Amount of logs deleted
     */
    public static int recycleLogs() {
        int count = 0;
        for (File file : logsDir.listFiles()) {
            ArrayList<Log> logs = new ArrayList<>();
            if (file.exists()) {
                try {
                    if (isFileEmpty(file)) {
                        file.delete();
                        continue;
                    }
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

    /**
     * Gets the {@link ConfigurationLoader} for the {@link File}.
     *
     * @param file The file
     * @return The configuration loader
     */
    public static ConfigurationLoader<CommentedConfigurationNode> getLoader(File file) {
        return HoconConfigurationLoader.builder().setFile(file).build();
    }

    /**
     * Gets the file of a {@link User}.
     *
     * @param user The user
     * @return The file
     */
    public static File getFile(User user) {
        return new File(logsDir, user.getUniqueId() + ".hocon");
    }

    /**
     * Checks if the {@link File} has any {@link Log}s, otherwise it is empty.
     *
     * @param file The file
     * @return True if no logs are found
     */
    private static boolean isFileEmpty(File file) {
        try {
            CommentedConfigurationNode node = getLoader(file).load();
            return node.getNode("logs").getChildrenList().isEmpty();
        } catch (Exception e) {
            SafeTrade.getLogger().error("Unable to load file to check if empty: " + file.getName());
            return false;
        }
    }
}
