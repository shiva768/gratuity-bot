package jp.vcoin.gratuitybot.command.text.impl.admin;

import jp.vcoin.gratuitybot.domain.Channel;
import jp.vcoin.gratuitybot.domain.DiscordObject;
import jp.vcoin.gratuitybot.enumeration.DynamicSettingType;
import jp.vcoin.gratuitybot.service.DiscordValidatorService;
import jp.vcoin.gratuitybot.service.DynamicSettingService;
import jp.vcoin.gratuitybot.adapter.MessageReceivedEventAdapter;
import jp.vcoin.gratuitybot.command.text.MessageCommand;
import jp.vcoin.gratuitybot.marker.Text;
import jp.vcoin.gratuitybot.util.DiscordUtil;
import jp.vcoin.gratuitybot.util.MessageSourceWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IUser;

import java.util.Locale;

@Component("set-emoji-notify-channel")
@Text
@Slf4j
public class SetEmojiNotifyChannel implements MessageCommand<MessageReceivedEvent> {

    private final DynamicSettingService dynamicSettingService;
    private final MessageSourceWrapper messageSourceWrapper;
    private final DiscordValidatorService discordValidatorService;

    @Autowired
    public SetEmojiNotifyChannel(DynamicSettingService dynamicSettingService, MessageSourceWrapper messageSourceWrapper, DiscordValidatorService discordValidatorService) {
        this.dynamicSettingService = dynamicSettingService;
        this.messageSourceWrapper = messageSourceWrapper;
        this.discordValidatorService = discordValidatorService;
    }

    @Override
    public void execute(MessageReceivedEventAdapter eventAdapter, Locale locale) {

        final IUser author = eventAdapter.getAuthor();
        final String channelId = eventAdapter.getContent(0);
        try {
            DiscordObject discordObject = DiscordUtil.convertDiscordObject(channelId, Channel.class);
            if (!validate(eventAdapter, locale, discordObject))
                return;
            dynamicSettingService.save(DynamicSettingType.EmojiNotifyChannel.getKey(), eventAdapter.getGuildId(), discordObject.getConvertedId());
            eventAdapter.sendMessage(messageSourceWrapper.getMessage(
                    "discord.admin.set-specific-xxx.success",
                    locale,
                    author,
                    discordObject.getConvertedId()
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

    private boolean validate(MessageReceivedEventAdapter eventAdapter, Locale locale, DiscordObject discordObject) {
        return discordValidatorService.validate(locale, eventAdapter.getAuthor(), eventAdapter, true, false, discordObject);
    }

}
