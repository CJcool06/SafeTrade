package io.github.cjcool06.safetrade.data;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.api.data.value.immutable.ImmutableListValue;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public class ImmutableSafeTradeData extends AbstractImmutableData<ImmutableSafeTradeData, SafeTradeData> {
    private List<ItemStackSnapshot> pendingItems;

    public ImmutableSafeTradeData() {
        this(new ArrayList<>());
    }

    public ImmutableSafeTradeData(List<ItemStackSnapshot> pendingItems) {
        this.pendingItems = pendingItems;
        registerGetters();
    }

    public ImmutableListValue<ItemStackSnapshot> getPendingItems() {
        return Sponge.getRegistry().getValueFactory().createListValue(SafeTradeKeys.PENDING_ITEMS, this.pendingItems).asImmutable();
    }

    @Override
    protected void registerGetters() {
        registerFieldGetter(SafeTradeKeys.PENDING_ITEMS, () -> this.pendingItems);
        registerKeyValue(SafeTradeKeys.PENDING_ITEMS, this::getPendingItems);
    }

    @Override
    public SafeTradeData asMutable() {
        return new SafeTradeData(this.pendingItems);
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
                .set(SafeTradeKeys.PENDING_ITEMS.getQuery(), this.pendingItems);
    }
}
