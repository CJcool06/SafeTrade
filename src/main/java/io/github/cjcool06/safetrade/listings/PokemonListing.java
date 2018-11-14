package io.github.cjcool06.safetrade.listings;

import com.pixelmonmod.pixelmon.api.pokemon.PokemonSpec;
import com.pixelmonmod.pixelmon.entities.pixelmon.abilities.AbilityBase;
import com.pixelmonmod.pixelmon.enums.EnumGrowth;
import com.pixelmonmod.pixelmon.enums.EnumNature;
import com.pixelmonmod.pixelmon.enums.EnumPokemon;
import com.pixelmonmod.pixelmon.enums.EnumType;
import io.github.cjcool06.safetrade.api.enquiry.ListingBase;
import io.github.cjcool06.safetrade.config.Config;
import io.github.cjcool06.safetrade.utils.GsonUtils;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.time.LocalDateTime;
import java.util.*;

public class PokemonListing extends ListingBase {
    // TODO: Use PokemonSpec to store these values
    // TODO: Have to change how toContainer and fromContainer handles it
    // TODO: Most likely map.put("nbt", string nbt)
    private PokemonSpec spec;
    private ArrayList<EnumType> types = new ArrayList<>();
    private Integer ivPercentage = null;
    private Integer evPercentage = null;

    // Allows players to specify if they are looking for something with at-least or at-most of a trait
    private Character levelOperator = null;
    private Character ivOperator = null;
    private Character evOperator = null;

    public PokemonListing(User user) {
        this(user, LocalDateTime.now().plusMinutes(Config.pokemonListingTime), UUID.randomUUID());
    }

    public PokemonListing(User user, LocalDateTime endDate, UUID uniqueID) {
        super(user, endDate, uniqueID);
        spec = new PokemonSpec();
    }

    public String getName() {
        return spec.name;
    }

    public ArrayList<EnumType> getTypes() {
        return types;
    }

    public Boolean isShiny() {
        return spec.shiny;
    }

    public Integer getLevel() {
        return spec.level;
    }

    public EnumNature getNature() {
        return spec.nature != null ? EnumNature.getNatureFromIndex(spec.nature) : null;
    }

    public EnumGrowth getGrowth() {
        return spec.growth != null ? EnumGrowth.getGrowthFromIndex(spec.growth) : null;
    }

    public AbilityBase getAbility() {
        return spec.ability != null ? AbilityBase.getAbility(spec.ability).get() : null;
    }

    public Integer getIVPercentage() {
        return ivPercentage;
    }

    public Integer getEVPercentage() {
        return evPercentage;
    }

    public Character getLevelOperator() {
        return levelOperator;
    }

    public Character getIVOperator() {
        return ivOperator;
    }

    public Character getEVOperator() {
        return evOperator;
    }

    public void setSpec(PokemonSpec spec) {
        this.spec = spec;
    }

    public void setPokemon(EnumPokemon pokemon) {
        if (pokemon == null) {
            spec.name = null;
            return;
        }
        spec.name = pokemon.name();
    }

    public void setShiny(Boolean shiny) {
        spec.shiny = shiny;
    }

    public void setLevel(Integer level) {
        spec.level = level;
    }

    public void setNature(EnumNature nature) {
        if (nature == null) {
            spec.nature = null;
            return;
        }
        spec.nature = (byte)nature.index;
    }

    public void setGrowth(EnumGrowth growth) {
        if (growth == null) {
            spec.growth = null;
            return;
        }
        spec.growth = (byte)growth.index;
    }

    public void setAbility(AbilityBase ability) {
        if (ability == null) {
            spec.ability = null;
            return;
        }
        spec.ability = ability.getName();
    }

    public void setIVPercentage(Integer ivPercentage) {
        this.ivPercentage = ivPercentage;
    }

    public void setEVPercentage(Integer evPercentage) {
        this.evPercentage = evPercentage;
    }

    public void setLevelOperator(Character levelOperator) {
        this.levelOperator = levelOperator;
    }

    public void setIVOperator(Character operator) {
        this.ivOperator = operator;
    }

    public void setEVOperator(Character operator) {
        this.evOperator = operator;
    }

    public Text getTypeText() {
        return Text.of(TextColors.GOLD, "Pokemon Listing");
    }

