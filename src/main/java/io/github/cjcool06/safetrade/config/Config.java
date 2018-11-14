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
    public static boolean showEggStats = false;
    public static boolean cleanListings = true;
    public static int pokemonListingTime = 60;
    public static int itemListingTime = 60;
    public static boolean gcLogsEnabled = false;
    public static int gcLogsExpiryTime = 31;
    public static int maxListingsPerPlayer = 3;
    public static int timeBeforeAllowedListingRemoval = 15;

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
                showEggStats = node.getNode("ShowEggStats").getBoolean(showEggStats);
                cleanListings = node.getNode("CleanListings").getBoolean(cleanListings);
                pokemonListingTime = node.getNode("Listings", "ExpiryTime", "Pokemon").getInt(pokemonListingTime);
                itemListingTime = node.getNode("Listings", "ExpiryTime", "Item").getInt(itemListingTime);
                maxListingsPerPlayer = node.getNode("Listings", "MaxPerPlayer").getInt(maxListingsPerPlayer);
                timeBeforeAllowedListingRemoval = node.getNode("Listings", "TimeBeforeAllowedListingRemoval").getInt(timeBeforeAllowedListingRemoval);
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
            node.getNode("ShowEggStats").setValue(showEggStats);
            node.getNode("CleanListings").setValue(cleanListings);
            node.getNode("Listings", "ExpiryTime", "Pokemon").setValue(pokemonListingTime);
            node.getNode("Listings", "ExpiryTime", "Item").setValue(itemListingTime);
            node.getNode("Listings", "MaxPerPlayer").setValue(maxListingsPerPlayer);
            node.getNode("Listings", "TimeBeforeAllowedListingRemoval").setValue(timeBeforeAllowedListingRemoval);
            node.getNode("GarbageCollector", "Logs", "Enabled").setValue(gcLogsEnabled);
            node.getNode("GarbageCollector", "Logs", "ExpiryTime").setValue(gcLogsExpiryTime);
            loader.save(node);
        } catch (Exception e) {
            SafeTrade.getLogger().error("Could not save config.");
        }
    }
}
