package jp.vcoin.gratuitybot.util;

import jp.vcoin.gratuitybot.domain.DiscordObject;
import jp.vcoin.gratuitybot.exception.DefaultNotifyChannelNotFoundException;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiscordUtil {

    public static DiscordObject convertDiscordObject(String id, Class<? extends DiscordObject> discordObjectClass) throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        Constructor<? extends DiscordObject> constructor = discordObjectClass.getDeclaredConstructor(String.class);
        return constructor.newInstance(id);
    }

    public static String convertUserId(String userId) {
        if (userId == null || !(userId.matches("<@!?[0-9]+>"))) return null;

        Pattern p = Pattern.compile("<@!?([0-9]+)>");
        return getString(userId, p);
    }

    public static String convertEmojiId(String emojiId) {
        if (emojiId == null || !(emojiId.matches("<:.+:[0-9]+>"))) return null;

        Pattern p = Pattern.compile("<:.+:([0-9]+)>");
        return getString(emojiId, p);
    }

    public static String convertChannelId(String channelId) {
        if (channelId == null || !(channelId.matches("<#[0-9]+>"))) return null;

        Pattern p = Pattern.compile("<#([0-9]+)>");
        return getString(channelId, p);
    }

    private static String getString(String userId, Pattern p) {
        Matcher m = p.matcher(userId);
        String id = null;

        while (m.find()) {
            id = m.group(1);
        }

        return id;
    }

    public static IChannel getDefaultChannel(IGuild guild) throws DefaultNotifyChannelNotFoundException {
        List<IChannel> channels = guild.getChannels();
        if (!channels.isEmpty()) {
            IChannel channel = channels.get(0);
            if (channel.getShard().isReady())
                return channel;
        }
        throw new DefaultNotifyChannelNotFoundException(String.format("not found. guild:%s", guild.getName()));
    }
}
