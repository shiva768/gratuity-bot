package jp.vcoin.gratuitybot.command.text.impl;

import jp.vcoin.gratuitybot.config.ApplicationProperties;
import jp.vcoin.gratuitybot.domain.RecordValue;
import jp.vcoin.gratuitybot.service.VirtualCoinRecordService;
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
public class Record implements MessageCommand<MessageReceivedEvent> {

    private final VirtualCoinRecordService virtualCoinRecordService;
    private final MessageSourceWrapper messageSourceWrapper;
    private final ApplicationProperties applicationProperties;

    @Autowired
    public Record(VirtualCoinRecordService virtualCoinRecordService, MessageSourceWrapper messageSourceWrapper, ApplicationProperties applicationProperties) {
        this.virtualCoinRecordService = virtualCoinRecordService;
        this.messageSourceWrapper = messageSourceWrapper;
        this.applicationProperties = applicationProperties;
    }

    @Override
    public void execute(MessageReceivedEventAdapter eventAdapter, Locale locale) {
        final IUser author = eventAdapter.getAuthor();
        final String authorId = author.getStringID();
        final RecordValue recordValue = virtualCoinRecordService.getRecord(authorId);
        final EmbedBuilder builder = recordValue.getBuilder(messageSourceWrapper, locale);
        final EmbedObject embedObject = builder
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
                ))
                .withThumbnail(author.getAvatarURL())
                .build();
        eventAdapter.sendMessage(author + "\n", embedObject);
    }
}
