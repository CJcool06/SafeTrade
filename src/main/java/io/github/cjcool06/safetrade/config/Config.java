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

    public static String prefix = "&9&lSafeTrade &7&l>> &r";
    public static boolean gcLogsEnabled = false;
    public static int gcLogsExpiryTime = 31;

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
                prefix = node.getNode("Prefix").getString(prefix);
                gcLogsEnabled = node.getNode("GarbageCollector", "Logs", "Enabled").getBoolean(gcLogsEnabled);
                gcLogsExpiryTime = node.getNode("GarbageCollector", "Logs", "ExpiryTime").getInt(gcLogsExpiryTime);
            }
        } catch (Exception e) {
            SafeTrade.getLogger().error("Could not load config.");
        }
    }

    public static void save() {
        try {
            node.getNode("Prefix").setValue(prefix);
            node.getNode("GarbageCollector", "Logs", "Enabled").setValue(gcLogsEnabled);
            node.getNode("GarbageCollector", "Logs", "ExpiryTime").setValue(gcLogsExpiryTime);
            loader.save(node);
        } catch (Exception e) {
            SafeTrade.getLogger().error("Could not save config.");
        }
    }
}
