package io.github.cjcool06.safetrade.data;

import com.google.common.reflect.TypeToken;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.TypeTokens;

public class SafeTradeKeys {
    public static Key<ListValue<ItemStackSnapshot>> PENDING_ITEMS;
    public static Key<ListValue<Text>> LOGS;

    public static void init() {
        PENDING_ITEMS = Key.builder()
                .type(new TypeToken<ListValue<ItemStackSnapshot>>(){})
                .id("pending_items")
                .name("Pending Items")
                .query(DataQuery.of('.', "pending_items"))
                .build();

        LOGS = Key.builder()
                .type(TypeTokens.LIST_TEXT_VALUE_TOKEN)
                .id("logs")
                .name("Logs")
                .query(DataQuery.of('.', "logs"))
                .build();
    }
}
