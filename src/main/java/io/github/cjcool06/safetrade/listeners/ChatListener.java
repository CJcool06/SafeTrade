package io.github.cjcool06.safetrade.listeners;

import com.pixelmonmod.pixelmon.api.pokemon.PokemonSpec;
import com.pixelmonmod.pixelmon.entities.pixelmon.abilities.AbilityBase;
import com.pixelmonmod.pixelmon.enums.EnumGrowth;
import com.pixelmonmod.pixelmon.enums.EnumNature;
import com.pixelmonmod.pixelmon.enums.EnumPokemon;
import com.pixelmonmod.pixelmon.enums.EnumType;
import io.github.cjcool06.safetrade.conditions.UserCondition;
import io.github.cjcool06.safetrade.guis.ListingsGUI;
import io.github.cjcool06.safetrade.guis.OptionsGUI;
import net.minecraft.world.World;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.enchantment.EnchantmentType;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;
import java.util.Optional;

public class ChatListener {
    public static ArrayList<OptionsGUI> optionsGUISListening = new ArrayList<>();
    public static ArrayList<ListingsGUI> listingsGUISListening = new ArrayList<>();

    @Listener(order = Order.FIRST)
    public void onChat(MessageChannelEvent event) {
        if (event.getSource() instanceof Player) {
            Player player = (Player)event.getSource();
            String message = event.getMessage().toPlain();
            String[] messageArgs = message.split(" ");

            OptionsGUI gui = null;
            for (OptionsGUI optionsGUI : optionsGUISListening) {
                if (optionsGUI.getPlayer().equals(player)) {
                    gui = optionsGUI;
                    break;
                }
            }
            if (gui != null && gui.listeningForChat != null) {
                event.setMessageCancelled(true);
                if (gui.pokemonListing != null) {
                    if (messageArgs.length > 2 && !gui.listeningForChat.equalsIgnoreCase("type")) {
                        player.sendMessage(Text.of(TextColors.RED, "The pokemon's " + gui.listeningForChat + " must only be 1 word."));
                    }
                    else if (gui.listeningForChat.equalsIgnoreCase("name")) {
                        if (messageArgs[1].equalsIgnoreCase("any")) {
                            gui.pokemonListing.setPokemon(null);
                            gui.pokemonListing.getTypes().clear();
                        }
                        else {
                            EnumPokemon pokemon = EnumPokemon.getFromNameAnyCase(messageArgs[1]);
                            if (pokemon == null) {
                                player.sendMessage(Text.of(TextColors.RED, "Invalid pokemon: " + messageArgs[1]));
                                return;
                            }
                            gui.pokemonListing.setPokemon(pokemon);
                            gui.pokemonListing.getTypes().clear();
                            gui.pokemonListing.getTypes().addAll(new PokemonSpec(pokemon.name()).create((World)gui.getPlayer().getWorld()).type);
                        }
                        gui.listeningForChat = null;
                        player.openInventory(gui.getInventory());
                        optionsGUISListening.remove(gui);
                    }
                    else if (gui.listeningForChat.equalsIgnoreCase("type")) {
                        if (messageArgs.length > 3) {
                            player.sendMessage(Text.of(TextColors.RED, "A Pokemon can only have a maximum of 2 types"));
                            return;
                        }
                        if (messageArgs[1].equalsIgnoreCase("any")) {
                            gui.pokemonListing.getTypes().clear();
                        }
                        else {
                            gui.pokemonListing.getTypes().clear();
                            for (int i = 1; i <= messageArgs.length - 1; i++) {
                                EnumType type = null;
                                for (EnumType t : EnumType.getAllTypes()) {
                                    if (t.getName().equalsIgnoreCase(messageArgs[i])) {
                                        type = t;
                                    }
                                }
                                if (type == null) {
                                    player.sendMessage(Text.of(TextColors.RED, "Invalid type: " + messageArgs[i]));
                                    return;
                                }
                                gui.pokemonListing.getTypes().add(type);
                            }
                        }
                        gui.listeningForChat = null;
                        player.openInventory(gui.getInventory());
                        optionsGUISListening.remove(gui);
                    }
                    else if (gui.listeningForChat.equalsIgnoreCase("shiny")) {
                        if (messageArgs[1].equalsIgnoreCase("any")) {
                            gui.pokemonListing.setShiny(null);
                        }
                        else if (messageArgs[1].equalsIgnoreCase("yes")) {
                            gui.pokemonListing.setShiny(true);
                        }
                        else if (messageArgs[1].equalsIgnoreCase("no")) {
                            gui.pokemonListing.setShiny(false);
                        }
                        else {
                            player.sendMessage(Text.of(TextColors.RED, "Must be 'yes', 'no', or 'any'."));
                            return;
                        }
                        gui.listeningForChat = null;
                        player.openInventory(gui.getInventory());
                        optionsGUISListening.remove(gui);
                    }
                    else if (gui.listeningForChat.equalsIgnoreCase("level")) {
                        if (messageArgs[1].equalsIgnoreCase("any")) {
                            gui.pokemonListing.setLevel(null);
                        }
                        else {
                            long level;
                            Character operator = null;
                            try {
                                String subString = null;
                                if (messageArgs[1].charAt(0) == '>' || messageArgs[1].charAt(0) == '<') {
                                    operator = messageArgs[1].charAt(0);
                                    subString = messageArgs[1].substring(1, messageArgs[1].length());
                                }
                                level = subString == null ? Long.parseLong(messageArgs[1]) : Long.parseLong(subString);
                            } catch (NumberFormatException nfe) {
                                player.sendMessage(Text.of(TextColors.RED, "Error parsing number at: " + messageArgs[1]));
                                return;
                            }
                            if (level < 1 || level > 100) {
                                player.sendMessage(Text.of(TextColors.RED, "Level must be between 1 and 100."));
                                return;
                            }
                            gui.pokemonListing.setLevel((int)level);
                            gui.pokemonListing.setLevelOperator(operator);
                        }
                        gui.listeningForChat = null;
                        player.openInventory(gui.getInventory());
                        optionsGUISListening.remove(gui);
                    }
                    else if (gui.listeningForChat.equalsIgnoreCase("nature")) {
                        if (messageArgs[1].equalsIgnoreCase("any")) {
                            gui.pokemonListing.setNature(null);
                        }
                        else {
                            EnumNature nature = EnumNature.natureFromString(messageArgs[1]);
                            if (nature == null) {
                                player.sendMessage(Text.of(TextColors.RED, "Invalid nature: " + messageArgs[1]));
                                return;
                            }
                            gui.pokemonListing.setNature(nature);
                        }
                        gui.listeningForChat = null;
                        player.openInventory(gui.getInventory());
                        optionsGUISListening.remove(gui);
                    }
                    else if (gui.listeningForChat.equalsIgnoreCase("growth")) {
                        if (messageArgs[1].equalsIgnoreCase("any")) {
                            gui.pokemonListing.setGrowth(null);
                        }
                        else {
                            EnumGrowth growth = EnumGrowth.growthFromString(messageArgs[1]);
                            if (growth == null) {
                                player.sendMessage(Text.of(TextColors.RED, "Invalid growth: " + messageArgs[1]));
                                return;
                            }
                            gui.pokemonListing.setGrowth(growth);
                        }
                        gui.listeningForChat = null;
                        player.openInventory(gui.getInventory());
                        optionsGUISListening.remove(gui);
                    }
                    else if (gui.listeningForChat.equalsIgnoreCase("ability")) {
                        if (messageArgs[1].equalsIgnoreCase("any")) {
                            gui.pokemonListing.setAbility(null);
                        }
                        else {
                            Optional<AbilityBase> optAbility = AbilityBase.getAbility(messageArgs[1]);
                            if (!optAbility.isPresent()) {
                                player.sendMessage(Text.of(TextColors.RED, "Invalid ability: " + messageArgs[1]));
                                return;
                            }
                            gui.pokemonListing.setAbility(optAbility.get());
                        }
                        gui.listeningForChat = null;
                        player.openInventory(gui.getInventory());
                        optionsGUISListening.remove(gui);
                    }
                    else if (gui.listeningForChat.equalsIgnoreCase("ivs")) {
                        if (messageArgs[1].equalsIgnoreCase("any")) {
                            gui.pokemonListing.setIVPercentage(null);
                        }
                        else {
                            long ivPercentage;
                            Character operator = null;
                            try {
                                String subString = null;
                                if (messageArgs[1].charAt(0) == '>' || messageArgs[1].charAt(0) == '<') {
                                    operator = messageArgs[1].charAt(0);
                                    subString = messageArgs[1].substring(1, messageArgs[1].length());
                                }
                                ivPercentage = subString == null ? Long.parseLong(messageArgs[1]) : Long.parseLong(subString);
                            } catch (NumberFormatException nfe) {
                                player.sendMessage(Text.of(TextColors.RED, "That is not a valid number."));
                                return;
                            }
                            if (ivPercentage < 0 || ivPercentage > 100) {
                                player.sendMessage(Text.of(TextColors.RED, "IV percentage must be between 0 and 100"));
                                return;
                            }
                            gui.pokemonListing.setIVPercentage((int)ivPercentage);
                            gui.pokemonListing.setIVOperator(operator);
                        }
                        gui.listeningForChat = null;
                        player.openInventory(gui.getInventory());
                        optionsGUISListening.remove(gui);
                    }
                    else if (gui.listeningForChat.equalsIgnoreCase("evs")) {
                        if (messageArgs[1].equalsIgnoreCase("any")) {
                            gui.pokemonListing.setEVPercentage(null);
                        }
                        else {
                            long evPercentage;
                            Character operator = null;
                            try {
                                String subString = null;
                                if (messageArgs[1].charAt(0) == '>' || messageArgs[1].charAt(0) == '<') {
                                    operator = messageArgs[1].charAt(0);
                                    subString = messageArgs[1].substring(1, messageArgs[1].length());
                                }
                                evPercentage = subString == null ? Long.parseLong(messageArgs[1]) : Long.parseLong(subString);
                            } catch (NumberFormatException nfe) {
                                player.sendMessage(Text.of(TextColors.RED, "That is not a valid number."));
                                return;
                            }
                            if (evPercentage < 0 || evPercentage > 100) {
                                player.sendMessage(Text.of(TextColors.RED, "EV percentage must be between 0 and 100"));
                                return;
                            }
                            gui.pokemonListing.setEVPercentage((int)evPercentage);
                            gui.pokemonListing.setEVOperator(operator);
                        }
                        gui.listeningForChat = null;
                        player.openInventory(gui.getInventory());
                        optionsGUISListening.remove(gui);
                    }
                    else {
                        player.sendMessage(Text.of(TextColors.GRAY, "Enter the " + gui.listeningForChat + " of the pokemon, or enter 'any' to not specify a value."));
                    }
                }
                else if (gui.itemListing != null) {
                    if (messageArgs.length > 2 && !gui.listeningForChat.equalsIgnoreCase("enchantments")) {
                        player.sendMessage(Text.of(TextColors.RED, "The " + (gui.listeningForChat.equalsIgnoreCase("item") ? "item" : ("item's " + gui.listeningForChat)) + " must only be 1 word."));
                    }
                    if (gui.listeningForChat.equalsIgnoreCase("item")) {
                        if (messageArgs[1].equalsIgnoreCase("any")) {
                            gui.itemListing.itemType = null;
                        }
                        else {
                            Optional<ItemType> optItemType = Sponge.getRegistry().getType(ItemType.class, messageArgs[1]);
                            if (!optItemType.isPresent()) {
                                player.sendMessage(Text.of(TextColors.RED, "Invalid item id."));
                                return;
                            }
                            gui.itemListing.itemType = optItemType.get();
                        }
                        player.openInventory(gui.getInventory());
                        optionsGUISListening.remove(gui);
                    }
                    else if (gui.listeningForChat.equalsIgnoreCase("quantity")) {
                        if (messageArgs[1].equalsIgnoreCase("any")) {
                            gui.itemListing.quantity = null;
                        }
                        else {
                            long quantity;
                            Character operator = null;
                            try {
                                String subString = null;
                                if (messageArgs[1].charAt(0) == '>' || messageArgs[1].charAt(0) == '<') {
                                    operator = messageArgs[1].charAt(0);
                                    subString = messageArgs[1].substring(1, messageArgs[1].length());
                                }
                                quantity = subString == null ? Long.parseLong(messageArgs[1]) : Long.parseLong(subString);
                            } catch (NumberFormatException nfe) {
                                player.sendMessage(Text.of(TextColors.RED, "That is not a valid number."));
                                return;
                            }
                            gui.itemListing.quantity = (int)quantity;
                            gui.itemListing.quantityOperator = operator;
                        }
                        player.openInventory(gui.getInventory());
                        optionsGUISListening.remove(gui);
                    }
                    else if (gui.listeningForChat.equalsIgnoreCase("enchantments")) {
                        if (messageArgs[1].equalsIgnoreCase("any")) {
                            gui.itemListing.enchantments.clear();
                        }
                        else {
                            gui.itemListing.enchantments.clear();
                            for (int i = 1; i < messageArgs.length; i++) {
                                try {
                                    String[] enchArr = messageArgs[i].split(":");
                                    if (enchArr.length != 2) {
                                        player.sendMessage(Text.of(TextColors.RED, "Enchantments must be in the format ", TextColors.GRAY, "enchantment:level"));
                                        return;
                                    }
                                    Optional<EnchantmentType> optType = Sponge.getRegistry().getType(EnchantmentType.class, enchArr[0]);
                                    if (!optType.isPresent()) {
                                        player.sendMessage(Text.of(TextColors.RED, "Invalid enchantment: " + enchArr[0]));
                                        return;
                                    }
                                    Enchantment enchantment = Enchantment.of(optType.get(), Integer.parseInt(enchArr[1]));
                                    // In-case player enters duplicates
                                    gui.itemListing.enchantments.removeIf(enchantment1 -> enchantment1.getType().equals(optType.get()));
                                    gui.itemListing.enchantments.add(enchantment);
                                } catch (NumberFormatException nfe) {
                                    player.sendMessage(Text.of(TextColors.RED, "Invalid number at " + messageArgs[i]));
                                    return;
                                }
                            }
                            player.openInventory(gui.getInventory());
                            optionsGUISListening.remove(gui);
                        }
                        player.openInventory(gui.getInventory());
                        optionsGUISListening.remove(gui);
                    }
                }

                gui.update();
                return;
            }

            ListingsGUI listingsGUI = null;
            for (ListingsGUI listingsGUI1 : listingsGUISListening) {
                if (listingsGUI1.player.equals(player)) {
                    listingsGUI = listingsGUI1;
                    break;
                }
            }
            if (listingsGUI != null) {
                event.setMessageCancelled(true);
                if (messageArgs.length > 2) {
                    player.sendMessage(Text.of(TextColors.RED, "A user's name must only be 1 word."));
                }
                else {
                    if (messageArgs[1].equalsIgnoreCase("any")) {
                        listingsGUI.conditions.removeIf(condition -> condition instanceof UserCondition);
                    }
                    else {
                        Optional<User> optUser = Sponge.getServiceManager().provide(UserStorageService.class).get().get(messageArgs[1]);
                        if (!optUser.isPresent()) {
                            player.sendMessage(Text.of(TextColors.RED, "User not found."));
                            return;
                        }
                        else {
                            listingsGUI.conditions.removeIf(condition -> condition instanceof UserCondition);
                            listingsGUI.conditions.add(new UserCondition(optUser.get()));
                        }
                    }
                    listingsGUI.update();
                    player.openInventory(listingsGUI.inventory);
                    listingsGUISListening.remove(listingsGUI);
                }
            }
        }
    }
}
