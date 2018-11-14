package io.github.cjcool06.safetrade.data;

import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;

import java.util.Optional;

@Deprecated
public class SafeTradeDataManipulatorBuilder extends AbstractDataBuilder<SafeTradeData> implements DataManipulatorBuilder<SafeTradeData, ImmutableSafeTradeData> {
    public SafeTradeDataManipulatorBuilder() {
        super(SafeTradeData.class, 2);
    }

    @Override
    public SafeTradeData create() {
        return new SafeTradeData();
    }

    @Override
    public Optional<SafeTradeData> createFrom(DataHolder dataHolder) {
        return create().fill(dataHolder);
    }

    @Override
    protected Optional<SafeTradeData> buildContent(DataView container) throws InvalidDataException {
        return create().from(container);
    }
}
