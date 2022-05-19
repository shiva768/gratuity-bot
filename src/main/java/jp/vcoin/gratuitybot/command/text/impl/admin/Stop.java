package jp.vcoin.gratuitybot.command.text.impl.admin;

import jp.vcoin.gratuitybot.config.ApplicationProperties;
import jp.vcoin.gratuitybot.adapter.MessageReceivedEventAdapter;
import jp.vcoin.gratuitybot.command.text.MessageCommand;
import jp.vcoin.gratuitybot.marker.Text;
import jp.vcoin.gratuitybot.util.MessageSourceWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.util.Locale;

@Component
@Text
public class Stop implements MessageCommand<MessageReceivedEvent> {

    private final ApplicationProperties applicationProperties;
    private final MessageSourceWrapper messageSourceWrapper;
    private final ApplicationContext applicationContext;

    @Autowired
    public Stop(ApplicationProperties applicationProperties, MessageSourceWrapper messageSourceWrapper, ApplicationContext applicationContext) {
        this.applicationProperties = applicationProperties;
        this.messageSourceWrapper = messageSourceWrapper;
        this.applicationContext = applicationContext;
    }

    @Override
    public void execute(MessageReceivedEventAdapter eventAdapter, Locale locale) {
        eventAdapter.sendMessage(
                applicationProperties.getDiscordBotNotificationChannelId(),
                messageSourceWrapper.getMessage("discord.bot.end-notification")
        );
        System.exit(SpringApplication.exit(applicationContext, () -> 0));
    }
}
