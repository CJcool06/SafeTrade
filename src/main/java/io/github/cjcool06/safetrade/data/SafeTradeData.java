package io.github.cjcool06.safetrade.data;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Deprecated
public class SafeTradeData extends AbstractData<SafeTradeData, ImmutableSafeTradeData> {
    private List<ItemStackSnapshot> pendingItems;

    public SafeTradeData() {
        this(new ArrayList<>());
    }

    public SafeTradeData(List<ItemStackSnapshot> pendingItems) {
        this.pendingItems = pendingItems;
        registerGettersAndSetters();
    }

    public void addPendingItem(ItemStackSnapshot snapshot) {
        pendingItems.add(snapshot);
    }

    public void addPendingItems(List<ItemStackSnapshot> snapshots) {
        pendingItems.addAll(snapshots);
    }

    public void removePendingItem(ItemStackSnapshot snapshot) {
        pendingItems.removeIf(item1 -> snapshot.createStack().equalTo(item1.createStack()));
    }

    public void clearPendingItems() {
        pendingItems.clear();
    }

    public ListValue<ItemStackSnapshot> getPendingItems() {
        return Sponge.getRegistry().getValueFactory().createListValue(SafeTradeKeys.PENDING_ITEMS, this.pendingItems);
    }

    @Override
    protected void registerGettersAndSetters() {
        registerFieldGetter(SafeTradeKeys.PENDING_ITEMS, () -> this.pendingItems);
        registerFieldSetter(SafeTradeKeys.PENDING_ITEMS, pendingItems -> this.pendingItems = pendingItems);
        registerKeyValue(SafeTradeKeys.PENDING_ITEMS, this::getPendingItems);
    }

    @Override
    public Optional<SafeTradeData> fill(DataHolder dataHolder, MergeFunction overlap) {
        Optional<SafeTradeData> dataOpt = dataHolder.get(SafeTradeData.class);

        if (dataOpt.isPresent()) {
            SafeTradeData otherData = dataOpt.get();
            SafeTradeData finalData = overlap.merge(this, otherData);
            this.pendingItems = finalData.pendingItems;
        }

        return Optional.of(this);
    }

    @Override
    public Optional from(DataContainer container) {
        return from((DataView) container);
    }

    public Optional<SafeTradeData> from(DataView view) {
        view.getSerializableList(SafeTradeKeys.PENDING_ITEMS.getQuery(), ItemStackSnapshot.class).ifPresent(pendingItems -> this.pendingItems.addAll(pendingItems));
        return Optional.of(this);
    }

    @Override
    public SafeTradeData copy() {
        return new SafeTradeData(this.pendingItems);
    }

    @Override
    public ImmutableSafeTradeData asImmutable() {
        return new ImmutableSafeTradeData(this.pendingItems);
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
