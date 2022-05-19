package jp.vcoin.gratuitybot.adapter.impl;

import jp.vcoin.gratuitybot.adapter.ReactionAddEventAdapter;
import lombok.AllArgsConstructor;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

@AllArgsConstructor
public class ReactionAddEventAdapterImpl implements ReactionAddEventAdapter {

    private ReactionAddEvent reactionAddEvent;


    /**
     * イベントを発生させたユーザの取得
     */
    public IUser getEventOccurrenceUser() {
        return reactionAddEvent.getUser();
    }

    /**
     * メッセージを発言したユーザの取得
     */
    public IUser getMessageAuthor() {
        return reactionAddEvent.getAuthor();
    }

    @Override
    public long getChannelId() {
        return reactionAddEvent.getChannel().getLongID();
    }

    @Override
    public long getGuildId() {
        return reactionAddEvent.getGuild().getLongID();
    }

    @Override
    public IGuild getGuild() {
        return reactionAddEvent.getGuild();
    }

    @Override
    public long getEmojiId() {
        return reactionAddEvent.getReaction().getEmoji().getLongID();
    }

    @Override
    public void removeReaction(IUser user) {
        reactionAddEvent.getMessage().removeReaction(user, reactionAddEvent.getReaction());
    }

    @Override
    public MessageEvent getRawEvent() {
        return reactionAddEvent;
    }
}
