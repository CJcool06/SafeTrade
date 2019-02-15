package io.github.cjcool06.safetrade.listeners;

import com.pixelmonmod.pixelmon.api.events.EvolveEvent;
import io.github.cjcool06.safetrade.SafeTrade;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.spongepowered.api.Sponge;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EvolutionListener {
    public static List<UUID> ongoingEvolutions = new ArrayList<>();

    @SubscribeEvent
    public void onEvolve(EvolveEvent.PostEvolve evolve) {
        if (ongoingEvolutions.contains(evolve.pokemon.getUniqueID())) {
            Sponge.getScheduler().createTaskBuilder().execute(evolve.pokemon::unloadEntity).delayTicks(100).submit(SafeTrade.getPlugin());
            ongoingEvolutions.remove(evolve.pokemon.getUniqueID());
        }
    }
}
