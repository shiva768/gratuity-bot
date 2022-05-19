package jp.vcoin.gratuitybot.command.text.impl;

import jp.vcoin.gratuitybot.config.ApplicationProperties;
import jp.vcoin.gratuitybot.adapter.MessageReceivedEventAdapter;
import jp.vcoin.gratuitybot.command.text.MessageCommand;
import jp.vcoin.gratuitybot.marker.Text;
import jp.vcoin.gratuitybot.util.MessageSourceWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

import java.util.Locale;

@Component
@Text
public class Help implements MessageCommand<MessageReceivedEvent> {

    private final MessageSourceWrapper messageSourceWrapper;
    private final ApplicationProperties applicationProperties;

    @Autowired
    public Help(MessageSourceWrapper messageSourceWrapper, ApplicationProperties applicationProperties) {
        this.messageSourceWrapper = messageSourceWrapper;
        this.applicationProperties = applicationProperties;
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void execute(MessageReceivedEventAdapter eventAdapter, Locale locale) {
        final IUser author = eventAdapter.getAuthor();
        final EmbedBuilder embedBuilder = new EmbedBuilder()
                .appendField(
                        messageSourceWrapper.getMessage("discord.bot.help.balance.title", locale),
                        messageSourceWrapper.getMessage("discord.bot.help.balance.content", locale),
                        false
                )
                .appendField(
                        messageSourceWrapper.getMessage("discord.bot.help.deposit.title", locale),
                        messageSourceWrapper.getMessage("discord.bot.help.deposit.content", locale),
                        false
                )
                .appendField(
                        messageSourceWrapper.getMessage("discord.bot.help.record.title", locale),
                        messageSourceWrapper.getMessage("discord.bot.help.record.content", locale),
                        false
                )
                .appendField(
                        messageSourceWrapper.getMessage("discord.bot.help.gratuity.title", locale),
                        messageSourceWrapper.getMessage("discord.bot.help.gratuity.content", locale),
                        false
                )
                .appendField(
                        messageSourceWrapper.getMessage("discord.bot.help.withdraw.title", locale),
                        messageSourceWrapper.getMessage("discord.bot.help.withdraw.content", locale),
                        false
                );
        final EmbedObject embedObject = embedBuilder
                .withColor(
                        applicationProperties.getDiscordEventTextColorRed(),
                        applicationProperties.getDiscordEventTextColorGreen(),
                        applicationProperties.getDiscordEventTextColorBlue()
                )
                .withFooterIcon(applicationProperties.getDiscordEventTextFooterIcon())
                .withFooterText(messageSourceWrapper.getMessage(
                        "discord.event.text.footer-text",
                        Locale.getDefault(),
                        applicationProperties.getAuthor(),
                        applicationProperties.getVersion()
                )).build();
        eventAdapter.sendMessage(author + "\n", embedObject);
    }

}
