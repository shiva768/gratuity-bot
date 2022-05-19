package jp.vcoin.gratuitybot.command.text;

import jp.vcoin.gratuitybot.adapter.MessageReceivedEventAdapter;
import sx.blah.discord.util.EmbedBuilder;

import java.util.Locale;

public interface ShowDynamicSettingCommand {
    void buildContent(MessageReceivedEventAdapter eventAdapter, Locale locale, EmbedBuilder builder);
}
