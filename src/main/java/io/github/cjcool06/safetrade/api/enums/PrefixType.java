package io.github.cjcool06.safetrade.api.enums;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

public enum PrefixType {
    SAFETRADE(Text.of(TextColors.DARK_AQUA, TextStyles.BOLD, "SafeTrade", TextColors.GOLD, " >> ")),
    STORAGE(Text.of(TextColors.DARK_AQUA, TextStyles.BOLD, "SafeTrade Storage", TextColors.GOLD, " >> ")),
    LOG(Text.of(TextColors.DARK_AQUA, TextStyles.BOLD, "SafeTrade Log", TextColors.GOLD, " >> ")),
    OVERVIEW(Text.of(TextColors.DARK_AQUA, TextStyles.BOLD, "SafeTrade Overview", TextColors.GOLD, " >> ")),
    CONFIG(Text.of(TextColors.DARK_AQUA, TextStyles.BOLD, "SafeTrade Config", TextColors.GOLD, " >> ")),
    NONE(Text.of());

    private Text prefix;

    PrefixType(Text prefix) {
        this.prefix = prefix;
    }

    /**
     * Gets the prefix of the type as a {@link Text}.
     *
     * @return The prefix
     */
    public Text getPrefix() {
        return prefix;
    }
}
