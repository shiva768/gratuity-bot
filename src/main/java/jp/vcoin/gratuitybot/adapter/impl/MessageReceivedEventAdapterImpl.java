package jp.vcoin.gratuitybot.adapter.impl;

import jp.vcoin.gratuitybot.domain.LinkageBotUser;
import jp.vcoin.gratuitybot.adapter.MessageReceivedEventAdapter;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.*;

import java.util.*;
import java.util.stream.Collectors;

public class MessageReceivedEventAdapterImpl implements MessageReceivedEventAdapter {

    private final MessageReceivedEvent messageReceivedEvent;
    @Getter
    private final String[] contents;
    private LinkageBotUser linkageBotUser;
    @Getter
    private boolean admin;

    public MessageReceivedEventAdapterImpl(MessageReceivedEvent messageReceivedEvent, int discordEventCommandMaxCount, List<LinkageBotUser> discordEventTextEnableLinkageBotUsers) {
        this.messageReceivedEvent = messageReceivedEvent;
        this.contents = messageReceivedEvent.getMessage().getContent().trim().split("\\s+", discordEventCommandMaxCount);
        Optional.ofNullable(discordEventTextEnableLinkageBotUsers).ifPresent(users -> users.stream().filter(u -> u.getId() == messageReceivedEvent.getAuthor().getLongID()).findFirst().ifPresent(u ->
                this.linkageBotUser = u
        ));
    }

    @Override
    public long getChannelId() {
        return messageReceivedEvent.getChannel().getLongID();
    }

    @Override
    public long getGuildId() {
        return Optional.ofNullable(messageReceivedEvent.getGuild()).map(IIDLinkedObject::getLongID).orElse(-1L);
    }

    @Override
    public IGuild getGuild() {
        return messageReceivedEvent.getGuild();
    }

    @Override
    public MessageEvent getRawEvent() {
        return messageReceivedEvent;
    }

    @Override
    public String getContent(int index) {
        if (this.contents.length > index + 1)
            return contents[index + 1];
        return null;
    }

    @Override
    public String getCommand() {
        return this.contents[0];
    }

    @Override
    public IUser getAuthor() {
        return messageReceivedEvent.getAuthor();
    }

    @Override
    public boolean isNullContent() {
        return messageReceivedEvent.getMessage().getContent() == null;
    }

    @Override
    public boolean isDM() {
        return messageReceivedEvent.getChannel().equals(getAuthor().getOrCreatePMChannel());
    }

    @Override
    public IUser getUserByID(long userId) {
        return messageReceivedEvent.getGuild().getUserByID(userId);
    }

    @Override
    public IUser getUserByStringId(String userId) {
        if (!StringUtils.isNumeric(userId)) return null;
        return getUserByID(Long.parseLong(userId));
    }

    @Override
    public IChannel getChannelById(long channelId) {
        return messageReceivedEvent.getGuild().getChannelByID(channelId);
    }

    @Override
    public IChannel getChannelByStringId(String channelId) {
        if (!StringUtils.isNumeric(channelId)) return null;
        return getChannelById(Long.parseLong(channelId));
    }

    @Override
    public List<IUser> getActiveUsersWithoutAuthor() {
        return messageReceivedEvent.getGuild().getUsers().stream().filter(
                u -> !u.isBot() && !getAuthor().getStringID().equals(u.getStringID()) && u.getPresence().getStatus() == StatusType.ONLINE
        ).collect(Collectors.toList());
    }

    @Override
    public boolean isLinkageBotUser() {
        return this.linkageBotUser != null;
    }

    @Override
    public Set<String> getLinkageBotUserCommands() {
        return new HashSet<>(Optional.ofNullable(this.linkageBotUser.getCommands()).orElse(new ArrayList<>()));
    }

    @Override
    public void sendPublicMessage(String message) {
        if (linkageBotUser != null && linkageBotUser.getPublicMessageChannelId() != null) {
            IChannel channel = getRawEvent().getGuild().getChannelByID(linkageBotUser.getPublicMessageChannelId());
            channel.sendMessage(message);
            return;
        }
        sendMessage(message);
    }

    @Override
    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    @Override
    public IEmoji getEmojiByStringId(String emojiId) {
        if (emojiId == null) return null;
        return getEmojiById(Long.parseLong(emojiId));
    }

    @Override
    public IEmoji getEmojiById(long emojiId) {
        return messageReceivedEvent.getGuild().getEmojiByID(emojiId);
    }
}
