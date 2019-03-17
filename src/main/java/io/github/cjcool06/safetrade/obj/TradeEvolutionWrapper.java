package io.github.cjcool06.safetrade.obj;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.evolution.types.TradeEvolution;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.enums.heldItems.EnumHeldItems;
import io.github.cjcool06.safetrade.SafeTrade;
import net.minecraft.entity.player.EntityPlayerMP;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * A TradeEvolutionWrapper encapsulates the functionality of Pixelmon's
 * evolution system to suit {@link SafeTrade}.
 */
public class TradeEvolutionWrapper {

    private final Trade trade;
    private final Player player;

    public TradeEvolutionWrapper(Trade trade, Player player) {
        this.trade = trade;
        this.player = player;
    }

    /**
     * Gets whether a {@link Pokemon} can evolve from the {@link Trade}.
     *
     * @param pokemon The Pokemon
     * @return True if can evolve
     */
    public boolean canEvolve(Pokemon pokemon) {
        if (trade.getSide(player.getUniqueId()).isPresent()) {
            Side side = trade.getSide(player.getUniqueId()).get();
            Side otherSide = side.getOtherSide();

            // Iterates through the other side's Pokemon to see if the Pokemon can evolve.
            for (Pokemon p : otherSide.vault.getAllPokemon()) {
                if (testTradeEvolution(pokemon, p.getSpecies())) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Attempts to evolve the {@link Pokemon}.
     *
     * @param pokemon The Pokemon
     * @return True if an evolution is scheduled, false if no evolutions were possible
     */
    public boolean doEvolution(Pokemon pokemon) {
        if (trade.getSide(player.getUniqueId()).isPresent()) {
            Side side = trade.getSide(player.getUniqueId()).get();
            Side otherSide = side.getOtherSide();

            // Iterates through the other side's Pokemon to see if the Pokemon can evolve.
            for (Pokemon p : otherSide.vault.getAllPokemon()) {
                if (testTradeEvolution(pokemon, p.getSpecies())) {
                    // Requires tick delay otherwise the player will become glitched
                    Sponge.getScheduler().createTaskBuilder().execute(() -> {
                        EntityPixelmon pixelmon = pokemon.getOrSpawnPixelmon((EntityPlayerMP)player);
                        pixelmon.testTradeEvolution(p.getSpecies());
                    }).delayTicks(20).submit(SafeTrade.getPlugin());
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Tests a trade evolution.
     *
     * The majority of this function's code was taken from {@link EntityPixelmon}#testTradeEvoltion
     * and slightly modified to suit my function's purpose.
     *
     * @param pokemon The {@link Pokemon} to evolve
     * @param with The {@link EnumSpecies} to test with
     * @return True if the Pokemon can evolve with the species
     */
    private boolean testTradeEvolution(Pokemon pokemon, EnumSpecies with) {
        EntityPixelmon pixelmon = pokemon.getOrSpawnPixelmon((EntityPlayerMP)player);

        if (pixelmon.getPokemonData().getHeldItemAsItemHeld().getHeldItemType() == EnumHeldItems.everStone) {
            pixelmon.unloadEntity();
            return false;
        }
        else {
            ArrayList<TradeEvolution> tradeEvolutions = pixelmon.getPokemonData().getEvolutions(TradeEvolution.class);
            Iterator<TradeEvolution> iter = tradeEvolutions.iterator();

            while (iter.hasNext()) {
                TradeEvolution evo = iter.next();

                if (evo.canEvolve(pixelmon, with)) {
                    pixelmon.unloadEntity();
                    return true;
                }
            }

            pixelmon.unloadEntity();
            return false;
        }
    }
}
