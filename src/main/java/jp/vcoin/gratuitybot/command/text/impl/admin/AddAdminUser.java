package jp.vcoin.gratuitybot.command.text.impl.admin;

import jp.vcoin.gratuitybot.domain.AdminUser;
import jp.vcoin.gratuitybot.domain.DiscordObject;
import jp.vcoin.gratuitybot.domain.GeneralSetting;
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

@Component("add-admin-user")
@Text
@Slf4j
public class AddAdminUser implements MessageCommand<MessageReceivedEvent> {

    private final DynamicSettingService dynamicSettingService;
    private final MessageSourceWrapper messageSourceWrapper;
    private final DiscordValidatorService discordValidatorService;

    @Autowired
    public AddAdminUser(DynamicSettingService dynamicSettingService, MessageSourceWrapper messageSourceWrapper, DiscordValidatorService discordValidatorService) {
        this.dynamicSettingService = dynamicSettingService;
        this.messageSourceWrapper = messageSourceWrapper;
        this.discordValidatorService = discordValidatorService;
    }

    @Override
    public void execute(MessageReceivedEventAdapter eventAdapter, Locale locale) {

        final IUser author = eventAdapter.getAuthor();
        String userId = eventAdapter.getContent(0);
        try {
            final DiscordObject discordObject = DiscordUtil.convertDiscordObject(userId, User.class);
            if (!validate(eventAdapter, locale, discordObject))
                return;
            dynamicSettingService.save(DynamicSettingType.AdminUser.getKey(), discordObject.getConvertedId(), eventAdapter.getGuildId(), "true");
            eventAdapter.sendMessage(messageSourceWrapper.getMessage(
                    "discord.admin.set-specific-xxx.success",
                    locale,
                    author,
                    new AdminUser(discordObject.getConvertedId()).format()
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
        final IUser author = eventAdapter.getAuthor();

        if (!discordValidatorService.validate(locale, author, eventAdapter, true, false, discordObject))
            return false;
        if (eventAdapter.getUserByStringId(discordObject.getConvertedId()).isBot()) {
            eventAdapter.sendMessage(messageSourceWrapper.getMessage(
                    "discord.admin.add-admin-user.validate.cannot-add-bot-user",
                    locale,
                    author
            ));
            return false;
        }
        if (dynamicSettingService.get(DynamicSettingType.AdminUser.getKey(), discordObject.getConvertedId(), eventAdapter.getGuildId(), GeneralSetting.class).isPresent()) {
            eventAdapter.sendMessage(messageSourceWrapper.getMessage(
                    "discord.admin.add-admin-user.validate.setting-exists-user",
                    locale,
                    author
            ));
            return false;
        }
        return true;
    }
}
