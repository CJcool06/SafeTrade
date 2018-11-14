package io.github.cjcool06.safetrade.api.gui;

import org.spongepowered.api.item.inventory.Inventory;

// TODO
public abstract class GUIBase {
    private final Inventory inventory;

    public GUIBase(Inventory inventory) {
        this.inventory = inventory;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public abstract void update();
}
