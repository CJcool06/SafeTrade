package io.github.cjcool06.safetrade.config;

import io.github.cjcool06.safetrade.SafeTrade;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    public static List<String> blacklistedCurrencies = new ArrayList<>();
    public static List<String> blacklistedPokemon = new ArrayList<>();
    public static List<String> blacklistedItems = new ArrayList<>();

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
                showEggStats = node.getNode("ShowEggStats").getBoolean(false);
                showEggName = node.getNode("ShowEggName").getBoolean(true);
                gcLogsEnabled = node.getNode("GarbageCollector", "Logs", "Enabled").getBoolean(true);
                gcLogsExpiryTime = node.getNode("GarbageCollector", "Logs", "ExpiryTime").getInt(31);
                gcStoragesEnabled = node.getNode("GarbageCollector", "Storages").getBoolean(true);
                asyncStoragesEnabled = node.getNode("AsyncSaving", "Storages", "Enabled").getBoolean(true);
                asyncStoragesInterval = node.getNode("AsyncSaving", "Storages", "Interval").getInt(1);

                blacklistedCurrencies = node.getNode("Blacklists", "Currencies").getChildrenList().stream().map(ConfigurationNode::getString).collect(Collectors.toList());
                blacklistedItems = node.getNode("Blacklists", "Items").getChildrenList().stream().map(ConfigurationNode::getString).collect(Collectors.toList());
                blacklistedPokemon = node.getNode("Blacklists", "Pokemon").getChildrenList().stream().map(ConfigurationNode::getString).collect(Collectors.toList());

                // This will load all values that are being using into the config at runtime, as well as
                // ensure old configs will have any new nodes that are added.
                save();
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

            node.getNode("Blacklists").setValue("Prevents players from trading certain things.");

            node.getNode("Blacklists", "Currencies").setComment("Prevents players from trading certain currencies. " +
                    "\nUse the currency ID. Eg. \"economylite:coin\" (notice the quotations)");
            node.getNode("Blacklists", "Currencies").setValue(blacklistedCurrencies);

            node.getNode("Blacklists", "Items").setComment("Prevents players from trading certain items." +
                    "\nUse the item ID. Eg. \"minecraft:paper\" (notice the quotations)");
            node.getNode("Blacklists", "Items").setValue(blacklistedItems);

            node.getNode("Blacklists", "Pokemon").setComment("Prevents players from trading certain pokemon." +
                    "\nUse pokemon species. Eg. magikarp (notice the lack of quotations");
            node.getNode("Blacklists", "Pokemon").setValue(blacklistedPokemon);

            loader.save(node);
        } catch (Exception e) {
            SafeTrade.getLogger().error("Could not save config.");
        }
    }
}
