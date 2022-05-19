package jp.vcoin.gratuitybot.service;

import jp.vcoin.gratuitybot.adapter.EventAdapter;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.util.Locale;

public interface LocaleService {

    Locale getLocale(EventAdapter eventAdapter);

    Locale getLocale(MessageReceivedEvent event);

    Locale getLocale(long serverId, long channelId);
}