    public List<Text> getDisplayLore() {
        ArrayList<Text> texts = new ArrayList<>();
        String type = "";
        if (getTypes().isEmpty()) {
            type = "Any";
        }
        else {
            Iterator<EnumType> iter = types.iterator();
            while (iter.hasNext()) {
                EnumType t = iter.next();
                type += t.getLocalizedName() + (iter.hasNext() ? ", " : "");
            }
        }
        String pokemon = getName() != null ? getName() : "Any";
        String shiny = isShiny() != null ? (isShiny() ? "Yes" : "No") : "Any";
        String level = getLevel() != null ? getLevel() + "" : "Any";
        String nature = getNature() != null ? getNature().getLocalizedName() : "Any";
        String growth = getGrowth() != null ? getGrowth().getLocalizedName() : "Any";
        String ability = getAbility() != null ? getAbility().getLocalizedName() : "Any";
        String ivs = (getIVOperator() != null ? getIVOperator().toString() : "") + (getIVPercentage() != null ? getIVPercentage() + "" : "Any") + "%";
        String evs = (getEVOperator() != null ? getEVOperator().toString() : "") + (getEVPercentage() != null ? getEVPercentage() + "" : "Any") + "%";

        if (Config.cleanListings) {
            if (getName() != null) {
                texts.add(Text.of(TextColors.DARK_AQUA, "Pokemon: ", TextColors.AQUA, pokemon));
            }
            if (!getTypes().isEmpty() && getName() == null) {
                texts.add(Text.of(TextColors.DARK_AQUA, "Type: ", TextColors.AQUA, type));
            }
            if (isShiny() != null) {
                texts.add(Text.of(TextColors.DARK_AQUA, "Shiny: ", TextColors.AQUA, shiny));
            }
            if (getLevel() != null) {
                texts.add(Text.of(TextColors.DARK_AQUA, "Level: ", TextColors.AQUA, level));
            }
            if (getNature() != null) {
                texts.add(Text.of(TextColors.DARK_AQUA, "Nature: ", TextColors.AQUA, nature));
            }
            if (getGrowth() != null) {
                texts.add(Text.of(TextColors.DARK_AQUA, "Growth: ", TextColors.AQUA, growth));
            }
            if (getAbility() != null) {
                texts.add(Text.of(TextColors.DARK_AQUA, "Ability: ", TextColors.AQUA, ability));
            }
            if (getIVPercentage() != null) {
                texts.add(Text.of(TextColors.DARK_AQUA, "IVs: ", TextColors.AQUA, ivs));
            }
            if (getEVPercentage() != null) {
                texts.add(Text.of(TextColors.DARK_AQUA, "EVs: ", TextColors.AQUA, evs));
            }
        }
        else {
            texts.add(Text.of(TextColors.DARK_AQUA, "Pokemon: ", TextColors.AQUA, pokemon));
            texts.add(Text.of(TextColors.DARK_AQUA, "Type: ", TextColors.AQUA, type));
            texts.add(Text.of(TextColors.DARK_AQUA, "Shiny: ", TextColors.AQUA, shiny));
            texts.add(Text.of(TextColors.DARK_AQUA, "Level: ", TextColors.AQUA, level));
            texts.add(Text.of(TextColors.DARK_AQUA, "Nature: ", TextColors.AQUA, nature));
            texts.add(Text.of(TextColors.DARK_AQUA, "Growth: ", TextColors.AQUA, growth));
            texts.add(Text.of(TextColors.DARK_AQUA, "Ability: ", TextColors.AQUA, ability));
            texts.add(Text.of(TextColors.DARK_AQUA, "IVs: ", TextColors.AQUA, ivs));
            texts.add(Text.of(TextColors.DARK_AQUA, "EVs: ", TextColors.AQUA, evs));
        }
        texts.add(Text.of());
        texts.add(getTypeText());

        return texts;
    }

    @Override
    public boolean isEmpty() {
        return spec.name == null && spec.shiny == null && spec.level == null && spec.nature == null && spec.growth == null && spec.ability == null && types.isEmpty() && ivPercentage == null && evPercentage == null;
    }

    @Override
    public Map<String, String> toContainer() {
        Map<String, String> map = super.toContainer();
        map.put("nbt", GsonUtils.serialize(spec.writeToNBT(new NBTTagCompound())));
        String types = "";
        if (getTypes() != null) {
            Iterator<EnumType> iter = getTypes().iterator();
            while (iter.hasNext()) {
                EnumType type = iter.next();
                types += type.getName() + (iter.hasNext() ? "," : "");
            }
        }
        map.put("pokemon-types", !types.equals("") ? types : "null");
        map.put("iv-percentage", getIVPercentage() != null ? String.valueOf(getIVPercentage()) : "null");
        map.put("ev-percentage", getEVPercentage() != null ? String.valueOf(getEVPercentage()) : "null");
        map.put("level-operator", getLevelOperator() != null ? String.valueOf(getLevelOperator()) : "null");
        map.put("iv-operator", getIVOperator() != null ? String.valueOf(getIVOperator()) : "null");
        map.put("ev-operator", getEVOperator() != null ? String.valueOf(getEVOperator()) : "null");

        return map;
    }

    @Override
    public void fromContainer(Map<String, String> map) {
        spec = new PokemonSpec().readFromNBT(GsonUtils.deserialize(map.get("nbt")));
        String pokemonTypes = map.get("pokemon-types");
        List<String> types = new ArrayList<>();
        if (!pokemonTypes.equals("null")) {
            for (String str : pokemonTypes.split(",")) {
                types.add(str);
            }
        }
        String ivPercentage = map.get("iv-percentage");
        String evPercentage = map.get("ev-percentage");
        String levelOperator = map.get("level-operator");
        String ivOperator = map.get("iv-operator");
        String evOperator = map.get("ev-operator");

        for (String str : types) {
            EnumType type = EnumType.parseType(str);
            if (type != null) {
                this.types.add(type);
            }
        }
        if (!ivPercentage.equalsIgnoreCase("null")) {
            setIVPercentage(Integer.parseInt(ivPercentage));
        }
        if (!evPercentage.equalsIgnoreCase("null")) {
            setEVPercentage(Integer.parseInt(evPercentage));
        }
        if (!levelOperator.equalsIgnoreCase("null")) {
            setLevelOperator(levelOperator.charAt(0));
        }
        if (!ivOperator.equalsIgnoreCase("null")) {
            setIVOperator(ivOperator.charAt(0));
        }
        if (!evOperator.equalsIgnoreCase("null")) {
            setEVOperator(evOperator.charAt(0));
        }
    }
}
