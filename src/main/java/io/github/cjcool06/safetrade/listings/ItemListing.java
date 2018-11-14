package io.github.cjcool06.safetrade.listings;

import io.github.cjcool06.safetrade.api.enquiry.ListingBase;
import io.github.cjcool06.safetrade.config.Config;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.enchantment.EnchantmentType;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.time.LocalDateTime;
import java.util.*;

public class ItemListing extends ListingBase {
    public ItemType itemType = null;
    public Integer quantity = null;
    public ArrayList<Enchantment> enchantments = new ArrayList<>();
    public Character quantityOperator = null;
    // TODO: Maybe make it so enchantments have operators

    public ItemListing(User user) {
        this(user, LocalDateTime.now().plusMinutes(Config.itemListingTime), UUID.randomUUID());
    }

    public ItemListing(User user, LocalDateTime endDate, UUID uniqueID) {
        super(user, endDate, uniqueID);
    }

    public Text getTypeText() {
        return Text.of(TextColors.GOLD, "Item Listing");
    }

    @Override
    public List<Text> getDisplayLore() {
        ArrayList<Text> texts = new ArrayList<>();

        if (Config.cleanListings) {
            if (itemType != null) {
                texts.add(Text.of(TextColors.DARK_AQUA, "Item: ", TextColors.AQUA, itemType.getTranslation()));
            }
            if (quantity != null) {
                texts.add(Text.of(TextColors.DARK_AQUA, "Quantity: ", TextColors.AQUA, quantityOperator != null ? quantityOperator : "", quantity));
            }
            if (!enchantments.isEmpty()) {
                texts.add(Text.of(TextColors.DARK_AQUA, "Enchantments:"));
                for (Enchantment enchantment : enchantments) {
                    texts.add(Text.of(TextColors.AQUA, enchantment.getType().getTranslation().get() + " ", enchantment.getLevel()));
                }
            }
        }
        else {
            texts.add(Text.of(TextColors.DARK_AQUA, "Item: ", TextColors.AQUA, itemType != null ? itemType.getTranslation() : "Any"));
            texts.add(Text.of(TextColors.DARK_AQUA, "Quantity: ", TextColors.AQUA, quantityOperator != null ? quantityOperator : "", quantity != null ? quantity : "Any"));
            texts.add(Text.of(TextColors.DARK_AQUA, "Enchantments: ", TextColors.AQUA, enchantments.isEmpty() ? "Any" : ""));
            for (Enchantment enchantment : enchantments) {
                texts.add(Text.of(TextColors.AQUA, enchantment.getType().getTranslation().get() + " ", enchantment.getLevel()));
            }
        }
        texts.add(Text.of());
        texts.add(getTypeText());

        return texts;
    }

    @Override
    public boolean isEmpty() {
        return itemType == null && quantity == null && enchantments.isEmpty();
    }

    @Override
    public Map<String, String> toContainer() {
        Map<String, String> map = super.toContainer();

        map.put("item-type", itemType != null ? itemType.getId() : "null");
        map.put("quantity", quantity != null ? String.valueOf(quantity) : "null");
        map.put("quantity-operator", quantityOperator != null ? String.valueOf(quantityOperator) : "null");
        String enchantments = "";
        if (!this.enchantments.isEmpty()) {
            Iterator<Enchantment> iter = this.enchantments.iterator();
            while (iter.hasNext()) {
                Enchantment enchantment = iter.next();
                enchantments += enchantment.getType().getId() + "-" + String.valueOf(enchantment.getLevel()) + (iter.hasNext() ? "," : "");
            }
        }
        map.put("enchantments", !enchantments.equals("") ? enchantments : "null");

        return map;
    }

    @Override
    public void fromContainer(Map<String, String> map) {
        String itemType = map.get("item-type");
        String quantity = map.get("quantity");
        String quantityOperator = map.get("quantity-operator");
        String enchantments = map.get("enchantments");

        if (!itemType.equalsIgnoreCase("null")) {
            this.itemType = Sponge.getRegistry().getType(ItemType.class, itemType).get();
        }
        if (!quantity.equalsIgnoreCase("null")) {
            this.quantity = Integer.parseInt(quantity);
        }
        if (!quantityOperator.equalsIgnoreCase("null")) {
            this.quantityOperator = quantityOperator.charAt(0);
        }
        if (!enchantments.equalsIgnoreCase("null")) {
            for (String string : enchantments.split(",")) {
                String[] str = string.split("-");
                Enchantment enchantment = Enchantment.of(Sponge.getRegistry().getType(EnchantmentType.class, str[0]).get(), Integer.parseInt(str[1]));
                this.enchantments.add(enchantment);
            }
        }
    }
}
