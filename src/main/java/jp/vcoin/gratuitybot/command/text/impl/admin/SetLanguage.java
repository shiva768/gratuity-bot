package jp.vcoin.gratuitybot.command.text.impl.admin;

import jp.vcoin.gratuitybot.config.ApplicationProperties;
import jp.vcoin.gratuitybot.domain.Channel;
import jp.vcoin.gratuitybot.domain.DiscordObject;
import jp.vcoin.gratuitybot.enumeration.DynamicSettingType;
import jp.vcoin.gratuitybot.service.DiscordValidatorService;
import jp.vcoin.gratuitybot.service.DynamicSettingService;
import jp.vcoin.gratuitybot.adapter.MessageReceivedEventAdapter;
import jp.vcoin.gratuitybot.command.text.MessageCommand;
import jp.vcoin.gratuitybot.domain.LanguageSetting;
import jp.vcoin.gratuitybot.marker.Text;
import jp.vcoin.gratuitybot.util.DiscordUtil;
import jp.vcoin.gratuitybot.util.MessageSourceWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IUser;

import java.util.Locale;

@Component("set-language")
@Text
@Slf4j
public class SetLanguage implements MessageCommand<MessageReceivedEvent> {

    private final DynamicSettingService dynamicSettingService;
    private final MessageSourceWrapper messageSourceWrapper;
    private final ApplicationProperties applicationProperties;
    private final DiscordValidatorService discordValidatorService;

    @Autowired
    public SetLanguage(DynamicSettingService dynamicSettingService, MessageSourceWrapper messageSourceWrapper, ApplicationProperties applicationProperties, DiscordValidatorService discordValidatorService) {
        this.dynamicSettingService = dynamicSettingService;
        this.messageSourceWrapper = messageSourceWrapper;
        this.applicationProperties = applicationProperties;
        this.discordValidatorService = discordValidatorService;
    }

    @Override
    public void execute(MessageReceivedEventAdapter eventAdapter, Locale locale) {

        final IUser author = eventAdapter.getAuthor();
        String language = eventAdapter.getContent(0);
        String channelId = eventAdapter.getContent(1);
        try {
            DiscordObject discordObject = DiscordUtil.convertDiscordObject(channelId, Channel.class);
            if (!validate(eventAdapter, locale, language, discordObject)) {
                return;
            }
            final LanguageSetting languageSetting = new LanguageSetting(language, discordObject.getConvertedId());
            dynamicSettingService.save(DynamicSettingType.Language.getKey(), languageSetting.getChannelId(), eventAdapter.getGuildId(), languageSetting.serialize());
            eventAdapter.sendMessage(messageSourceWrapper.getMessage(
                    "discord.admin.set-specific-xxx.success",
                    locale,
                    author,
                    languageSetting.format()
            ));

        } catch (Throwable t) {
            failed(eventAdapter, locale, author);
        }
    }

    private void failed(MessageReceivedEventAdapter eventAdapter, Locale locale, IUser author) {
        eventAdapter.sendMessage(messageSourceWrapper.getMessage(
                "discord.admin.set-specific-xxx.failed",
                locale,
                author
        ));
    }

    private boolean validate(MessageReceivedEventAdapter eventAdapter, Locale locale, String language, DiscordObject discordObject) {
        final IUser author = eventAdapter.getAuthor();
        if (!discordValidatorService.validate(locale, author, eventAdapter, false, false, discordObject))
            return false;
        if (StringUtils.isEmpty(language)) {
            eventAdapter.sendMessage(messageSourceWrapper.getMessage(
                    "discord.admin.set-language.validate.language-required",
                    locale,
                    author
            ));
            return false;
        }
        if (!applicationProperties.getDiscordSupportLanguages().contains(language)) {
            eventAdapter.sendMessage(messageSourceWrapper.getMessage(
                    "discord.admin.set-language.validate.not-support-language",
                    locale,
                    author
            ));
            return false;
        }
        return true;
    }

}
