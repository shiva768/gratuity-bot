package jp.vcoin.gratuitybot.command.text.impl;

import jp.vcoin.gratuitybot.domain.BalanceValue;
import jp.vcoin.gratuitybot.service.VirtualCoinWalletService;
import jp.vcoin.gratuitybot.adapter.MessageReceivedEventAdapter;
import jp.vcoin.gratuitybot.command.text.MessageCommand;
import jp.vcoin.gratuitybot.marker.Text;
import jp.vcoin.gratuitybot.util.Formatter;
import jp.vcoin.gratuitybot.util.MessageSourceWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IUser;

import java.math.BigDecimal;
import java.util.Locale;

import static sx.blah.discord.util.MessageBuilder.Styles.BOLD;

@Component
@Text
public class Balance implements MessageCommand<MessageReceivedEvent> {

    private final VirtualCoinWalletService virtualCoinWalletService;
    private final MessageSourceWrapper messageSourceWrapper;

    @Autowired
    public Balance(VirtualCoinWalletService virtualCoinWalletService, MessageSourceWrapper messageSourceWrapper) {
        this.virtualCoinWalletService = virtualCoinWalletService;
        this.messageSourceWrapper = messageSourceWrapper;
    }

    @Override
    public void execute(MessageReceivedEventAdapter eventAdapter, Locale locale) {
        final IUser author = eventAdapter.getAuthor();
        final String authorId = author.getStringID();
        final BalanceValue balance = virtualCoinWalletService.getBalance(authorId);

        String message = messageSourceWrapper.getMessage("discord.bot.balance", locale, author, "" + BOLD + balance.getBalancePlainString() + BOLD);
        if (balance.isDuringVerification()) {
            final BigDecimal difference = balance.difference();
            message += messageSourceWrapper.getMessage("discord.bot.balance_verification", locale, "" + BOLD + Formatter.formatValue().format(difference) + BOLD);
        }
        eventAdapter.sendMessage(message);
    }

}
