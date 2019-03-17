package io.github.cjcool06.safetrade.utils;

import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonSpec;
import com.pixelmonmod.pixelmon.api.storage.PCStorage;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.battles.controller.BattleControllerBase;
import com.pixelmonmod.pixelmon.config.PixelmonItems;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.items.ItemPixelmonSprite;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import io.github.cjcool06.safetrade.SafeTrade;
import io.github.cjcool06.safetrade.config.Config;
import io.github.cjcool06.safetrade.obj.Side;
import io.github.cjcool06.safetrade.obj.Trade;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;

public class Utils {
    public static Optional<User> getUser(UUID uuid) {
        return Sponge.getServiceManager().provide(UserStorageService.class).get().get(uuid);
    }

    public static ItemStack getPicture(Pokemon pokemon) {
        if (pokemon.isEgg()) {
            net.minecraft.item.ItemStack itemStack = new net.minecraft.item.ItemStack(PixelmonItems.itemPixelmonSprite);
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setString("SpriteName", "pixelmon:sprites/eggs/egg1");
            itemStack.setTagCompound(nbt);
            return (ItemStack)(Object)itemStack;
        }

        return (ItemStack)(Object)ItemPixelmonSprite.getPhoto(pokemon);
    }

    public static Text[] getTradeOverviewLore(Trade trade) {
        Text.Builder builder1 =  Text.builder();
        Text.Builder builder2 = Text.builder();

        builder1.append(Text.of("Money:"))
                .color(TextColors.DARK_AQUA)
                .append(Text.builder().append(Text.of(TextColors.AQUA, " ", trade.getSides()[0].vault.account.getBalance(SafeTrade.getEcoService().getDefaultCurrency()).intValue())).build())
                .build();
        builder2.append(Text.of("Money:"))
                .color(TextColors.DARK_AQUA)
                .append(Text.builder().append(Text.of(TextColors.AQUA, " ", trade.getSides()[1].vault.account.getBalance(SafeTrade.getEcoService().getDefaultCurrency()).intValue())).build())
                .build();

        builder1.append(Text.of("\n" + "Pokemon:"))
                .color(TextColors.DARK_AQUA)
                .build();
        builder2.append(Text.of("\n" + "Pokemon:"))
                .color(TextColors.DARK_AQUA)
                .build();
        for (Pokemon pokemon : trade.getSides()[0].vault.getAllPokemon()) {
            if (pokemon.isEgg()) {
                builder1.append(Text.builder().append(Text.of(TextColors.AQUA, "\n" + (Config.showEggName ? pokemon.getSpecies().getLocalizedName() + " Egg" : "Egg"))).build()).build();
            }
            else {
                builder1.append(Text.builder().append(Text.of(TextColors.AQUA, "\n" + pokemon.getSpecies().getLocalizedName())).build()).build();
            }
        }
        for (Pokemon pokemon : trade.getSides()[1].vault.getAllPokemon()) {
            if (pokemon.isEgg()) {
                builder2.append(Text.builder().append(Text.of(TextColors.AQUA, "\n" + (Config.showEggName ? pokemon.getSpecies().getLocalizedName() + " Egg" : "Egg"))).build()).build();
            }
            else {
                builder2.append(Text.builder().append(Text.of(TextColors.AQUA, "\n" + pokemon.getSpecies().getLocalizedName())).build()).build();
            }
        }

        builder1.append(Text.of("\n" + "Items:"))
                .color(TextColors.DARK_AQUA)
                .build();
        builder2.append(Text.of("\n" + "Items:"))
                .color(TextColors.DARK_AQUA)
                .build();
        for (ItemStackSnapshot snapshot : trade.getSides()[0].vault.getAllItems()) {
            builder1.append(Text.builder().append(Text.of("\n", TextColors.GREEN, snapshot.getQuantity() + "x ", TextColors.AQUA, snapshot.getTranslation().get()))
                    .build()).build();
            if (snapshot.get(Keys.DISPLAY_NAME).isPresent()) {
                builder1.append(Text.builder().append(Text.of("  ", TextColors.GOLD, "[", snapshot.get(Keys.DISPLAY_NAME).get(), TextColors.GOLD, "]")).build()).build();
            }
        }
        for (ItemStackSnapshot snapshot : trade.getSides()[1].vault.getAllItems()) {
            builder2.append(Text.builder().append(Text.of("\n", TextColors.GREEN, snapshot.getQuantity() + "x ", TextColors.AQUA, snapshot.getTranslation().get()))
                    .build()).build();
            if (snapshot.get(Keys.DISPLAY_NAME).isPresent()) {
                builder2.append(Text.builder().append(Text.of("  ", TextColors.GRAY, "[", TextColors.GOLD, snapshot.get(Keys.DISPLAY_NAME).get(), TextColors.GRAY, "]")).build()).build();
            }
        }

        return new Text[]{builder1.build(), builder2.build()};
    }

