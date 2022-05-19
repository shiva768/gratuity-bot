package jp.vcoin.gratuitybot.command.emoji;

import jp.vcoin.gratuitybot.adapter.ReactionAddEventAdapter;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent;
import sx.blah.discord.handle.obj.IGuild;

import java.util.List;
import java.util.Locale;

public interface ReactionCommand<T extends ReactionAddEvent> {

    void execute(ReactionAddEventAdapter event, Locale locale);

    List<Long> getTriggerEmoji(long serverId);

    Long getNotifyChannelId(IGuild guild);
}
