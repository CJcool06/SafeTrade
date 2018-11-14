package io.github.cjcool06.safetrade.conditions;

import io.github.cjcool06.safetrade.SafeTrade;
import io.github.cjcool06.safetrade.api.condition.Condition;
import io.github.cjcool06.safetrade.api.enquiry.ListingBase;
import io.github.cjcool06.safetrade.listings.ItemListing;
import org.spongepowered.api.item.enchantment.Enchantment;

public class ItemCondition implements Condition {
    private final ItemListing conditionListing;

    public ItemCondition(ItemListing itemListing) {
        this.conditionListing = itemListing;
    }

    public boolean passes(ListingBase listing) {
        if (listing instanceof ItemListing) {
            ItemListing itemListing = (ItemListing)listing;
            if (conditionListing.itemType != null) {
                if (!conditionListing.itemType.equals(itemListing.itemType)) {
                    return false;
                }
            }
            else if (conditionListing.quantity != null) {
                Character quantityOperator = itemListing.quantityOperator;
                if (itemListing.quantity == null) {
                    return false;
                }
                if (quantityOperator == null) {
                    if (!conditionListing.quantity.equals(itemListing.quantity)) {
                        return false;
                    }
                }
                else if (quantityOperator == '>'){
                    if (itemListing.quantity > conditionListing.quantity) {
                        return false;
                    }
                }
                else if (quantityOperator == '<') {
                    if (itemListing.quantity < conditionListing.quantity) {
                        return false;
                    }
                }
                else {
                    SafeTrade.getLogger().warn("An item condition has caught a stray quantity operator, '", quantityOperator, "', whilst parsing listing " + listing.getUniqueID() +
                            " associated with user " + listing.getUser().getName());
                    return false;
                }
            }
            // TODO: Maybe make it so you can check for certain enchantment levels using operators
            else if (!conditionListing.enchantments.isEmpty()) {
                for (Enchantment enchantment : conditionListing.enchantments) {
                    boolean passed = false;
                    for (Enchantment ench : itemListing.enchantments) {
                        if (enchantment.getType().equals(ench.getType())) {
                            passed = true;
                            break;
                        }
                    }
                    if (!passed) {
                        return false;
                    }
                }
            }
        }
        else {
            return false;
        }

        return true;
    }
}
