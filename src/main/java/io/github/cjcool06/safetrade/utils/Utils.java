package io.github.cjcool06.safetrade.utils;

import com.pixelmonmod.pixelmon.api.pokemon.PokemonSpec;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.battles.controller.BattleControllerBase;
import com.pixelmonmod.pixelmon.config.PixelmonEntityList;
import com.pixelmonmod.pixelmon.config.PixelmonItems;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;
import com.pixelmonmod.pixelmon.util.helpers.SpriteHelper;
import io.github.cjcool06.safetrade.SafeTrade;
import io.github.cjcool06.safetrade.api.enquiry.ListingBase;
import io.github.cjcool06.safetrade.config.Config;
import io.github.cjcool06.safetrade.listings.PokemonListing;
import io.github.cjcool06.safetrade.managers.DataManager;
import io.github.cjcool06.safetrade.obj.Trade;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.entity.Hotbar;
import org.spongepowered.api.item.inventory.entity.MainPlayerInventory;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Utils {
    public static Optional<User> getUser(UUID uuid) {
        return Sponge.getServiceManager().provide(UserStorageService.class).get().get(uuid);
    }

    public static EntityPixelmon getPokemonInSlot(Player player, int slot) {
        PlayerStorage storage = PixelmonStorage.pokeBallManager.getPlayerStorage((EntityPlayerMP)player).get();
        if (storage.partyPokemon[slot - 1] != null) {
            return (EntityPixelmon)PixelmonEntityList.createEntityFromNBT(storage.partyPokemon[slot - 1], (World)player.getWorld());
            // Doesn't work
            //return new PokemonSpec().readFromNBT(storage.partyPokemon[slot - 1]).create((World)player.getWorld());
        }

        return null;
    }

    public static ItemStack getPicture(EntityPixelmon pokemon) {
        net.minecraft.item.ItemStack item = new net.minecraft.item.ItemStack(PixelmonItems.itemPixelmonSprite);
        NBTTagCompound nbt = new NBTTagCompound();
        String idValue = String.format("%03d", pokemon.baseStats.nationalPokedexNumber);
        if (pokemon.isEgg) {
            switch(pokemon.getSpecies()) {
                case Manaphy:
                case Togepi:
                    nbt.setString("SpriteName", String.format("pixelmon:sprites/eggs/%s1", pokemon.getSpecies().name.toLowerCase()));
                    break;
                default:
                    nbt.setString("SpriteName", "pixelmon:sprites/eggs/egg1");
            }
        } else if (pokemon.getIsShiny()) {
            nbt.setString("SpriteName", "pixelmon:sprites/shinypokemon/" + idValue + SpriteHelper.getSpriteExtra(pokemon.getSpecies().name, pokemon.getForm()));
        } else {
            nbt.setString("SpriteName", "pixelmon:sprites/pokemon/" + idValue + SpriteHelper.getSpriteExtra(pokemon.getSpecies().name, pokemon.getForm()));
        }
        item.setTagCompound(nbt);
        return (ItemStack)(Object)item;
    }

    public static Text[] getTradeOverviewLore(Trade trade) {
        Text.Builder builder1 =  Text.builder();
        Text.Builder builder2 = Text.builder();

        builder1.append(Text.of("Money:"))
                .color(TextColors.DARK_AQUA)
                .append(Text.builder().append(Text.of(TextColors.AQUA, " ", trade.money.get(trade.participants[0]))).build())
                .build();
        builder2.append(Text.of("Money:"))
                .color(TextColors.DARK_AQUA)
                .append(Text.builder().append(Text.of(TextColors.AQUA, " ", trade.money.get(trade.participants[1]))).build())
                .build();

        builder1.append(Text.of("\n" + "Pokemon:"))
                .color(TextColors.DARK_AQUA)
                .build();
        builder2.append(Text.of("\n" + "Pokemon:"))
                .color(TextColors.DARK_AQUA)
                .build();
        for (EntityPixelmon pixelmon : trade.listedPokemon.get(trade.participants[0]).values()) {
            builder1.append(Text.builder().append(Text.of(TextColors.AQUA, "\n" + pixelmon.getName() + (pixelmon.isEgg && !Config.showEggStats ? " Egg" : ""))).build()).build();
        }
        for (EntityPixelmon pixelmon : trade.listedPokemon.get(trade.participants[1]).values()) {
            builder2.append(Text.builder().append(Text.of(TextColors.AQUA, "\n" + pixelmon.getName() + (pixelmon.isEgg && !Config.showEggStats ? " Egg" : ""))).build()).build();
        }

        builder1.append(Text.of("\n" + "Items:"))
                .color(TextColors.DARK_AQUA)
                .build();
        builder2.append(Text.of("\n" + "Items:"))
                .color(TextColors.DARK_AQUA)
                .build();
        for (ItemStackSnapshot snapshot : trade.getItems(trade.participants[0])) {
            builder1.append(Text.builder().append(Text.of("\n", TextColors.GREEN, snapshot.getQuantity() + "x ", TextColors.AQUA, snapshot.getTranslation().get()))
                    .build()).build();
            if (snapshot.get(Keys.DISPLAY_NAME).isPresent()) {
                builder1.append(Text.builder().append(Text.of("  ", TextColors.GOLD, "[", snapshot.get(Keys.DISPLAY_NAME).get(), TextColors.GOLD, "]")).build()).build();
            }
        }
        for (ItemStackSnapshot snapshot : trade.getItems(trade.participants[1])) {
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
                .append(Text.builder().append(Text.of(TextColors.DARK_AQUA, trade.participants[0].getName()))
                        .onHover(TextActions.showText(texts[0]))
                        .build())
                .append(Text.builder().append(Text.of(TextColors.DARK_AQUA, " & "))
                        .build())
                .append(Text.builder().append(Text.of(TextColors.DARK_AQUA, trade.participants[1].getName()))
                        .onHover(TextActions.showText(texts[1]))
                        .build())
                .build();
    }

    public static ArrayList<Text> getPokemonLore(EntityPixelmon pokemon) {
        ArrayList<Text> lore = new ArrayList<>();
        if (pokemon.isEgg && !Config.showEggStats) {
            lore.add(Text.of(TextColors.GRAY, "The stats of this egg are a mystery."));
            return lore;
        }
        DecimalFormat df = new DecimalFormat("#0.##");
        int ivSum = pokemon.stats.ivs.HP + pokemon.stats.ivs.Attack + pokemon.stats.ivs.Defence + pokemon.stats.ivs.SpAtt + pokemon.stats.ivs.SpDef + pokemon.stats.ivs.Speed;
        int evSum = pokemon.stats.evs.hp + pokemon.stats.evs.attack + pokemon.stats.evs.defence + pokemon.stats.evs.specialAttack + pokemon.stats.evs.specialDefence + pokemon.stats.evs.speed;
        // Stats
        //String star = "\u2605";
        String nickname = pokemon.getNickname().equals("") ? pokemon.getName() : pokemon.getNickname();
        //String shiny = pokemon.getIsShiny() ? star : "";
        String shiny = pokemon.getIsShiny() ? "Yes" : "No";
        int level = pokemon.getLvl().getLevel();
        String nature = pokemon.getNature().getLocalizedName();
        String growth = pokemon.getGrowth().getLocalizedName();
        String ability = pokemon.getAbility().getLocalizedName();
        String originalTrainer = pokemon.originalTrainer;
        String heldItem = "";
        if(!pokemon.getItemHeld().getLocalizedName().contains(".name")) {
            heldItem += pokemon.getItemHeld().getLocalizedName();
        }
        else {
            heldItem += "None";
        }
        String breedable = new PokemonSpec("unbreedable").matches(pokemon) ? "No" : "Yes";
        // EVs
        int hpEV = pokemon.stats.evs.hp;
        int attackEV = pokemon.stats.evs.attack;
        int defenceEV = pokemon.stats.evs.defence;
        int spAttkEV = pokemon.stats.evs.specialAttack;
        int spDefEV = pokemon.stats.evs.specialDefence;
        int speedEV = pokemon.stats.evs.speed;
        String totalEVs = df.format((long)((int)((double)evSum / 510.0D * 100.0D))) + "%";
        // IVs
        int hpIV = pokemon.stats.ivs.HP;
        int attackIV = pokemon.stats.ivs.Attack;
        int defenceIV = pokemon.stats.ivs.Defence;
        int spAttkIV = pokemon.stats.ivs.SpAtt;
        int spDefIV = pokemon.stats.ivs.SpDef;
        int speedIV = pokemon.stats.ivs.Speed;
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

    public static boolean giveItem(User user, ItemStack item) {
        Inventory hotbar = user.getInventory().query(QueryOperationTypes.INVENTORY_TYPE.of(Hotbar.class));
        InventoryTransactionResult hotbarResult =  hotbar.offer(item);

        if (hotbarResult.getType() != InventoryTransactionResult.Type.SUCCESS) {
            Inventory main = user.getInventory().query(QueryOperationTypes.INVENTORY_TYPE.of(MainPlayerInventory.class));
            InventoryTransactionResult mainResult =  main.offer(item);

            return mainResult.getType() == InventoryTransactionResult.Type.SUCCESS;
        }

        return true;
    }

    // If items are unsuccessful they get added to storage
    public static void giveOrStoreSnapshot(User user, ItemStackSnapshot snapshot, boolean sendInfoMessage) {
        if (!giveItem(user, snapshot.createStack())) {
            Sponge.getScheduler().createTaskBuilder().execute(() -> DataManager.storeItem(user, snapshot)).async().submit(SafeTrade.getPlugin());
            if (sendInfoMessage) {
                user.getPlayer().get().sendMessage(Text.of(TextColors.RED, "SafeTrade wasn't able to place an item in to your inventory. \n" +
                        "SafeTrade has stored your item and awaits your relog to obtain it."));
            }
        }
    }

    // If items are unsuccessful they get added to storage
    public static void giveOrStoreSnapshots(User user, List<ItemStackSnapshot> snapshots, boolean sendInfoMessage) {
        int unsuccessfulCount = 0;
        for (ItemStackSnapshot snapshot : snapshots) {
            if (!user.isOnline()) {
                Sponge.getScheduler().createTaskBuilder().execute(() -> DataManager.storeItem(user, snapshot)).async().submit(SafeTrade.getPlugin());
                continue;
            }

            Inventory hotbar = user.getInventory().query(QueryOperationTypes.INVENTORY_TYPE.of(Hotbar.class));
            InventoryTransactionResult hotbarResult =  hotbar.offer(snapshot.createStack());

            if (hotbarResult.getType() != InventoryTransactionResult.Type.SUCCESS) {
                Inventory main = user.getInventory().query(QueryOperationTypes.INVENTORY_TYPE.of(MainPlayerInventory.class));
                InventoryTransactionResult mainResult =  main.offer(snapshot.createStack());

                if (mainResult.getType() != InventoryTransactionResult.Type.SUCCESS) {
                    unsuccessfulCount++;
                    Sponge.getScheduler().createTaskBuilder().execute(() -> DataManager.storeItem(user, snapshot)).async().submit(SafeTrade.getPlugin());
                }
            }
        }
        if (unsuccessfulCount > 0 && sendInfoMessage && user.isOnline()) {
            user.getPlayer().get().sendMessage(Text.of(TextColors.RED, "SafeTrade wasn't able to place " + unsuccessfulCount + " items in to your inventory. \n" +
                    "SafeTrade has stored your items and awaits your relog to obtain them."));
        }
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

    public static Text createHoverText(Text baseText, Text hoverText) {
        return Text.builder().append(baseText).onHover(TextActions.showText(hoverText)).build();
    }

    public static LocalDateTime convertToUTC(LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }

    public static int getActiveTime(ListingBase listing) {
        // minutes will be negative most of the time as the end date will most likely be sometime in the future.
        int minutes = (int)ChronoUnit.MINUTES.between(listing.getEndDate(), LocalDateTime.now());
        // If minutes is positive, it will still give a correct active time granted the config hasn't changed.
        return listing instanceof PokemonListing ? Config.pokemonListingTime + minutes : Config.itemListingTime + minutes;
    }
}