    public static Text getSuccessMessage(Trade trade) {
        Text[] texts = getTradeOverviewLore(trade);

        return Text.builder("SafeTrade Overview >> ")
                .color(TextColors.GREEN)
                .style(TextStyles.BOLD)
                .append(Text.builder().append(Text.of(TextColors.DARK_AQUA, trade.getSides()[0].getUser().get().getName()))
                        .onHover(TextActions.showText(texts[0]))
                        .build())
                .append(Text.builder().append(Text.of(TextColors.DARK_AQUA, " & "))
                        .build())
                .append(Text.builder().append(Text.of(TextColors.DARK_AQUA, trade.getSides()[1].getUser().get().getName()))
                        .onHover(TextActions.showText(texts[1]))
                        .build())
                .build();
    }

    public static Text getBroadcastOverview(Trade trade) {
        Text[] texts = getTradeOverviewLore(trade);

        return Text.builder()
                .append(Text.builder().append(Text.of(TextColors.DARK_AQUA, trade.getSides()[0].getUser().get().getName()))
                        .onHover(TextActions.showText(texts[0]))
                        .build())
                .append(Text.builder().append(Text.of(TextColors.DARK_AQUA, " & "))
                        .build())
                .append(Text.builder().append(Text.of(TextColors.DARK_AQUA, trade.getSides()[1].getUser().get().getName()))
                        .onHover(TextActions.showText(texts[1]))
                        .build())
                .build();
    }

