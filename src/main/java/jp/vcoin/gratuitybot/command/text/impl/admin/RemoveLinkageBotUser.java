package jp.vcoin.gratuitybot.command.text.impl.admin;

import jp.vcoin.gratuitybot.domain.DiscordObject;
import jp.vcoin.gratuitybot.domain.LinkageBotUser;
import jp.vcoin.gratuitybot.domain.User;
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

@Component("remove-linkage-bot-user")
@Text
@Slf4j
public class RemoveLinkageBotUser implements MessageCommand<MessageReceivedEvent> {

    private final DynamicSettingService dynamicSettingService;
    private final MessageSourceWrapper messageSourceWrapper;
    private final DiscordValidatorService discordValidatorService;

    @Autowired
    public RemoveLinkageBotUser(DynamicSettingService dynamicSettingService, MessageSourceWrapper messageSourceWrapper, DiscordValidatorService discordValidatorService) {
        this.dynamicSettingService = dynamicSettingService;
        this.messageSourceWrapper = messageSourceWrapper;
        this.discordValidatorService = discordValidatorService;
    }

    @Override
    public void execute(MessageReceivedEventAdapter eventAdapter, Locale locale) {

        final IUser author = eventAdapter.getAuthor();
        String userId = eventAdapter.getContent(0);
        try {
            DiscordObject discordObject = DiscordUtil.convertDiscordObject(userId, User.class);
            if (!validate(eventAdapter, locale, discordObject))
                return;
            boolean result = dynamicSettingService.delete(DynamicSettingType.LinkageBotUser.getKey(), discordObject.getConvertedId(), eventAdapter.getGuildId());
            if (result)
                eventAdapter.sendMessage(messageSourceWrapper.getMessage(
                        "discord.admin.remove-specific-xxx.success",
                        locale,
                        author
                ));
            else
                failed(eventAdapter, locale, author);
        } catch (Throwable t) {
            failed(eventAdapter, locale, author);
        }
    }

    private void failed(MessageReceivedEventAdapter eventAdapter, Locale locale, IUser author) {
        eventAdapter.sendMessage(messageSourceWrapper.getMessage(
                "discord.admin.remove-specific-xxx.failed",
                locale,
                author
        ));
    }

    private boolean validate(MessageReceivedEventAdapter eventAdapter, Locale locale, DiscordObject discordObject) {
        final IUser author = eventAdapter.getAuthor();
        if (!discordValidatorService.validate(locale, author, eventAdapter, true, true, discordObject))
            return false;
        final IUser user = eventAdapter.getUserByStringId(discordObject.getConvertedId());
        if (user != null && !user.isBot()) {
            eventAdapter.sendMessage(messageSourceWrapper.getMessage(
                    "discord.admin.remove-linkage-bot-user.validate.not-exists-bot-user",
                    locale,
                    author
            ));
            return false;
        }
        if (!dynamicSettingService.get(DynamicSettingType.LinkageBotUser.getKey(), discordObject.getConvertedId(), eventAdapter.getGuildId(), LinkageBotUser.class).isPresent()) {
            eventAdapter.sendMessage(messageSourceWrapper.getMessage(
                    "discord.admin.remove-specific-xxx.validate.not-exists-setting",
                    locale,
                    author
            ));
            return false;
        }
        return true;
    }
}
