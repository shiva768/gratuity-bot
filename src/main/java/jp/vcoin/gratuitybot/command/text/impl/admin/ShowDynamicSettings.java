package jp.vcoin.gratuitybot.command.text.impl.admin;

import jp.vcoin.gratuitybot.config.ApplicationProperties;
import jp.vcoin.gratuitybot.domain.DynamicSettingDomain;
import jp.vcoin.gratuitybot.domain.GeneralSetting;
import jp.vcoin.gratuitybot.enumeration.DynamicSettingType;
import jp.vcoin.gratuitybot.service.CommandService;
import jp.vcoin.gratuitybot.service.DynamicSettingService;
import jp.vcoin.gratuitybot.adapter.MessageReceivedEventAdapter;
import jp.vcoin.gratuitybot.command.text.MessageCommand;
import jp.vcoin.gratuitybot.command.text.ShowDynamicSettingCommand;
import jp.vcoin.gratuitybot.marker.Text;
import jp.vcoin.gratuitybot.util.MessageSourceWrapper;
import jp.vcoin.gratuitybot.util.OptionalConsumer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.util.EmbedBuilder;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Component("show-dynamic-settings")
@Text
@Slf4j
public class ShowDynamicSettings implements MessageCommand<MessageReceivedEvent> {
    private final DynamicSettingService dynamicSettingService;
    private final ApplicationProperties applicationProperties;
    private final MessageSourceWrapper messageSourceWrapper;
    private final Map<String, ShowDynamicSettingCommand> showDynamicSettingCommandMap;

    @Autowired
    public ShowDynamicSettings(DynamicSettingService dynamicSettingService, ApplicationProperties applicationProperties, MessageSourceWrapper messageSourceWrapper, CommandService commandService) {
        this.dynamicSettingService = dynamicSettingService;
        this.applicationProperties = applicationProperties;
        this.messageSourceWrapper = messageSourceWrapper;
        this.showDynamicSettingCommandMap = commandService.getShowDynamicSettingCommandMap();
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void execute(MessageReceivedEventAdapter eventAdapter, Locale locale) {

        final EmbedBuilder builder = new EmbedBuilder();
        Arrays.stream(DynamicSettingType.values()).forEach(
                d -> OptionalConsumer.of(d.getShowCommand())
                        .ifPresent(s -> OptionalConsumer.of(Optional.ofNullable(showDynamicSettingCommandMap.get(s)))
                                .ifPresent(c -> c.buildContent(eventAdapter, locale, builder))
                                .ifNotPresent(() -> buildSimpleContent(d, eventAdapter, locale, builder))
                        )
                        .ifNotPresent(() -> buildSimpleContent(d, eventAdapter, locale, builder))
        );
        if (builder.getFieldCount() > 0) {
            final EmbedObject embedObject = builder
                    .withColor(
                            applicationProperties.getDiscordEventTextColorRed(),
                            applicationProperties.getDiscordEventTextColorGreen(),
                            applicationProperties.getDiscordEventTextColorBlue()
                    ).build();
            eventAdapter.sendMessage(eventAdapter.getAuthor() + "\n", embedObject);
        } else {
            eventAdapter.sendMessage(messageSourceWrapper.getMessage(
                    "discord.admin.show-dynamic-setting.empty",
                    locale,
                    eventAdapter.getAuthor()
            ));
        }
    }

    private void buildSimpleContent(DynamicSettingType key, MessageReceivedEventAdapter eventAdapter, Locale locale, EmbedBuilder builder) {
        dynamicSettingService.getList(key.getKey(), eventAdapter.getGuildId(), GeneralSetting.class).forEach(
                g -> builder.appendField(
                        messageSourceWrapper.getMessage(DynamicSettingDomain.SETTING_NAME_KEY_PREFIX + getMessageKeyParts(g.getKey().getKey()), locale),
                        key.isChannelSetting() ? String.format("<#%s>", g.serialize()) : g.format(),
                        false)
        );
    }

    private String getMessageKeyParts(String key) {
        if (key.contains(".")) {
            return StringUtils.substringAfterLast(key, ".");
        }
        return key;
    }
}
