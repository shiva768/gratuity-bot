package jp.vcoin.gratuitybot.command.text.impl.admin;

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

@Component("admin-help")
@Text
public class AdminHelp implements MessageCommand<MessageReceivedEvent> {

    private final MessageSourceWrapper messageSourceWrapper;
    private final ApplicationProperties applicationProperties;

    @Autowired
    public AdminHelp(MessageSourceWrapper messageSourceWrapper, ApplicationProperties applicationProperties) {
        this.messageSourceWrapper = messageSourceWrapper;
        this.applicationProperties = applicationProperties;
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void execute(MessageReceivedEventAdapter eventAdapter, Locale locale) {
        final IUser author = eventAdapter.getAuthor();
        final EmbedBuilder embedBuilder = new EmbedBuilder()
                .appendField(
                        messageSourceWrapper.getMessage("discord.admin.help.show-dynamic-settings.title", locale),
                        messageSourceWrapper.getMessage("discord.admin.help.show-dynamic-settings.content", locale),
                        false
                )
                .appendField(
                        messageSourceWrapper.getMessage("discord.admin.help.add-admin-user.title", locale),
                        messageSourceWrapper.getMessage("discord.admin.help.add-admin-user.content", locale),
                        false
                )
                .appendField(
                        messageSourceWrapper.getMessage("discord.admin.help.remove-admin-user.title", locale),
                        messageSourceWrapper.getMessage("discord.admin.help.remove-admin-user.content", locale),
                        false
                )
                .appendField(
                        messageSourceWrapper.getMessage("discord.admin.help.show-admin-users.title", locale),
                        messageSourceWrapper.getMessage("discord.admin.help.show-admin-users.content", locale),
                        false
                )
                .appendField(
                        messageSourceWrapper.getMessage("discord.admin.help.set-notify-channel.title", locale),
                        messageSourceWrapper.getMessage("discord.admin.help.set-notify-channel.content", locale),
                        false
                )
                .appendField(
                        messageSourceWrapper.getMessage("discord.admin.help.set-emoji-notify-channel.title", locale),
                        messageSourceWrapper.getMessage("discord.admin.help.set-emoji-notify-channel.content", locale),
                        false
                )
                .appendField(
                        messageSourceWrapper.getMessage("discord.admin.help.set-random-gratuity-min-limit.title", locale),
                        messageSourceWrapper.getMessage("discord.admin.help.set-random-gratuity-min-limit.content", locale),
                        false
                )
                .appendField(
                        messageSourceWrapper.getMessage("discord.admin.help.set-language.title", locale),
                        messageSourceWrapper.getMessage("discord.admin.help.set-language.content", locale),
                        false
                )
                .appendField(
                        messageSourceWrapper.getMessage("discord.admin.help.remove-language.title", locale),
                        messageSourceWrapper.getMessage("discord.admin.help.remove-language.content", locale),
                        false
                )
                .appendField(
                        messageSourceWrapper.getMessage("discord.admin.help.show-languages.title", locale),
                        messageSourceWrapper.getMessage("discord.admin.help.show-languages.content", locale),
                        false
                )
                .appendField(
                        messageSourceWrapper.getMessage("discord.admin.help.set-emoji-amount.title", locale),
                        messageSourceWrapper.getMessage("discord.admin.help.set-emoji-amount.content", locale),
                        false
                )
                .appendField(
                        messageSourceWrapper.getMessage("discord.admin.help.remove-emoji-amount.title", locale),
                        messageSourceWrapper.getMessage("discord.admin.help.remove-emoji-amount.content", locale),
                        false
                )
                .appendField(
                        messageSourceWrapper.getMessage("discord.admin.help.show-emoji-amounts.title", locale),
                        messageSourceWrapper.getMessage("discord.admin.help.show-emoji-amounts.content", locale),
                        false
                )
                .appendField(
                        messageSourceWrapper.getMessage("discord.admin.help.set-linkage-bot-user.title", locale),
                        messageSourceWrapper.getMessage("discord.admin.help.set-linkage-bot-user.content", locale),
                        false
                )
                .appendField(
                        messageSourceWrapper.getMessage("discord.admin.help.remove-linkage-bot-user.title", locale),
                        messageSourceWrapper.getMessage("discord.admin.help.remove-linkage-bot-user.content", locale),
                        false
                )
                .appendField(
                        messageSourceWrapper.getMessage("discord.admin.help.show-linkage-bot-users.title", locale),
                        messageSourceWrapper.getMessage("discord.admin.help.show-linkage-bot-users.content", locale),
                        false
                )
                .appendField(
                        messageSourceWrapper.getMessage("discord.admin.help.add-command-enable-channel.title", locale),
                        messageSourceWrapper.getMessage("discord.admin.help.add-command-enable-channel.content", locale),
                        false
                )
                .appendField(
                        messageSourceWrapper.getMessage("discord.admin.help.remove-command-enable-channel.title", locale),
                        messageSourceWrapper.getMessage("discord.admin.help.remove-command-enable-channel.content", locale),
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
