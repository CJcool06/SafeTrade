package io.github.cjcool06.safetrade.obj;

import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.enums.ReceiveType;
import com.pixelmonmod.pixelmon.api.events.EvolveEvent;
import com.pixelmonmod.pixelmon.api.events.PixelmonReceivedEvent;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonSpec;
import com.pixelmonmod.pixelmon.api.storage.PokemonStorage;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import com.pixelmonmod.pixelmon.battles.attacks.AttackBase;
import com.pixelmonmod.pixelmon.battles.status.StatusPersist;
import com.pixelmonmod.pixelmon.comm.ChatHandler;
import com.pixelmonmod.pixelmon.comm.EnumUpdateType;
import com.pixelmonmod.pixelmon.comm.packetHandlers.OpenReplaceMoveScreen;
import com.pixelmonmod.pixelmon.config.PixelmonEntityList;
import com.pixelmonmod.pixelmon.config.PixelmonItemsPokeballs;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.BaseStats;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Moveset;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.evolution.types.TradeEvolution;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.enums.heldItems.EnumHeldItems;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import io.github.cjcool06.safetrade.SafeTrade;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import java.util.*;

/**
 * A TradeEvolutionWrapper encapsulates the functionality of Pixelmon's
 * evolution system to suit {@link SafeTrade}.
 *
 * This class is meant for checking and handling possible trade evolutions BEFORE
 * the Pokemon are given.
 */
public class TradeEvolutionWrapper {

    private final Trade trade;

    public TradeEvolutionWrapper(Trade trade) {
        this.trade = trade;
    }

    /**
     * Executes all possible trade evolutions in the {@link Trade}.
     *
     * If the a side is offline the Pokemon they hold cannot evolve.
     * This is due to me wanting to fire Pixelmon's evolution and received event for better
     * API support, and that requires an {@link EntityPlayerMP}.
     *
     * @return The result
     */
    public TradeEvolutionWrapper.Result doEvolutions() {
        Map<Side, Map<Pokemon, Pokemon>> successes = new HashMap<>();

        for (Side side : trade.getSides()) {
            successes.put(side, new HashMap<>());

            for (Pokemon pokemon : getPossibleEvolutions(side)) {
                // The player will always be online at this point
                EntityPixelmon pixelmon = pokemon.getOrSpawnPixelmon((EntityPlayerMP)side.getPlayer().get());
                Map<Pokemon, Pokemon> map = doEvolution(side, pixelmon);
                pixelmon.unloadEntity();

                if (!map.isEmpty()) {
                    for (Pokemon p : map.keySet()) {
                        successes.get(side).put(p, map.get(p));
                    }
                }
            }
        }

        return new TradeEvolutionWrapper.Result(trade, successes);
    }

    /**
     * Gets all possible {@link TradeEvolution}s for the trade.
     *
     * If a side is offline no evolutions can be calculated for it
     * an empty list will be returned.
     *
     * @return The evolutions
     */
    private List<Pokemon> getPossibleEvolutions(Side side) {
        List<Pokemon> possibleEvolutions = new ArrayList<>();

        if (!side.getPlayer().isPresent()) {
            for (Pokemon pokemon : side.vault.getAllPokemon()) {
                EntityPixelmon pixelmon = pokemon.getOrSpawnPixelmon((EntityPlayerMP) side.getPlayer().get());

                if (canDoEvolution(side, pixelmon)) {
                    possibleEvolutions.add(pokemon);
                }
                pixelmon.unloadEntity();
            }
        }

        return possibleEvolutions;
    }

    /**
     * Gets whether the wrapper is able to trade evolve the {@link EntityPixelmon}.
     *
     * @param pixelmon The Pixelmon
     * @return True if an evolution can take place
     */
    private boolean canDoEvolution(Side side, EntityPixelmon pixelmon) {
        if (side.getPlayer().isPresent()) {
            return getSpeciesToEvolveWith(pixelmon, side.getOtherSide()) != null;
        }

        return false;
    }

