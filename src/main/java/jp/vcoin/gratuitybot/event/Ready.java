package jp.vcoin.gratuitybot.event;

import jp.vcoin.gratuitybot.config.ApplicationProperties;
import jp.vcoin.gratuitybot.service.DynamicSettingService;
import jp.vcoin.gratuitybot.marker.Event;
import jp.vcoin.gratuitybot.util.MessageSourceWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sx.blah.discord.handle.impl.events.ReadyEvent;

import java.util.Optional;

@Component
@Event
@Slf4j
public class Ready extends NotifyEvent<ReadyEvent> {

    @Autowired
    public Ready(MessageSourceWrapper messageSource, ApplicationProperties applicationProperties, DynamicSettingService dynamicSettingService) {
        super(messageSource, applicationProperties, dynamicSettingService);
    }

    @Override
    public void handle(ReadyEvent event) {
        if (applicationProperties.getDiscordBotSilentStartUp()) {
            log.info("silent start up done");
            Optional.ofNullable(event.getClient().getUserByID(applicationProperties.getDiscordBotNotificationUserId()))
                    .ifPresent(u -> u.getOrCreatePMChannel()
                            .sendMessage(messageSource.getMessage("discord.bot.start-notification")));
            return;
        }
        notify(event, "discord.bot.start-notification");
    }

}