    public static ArrayList<Text> getPokemonLore(Pokemon pokemon) {
        ArrayList<Text> lore = new ArrayList<>();
        if (pokemon.isEgg() && !Config.showEggStats) {
            lore.add(Text.of(TextColors.GRAY, "The stats of this egg are a mystery."));
            return lore;
        }
        DecimalFormat df = new DecimalFormat("#0.##");
        int ivSum = pokemon.getStats().ivs.hp + pokemon.getStats().ivs.attack + pokemon.getStats().ivs.defence + pokemon.getStats().ivs.specialAttack + pokemon.getStats().ivs.specialDefence + pokemon.getStats().ivs.speed;
        int evSum = pokemon.getStats().evs.hp + pokemon.getStats().evs.attack + pokemon.getStats().evs.defence + pokemon.getStats().evs.specialAttack + pokemon.getStats().evs.specialDefence + pokemon.getStats().evs.speed;
        // Stats
        //String star = "\u2605";
        String nickname = pokemon.getNickname() == null ? pokemon.getSpecies().getLocalizedName() : pokemon.getNickname();
        //String shiny = pokemon.getIsShiny() ? star : "";
        String shiny = pokemon.isShiny() ? "Yes" : "No";
        int level = pokemon.getLevel();
        String nature = pokemon.getNature().getLocalizedName();
        String growth = pokemon.getGrowth().getLocalizedName();
        String ability = pokemon.getAbility().getLocalizedName();
        String originalTrainer = pokemon.getOriginalTrainer();
        String heldItem = "";
        if(pokemon.getHeldItem() != net.minecraft.item.ItemStack.EMPTY) {
            heldItem += pokemon.getHeldItem().getDisplayName();
        }
        else {
            heldItem += "None";
        }
        String breedable = new PokemonSpec("unbreedable").matches(pokemon) ? "No" : "Yes";
        String tradeable = new PokemonSpec("untradeable").matches(pokemon) ? "No" : "Yes";
        // EVs
        int hpEV = pokemon.getStats().evs.hp;
        int attackEV = pokemon.getStats().evs.attack;
        int defenceEV = pokemon.getStats().evs.defence;
        int spAttkEV = pokemon.getStats().evs.specialAttack;
        int spDefEV = pokemon.getStats().evs.specialDefence;
        int speedEV = pokemon.getStats().evs.speed;
        String totalEVs = df.format((long)((int)((double)evSum / 510.0D * 100.0D))) + "%";
        // IVs
        int hpIV = pokemon.getStats().ivs.hp;
        int attackIV = pokemon.getStats().ivs.attack;
        int defenceIV = pokemon.getStats().ivs.defence;
        int spAttkIV = pokemon.getStats().ivs.specialAttack;
        int spDefIV = pokemon.getStats().ivs.specialDefence;
        int speedIV = pokemon.getStats().ivs.speed;
        String totalIVs = df.format((long)((int)((double)ivSum / 186.0D * 100.0D))) + "%";
        // Moves
        String move1 = pokemon.getMoveset().attacks[0] != null ? "" + pokemon.getMoveset().attacks[0] : "None";
        String move2 = pokemon.getMoveset().attacks[1] != null ? "" + pokemon.getMoveset().attacks[1] : "None";
        String move3 = pokemon.getMoveset().attacks[2] != null ? "" + pokemon.getMoveset().attacks[2] : "None";
        String move4 = pokemon.getMoveset().attacks[3] != null ? "" + pokemon.getMoveset().attacks[3] : "None";

        lore.add(Text.of(TextColors.DARK_AQUA, "Nickname: ", TextColors.AQUA, nickname));
        lore.add(Text.of(TextColors.DARK_AQUA, "Shiny: ", TextColors.AQUA, shiny));
        lore.add(Text.of(TextColors.DARK_AQUA, "Level: ", TextColors.AQUA, level));
        lore.add(Text.of(TextColors.DARK_AQUA, "Nature: ", TextColors.AQUA, nature));
        lore.add(Text.of(TextColors.DARK_AQUA, "Growth: ", TextColors.AQUA, growth));
        lore.add(Text.of(TextColors.DARK_AQUA, "Ability: ", TextColors.AQUA, ability));
        lore.add(Text.of(TextColors.DARK_AQUA, "OT: ", TextColors.AQUA, originalTrainer));
        lore.add(Text.of(TextColors.DARK_AQUA, "Held Item: ", TextColors.AQUA, heldItem));
        lore.add(Text.of(TextColors.DARK_AQUA, "Breedable: ", TextColors.AQUA, breedable));
        lore.add(Text.of(TextColors.DARK_AQUA, "Tradeable: ", TextColors.AQUA, tradeable));
        lore.add(Text.of());
        lore.add(Text.of(TextColors.DARK_AQUA, "IVs: ", TextColors.GRAY, "(", TextColors.RED, totalIVs, TextColors.GRAY, ")"));
        lore.add(Text.of(TextColors.AQUA, "Att: ", TextColors.GREEN, attackIV, TextColors.DARK_GRAY, " | ", TextColors.AQUA, "Sp.Att: ", TextColors.GREEN, spAttkIV));
        lore.add(Text.of(TextColors.AQUA, "Def: ", TextColors.GREEN, defenceIV, TextColors.DARK_GRAY, " | ", TextColors.AQUA, "Sp.Def: ", TextColors.GREEN, spDefIV));
        lore.add(Text.of(TextColors.AQUA, "HP: ", TextColors.GREEN, hpIV, TextColors.DARK_GRAY, " | ", TextColors.AQUA, "Speed: ", TextColors.GREEN, speedIV));
        lore.add(Text.of());
        lore.add(Text.of(TextColors.DARK_AQUA, "EVs: ", TextColors.GRAY, "(", TextColors.RED, totalEVs, TextColors.GRAY, ")"));
        lore.add(Text.of(TextColors.AQUA, "Att: ", TextColors.GREEN, attackEV, TextColors.DARK_GRAY, " | ", TextColors.AQUA, "Sp.Att: ", TextColors.GREEN, spAttkEV));
        lore.add(Text.of(TextColors.AQUA, "Def: ", TextColors.GREEN, defenceEV, TextColors.DARK_GRAY, " | ", TextColors.AQUA, "Sp.Def: ", TextColors.GREEN, spDefEV));
        lore.add(Text.of(TextColors.AQUA, "HP: ", TextColors.GREEN, hpEV, TextColors.DARK_GRAY, " | ", TextColors.AQUA, "Speed: ", TextColors.GREEN, speedEV));
        lore.add(Text.of());
        lore.add(Text.of(TextColors.DARK_AQUA, "Moves:"));
        lore.add(Text.of(TextColors.AQUA, move1, TextColors.DARK_GRAY, " | ", TextColors.AQUA, move2));
        lore.add(Text.of(TextColors.AQUA, move3, TextColors.DARK_GRAY, " | ", TextColors.AQUA, move4));

        return lore;
    }

