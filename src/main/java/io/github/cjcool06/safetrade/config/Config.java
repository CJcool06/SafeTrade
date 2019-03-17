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
    public static boolean broadcastTradeOverviews = false;

    public static boolean gcLogsEnabled = true;
    public static int gcLogsExpiryTime = 31;

    public static boolean gcStoragesEnabled = true;

    public static void load() {
        File file = new File(DIR, "safetrade.conf");
        try {
            loader = HoconConfigurationLoader.builder().setFile(file).build();
            node = loader.load();
            if (!file.exists()) {
                new File("config/safetrade").mkdirs();  // Creates the dir
                save();
            }
            else {
                showEggStats = node.getNode("ShowEggStats").getBoolean();
                showEggName = node.getNode("ShowEggName").getBoolean();
                broadcastTradeOverviews = node.getNode("BroadcastTradeOverviews").getBoolean();
                gcLogsEnabled = node.getNode("GarbageCollector", "Logs", "Enabled").getBoolean();
                gcLogsExpiryTime = node.getNode("GarbageCollector", "Logs", "ExpiryTime").getInt();
                gcStoragesEnabled = node.getNode("GarbageCollector", "Storages").getBoolean();
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

            node.getNode("BroadcastTradeOverviews").setComment("Trade overviews will be seen by all players in chat.");
            node.getNode("BroadcastTradeOverviews").setValue(broadcastTradeOverviews);

            node.getNode("GarbageCollector").setComment("The GC improves the efficiency of SafeTrade." +
                    "\n" +
                    "\nDo not change anything unless you know what you're doing.");

            node.getNode("GarbageCollector", "Logs").setComment("Quicken log checkups by deleting old logs.");

            node.getNode("GarbageCollector", "Logs", "Enabled").setComment("Enables the GC to handle logs.");
            node.getNode("GarbageCollector", "Logs", "Enabled").setValue(gcLogsEnabled);

            node.getNode("GarbageCollector", "Logs", "ExpiryTime").setComment("The age a log must be to be deleted, in days.");
            node.getNode("GarbageCollector", "Logs", "ExpiryTime").setValue(gcLogsExpiryTime);

            node.getNode("GarbageCollector", "Storages").setComment("Quicken storage saving & loading by deleting empty storage files.");

            node.getNode("GarbageCollector", "Storages", "Enabled").setComment("Enables the GC to handle storages.");
            node.getNode("GarbageCollector", "Storages", "Enabled").setValue(gcStoragesEnabled);

            loader.save(node);
        } catch (Exception e) {
            SafeTrade.getLogger().error("Could not save config.");
        }
    }
}
