package io.github.cjcool06.safetrade.guis;

import io.github.cjcool06.safetrade.SafeTrade;
import io.github.cjcool06.safetrade.api.condition.Condition;
import io.github.cjcool06.safetrade.api.events.listing.AddListingEvent;
import io.github.cjcool06.safetrade.conditions.ItemCondition;
import io.github.cjcool06.safetrade.conditions.PokemonCondition;
import io.github.cjcool06.safetrade.config.Config;
import io.github.cjcool06.safetrade.listeners.ChatListener;
import io.github.cjcool06.safetrade.listings.ItemListing;
import io.github.cjcool06.safetrade.listings.PokemonListing;
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
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.ArrayList;

public class OptionsGUI {
    private Player player;
    private Inventory inventory;
    public PokemonListing pokemonListing = null;
    public ItemListing itemListing = null;
    public String listeningForChat = null;

    public OptionsGUI(Player player) {
        this.player = player;
        inventory = getNewBooleanInv();
        update();
        player.openInventory(inventory);
    }

    public Player getPlayer() {
        return player;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void update() {
        if (pokemonListing != null) {
            drawPokemonInventory();
        }
        else if (itemListing != null) {
            drawItemInventory();
        }
        else {
            drawBooleanInventory();
        }
    }

    private void drawBooleanInventory() {
        inventory.slots().forEach(slot -> {
            final int slotIndex = slot.getProperty(SlotIndex.class, "slotindex").get().getValue();
            if (slotIndex == 2) {
                slot.set(ItemUtils.getItemListingButton());
            }
            else if (slotIndex == 4) {
                slot.set(ItemUtils.getALlListingsButton());
            }
            else if (slotIndex == 6) {
                slot.set(ItemUtils.getPokemonListingButton());
            }
            else if (slotIndex >= 0 && slotIndex <= 8){
                slot.set(ItemUtils.getBorder(DyeColors.GRAY));
            }
        });
    }

    private void drawPokemonInventory() {
        //shuffleItems(0);
        inventory.slots().forEach(slot -> {
            final int slotIndex = slot.getProperty(SlotIndex.class, "slotindex").get().getValue();
            if (slotIndex == 11) {
                slot.set(ItemUtils.getPokemonNatureButton(this));
            }
            else if (slotIndex == 13) {
                slot.set(ItemUtils.getPokemonShinyButton(this));
            }
            else if (slotIndex == 15) {
                slot.set(ItemUtils.getPokemonIVsButton(this));
            }
            else if (slotIndex == 21) {
                slot.set(ItemUtils.getPokemonGrowthButton(this));
            }
            else if (slotIndex == 22) {
                slot.set(ItemUtils.getPokemonNameButton(this));
            }
            else if (slotIndex == 23) {
                slot.set(ItemUtils.getPokemonLevelButton(this));
            }
            else if (slotIndex == 29) {
                slot.set(ItemUtils.getPokemonAbilityButton(this));
            }
            else if (slotIndex == 31) {
                slot.set(ItemUtils.getPokemonTypeButton(this));
            }
            else if (slotIndex == 33) {
                slot.set(ItemUtils.getPokemonEVsButton(this));
            }
            else if (slotIndex == 48) {
                slot.set(ItemUtils.getSearchListingsButton());
            }
            else if (slotIndex == 50) {
                slot.set(ItemUtils.getNewListingButton());
            }
            else {
                slot.set(ItemUtils.getBorder(DyeColors.GRAY));
            }
        });
    }

    private void drawItemInventory() {
        //shuffleItems(0);
        inventory.slots().forEach(slot -> {
            final int slotIndex = slot.getProperty(SlotIndex.class, "slotindex").get().getValue();
            if (slotIndex == 11) {
                slot.set(ItemUtils.getChooseItemButton(this));
            }
            else if (slotIndex == 13) {
                slot.set(ItemUtils.getQuantityButton(this));
            }
            else if (slotIndex == 15) {
                slot.set(ItemUtils.getEnchantmentsButton(this));
            }
            else if (slotIndex == 30) {
                slot.set(ItemUtils.getSearchListingsButton());
            }
            else if (slotIndex == 32) {
                slot.set(ItemUtils.getNewListingButton());
            }
            else {
                slot.set(ItemUtils.getBorder(DyeColors.GRAY));
            }
        });
    }

    private Inventory getNewBooleanInv() {
        return Inventory.builder()
                .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(Text.of(TextColors.DARK_AQUA, "Select a listing type")))
                .property(InventoryDimension.PROPERTY_NAME, new InventoryDimension(9,1))
                .of(InventoryArchetypes.MENU_GRID)
                .listener(ClickInventoryEvent.class, this::handleClickBoolean)
                .build(SafeTrade.getPlugin());
    }

