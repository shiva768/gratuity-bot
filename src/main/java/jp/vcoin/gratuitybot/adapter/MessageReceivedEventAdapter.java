package jp.vcoin.gratuitybot.adapter;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IEmoji;
import sx.blah.discord.handle.obj.IUser;

import java.util.List;
import java.util.Set;

public interface MessageReceivedEventAdapter extends EventAdapter {

    MessageEvent getRawEvent();

    String getContent(int index);

    String getCommand();

    IUser getAuthor();

    boolean isNullContent();

    boolean isDM();

    IUser getUserByID(long userId);

    IUser getUserByStringId(String userId);

    IChannel getChannelById(long channelId);

    IChannel getChannelByStringId(String channelId);

    List<IUser> getActiveUsersWithoutAuthor();

    boolean isLinkageBotUser();

    Set<String> getLinkageBotUserCommands();

    void sendPublicMessage(String message);

    boolean isAdmin();

    void setAdmin(boolean admin);

    IEmoji getEmojiByStringId(String emojiId);

    IEmoji getEmojiById(long parseLong);
}
