package jp.vcoin.gratuitybot.command.text.impl.admin;

import jp.vcoin.gratuitybot.config.ApplicationProperties;
import jp.vcoin.gratuitybot.domain.DiscordObject;
import jp.vcoin.gratuitybot.domain.Emoji;
import jp.vcoin.gratuitybot.domain.EmojiAmount;
import jp.vcoin.gratuitybot.enumeration.DynamicSettingType;
import jp.vcoin.gratuitybot.service.CommandService;
import jp.vcoin.gratuitybot.service.DiscordValidatorService;
import jp.vcoin.gratuitybot.service.DynamicSettingService;
import jp.vcoin.gratuitybot.adapter.MessageReceivedEventAdapter;
import jp.vcoin.gratuitybot.command.text.MessageCommand;
import jp.vcoin.gratuitybot.marker.Text;
import jp.vcoin.gratuitybot.util.DiscordUtil;
import jp.vcoin.gratuitybot.util.MessageSourceWrapper;
import jp.vcoin.gratuitybot.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IUser;

import java.util.Locale;

@Component("set-emoji-amount")
@Text
@Slf4j
public class SetEmojiAmount implements MessageCommand<MessageReceivedEvent> {

    private final DynamicSettingService dynamicSettingService;
    private final MessageSourceWrapper messageSourceWrapper;
    private final CommandService commandService;
    private final DiscordValidatorService discordValidatorService;

    @Autowired
    public SetEmojiAmount(DynamicSettingService dynamicSettingService, MessageSourceWrapper messageSourceWrapper, ApplicationProperties applicationProperties, CommandService commandService, DiscordValidatorService discordValidatorService) {
        this.dynamicSettingService = dynamicSettingService;
        this.messageSourceWrapper = messageSourceWrapper;
        this.commandService = commandService;
        this.discordValidatorService = discordValidatorService;
    }

    @Override
    public void execute(MessageReceivedEventAdapter eventAdapter, Locale locale) {

        final IUser author = eventAdapter.getAuthor();
        final String emojiId = eventAdapter.getContent(0);
        final String amount = eventAdapter.getContent(1);
        try {
            DiscordObject discordObject = DiscordUtil.convertDiscordObject(emojiId, Emoji.class);
            if (!validate(eventAdapter, locale, discordObject, amount))
                return;
            EmojiAmount emojiAmount = new EmojiAmount(discordObject.getConvertedId(), amount);
            dynamicSettingService.save(DynamicSettingType.EmojiAmount.getKey(), emojiAmount.getEmojiId(), eventAdapter.getGuildId(), emojiAmount.serialize());
            commandService.evict(eventAdapter.getGuildId());
            eventAdapter.sendMessage(messageSourceWrapper.getMessage(
                    "discord.admin.set-specific-xxx.success",
                    locale,
                    author,
                    emojiAmount.format()
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

    private boolean validate(MessageReceivedEventAdapter eventAdapter, Locale locale, DiscordObject discordObject, String amount) {
        final IUser author = eventAdapter.getAuthor();
        if (!discordValidatorService.validate(locale, author, eventAdapter, true, false, discordObject))
            return false;
        if (amount == null || Util.invalidAmount(amount)) {
            eventAdapter.sendMessage(messageSourceWrapper.getMessage(
                    "discord.admin.set-emoji-amount.validate.invalid-amount",
                    locale,
                    author
            ));
            return false;
        }
        return true;
    }

}