    private Inventory getNewMainPokemonInv() {
        return Inventory.builder()
                .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(Text.of(TextColors.DARK_AQUA, "What pokemon are you looking for?")))
                .property(InventoryDimension.PROPERTY_NAME, new InventoryDimension(9,6))
                .of(InventoryArchetypes.MENU_GRID)
                .listener(ClickInventoryEvent.class, this::handleClickMain)
                .build(SafeTrade.getPlugin());
    }

    private Inventory getNewMainItemInv() {
        return Inventory.builder()
                .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(Text.of(TextColors.DARK_AQUA, "What item are you looking for?")))
                .property(InventoryDimension.PROPERTY_NAME, new InventoryDimension(9,4))
                .of(InventoryArchetypes.MENU_GRID)
                .listener(ClickInventoryEvent.class, this::handleClickMain)
                .build(SafeTrade.getPlugin());
    }

    private void handleClickBoolean(ClickInventoryEvent event) {
        event.setCancelled(true);
        event.getCause().first(Player.class).ifPresent(player -> {
            event.getTransactions().forEach(transaction -> {
                Sponge.getScheduler().createTaskBuilder().execute(() -> {
                    ItemStackSnapshot snapshot = transaction.getOriginal();
                    if (snapshot.createStack().equalTo(ItemUtils.getItemListingButton())) {
                        itemListing = new ItemListing(player);
                        inventory = getNewMainItemInv();
                        update();
                        player.openInventory(inventory);
                    }
                    else if (snapshot.createStack().equalTo(ItemUtils.getALlListingsButton())) {
                        if (!player.hasPermission("safetrade.common.find.search")) {
                            player.sendMessage(Text.of(TextColors.RED, "You do not have permission to search listings."));
                            player.closeInventory();
                            return;
                        }
                        new ListingsGUI(player);
                    }
                    else if (snapshot.createStack().equalTo(ItemUtils.getPokemonListingButton())) {
                        pokemonListing = new PokemonListing(player);
                        inventory = getNewMainPokemonInv();
                        update();
                        player.openInventory(inventory);
                    }
                }).delayTicks(1).submit(SafeTrade.getPlugin());
            });
        });
    }

    private void handleClickMain(ClickInventoryEvent event) {
        event.setCancelled(true);
        event.getCause().first(Player.class).ifPresent(player -> {
            event.getTransactions().forEach(transaction -> {
                ItemStackSnapshot snapshot = transaction.getOriginal();
                Sponge.getScheduler().createTaskBuilder().execute(() -> {
                    if (pokemonListing != null) {
                        if (snapshot.createStack().equalTo(ItemUtils.getPokemonNameButton(this))) {
                            listeningForChat = "name";
                            player.closeInventory();
                            ChatListener.optionsGUISListening.add(this);
                            player.sendMessage(Utils.createHoverText(Text.of(TextColors.GRAY, "Enter the Pokemon's name: (Hover for help)"), Text.of(TextColors.GOLD, "- Accepts \"any\"\n" +
                                    TextColors.GRAY, "Example: Shuckle")));
                        }
                        // Can only change the type if the name is null, as the type becomes moot if a pokemon is specified
                        else if (snapshot.createStack().equalTo(ItemUtils.getPokemonTypeButton(this)) && pokemonListing.getName() == null) {
                            listeningForChat = "type";
                            player.closeInventory();
                            ChatListener.optionsGUISListening.add(this);
                            player.sendMessage(Utils.createHoverText(Text.of(TextColors.GRAY, "Enter the Pokemon's type: (Hover for help)"), Text.of(TextColors.GOLD, "- Accepts \"any\"\n" +
                                    "- For 2 types, leave a space in-between.\n", TextColors.GRAY, "Example: Grass Poison")));
                        }
                        else if (snapshot.createStack().equalTo(ItemUtils.getPokemonShinyButton(this))) {
                            listeningForChat = "shiny";
                            player.closeInventory();
                            ChatListener.optionsGUISListening.add(this);
                            player.sendMessage(Utils.createHoverText(Text.of(TextColors.GRAY, "Is this Pokemon shiny? Yes or No: (Hover for help)"), Text.of(TextColors.GOLD, "- Accepts \"any\"\n",
                                    TextColors.GRAY, "I refuse to give an example on how to give a yes or no answer.")));
                        }
                        else if (snapshot.createStack().equalTo(ItemUtils.getPokemonLevelButton(this))) {
                            listeningForChat = "level";
                            player.closeInventory();
                            ChatListener.optionsGUISListening.add(this);
                            player.sendMessage(Utils.createHoverText(Text.of(TextColors.GRAY, "Enter the Pokemon's level: (Hover for help)"), Text.of(TextColors.GOLD, "- Accepts \"any\"\n" +
                                    "- You can use '>' (greater-than or equal-to) or '<' (less-than or equal-to)\n",
                                    TextColors.GRAY, "Examples: 50, >90, <25")));
                        }
                        else if (snapshot.createStack().equalTo(ItemUtils.getPokemonNatureButton(this))) {
                            listeningForChat = "nature";
                            player.closeInventory();
                            ChatListener.optionsGUISListening.add(this);
                            player.sendMessage(Utils.createHoverText(Text.of(TextColors.GRAY, "Enter the Pokemon's nature: (Hover for help)"), Text.of(TextColors.GOLD, "- Accepts \"any\"\n",
                                    TextColors.GRAY, "Examples: Bold, Adamant")));
                        }
                        else if (snapshot.createStack().equalTo(ItemUtils.getPokemonGrowthButton(this))) {
                            listeningForChat = "growth";
                            player.closeInventory();
                            ChatListener.optionsGUISListening.add(this);
                            player.sendMessage(Utils.createHoverText(Text.of(TextColors.GRAY, "Enter the Pokemon's growth: (Hover for help)"), Text.of(TextColors.GOLD, "- Accepts \"any\"\n",
                                    TextColors.GRAY, "Examples: Small, Ginormous")));
                        }
                        else if (snapshot.createStack().equalTo(ItemUtils.getPokemonAbilityButton(this))) {
                            listeningForChat = "ability";
                            player.closeInventory();
                            ChatListener.optionsGUISListening.add(this);
                            player.sendMessage(Utils.createHoverText(Text.of(TextColors.GRAY, "Enter the Pokemon's ability: (Hover for help)"), Text.of(TextColors.GOLD, "- Accepts \"any\"\n" +
                                    "- Case-Sensitive",
                                    TextColors.GRAY, "Examples: IronFist, ThickFat, Sturdy, Intimidate")));
                        }
                        else if (snapshot.createStack().equalTo(ItemUtils.getPokemonIVsButton(this))) {
                            listeningForChat = "ivs";
                            player.closeInventory();
                            ChatListener.optionsGUISListening.add(this);
                            player.sendMessage(Utils.createHoverText(Text.of(TextColors.GRAY, "Enter the Pokemon's iv percentage: (Hover for help)"), Text.of(TextColors.GOLD, "- Accepts \"any\"\n" +
                                            "- You can use '>' (greater-than or equal-to) or '<' (less-than or equal-to)\n",
                                    TextColors.GRAY, "Examples: 50, >90, <25")));
                        }
                        else if (snapshot.createStack().equalTo(ItemUtils.getPokemonEVsButton(this))) {
                            listeningForChat = "evs";
                            player.closeInventory();
                            ChatListener.optionsGUISListening.add(this);
                            player.sendMessage(Utils.createHoverText(Text.of(TextColors.GRAY, "Enter the Pokemon's ev percentage: (Hover for help)"), Text.of(TextColors.GOLD, "- Accepts \"any\"\n" +
                                            "- You can use '>' (greater-than or equal-to) or '<' (less-than or equal-to)\n",
                                    TextColors.GRAY, "Examples: 50, >90, <25")));
                        }
                        else if (snapshot.createStack().equalTo(ItemUtils.getNewListingButton())) {
                            if (!pokemonListing.isEmpty()) {
                                if (SafeTrade.EVENT_BUS.post(new AddListingEvent(pokemonListing))) {
                                    return;
                                }
                                else if (DataManager.getActiveListings(player).size() >= Config.maxListingsPerPlayer && !player.hasPermission("safetrade.admin.find.exempt")) {
                                    player.sendMessage(Text.of(TextColors.RED, "You cannot have more than ", TextColors.GOLD, Config.maxListingsPerPlayer, TextColors.RED, " listings at once."));
                                    player.closeInventory();
                                    return;
                                }
                                else if (!player.hasPermission("safetrade.common.find.create.pokemon")) {
                                    player.sendMessage(Text.of(TextColors.RED, "You do not have permission to create pokemon listings."));
                                    player.closeInventory();
                                    return;
                                }
                                Sponge.getScheduler().createTaskBuilder().execute(() -> DataManager.addListing(pokemonListing)).async().submit(SafeTrade.getPlugin());
                                this.player.closeInventory();
                                MessageChannel.TO_PLAYERS.send(
                                        Text.builder()
                                                .append(Text.of(TextSerializers.FORMATTING_CODE.deserialize(Config.prefix), TextColors.AQUA, this.player.getName(), " is looking for a "))
                                                .append(Text.builder().append(Text.of(TextColors.GOLD, "Pokemon"))
                                                        .onHover(TextActions.showText(Text.joinWith(Text.of("\n"), pokemonListing.getDisplayLore())))
                                                        .build()).build());
                            }
                        }
                        else if (snapshot.createStack().equalTo(ItemUtils.getSearchListingsButton())) {
                            if (!player.hasPermission("safetrade.common.find.search")) {
                                player.sendMessage(Text.of(TextColors.RED, "You do not have permission to search listings."));
                                player.closeInventory();
                                return;
                            }
                            ArrayList<Condition> conditions = new ArrayList<>();
                            conditions.add(new PokemonCondition(pokemonListing));
                            new ListingsGUI(player, conditions);
                        }
                    }
                    else if (itemListing != null) {
                        if (snapshot.createStack().equalTo(ItemUtils.getChooseItemButton(this))) {
                            listeningForChat = "item";
                            player.closeInventory();
                            ChatListener.optionsGUISListening.add(this);
                            player.sendMessage(Utils.createHoverText(Text.of(TextColors.GRAY, "Enter the item: (Hover for help)"), Text.of(TextColors.GOLD, "- Accepts \"any\"\n",
                                    TextColors.GRAY, "Examples: diamond_ore, pixelmon:poke_ball, pixelmon:revive")));
                        }
                        else if (snapshot.createStack().equalTo(ItemUtils.getQuantityButton(this))) {
                            listeningForChat = "quantity";
                            player.closeInventory();
                            ChatListener.optionsGUISListening.add(this);
                            player.sendMessage(Utils.createHoverText(Text.of(TextColors.GRAY, "Enter the item's quantity: (Hover for help)"), Text.of(TextColors.GOLD, "- Accepts \"any\"\n" +
                                            "- You can use '>' (greater-than or equal-to) or '<' (less-than or equal-to)\n",
                                    TextColors.GRAY, "Examples: 50, >100, <300")));
                        }
                        else if (snapshot.createStack().equalTo(ItemUtils.getEnchantmentsButton(this))) {
                            listeningForChat = "enchantments";
                            player.closeInventory();
                            ChatListener.optionsGUISListening.add(this);
                            player.sendMessage(Utils.createHoverText(Text.of(TextColors.GRAY, "Enter the item's enchantments: (Hover for help)"), Text.of(TextColors.GOLD, "- Accepts \"any\"\n" +
                                            "- For multiple enchantments, leave a space in-between each one.\n",
                                    TextColors.GRAY, "Example: sharpness:4 bane_of_arthropods:2 fire_aspect:2")));
                        }
                        else if (snapshot.createStack().equalTo(ItemUtils.getNewListingButton())) {
                            if (!itemListing.isEmpty()) {
                                if (SafeTrade.EVENT_BUS.post(new AddListingEvent(itemListing))) {
                                    return;
                                }
                                else if (DataManager.getActiveListings(player).size() >= Config.maxListingsPerPlayer && !player.hasPermission("safetrade.admin.find.exempt")) {
                                    player.sendMessage(Text.of(TextColors.RED, "You cannot have more than ", TextColors.GOLD, Config.maxListingsPerPlayer, TextColors.RED, " listings at once."));
                                    player.closeInventory();
                                    return;
                                }
                                else if (!player.hasPermission("safetrade.common.find.create.item")) {
                                    player.sendMessage(Text.of(TextColors.RED, "You do not have permission to create item listings."));
                                    player.closeInventory();
                                    return;
                                }
                                Sponge.getScheduler().createTaskBuilder().execute(() -> DataManager.addListing(itemListing)).async().submit(SafeTrade.getPlugin());
                                this.player.closeInventory();
                                MessageChannel.TO_PLAYERS.send(
                                        Text.builder()
                                                .append(Text.of(TextSerializers.FORMATTING_CODE.deserialize(Config.prefix), TextColors.AQUA, this.player.getName(), " is looking for an "))
                                                .append(Text.builder().append(Text.of(TextColors.GOLD, "Item"))
                                                        .onHover(TextActions.showText(Text.joinWith(Text.of("\n"), itemListing.getDisplayLore())))
                                                        .build())
                                                .build());
                            }
                        }
                        else if (snapshot.createStack().equalTo(ItemUtils.getSearchListingsButton())) {
                            if (!player.hasPermission("safetrade.common.find.search")) {
                                player.sendMessage(Text.of(TextColors.RED, "You do not have permission to search listings."));
                                player.closeInventory();
                                return;
                            }
                            ArrayList<Condition> conditions = new ArrayList<>();
                            conditions.add(new ItemCondition(itemListing));
                            new ListingsGUI(player, conditions);
                        }
                    }

                }).delayTicks(1).submit(SafeTrade.getPlugin());
            });
        });
    }
}
