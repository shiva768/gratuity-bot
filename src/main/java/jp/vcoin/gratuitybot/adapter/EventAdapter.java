package jp.vcoin.gratuitybot.adapter;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEvent;
import sx.blah.discord.handle.obj.IGuild;

public interface EventAdapter {

    long getChannelId();

    long getGuildId();

    IGuild getGuild();

    default void sendMessage(Long discordEventEmojiNotificationChannelId, String message) {
        getRawEvent().getClient()
                .getChannelByID(discordEventEmojiNotificationChannelId)
                .sendMessage(message);
    }

    MessageEvent getRawEvent();

    default void sendMessage(String message) {
        getRawEvent().getChannel().sendMessage(message);
    }

    default void sendMessage(String s, EmbedObject embedObject) {
        getRawEvent().getChannel().sendMessage(s, embedObject);
    }
}
