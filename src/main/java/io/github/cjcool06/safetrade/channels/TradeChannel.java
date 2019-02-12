package io.github.cjcool06.safetrade.channels;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.channel.MutableMessageChannel;
import org.spongepowered.api.text.chat.ChatType;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * A TradeChannel represents the chat between the two {@link io.github.cjcool06.safetrade.obj.Side}s of a {@link io.github.cjcool06.safetrade.obj.Trade}.
 */
public class TradeChannel implements MutableMessageChannel {
    List<MessageReceiver> members = new ArrayList<>();

    @Override
    public boolean addMember(MessageReceiver member) {
        return members.add(member);
    }

    @Override
    public boolean removeMember(MessageReceiver member) {
        return members.remove(member);
    }

    @Override
    public void clearMembers() {
        members.clear();
    }

    @Override
    public Optional<Text> transformMessage(@Nullable Object sender, MessageReceiver recipient, Text original, ChatType type) {
        if (sender instanceof CommandSource) {
            return Optional.of(Text.of(TextColors.AQUA, TextStyles.BOLD, "[Trade] ", TextStyles.RESET, TextColors.RESET, original));
        }
        else {
            return Optional.of(Text.of(TextColors.AQUA, TextStyles.BOLD, "[Trade] ", TextStyles.RESET, TextColors.RESET, original));
        }
    }

    @Override
    public Collection<MessageReceiver> getMembers(){
            return new ArrayList<>(members);
    }
}
