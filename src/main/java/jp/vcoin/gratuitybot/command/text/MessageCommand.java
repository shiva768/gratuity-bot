package jp.vcoin.gratuitybot.command.text;

import jp.vcoin.gratuitybot.adapter.MessageReceivedEventAdapter;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEvent;

import java.util.Locale;

public interface MessageCommand<T extends MessageEvent> {

    void execute(MessageReceivedEventAdapter eventAdapter, Locale locale);
}
