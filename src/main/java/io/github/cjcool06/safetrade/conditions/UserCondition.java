package io.github.cjcool06.safetrade.conditions;

import io.github.cjcool06.safetrade.api.condition.Condition;
import io.github.cjcool06.safetrade.api.enquiry.ListingBase;
import org.spongepowered.api.entity.living.player.User;

public class UserCondition implements Condition {
    public final User user;

    public UserCondition(User user) {
        this.user = user;
    }

    public boolean passes(ListingBase listing) {
        return listing.getUser().getUniqueId().equals(user.getUniqueId());
    }
}
