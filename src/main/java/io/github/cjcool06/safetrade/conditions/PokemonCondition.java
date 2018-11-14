package io.github.cjcool06.safetrade.conditions;

import com.pixelmonmod.pixelmon.enums.EnumType;
import io.github.cjcool06.safetrade.SafeTrade;
import io.github.cjcool06.safetrade.api.condition.Condition;
import io.github.cjcool06.safetrade.api.enquiry.ListingBase;
import io.github.cjcool06.safetrade.listings.PokemonListing;

public class PokemonCondition implements Condition {
    private final PokemonListing conditionListing;

    // iv and ev parameters can take nulls
    public PokemonCondition(PokemonListing pokemonListing) {
        this.conditionListing = pokemonListing;
    }

    // The listing must satisfy all conditions to return true
    public boolean passes(ListingBase listing) {
        if (listing instanceof PokemonListing) {
            PokemonListing pokemonListing = (PokemonListing)listing;
            if (conditionListing.getName() != null) {
                if (pokemonListing.getName() == null || !conditionListing.getName().equalsIgnoreCase(pokemonListing.getName())) {
                    return false;
                }
            }
            if (!conditionListing.getTypes().isEmpty()) {
                for (EnumType type : conditionListing.getTypes()) {
                    if (!pokemonListing.getTypes().contains(type)) {
                        return false;
                    }
                }
            }
            if (conditionListing.isShiny() != null) {
                if (pokemonListing.isShiny() == null || !conditionListing.isShiny().equals(pokemonListing.isShiny())) {
                    return false;
                }
            }
            if (conditionListing.getLevel() != null) {
                Character levelOperator = pokemonListing.getLevelOperator();
                if (pokemonListing.getLevel() == null) {
                    return false;
                }
                if (levelOperator == null) {
                    if (!conditionListing.getLevel().equals(pokemonListing.getLevel())) {
                        return false;
                    }
                }
                else if (levelOperator == '>'){
                    if (pokemonListing.getLevel() > conditionListing.getLevel()) {
                        return false;
                    }
                }
                else if (levelOperator == '<') {
                    if (pokemonListing.getLevel() < conditionListing.getLevel()) {
                        return false;
                    }
                }
                else {
                    SafeTrade.getLogger().warn("A pokemon condition has caught a stray level operator, '", levelOperator, "', whilst parsing listing " + listing.getUniqueID() +
                            " associated with user " + listing.getUser().getName());
                    return false;
                }
            }
            if (conditionListing.getNature() != null) {
                if (pokemonListing.getNature() == null || !conditionListing.getNature().equals(pokemonListing.getNature())) {
                    return false;
                }
            }
            if (conditionListing.getGrowth() != null) {
                if (pokemonListing.getGrowth() == null || !conditionListing.getGrowth().equals(pokemonListing.getGrowth())) {
                    return false;
                }
            }
            if (conditionListing.getAbility() != null) {
                if (pokemonListing.getAbility() == null || !conditionListing.getAbility().getName().equalsIgnoreCase(pokemonListing.getAbility().getName())) {
                    return false;
                }
            }
            if (conditionListing.getIVPercentage() != null) {
                Character ivOperator = pokemonListing.getIVOperator();
                if (pokemonListing.getIVPercentage() == null) {
                    return false;
                }
                if (ivOperator == null) {
                    if (!conditionListing.getIVPercentage().equals(pokemonListing.getIVPercentage())) {
                        return false;
                    }
                }
                else if (ivOperator == '>'){
                    if (pokemonListing.getIVPercentage() > conditionListing.getIVPercentage()) {
                        return false;
                    }
                }
                else if (ivOperator == '<') {
                    if (pokemonListing.getIVPercentage() < conditionListing.getIVPercentage()) {
                        return false;
                    }
                }
                else {
                    SafeTrade.getLogger().warn("A pokemon condition has caught a stray iv-percentage operator, '", ivOperator, "', whilst parsing listing " + listing.getUniqueID() +
                            " associated with user " + listing.getUser().getName());
                    return false;
                }
            }
            if (conditionListing.getEVPercentage() != null) {
                Character evOperator = pokemonListing.getEVOperator();
                if (pokemonListing.getEVPercentage() == null) {
                    return false;
                }
                if (evOperator == null) {
                    if (!conditionListing.getEVPercentage().equals(pokemonListing.getEVPercentage())) {
                        return false;
                    }
                }
                else if (evOperator == '>'){
                    if (pokemonListing.getEVOperator() > conditionListing.getEVPercentage()) {
                        return false;
                    }
                }
                else if (evOperator == '<') {
                    if (pokemonListing.getEVOperator() < conditionListing.getEVPercentage()) {
                        return false;
                    }
                }
                else {
                    SafeTrade.getLogger().warn("A pokemon condition has caught a stray ev-percentage operator, '", evOperator, "', whilst parsing listing " + listing.getUniqueID() +
                            " associated with user " + listing.getUser().getName());
                    return false;
                }
            }
        }
        else {
            return false;
        }

        return true;
    }
}
