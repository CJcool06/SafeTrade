package io.github.cjcool06.safetrade.api.enquiry;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.RepresentedPlayerData;
import org.spongepowered.api.data.manipulator.mutable.SkullData;
import org.spongepowered.api.data.type.SkullTypes;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class ListingBase {
    private final UUID uniqueID;
    private final User user;
    private LocalDateTime endDate;
    private boolean expired = false;

    public ListingBase(User user, LocalDateTime localDateTime, UUID uniqueID) {
        this.user = user;
        this.endDate = localDateTime;
        this.uniqueID = uniqueID;
    }

    public ItemStack getDisplayItem() {
        SkullData skullData = Sponge.getDataManager().getManipulatorBuilder(SkullData.class).get().create();
        skullData.set(Keys.SKULL_TYPE, SkullTypes.PLAYER);
        ItemStack itemStack = Sponge.getRegistry().createBuilder(ItemStack.Builder.class).itemType(ItemTypes.SKULL).itemData(skullData).build();
        RepresentedPlayerData skinData = Sponge.getDataManager().getManipulatorBuilder(RepresentedPlayerData.class).get().create();
        skinData.set(Keys.REPRESENTED_PLAYER, GameProfile.of(user.getUniqueId()));
        itemStack.offer(skinData);
        itemStack.offer(Keys.ITEM_LORE, getDisplayLore());
        itemStack.offer(Keys.DISPLAY_NAME, Text.of(TextColors.LIGHT_PURPLE, user.getName()));
        return itemStack;
    }

    public UUID getUniqueID() {
        return uniqueID;
    }

    public User getUser() {
        return user;
    }

    public LocalDateTime getEndDate() {
        return this.endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public static DateTimeFormatter getFormatter() {
        return DateTimeFormatter.ofPattern("[dd/MM/yyyy HH:mm]");
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    public abstract Text getTypeText();

    public abstract List<Text> getDisplayLore();

    public abstract boolean isEmpty();

    public Map<String, String> toContainer() {
        Map<String, String> map = new HashMap<>();
        map.put("type", ListingRegistrar.getKeyOfClass(getClass()));
        map.put("uuid", getUniqueID().toString());
        map.put("user", getUser().getUniqueId().toString());
        map.put("endDate", getEndDate().format(getFormatter()));
        return map;
    }

    public abstract void fromContainer(Map<String, String> map);
}
