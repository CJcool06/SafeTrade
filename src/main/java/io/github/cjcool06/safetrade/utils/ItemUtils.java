package io.github.cjcool06.safetrade.utils;

import com.pixelmonmod.pixelmon.api.pokemon.PokemonSpec;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.enums.EnumType;
import io.github.cjcool06.safetrade.SafeTrade;
import io.github.cjcool06.safetrade.api.condition.Condition;
import io.github.cjcool06.safetrade.conditions.UserCondition;
import io.github.cjcool06.safetrade.config.Config;
import io.github.cjcool06.safetrade.guis.ListingsGUI;
import io.github.cjcool06.safetrade.guis.OptionsGUI;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.RepresentedPlayerData;
import org.spongepowered.api.data.manipulator.mutable.SkullData;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.data.type.SkullTypes;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ItemUtils {
    // TradeGUI Items
    // Items
    public static ItemStack getStatusItem(DyeColor color, int amount) {
        ItemStack accept = ItemStack.of(ItemTypes.DYE, amount);
        accept.offer(Keys.DYE_COLOR, color);
        return accept;
    }

    public static ItemStack getPokemonIcon(EntityPixelmon pokemon) {
        //String star = pokemon.getIsShiny() ? "\u2605" : "";
        ItemStack pokemonIcon = Utils.getPicture(pokemon);
        pokemonIcon.offer(Keys.DISPLAY_NAME, Text.of(TextColors.LIGHT_PURPLE, pokemon.getName() + (pokemon.isEgg && !Config.showEggStats ? " Egg" : "")));
        pokemonIcon.offer(Keys.ITEM_LORE, Utils.getPokemonLore(pokemon));

        return pokemonIcon;
    }

    public static ItemStack getInfoItem(Text name, List<Text> lore, DyeColor color) {
        ItemStack info = ItemStack.of(ItemTypes.WOOL, 1);
        info.offer(Keys.DYE_COLOR, color);
        info.offer(Keys.DISPLAY_NAME, name);
        info.offer(Keys.ITEM_LORE, lore);
        return info;
    }

    // Clickable
    public static ItemStack getSlotButton(int slot) {
        ItemStack item = ItemStack.of(Sponge.getRegistry().getType(ItemType.class, "pixelmon:master_ball").get(), slot);
        item.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Slot " + slot));
        return item;
    }

    public static ItemStack getAcceptButton() {
        ItemStack accept = ItemStack.of(ItemTypes.DYE, 1);
        accept.offer(Keys.DYE_COLOR, DyeColors.GREEN);
        accept.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GREEN, "Accept Trade"));
        return accept;
    }

    public static ItemStack getCancelTradeButton() {
        ItemStack cancel = ItemStack.of(ItemTypes.DYE, 1);
        cancel.offer(Keys.DYE_COLOR, DyeColors.RED);
        cancel.offer(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "Cancel Trade"));
        return cancel;
    }

    public static ItemStack getResetMoneyButton() {
        ItemStack reset = ItemStack.of(ItemTypes.DYE, 1);
        reset.offer(Keys.DYE_COLOR, DyeColors.ORANGE);
        reset.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Reset Money"));
        return reset;
    }

    public static ItemStack getExitButton() {
        ItemStack close = ItemStack.of(ItemTypes.BARRIER, 1);
        close.offer(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "Exit Trade"));
        return close;
    }

    public static ItemStack getMoneyButton(int moneyAmount) {
        ItemStack money = ItemStack.of(ItemTypes.GOLD_INGOT, 1);
        money.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GREEN, SafeTrade.getEcoService().getDefaultCurrency().getSymbol(), moneyAmount));
        return money;
    }

    // ListingGUI Items
    // Items
    public static ItemStack getBorder(DyeColor color) {
        ItemStack background = ItemStack.of(ItemTypes.STAINED_GLASS_PANE, 1);
        background.offer(Keys.DYE_COLOR, color);
        return background;
    }

    // Clickable
    public static ItemStack getNextPage(ListingsGUI gui) {
        ItemStack itemStack = ItemStack.of(ItemTypes.DYE, 1);
        itemStack.offer(Keys.DYE_COLOR, DyeColors.GREEN);
        itemStack.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GREEN, "Next Page"));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of(TextColors.GOLD, "Current Page: ", TextColors.YELLOW, gui.getCurrentPage() + 1));
        itemStack.offer(Keys.ITEM_LORE, lore);
        return itemStack;
    }

    public static ItemStack getPreviousPage(ListingsGUI gui) {
        ItemStack itemStack = ItemStack.of(ItemTypes.DYE, 1);
        itemStack.offer(Keys.DYE_COLOR, DyeColors.RED);
        itemStack.offer(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "Previous Page"));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of(TextColors.GOLD, "Current Page: ", TextColors.YELLOW, gui.getCurrentPage() + 1));
        itemStack.offer(Keys.ITEM_LORE, lore);
        return itemStack;
    }

    public static ItemStack getUpdateButton() {
        ItemStack itemStack = ItemStack.of(ItemTypes.DYE, 1);
        itemStack.offer(Keys.DYE_COLOR, DyeColors.ORANGE);
        itemStack.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Update Listings"));
        return itemStack;
    }

    // OptionsGUI Items
    // Clickable
    public static ItemStack getItemListingButton() {
        ItemStack itemStack = ItemStack.of(ItemTypes.DIAMOND_PICKAXE, 1);
        itemStack.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Item Listing"));
        return itemStack;
    }

    public static ItemStack getPokemonListingButton() {
        ItemStack itemStack = Utils.getPicture(new PokemonSpec("ditto").create(FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld()));
        itemStack.offer(Keys.DISPLAY_NAME, Text.of(TextColors.AQUA, "Pokemon Listing"));
        return itemStack;
    }

    public static ItemStack getALlListingsButton() {
        ItemStack itemStack =ItemStack.of(ItemTypes.PAPER, 1);
        itemStack.offer(Keys.DISPLAY_NAME, Text.of(TextColors.AQUA, "All Listings"));
        return itemStack;
    }

    public static ItemStack getNewListingButton() {
        ItemStack itemStack = ItemStack.of(ItemTypes.TIPPED_ARROW, 1);
        itemStack.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GREEN, "New Listing"));
        return itemStack;
    }

    public static ItemStack getSearchListingsButton() {
        ItemStack itemStack = ItemStack.of(ItemTypes.PAPER, 1);
        itemStack.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Search"));
        return itemStack;
    }

    // Pokemon
    public static ItemStack getPokemonNameButton(OptionsGUI gui) {
        ItemStack itemStack = gui.pokemonListing.getName() == null ? ItemStack.of(Sponge.getRegistry().getType(ItemType.class, "pixelmon:poke_ball").get(), 1) : Utils.getPicture(new PokemonSpec(gui.pokemonListing.getName()).create((World)gui.getPlayer().getWorld()));
        itemStack.offer(Keys.DISPLAY_NAME, Text.of(TextColors.DARK_AQUA, "Pokemon: ", TextColors.AQUA, (gui.pokemonListing.getName() != null ? gui.pokemonListing.getName() : "Any")));
        return itemStack;
    }

    public static ItemStack getPokemonTypeButton(OptionsGUI gui) {
        ItemStack itemStack = ItemStack.of(ItemTypes.DYE, 1);
        itemStack.offer(Keys.DYE_COLOR, DyeColors.GRAY);
        Text.Builder builder = Text.builder();
        builder.append(Text.of(TextColors.DARK_AQUA, "Type: "));
        if (gui.pokemonListing.getTypes().isEmpty()) {
            builder.append(Text.of(TextColors.AQUA, "Any"));
        }
        else {
            Iterator<EnumType> iter = gui.pokemonListing.getTypes().iterator();
            while (iter.hasNext()) {
                EnumType type = iter.next();
                builder.append(Text.of(TextColors.AQUA, type.getName() + (iter.hasNext() ? ", " : "")));
            }
        }
        itemStack.offer(Keys.DISPLAY_NAME, builder.build());
        ArrayList<Text> lore = new ArrayList<>();
        if (gui.pokemonListing.getName() != null) {
            lore.add(Text.of(TextColors.GRAY, "You can only change the type if there is no pokemon specified"));
        }
        itemStack.offer(Keys.ITEM_LORE, lore);
        return itemStack;
    }

    public static ItemStack getPokemonShinyButton(OptionsGUI gui) {
        ItemStack itemStack = ItemStack.of(ItemTypes.DIAMOND, 1);
        itemStack.offer(Keys.DISPLAY_NAME, Text.of(TextColors.DARK_AQUA, "Shiny: ", TextColors.AQUA, (gui.pokemonListing.isShiny() != null ? (gui.pokemonListing.isShiny() ? "Yes" : "No") : "Any")));
        return itemStack;
    }

    public static ItemStack getPokemonLevelButton(OptionsGUI gui) {
        ItemStack itemStack = ItemStack.of(Sponge.getRegistry().getType(ItemType.class, "pixelmon:rare_candy").get(), 1);
        itemStack.offer(Keys.DISPLAY_NAME, Text.of(TextColors.DARK_AQUA, "Level: ", TextColors.AQUA, (gui.pokemonListing.getLevelOperator() != null ? gui.pokemonListing.getLevelOperator() : ""), (gui.pokemonListing.getLevel() != null ? gui.pokemonListing.getLevel() : "Any")));
        return itemStack;
    }

    public static ItemStack getPokemonNatureButton(OptionsGUI gui) {
        ItemStack itemStack = ItemStack.of(Sponge.getRegistry().getType(ItemType.class, "pixelmon:ever_stone").get(), 1);
        itemStack.offer(Keys.DISPLAY_NAME, Text.of(TextColors.DARK_AQUA, "Nature: ", TextColors.AQUA, (gui.pokemonListing.getNature() != null ? gui.pokemonListing.getNature().getLocalizedName() : "Any")));
        return itemStack;
    }

    public static ItemStack getPokemonGrowthButton(OptionsGUI gui) {
        ItemStack itemStack = ItemStack.of(ItemTypes.TOTEM_OF_UNDYING, 1);
        itemStack.offer(Keys.DISPLAY_NAME, Text.of(TextColors.DARK_AQUA, "Growth: ", TextColors.AQUA, (gui.pokemonListing.getGrowth() != null ? gui.pokemonListing.getGrowth().getLocalizedName() : "Any")));
        return itemStack;
    }

    public static ItemStack getPokemonAbilityButton(OptionsGUI gui) {
        ItemStack itemStack = ItemStack.of(Sponge.getRegistry().getType(ItemType.class, "pixelmon:ability_capsule").get(), 1);
        itemStack.offer(Keys.DISPLAY_NAME, Text.of(TextColors.DARK_AQUA, "Ability: ", TextColors.AQUA, (gui.pokemonListing.getAbility() != null ? gui.pokemonListing.getAbility().getLocalizedName() : "Any")));
        return itemStack;
    }

    public static ItemStack getPokemonIVsButton(OptionsGUI gui) {
        ItemStack itemStack = ItemStack.of(Sponge.getRegistry().getType(ItemType.class, "pixelmon:destiny_knot").get(), 1);
        itemStack.offer(Keys.DISPLAY_NAME, Text.of(TextColors.DARK_AQUA, "IVs: ", TextColors.AQUA, gui.pokemonListing.getIVOperator() != null ? gui.pokemonListing.getIVOperator() : "", (gui.pokemonListing.getIVPercentage() != null ? gui.pokemonListing.getIVPercentage() : "Any") + "%"));
        return itemStack;
    }

    public static ItemStack getPokemonEVsButton(OptionsGUI gui) {
        ItemStack itemStack = ItemStack.of(Sponge.getRegistry().getType(ItemType.class, "pixelmon:power_lens").get(), 1);
        itemStack.offer(Keys.DISPLAY_NAME, Text.of(TextColors.DARK_AQUA, "EVs: ", TextColors.AQUA, gui.pokemonListing.getEVOperator() != null ? gui.pokemonListing.getEVOperator() : "", (gui.pokemonListing.getEVPercentage() != null ? gui.pokemonListing.getEVPercentage() : "Any") + "%"));
        return itemStack;
    }

    // Item
    public static ItemStack getChooseItemButton(OptionsGUI gui) {
        ItemStack itemStack = ItemStack.of(gui.itemListing.itemType != null ? gui.itemListing.itemType : ItemTypes.MYCELIUM, 1);
        itemStack.offer(Keys.DISPLAY_NAME, Text.of(TextColors.DARK_AQUA, "Item: ", TextColors.AQUA, gui.itemListing.itemType != null ? gui.itemListing.itemType.getTranslation() : "Any"));
        return itemStack;
    }

    public static ItemStack getQuantityButton(OptionsGUI gui) {
        ItemStack itemStack = ItemStack.of(ItemTypes.EMERALD, 1);
        itemStack.offer(Keys.DISPLAY_NAME, Text.of(TextColors.DARK_AQUA, "Quantity: ", TextColors.AQUA, gui.itemListing.quantityOperator != null ? gui.itemListing.quantityOperator : "", gui.itemListing.quantity != null ? gui.itemListing.quantity : "Any"));
        return itemStack;
    }

    public static ItemStack getEnchantmentsButton(OptionsGUI gui) {
        ItemStack itemStack = ItemStack.of(ItemTypes.ENCHANTING_TABLE, 1);
        itemStack.offer(Keys.DISPLAY_NAME, Text.of(TextColors.DARK_AQUA, "Enchantments: ", TextColors.AQUA, gui.itemListing.enchantments.isEmpty() ? "Any" : ""));
        ArrayList<Text> lore = new ArrayList<>();
        for (Enchantment enchantment : gui.itemListing.enchantments) {
            lore.add(Text.of(TextColors.AQUA, enchantment.getType().getTranslation(), " ", enchantment.getLevel()));
        }
        itemStack.offer(Keys.ITEM_LORE, lore);
        return itemStack;
    }

    // ListingsGUI
    public static ItemStack getSearchUserButton(ListingsGUI gui) {
        SkullData skullData = Sponge.getDataManager().getManipulatorBuilder(SkullData.class).get().create();
        skullData.set(Keys.SKULL_TYPE, SkullTypes.PLAYER);
        ItemStack itemStack = Sponge.getRegistry().createBuilder(ItemStack.Builder.class).itemType(ItemTypes.SKULL).itemData(skullData).build();
        itemStack.offer(Keys.DISPLAY_NAME, Text.of(TextColors.DARK_AQUA, "Listings by: ", TextColors.LIGHT_PURPLE, "Everyone"));
        for (Condition condition : gui.conditions) {
            if (condition instanceof UserCondition) {
                RepresentedPlayerData skinData = Sponge.getDataManager().getManipulatorBuilder(RepresentedPlayerData.class).get().create();
                skinData.set(Keys.REPRESENTED_PLAYER, GameProfile.of(((UserCondition)condition).user.getUniqueId()));
                itemStack.offer(skinData);
                itemStack.offer(Keys.DISPLAY_NAME, Text.of(TextColors.DARK_AQUA, "Listings by: ", TextColors.LIGHT_PURPLE, ((UserCondition)condition).user.getName()));
                break;
            }
        }
        ArrayList<Text> lore = new ArrayList<>();
        lore.add(Text.of(TextColors.GRAY, "Click to search listings by player"));
        itemStack.offer(Keys.ITEM_LORE, lore);
        return itemStack;
    }

    public static ItemStack getConfirmButton() {
        ItemStack itemStack = ItemStack.of(ItemTypes.WOOL, 1);
        itemStack.offer(Keys.DYE_COLOR, DyeColors.GREEN);
        itemStack.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GREEN, "Confirm"));
        return itemStack;
    }

    public static ItemStack getCancelButton() {
        ItemStack itemStack = ItemStack.of(ItemTypes.WOOL, 1);
        itemStack.offer(Keys.DYE_COLOR, DyeColors.RED);
        itemStack.offer(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "Cancel"));
        return itemStack;
    }
}