    /**
     * Attempts to evolve the {@link EntityPixelmon}.
     *
     * @param pixelmon The Pixelmon
     * @return True if an evolution is scheduled, false if no evolutions were possible
     */
    private Map<Pokemon, Pokemon> doEvolution(Side side, EntityPixelmon pixelmon) {
        Map<Pokemon, Pokemon> map = new HashMap<>();
        EnumSpecies species = getSpeciesToEvolveWith(pixelmon, side.getOtherSide());
        if (species != null) {
            TradeEvolution evolution = getEvolution(pixelmon, species);
            if (evolution != null) {
                EntityPixelmon pre = (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(pixelmon.writeToNBT(new NBTTagCompound()), pixelmon.getEntityWorld());
                pixelmon.evolve(evolution.to);
                this.checkShedinja(pixelmon);
                this.checkForLearnMoves(pixelmon);
                this.checkForEvolutionMoves(pixelmon, evolution);
                evolution.finishedEvolving(pixelmon);
                Pixelmon.EVENT_BUS.post(new EvolveEvent.PostEvolve((EntityPlayerMP) side.getPlayer().get(), pre, evolution, pixelmon));
                if (!pixelmon.getPokemonName().equals(EnumSpecies.Shedinja.name)) {
                    Pixelmon.EVENT_BUS.post(new PixelmonReceivedEvent((EntityPlayerMP) side.getPlayer().get(), ReceiveType.Evolution, pixelmon.getPokemonData()));
                }
                map.put(pre.getPokemonData(), pixelmon.getPokemonData());
                return map;
            }
        }

        return map;
    }

    /**
     * Gets a {@link TradeEvolution} for the {@link EntityPixelmon}, if one is present.
     *
     * The majority of this function's code was taken from EntityPixelmon#testTradeEvoltion
     * and slightly modified to suit my function's purpose.
     *
     * @param pixelmon The {@link EntityPixelmon} to evolve
     * @param with The {@link EnumSpecies} to test with
     * @return The {@link TradeEvolution}, if present
     */
    private TradeEvolution getEvolution(EntityPixelmon pixelmon, EnumSpecies with) {
        if (!(pixelmon.getPokemonData().getHeldItemAsItemHeld().getHeldItemType() == EnumHeldItems.everStone)) {
            ArrayList<TradeEvolution> tradeEvolutions = pixelmon.getPokemonData().getEvolutions(TradeEvolution.class);
            Iterator<TradeEvolution> iter = tradeEvolutions.iterator();

            while (iter.hasNext()) {
                TradeEvolution evo = iter.next();

                if (evo.canEvolve(pixelmon, with)) {
                    return evo;
                }
            }
        }

        return null;
    }

    /**
     * Gets an {@link EnumSpecies} from the other side that can trade evolve with the {@link EntityPixelmon}.
     *
     * @param pixelmonToEvolve The Pixelmon to be evolved
     * @param otherSide The other side
     * @return The species, if present
     */
    private EnumSpecies getSpeciesToEvolveWith(EntityPixelmon pixelmonToEvolve, Side otherSide) {
        for (Pokemon p : otherSide.vault.getAllPokemon()) {
            if (getEvolution(pixelmonToEvolve, p.getSpecies()) != null) {
                return p.getSpecies();
            }
        }

        return null;
    }

    //
    //  Pixelmon code from EvolutionQuery
    //

    private void checkShedinja(EntityPixelmon pixelmon) {
        if (pixelmon.getBaseStats().pokemon == EnumSpecies.Ninjask) {
            PokemonStorage party = pixelmon.getPokemonData().getStorageAndPosition().getFirst();
            if (party.hasSpace()) {
                EntityPlayerMP player = party instanceof PlayerPartyStorage ? ((PlayerPartyStorage)party).getPlayer() : null;
                if (player != null && player.inventory.clearMatchingItems(PixelmonItemsPokeballs.pokeBall, 0, 1, null) == 1) {
                    Pokemon shedinja = Pixelmon.pokemonFactory.create(new PokemonSpec(new String[]{EnumSpecies.Shedinja.name, "lvl:" + pixelmon.getPokemonData().getLevel()}));
                    shedinja.getMoveset().clear();
                    shedinja.getMoveset().addAll(pixelmon.getPokemonData().getMoveset());
                    shedinja.setStatus((StatusPersist)pixelmon.getPokemonData().getStatus().copy());
                    shedinja.setShiny(pixelmon.getPokemonData().isShiny());
                    shedinja.setGrowth(pixelmon.getPokemonData().getGrowth());
                    shedinja.setFriendship(pixelmon.getPokemonData().getFriendship());
                    shedinja.setNature(pixelmon.getPokemonData().getNature());
                    shedinja.setExperience(pixelmon.getPokemonData().getExperience());
                    shedinja.getEVs().fillFromArray(pixelmon.getPokemonData().getEVs().getArray());
                    shedinja.getIVs().CopyIVs(pixelmon.getPokemonData().getIVs());
                    shedinja.setOriginalTrainer(pixelmon.getPokemonData().getOriginalTrainerUUID(), pixelmon.getPokemonData().getOriginalTrainer());
                    party.add(shedinja);
                }
            }
        }
    }

    private void checkForLearnMoves(EntityPixelmon pixelmon) {
        if (pixelmon.getBaseStats() != null) {
            BaseStats baseStats = pixelmon.getBaseStats();
            pixelmon.getBaseStats().id = baseStats.id;
            pixelmon.getBaseStats().baseFormID = baseStats.baseFormID;
            int level = pixelmon.getLvl().getLevel();
            if (level == 1) {
                level = 0;
            }

            if (!pixelmon.getBaseStats().getMovesAtLevel(level).isEmpty()) {
                ArrayList<Attack> newAttacks = pixelmon.getBaseStats().getMovesAtLevel(level);
                Moveset moveset = pixelmon.getPokemonData().getMoveset();
                newAttacks.stream().filter((a) -> {
                    return !moveset.hasAttack(a);
                }).forEach((a) -> {
                    if (moveset.size() >= 4) {
                        Pixelmon.network.sendTo(new OpenReplaceMoveScreen(pixelmon.getOwnerId(), a.baseAttack.attackIndex), (EntityPlayerMP)pixelmon.func_70902_q());
                    } else {
                        moveset.add(a);
                        pixelmon.update(new EnumUpdateType[]{EnumUpdateType.Moveset});
                        if (BattleRegistry.getBattle((EntityPlayer)pixelmon.func_70902_q()) != null) {
                            ChatHandler.sendBattleMessage(pixelmon.func_70902_q(), "pixelmon.stats.learnedmove", new Object[]{pixelmon.getNickname(), a.baseAttack.getTranslatedName()});
                        } else {
                            ChatHandler.sendChat(pixelmon.func_70902_q(), "pixelmon.stats.learnedmove", new Object[]{pixelmon.getNickname(), a.baseAttack.getTranslatedName()});
                        }
                    }
                });
            }
        }
    }

    private void checkForEvolutionMoves(EntityPixelmon pixelmon, TradeEvolution evolution) {
        if (evolution.moves != null && !evolution.moves.isEmpty()) {
            List<AttackBase> evoMoves = new ArrayList();
            Iterator var2 = evolution.moves.iterator();

            while(var2.hasNext()) {
                String moveName = (String)var2.next();
                AttackBase ab = (AttackBase)AttackBase.getAttackBase(moveName).orElse(null);
                if (ab == null) {
                    Pixelmon.LOGGER.error("Unknown move in evolution. To: " + evolution.to.name + ". Move: " + moveName);
                } else {
                    evoMoves.add(ab);
                    if (!pixelmon.relearnableEvolutionMoves.contains(ab.attackIndex)) {
                        pixelmon.relearnableEvolutionMoves.add(ab.attackIndex);
                    }
                }
            }

            pixelmon.update(new EnumUpdateType[]{EnumUpdateType.Moveset});
            var2 = evoMoves.iterator();

            while(var2.hasNext()) {
                AttackBase ab = (AttackBase)var2.next();
                Attack a = new Attack(ab);
                Moveset moveset = pixelmon.getPokemonData().getMoveset();
                if (!moveset.hasAttack(a)) {
                    if (moveset.size() >= 4) {
                        Pixelmon.network.sendTo(new OpenReplaceMoveScreen(pixelmon.getOwnerId(), a.baseAttack.attackIndex), (EntityPlayerMP)pixelmon.func_70902_q());
                    } else {
                        moveset.add(a);
                        pixelmon.update(new EnumUpdateType[]{EnumUpdateType.Moveset});
                        if (BattleRegistry.getBattle((EntityPlayer)pixelmon.func_70902_q()) != null) {
                            ChatHandler.sendBattleMessage(pixelmon.func_70902_q(), "pixelmon.stats.learnedmove", new Object[]{pixelmon.getNickname(), a.baseAttack.getTranslatedName()});
                        } else {
                            ChatHandler.sendChat(pixelmon.func_70902_q(), "pixelmon.stats.learnedmove", new Object[]{pixelmon.getNickname(), a.baseAttack.getTranslatedName()});
                        }
                    }
                }
            }

        }
    }


    /**
     * The result of a {@link TradeEvolutionWrapper}.
     */
    public class Result {
        private final Trade trade;
        // Map<Side, Map<PreEvo, PostEvo>> <-- This is what's in this map
        private final Map<Side, Map<Pokemon, Pokemon>> successes;

        public Result(Trade trade, Map<Side, Map<Pokemon, Pokemon>> successes) {
            this.trade = trade;
            this.successes = successes;
        }

        /**
         * Gets the trade associated to the original {@link TradeEvolutionWrapper}.
         *
         * @return The trade
         */
        public Trade getTrade() {
            return trade;
        }

        /**
         * Gets all successes from the execution of the {@link TradeEvolutionWrapper}.
         *
         * @return The successes
         */
        public Map<Side, Map<Pokemon, Pokemon>> getSuccesses() {
            return Collections.unmodifiableMap(successes);
        }

        /**
         * Gets all successes from the execution of the {@link TradeEvolutionWrapper}
         * by the {@link Side}.
         *
         * @param side The side
         * @return The successes of the side
         */
        public Map<Pokemon, Pokemon> getSuccessesOf(Side side) {
            return successes.getOrDefault(side, new HashMap<>());
        }
    }

    /**
     * Creates a dummy {@link Result}.
     *
     * This dummy has an initialised state, but is empty.
     *
     * @return The dummy result
     */
    public  Result DUMMY() {
        return new Result(trade, new HashMap<>());
    }
}