    public static boolean isPlayerOccupied(Player player) {
        BattleControllerBase bcb = BattleRegistry.getSpectatedBattle((EntityPlayerMP)player);
        if (bcb != null) {
            return true;
        }
        bcb = BattleRegistry.getBattle((EntityPlayerMP)player);
        if (bcb != null) {
            return true;
        }
        return false;
    }

    public static LocalDateTime convertToUTC(LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }

    public static void recallAllPokemon(PlayerPartyStorage storage) {
        storage.getTeam().forEach(pokemon -> {
            EntityPixelmon entity = pokemon.getPixelmonIfExists();
            if (entity != null) {
                entity.unloadEntity();
            }
        });
    }

    @SuppressWarnings("unchecked")
    public static LinkedHashMap<ItemStack, Pokemon>[] generatePCMaps(Side side) {
        PCStorage pcStorage = Pixelmon.storageManager.getPCForPlayer(side.getUser().get().getUniqueId());
        List<Pokemon> pcPokemon = getAllPokemon(pcStorage);
        PlayerPartyStorage partyStorage = Pixelmon.storageManager.getParty(side.getUser().get().getUniqueId());
        List<Pokemon> partyPokemon = getAllPokemon(partyStorage);

        LinkedHashMap<ItemStack, Pokemon> partyMap = new LinkedHashMap<>();
        LinkedHashMap<ItemStack, Pokemon> pcMap = new LinkedHashMap<>();

        for (Pokemon pokemon : partyPokemon) {
            partyMap.put(ItemUtils.Pokemon.getPokemonIcon(pokemon), pokemon);
        }
        for (Pokemon pokemon : pcPokemon) {
            pcMap.put(ItemUtils.Pokemon.getPokemonIcon(pokemon), pokemon);
        }

        return new LinkedHashMap[]{partyMap, pcMap};
    }

    public static List<Pokemon> getAllPokemon(PCStorage storage) {
        List<Pokemon> pokemon = new ArrayList<>();
        for (Pokemon p : storage.getAll()) {
            if (p != null) {
                pokemon.add(p);
            }
        }

        return pokemon;
    }

    public static List<Pokemon> getAllPokemon(PlayerPartyStorage storage) {
        List<Pokemon> pokemon = new ArrayList<>();
        for (Pokemon p : storage.getAll()) {
            if (p != null) {
                pokemon.add(p);
            }
        }

        return pokemon;
    }
}
