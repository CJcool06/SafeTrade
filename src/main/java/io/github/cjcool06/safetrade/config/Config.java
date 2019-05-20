package io.github.cjcool06.safetrade.config;

import io.github.cjcool06.safetrade.SafeTrade;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.io.File;

public class Config {
    private static String DIR = "config/safetrade";
    private static ConfigurationLoader<CommentedConfigurationNode> loader;
    private static CommentedConfigurationNode node;

    public static boolean showEggStats = false;
    public static boolean showEggName = true;

    public static boolean gcLogsEnabled = true;
    public static int gcLogsExpiryTime = 31;

    public static boolean gcStoragesEnabled = true;

    public static boolean asyncStoragesEnabled = true;
    public static int asyncStoragesInterval = 1;

    public static void load() {
        File file = new File(DIR, "safetrade.conf");
        try {
            loader = HoconConfigurationLoader.builder().setFile(file).build();
            node = loader.load();

            // If the config file doesn't exist, then presume the dir is missing also.
            if (!file.exists()) {
                new File("config/safetrade").mkdirs();
                save();
            }
            else {
                showEggStats = node.getNode("ShowEggStats").getBoolean();
                showEggName = node.getNode("ShowEggName").getBoolean();
                gcLogsEnabled = node.getNode("GarbageCollector", "Logs", "Enabled").getBoolean();
                gcLogsExpiryTime = node.getNode("GarbageCollector", "Logs", "ExpiryTime").getInt();
                gcStoragesEnabled = node.getNode("GarbageCollector", "Storages").getBoolean();
                asyncStoragesEnabled = node.getNode("AsyncSaving", "Storages", "Enabled").getBoolean();
                asyncStoragesInterval = node.getNode("AsyncSaving", "Storages", "Interval").getInt();
            }
        } catch (Exception e) {
            SafeTrade.getLogger().error("Could not load config.");
        }
    }

    public static void save() {
        try {
            node.getNode("ShowEggStats").setComment("Show the stats of the Pokemon inside the egg.");
            node.getNode("ShowEggStats").setValue(showEggStats);

            node.getNode("ShowEggName").setComment("Show the name of the Pokemon inside the egg.");
            node.getNode("ShowEggName").setValue(showEggName);

            node.getNode("GarbageCollector").setComment("The GC improves the efficiency of SafeTrade." +
                    "\n" +
                    "\nRefer to the wiki if you don't know what you're doing.");

            node.getNode("GarbageCollector", "Logs").setComment("Quicken log checkups by deleting old logs.");

            node.getNode("GarbageCollector", "Logs", "Enabled").setComment("Enables the GC to handle logs.");
            node.getNode("GarbageCollector", "Logs", "Enabled").setValue(gcLogsEnabled);

            node.getNode("GarbageCollector", "Logs", "ExpiryTime").setComment("The age a log must be to be deleted, in days.");
            node.getNode("GarbageCollector", "Logs", "ExpiryTime").setValue(gcLogsExpiryTime);

            node.getNode("GarbageCollector", "Storages").setComment("Quickens storage saving & loading by deleting empty storage files.");

            node.getNode("GarbageCollector", "Storages", "Enabled").setComment("Enables the GC to handle storages.");
            node.getNode("GarbageCollector", "Storages", "Enabled").setValue(gcStoragesEnabled);

            node.getNode("AsyncSaving").setComment("Asynchronous saving improves the efficiency of SafeTrade." +
                    "\n" +
                    "\nRefer to the wiki if you don't know what you're doing.");

            node.getNode("AsyncSaving", "Storages").setComment("Quickens shutdown saving and prevents loss of data in case of crash.");

            node.getNode("AsyncSaving", "Storages", "Enabled").setComment("Enables asynchronous storage saving.");
            node.getNode("AsyncSaving", "Storages", "Enabled").setValue(asyncStoragesEnabled);

            node.getNode("AsyncSaving", "Storages", "Interval").setComment("The interval of asynchronous storage saving, in hours.");
            node.getNode("AsyncSaving", "Storages", "Interval").setValue(asyncStoragesInterval);

            loader.save(node);
        } catch (Exception e) {
            SafeTrade.getLogger().error("Could not save config.");
        }
    }
}
