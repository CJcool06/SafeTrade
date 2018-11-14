package io.github.cjcool06.safetrade.data;

import com.google.common.reflect.TypeToken;
import io.github.cjcool06.safetrade.obj.Log;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.TypeTokens;

import java.time.LocalDateTime;

@Deprecated
public class SafeTradeKeys {
    public static Key<ListValue<ItemStackSnapshot>> PENDING_ITEMS;
    public static Key<ListValue<Log>> LOGS;
    public static Key<Value<LocalDateTime>> LOG_DATE;
    public static Key<Value<Text>> LOG_TEXT;
    public static Key<Value<Text>> EXTENDED_LOG_TEXT;
    public static Key<Value<String>> ENQUIRY_KEY;

    public static void init() {
        PENDING_ITEMS = Key.builder()
                .type(new TypeToken<ListValue<ItemStackSnapshot>>(){})
                .id("pending_items")
                .name("Pending Items")
                .query(DataQuery.of('.', "pending_items"))
                .build();

        LOGS = Key.builder()
                .type(new TypeToken<ListValue<Log>>(){})
                .id("logs")
                .name("Logs")
                .query(DataQuery.of('.', "logs"))
                .build();

        LOG_DATE = Key.builder()
                .type(new TypeToken<Value<LocalDateTime>>(){})
                .id("log_date")
                .name("Log Date")
                .query(DataQuery.of('.', "log_date"))
                .build();

        LOG_TEXT = Key.builder()
                .type(TypeTokens.TEXT_VALUE_TOKEN)
                .id("log_text")
                .name("Log Text")
                .query(DataQuery.of('.', "log_text"))
                .build();

        EXTENDED_LOG_TEXT = Key.builder()
                .type(TypeTokens.TEXT_VALUE_TOKEN)
                .id("extended_text")
                .name("Extended Text")
                .query(DataQuery.of('.', "extended_text"))
                .build();

        ENQUIRY_KEY = Key.builder()
                .type(TypeTokens.STRING_VALUE_TOKEN)
                .id("enquiry_key")
                .name("Enquiry Key")
                .query(DataQuery.of('.', "enquiry_key"))
                .build();
    }
}
