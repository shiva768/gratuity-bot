package jp.vcoin.gratuitybot.event;

import jp.vcoin.gratuitybot.config.ApplicationProperties;
import jp.vcoin.gratuitybot.domain.GeneralSetting;
import jp.vcoin.gratuitybot.enumeration.DynamicSettingType;
import jp.vcoin.gratuitybot.exception.DefaultNotifyChannelNotFoundException;
import jp.vcoin.gratuitybot.service.DynamicSettingService;
import jp.vcoin.gratuitybot.util.DiscordUtil;
import jp.vcoin.gratuitybot.util.MessageSourceWrapper;
import jp.vcoin.gratuitybot.util.OptionalConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import sx.blah.discord.api.events.Event;
import sx.blah.discord.api.events.IListener;

import java.util.Optional;

@Slf4j
public abstract class NotifyEvent<T extends Event> implements IListener<T> {

    protected final MessageSourceWrapper messageSource;
    protected final ApplicationProperties applicationProperties;
    private final DynamicSettingService dynamicSettingService;

    @Autowired
    public NotifyEvent(MessageSourceWrapper messageSource, ApplicationProperties applicationProperties, DynamicSettingService dynamicSettingService) {
        this.messageSource = messageSource;
        this.applicationProperties = applicationProperties;
        this.dynamicSettingService = dynamicSettingService;
    }

    void notify(T event, String messageKey) {
        event.getClient().getGuilds().forEach(g -> {
            final Long channelId = dynamicSettingService.get(DynamicSettingType.NotifyChannel.getKey(), g.getLongID(), GeneralSetting.class)
                    .map(gs -> Long.parseLong(gs.content))
                    .orElse(applicationProperties.getDiscordBotNotificationChannelId());
            OptionalConsumer.of(Optional.ofNullable(channelId).map(c -> Optional.ofNullable(g.getChannelByID(c))).orElse(Optional.empty()))
                    .ifPresent(c -> c.sendMessage(messageSource.getMessage(messageKey)))
                    .ifNotPresent(() -> {
                        try {
                            DiscordUtil.getDefaultChannel(g).sendMessage(
                                    messageSource.getMessage(messageKey) + " " +
                                            messageSource.getMessage("discord.bot.supplementary.notify-channel-undefined")
                            );
                        } catch (DefaultNotifyChannelNotFoundException e) {
                            log.error("notify error", e);
                        }
                    });
        });
    }
}
