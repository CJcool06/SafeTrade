package io.github.cjcool06.safetrade.guis;


import com.google.common.collect.Lists;
import io.github.cjcool06.safetrade.SafeTrade;
import io.github.cjcool06.safetrade.api.condition.Condition;
import io.github.cjcool06.safetrade.api.enquiry.ListingBase;
import io.github.cjcool06.safetrade.config.Config;
import io.github.cjcool06.safetrade.listeners.ChatListener;
import io.github.cjcool06.safetrade.managers.DataManager;
import io.github.cjcool06.safetrade.utils.ItemUtils;
import io.github.cjcool06.safetrade.utils.Utils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ListingsGUI {
    public final Player player;
    public Inventory inventory;
    // <Page, <Slot, Listing>>
    private final HashMap<Integer, HashMap<Integer, ListingBase>> view = new HashMap<>();
    public ArrayList<Condition> conditions = new ArrayList<>();
    private int currentPage = 0;
    private ListingBase currentListing = null;

    public ListingsGUI(Player player) {
        this(player, new ArrayList<>());
    }

    public ListingsGUI(Player player, ArrayList<Condition> conditions) {
        this.player = player;
        this.conditions = conditions;
        inventory = getNewMainInv();
        update();
        player.openInventory(inventory);
    }

    public ArrayList<ListingBase> getListings() {
        ArrayList<ListingBase> listings = new ArrayList<>();
        for (HashMap<Integer, ListingBase> map : view.values()) {
            listings.addAll(map.values());
        }
        return listings;
    }

    public void removeListing(int page, int slot) {
        if (view.containsKey(page)) {
            view.get(page).remove(slot);
        }
    }

    public void removeListing(ListingBase listing) {
        Integer p = null;
        Integer s = null;
        outerloop:
        for (int page : view.keySet()) {
            for (int slot : view.get(page).keySet()) {
                if (view.get(page).get(slot).getUniqueID().equals(listing.getUniqueID())) {
                    p = page;
                    s = slot;
                    break outerloop;
                }
            }
        }
        if (p != null) {
            removeListing(p, s);
        }
    }

    public void addListing(ListingBase listing) {
        for (Condition condition : conditions) {
            if (!condition.passes(listing)) {
                return;
            }
        }
        int[] pageAndSlot = getNextFreeSlot();
        view.get(pageAndSlot[0]).put(pageAndSlot[1], listing);
    }

    public void addListings(List<ListingBase> listings) {
        outerloop:
        for (ListingBase listing : listings) {
            for (Condition condition : conditions) {
                if (!condition.passes(listing)) {
                    continue outerloop;
                }
            }
            int[] pageAndSlot = getNextFreeSlot();
            view.get(pageAndSlot[0]).put(pageAndSlot[1], listing);
        }
    }

    public void clear() {
        inventory.clear();
        view.clear();
    }

    public int getCurrentPage() {
        return currentPage;
    }

    // index 0 is the page, index 1 is the slot
    private int[] getNextFreeSlot() {
        for (int page = 0; page <= 100; page++) {
            if (!view.containsKey(page)) {
                view.put(page, new HashMap<Integer, ListingBase>(){});
            }
            if (hasSpace(page)) {
                return new int[]{page, getFreeSlotInPage(page)};
            }
        }

        // Shouldn't ever be reached
        return new int[]{0, 0};
    }

    private int getFreeSlotInPage(int page) {
        for (int i = 0; i < 54; i++) {
            if (isInBounds(i) && !view.get(page).containsKey(i)) {
                return i;
            }
        }

        return 0;
    }

    private boolean hasSpace(int page) {
        for (int slot = 0; slot < 54; slot++) {
            if (isInBounds(slot) && !view.get(page).containsKey(slot)) {
                return true;
            }
        }

        return false;
    }

    private boolean isPageEmpty(int page) {
        return view.get(page).keySet().isEmpty();
    }

    private int getMaxPage() {
        int maxPage = 0;
        for (int page : view.keySet()) {
            if (page > maxPage) {
                if (!isPageEmpty(page)) {
                    player.sendMessage(Text.of("Page ", page, " is not empty."));   // TODO DEBUG
                    maxPage = page;
                }
                else {
                    player.sendMessage(Text.of("Page ", page, " is empty."));   // TODO DEBUG
                    maxPage = page;
                }
            }
        }

        return maxPage;
    }

    private boolean isInBounds(int slot) {
        return !((slot >= 6 && slot <= 8) || (slot >= 15 && slot <= 17) || (slot >= 24 && slot <= 26) || (slot >= 33 && slot <= 35) || (slot >= 42 && slot <= 44) || (slot >= 51 && slot <= 53));
    }

    public void changePage(int newPage) {
        if (currentPage ==  newPage) {
            return;
        }
        currentPage = newPage;
        update();
    }

    public void update() {
        inventory.clear();
        if (inventory.getInventoryProperty(InventoryTitle.class).get().getValue().toPlain().equalsIgnoreCase("SafeTrade Listings")) {
            updateListings();
            drawInventory();
        }
        else {
            drawBooleanInventory(currentListing);
        }
    }

    private void updateListings() {
        view.clear();
        List<ListingBase> listings = DataManager.getActiveListings();
        List<ListingBase> reversedListings = Lists.reverse(listings);
        addListings(reversedListings);
    }

    private Inventory getNewBooleanInv() {
        return Inventory.builder()
                .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(Text.of(TextColors.DARK_AQUA, "Remove this listing?")))
                .property(InventoryDimension.PROPERTY_NAME, new InventoryDimension(9,1))
                .of(InventoryArchetypes.MENU_GRID)
                .listener(ClickInventoryEvent.class, this::handleClickBoolean)
                .build(SafeTrade.getPlugin());
    }

    private Inventory getNewMainInv() {
        return Inventory.builder()
                .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(Text.of(TextColors.DARK_AQUA, "SafeTrade Listings")))
                .property(InventoryDimension.PROPERTY_NAME, new InventoryDimension(9,6))
                .of(InventoryArchetypes.MENU_GRID)
                .listener(ClickInventoryEvent.class, this::handleClick)
                .build(SafeTrade.getPlugin());
    }

    private void drawBooleanInventory(ListingBase listing) {
        inventory.slots().forEach(slot -> {
            final int slotIndex = slot.getProperty(SlotIndex.class, "slotindex").get().getValue();
            if (slotIndex == 2) {
                slot.set(ItemUtils.getCancelButton());
            }
            else if (slotIndex == 4) {
                slot.set(listing.getDisplayItem());
            }
            else if (slotIndex == 6) {
                slot.set(ItemUtils.getConfirmButton());
            }
            else if (slotIndex >= 0 && slotIndex <= 8){
                slot.set(ItemUtils.getBorder(DyeColors.GRAY));
            }
        });
    }

    private void drawInventory() {
        inventory.slots().forEach(slot -> {
            if (slot.getProperty(SlotIndex.class, "slotindex").isPresent()) {
                final int slotIndex = slot.getProperty(SlotIndex.class, "slotindex").get().getValue();
                if ((slotIndex >= 6 && slotIndex <= 8) || slotIndex == 15 || slotIndex == 17 || slotIndex == 24 || slotIndex == 26 || (slotIndex >= 33 && slotIndex <= 35) || slotIndex == 42 || slotIndex == 44 || slotIndex == 51 || slotIndex == 53) {
                    slot.set(ItemUtils.getBorder(DyeColors.GRAY));
                }
                else if (slotIndex == 16) {
                    slot.set(ItemUtils.getPreviousPage(this));
                }
                else if (slotIndex == 25) {
                    slot.set(ItemUtils.getNextPage(this));
                }
                else if (slotIndex == 43) {
                    slot.set(ItemUtils.getUpdateButton());
                }
                else if (slotIndex == 52) {
                    slot.set(ItemUtils.getSearchUserButton(this));
                }
                else if (slotIndex <= 53) {
                    if (view.containsKey(currentPage) && view.get(currentPage).containsKey(slotIndex)) {
                        slot.set(view.get(currentPage).get(slotIndex).getDisplayItem());
                    }
                }
            }
        });
    }

    private void handleClick(ClickInventoryEvent event) {
        event.setCancelled(true);
        event.getCause().first(Player.class).ifPresent(player -> {
            event.getTransactions().forEach(transaction -> {
                transaction.getSlot().getProperty(SlotIndex.class, "slotindex").ifPresent(slot -> {
                    Sponge.getScheduler().createTaskBuilder().execute(() -> {
                        ItemStackSnapshot snapshot = transaction.getOriginal();
                        if (!isInBounds(slot.getValue())) {
                            if (snapshot.createStack().equalTo(ItemUtils.getNextPage(this))) {
                                if (currentPage == getMaxPage()) {
                                    changePage(0);
                                }
                                else {
                                    changePage(currentPage + 1);
                                }
                            }
                            else if (snapshot.createStack().equalTo(ItemUtils.getPreviousPage(this))) {
                                if (currentPage == 0) {
                                    changePage(getMaxPage());
                                }
                                else {
                                    changePage(currentPage - 1);
                                }
                            }
                            else if (snapshot.createStack().equalTo(ItemUtils.getUpdateButton())) {
                                update();
                            }
                            else if (snapshot.createStack().equalTo(ItemUtils.getSearchUserButton(this))) {
                                player.sendMessage(Text.of(TextColors.GRAY, "Enter the name of a user: (Also accepts 'any')"));
                                ChatListener.listingsGUISListening.add(this);
                                Sponge.getScheduler().createTaskBuilder().execute(() -> player.closeInventory()).delayTicks(1).submit(SafeTrade.getPlugin());
                            }
                        }
                        else {
                            for (ListingBase listing : getListings()) {
                                if (snapshot.createStack().equalTo(listing.getDisplayItem())) {
                                    if ((player.getUniqueId().equals(listing.getUser().getUniqueId()) && player.hasPermission("safetrade.common.find.cancel")) || player.hasPermission("safetrade.admin.find.remove")) {
                                        if (!player.hasPermission("safetrade.admin.find.remove") && !(Utils.getActiveTime(listing) >= Config.timeBeforeAllowedListingRemoval)) {
                                            player.sendMessage(Text.of(TextColors.RED, "You must wait ", TextColors.GOLD, Config.timeBeforeAllowedListingRemoval - Utils.getActiveTime(listing),
                                                    TextColors.RED, " minutes before you can cancel that listing."));
                                        }
                                        else {
                                            inventory = getNewBooleanInv();
                                            currentListing = listing;
                                            update();
                                            player.openInventory(inventory);
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }).delayTicks(1).submit(SafeTrade.getPlugin());
                });
            });
        });
    }

    private void handleClickBoolean(ClickInventoryEvent event) {
        event.setCancelled(true);
        event.getCause().first(Player.class).ifPresent(player -> {
            event.getTransactions().forEach(transaction -> {
                transaction.getSlot().getProperty(SlotIndex.class, "slotindex").ifPresent(slot -> {
                    Sponge.getScheduler().createTaskBuilder().execute(() -> {
                        ItemStackSnapshot snapshot = transaction.getOriginal();
                        if (snapshot.createStack().equalTo(ItemUtils.getConfirmButton())) {
                            Sponge.getScheduler().createTaskBuilder().execute(() -> {
                                DataManager.removeListing(currentListing);
                                currentListing = null;
                            }).async().submit(SafeTrade.getPlugin());
                            inventory = getNewMainInv();
                            update();
                            player.openInventory(inventory);
                        }
                        else if (snapshot.createStack().equalTo(ItemUtils.getCancelButton())) {
                            currentListing = null;
                            inventory = getNewMainInv();
                            update();
                            player.openInventory(inventory);
                        }
                    }).delayTicks(1).submit(SafeTrade.getPlugin());
                });
            });
        });
    }
}
