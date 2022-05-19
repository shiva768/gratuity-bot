package jp.vcoin.gratuitybot.command.text.impl.admin;

import jp.vcoin.gratuitybot.config.ApplicationProperties;
import jp.vcoin.gratuitybot.service.DynamicSettingService;
import jp.vcoin.gratuitybot.adapter.MessageReceivedEventAdapter;
import jp.vcoin.gratuitybot.command.text.MessageCommand;
import jp.vcoin.gratuitybot.marker.Text;
import jp.vcoin.gratuitybot.util.Formatter;
import jp.vcoin.gratuitybot.util.MessageSourceWrapper;
import jp.vcoin.gratuitybot.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IUser;

import java.math.BigDecimal;
import java.util.Locale;

import static sx.blah.discord.util.MessageBuilder.Styles.BOLD;

@Component("set-random-gratuity-min-limit")
@Text
@Slf4j
public class SetRandomGratuityMinLimit implements MessageCommand<MessageReceivedEvent> {

    private final DynamicSettingService dynamicSettingService;
    private final MessageSourceWrapper messageSourceWrapper;

    @Autowired
    public SetRandomGratuityMinLimit(DynamicSettingService dynamicSettingService, MessageSourceWrapper messageSourceWrapper, ApplicationProperties applicationProperties) {
        this.dynamicSettingService = dynamicSettingService;
        this.messageSourceWrapper = messageSourceWrapper;
    }

    @Override
    public void execute(MessageReceivedEventAdapter eventAdapter, Locale locale) {
        String amountString = eventAdapter.getContent(0);
        if (!validate(eventAdapter, amountString, locale)) return;
        final BigDecimal amount = new BigDecimal(amountString);

        try {

            dynamicSettingService.save("gratuity.random-min-amount", null, eventAdapter.getGuildId(), amount.toPlainString());
            eventAdapter.sendMessage(messageSourceWrapper.getMessage(
                    "discord.admin.set-xxx.success",
                    locale,
                    eventAdapter.getAuthor(),
                    "" + BOLD + messageSourceWrapper.getMessage("discord.admin.word.dynamic-setting.random-min-amount", locale) + BOLD,
                    "" + BOLD + Formatter.formatValue().format(amount) + BOLD
            ));
            IUser author = eventAdapter.getAuthor();
            log.info("setting done. setting value {} from {}#{}", Formatter.formatValue().format(amount), author.getName(), author.getStringID());
        } catch (Exception e) {
            log.error("setting failed.", e);
            eventAdapter.sendMessage(messageSourceWrapper.getMessage(
                    "discord.admin.set-xxx.failed",
                    locale,
                    eventAdapter.getAuthor(),
                    "" + BOLD + messageSourceWrapper.getMessage("discord.admin.word.dynamic-setting.random-min-amount", locale) + BOLD
            ));
        }
    }

    private boolean validate(MessageReceivedEventAdapter eventAdapter, String amountString, Locale locale) {

        if (amountString == null || Util.invalidAmount(amountString)) {
            IUser author = eventAdapter.getAuthor();
            invalidAmount(eventAdapter, locale, author);
            log.warn("setting failed. setting value {} from {}#{}", amountString, author.getName(), author.getStringID());
            return false;
        }
        return true;
    }

    private void invalidAmount(MessageReceivedEventAdapter eventAdapter, Locale locale, IUser author) {
        eventAdapter.sendMessage(messageSourceWrapper.getMessage(
                "discord.admin.set-xxx.validate.amount-invalid",
                locale,
                author
        ));
    }
}
