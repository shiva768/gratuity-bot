package jp.vcoin.gratuitybot.adapter;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEvent;
import sx.blah.discord.handle.obj.IUser;

public interface ReactionAddEventAdapter extends EventAdapter {

    IUser getEventOccurrenceUser();

    IUser getMessageAuthor();

    long getChannelId();

    long getEmojiId();

    void removeReaction(IUser user);

    MessageEvent getRawEvent();
}
