package jp.vcoin.gratuitybot.event;

import jp.vcoin.gratuitybot.config.ApplicationProperties;
import jp.vcoin.gratuitybot.service.DynamicSettingService;
import jp.vcoin.gratuitybot.marker.Event;
import jp.vcoin.gratuitybot.util.MessageSourceWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sx.blah.discord.handle.impl.events.shard.ReconnectSuccessEvent;

@Component
@Event
@Slf4j
public class Reconnect extends NotifyEvent<ReconnectSuccessEvent> {

    @Autowired
    public Reconnect(MessageSourceWrapper messageSource, ApplicationProperties applicationProperties, DynamicSettingService dynamicSettingService) {
        super(messageSource, applicationProperties, dynamicSettingService);
    }

    @Override
    public void handle(ReconnectSuccessEvent event) {
        notify(event, "discord.bot.reconnect-notification");
    }
}