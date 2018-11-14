package io.github.cjcool06.safetrade.api.enquiry;

import org.spongepowered.api.entity.living.player.User;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.UUID;

public class ListingRegistrar {
    private static HashMap<String, Class<? extends ListingBase>> listingTypes = new HashMap<>();

    public static void register(String key, Class<? extends ListingBase> listingClass) {
        listingTypes.keySet().removeIf(oldKey -> oldKey.equalsIgnoreCase(key));
        listingTypes.put(key.toLowerCase(), listingClass);
    }

    public static Class<? extends ListingBase> getClassFromKey(String key) {
        return listingTypes.keySet().contains(key.toLowerCase()) ? listingTypes.get(key) : null;
    }

    public static String getKeyOfClass(Class<? extends ListingBase> listingClass) {
        for (String key : listingTypes.keySet()) {
            if (listingClass.equals(listingTypes.get(key))) {
                return key;
            }
        }

        return null;
    }

    public static ListingBase parse(String key, User user, LocalDateTime endDate, UUID uniqueID) {
        Class<? extends ListingBase> listingClass = getClassFromKey(key);

        if (listingClass == null) {
            return null;
        }

        try {
            return listingClass.getDeclaredConstructor(User.class, LocalDateTime.class, UUID.class).newInstance(user, endDate, uniqueID);
        } catch (Exception e) {
            return null;
        }
    }
}
