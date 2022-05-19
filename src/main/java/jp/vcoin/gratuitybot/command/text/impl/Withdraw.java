package jp.vcoin.gratuitybot.command.text.impl;

import jp.vcoin.gratuitybot.config.ApplicationProperties;
import jp.vcoin.gratuitybot.domain.BalanceValue;
import jp.vcoin.gratuitybot.domain.TransactionValue;
import jp.vcoin.gratuitybot.exception.BalanceShortfallException;
import jp.vcoin.gratuitybot.service.VirtualCoinWalletService;
import jp.vcoin.gratuitybot.adapter.MessageReceivedEventAdapter;
import jp.vcoin.gratuitybot.command.text.MessageCommand;
import jp.vcoin.gratuitybot.marker.Text;
import jp.vcoin.gratuitybot.util.MessageSourceWrapper;
import jp.vcoin.gratuitybot.util.Util;
import jp.vcoin.gratuitybot.util.Util.CompareResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.MessageBuilder.Styles;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Optional;

import static sx.blah.discord.util.MessageBuilder.Styles.BOLD;

@Component
@Text
@Slf4j
public class Withdraw implements MessageCommand<MessageReceivedEvent> {

    private final VirtualCoinWalletService virtualCoinWalletService;
    private final MessageSourceWrapper messageSourceWrapper;
    private final ApplicationProperties applicationProperties;

    @Autowired
    public Withdraw(VirtualCoinWalletService virtualCoinWalletService, MessageSourceWrapper messageSourceWrapper, ApplicationProperties applicationProperties) {
        this.virtualCoinWalletService = virtualCoinWalletService;
        this.messageSourceWrapper = messageSourceWrapper;
        this.applicationProperties = applicationProperties;
    }

    @Override
    public void execute(MessageReceivedEventAdapter eventAdapter, Locale locale) {
        final IUser author = eventAdapter.getAuthor();
        final String authorId = author.getStringID();
        final BalanceValue balanceValue = virtualCoinWalletService.getBalance(authorId);
        if (!validate(eventAdapter, eventAdapter.getContent(0), eventAdapter.getContent(1), locale, balanceValue))
            return;

        final String targetAddress = eventAdapter.getContent(0);
        final String amountString = eventAdapter.getContent(1);
        final BigDecimal amount = "all".equals(amountString) ? virtualCoinWalletService.getBalance(authorId).getBalance() : new BigDecimal(amountString);

        final Optional<String> transactionId;
        try {
            transactionId = virtualCoinWalletService.sendMany(authorId, targetAddress, amount);
            transactionId.ifPresent(id -> afterProcess(eventAdapter, locale, author, authorId, targetAddress, amount, id));
        } catch (BalanceShortfallException e) {
            log.warn("shortfall");
            final BalanceValue balance = virtualCoinWalletService.getBalance(author.getStringID());
            shortfall(eventAdapter, locale, balance, author);
        }
    }

    private void afterProcess(MessageReceivedEventAdapter eventAdapter, Locale locale, IUser author, String authorId, String targetAddress, BigDecimal amount, String id) {
        log.info("transaction id:{}", id);
        final TransactionValue transaction = virtualCoinWalletService.getTransaction(id);
        log.info("withdraw {} to {} from id {}#{} ", amount, targetAddress, author.getName(), authorId);
        log.info(transaction.toString());
        eventAdapter.sendMessage(messageSourceWrapper.getMessage(
                "discord.bot.withdraw.success",
                locale,
                author,
                transaction.getAmountPlainString(),
                transaction.getFeePlainString(),
                "http://explorer.virtualcoin.jp/tx/" + transaction.getTransactionId()
        ));
    }

    private boolean validate(MessageReceivedEventAdapter eventAdapter, String targetAddress, String amountTempString, Locale locale, BalanceValue balance) {
        final String amountString = Optional.ofNullable(amountTempString).orElse("");
        final IUser author = eventAdapter.getAuthor();
        if (!virtualCoinWalletService.validateAddress(targetAddress)) {
            eventAdapter.sendMessage(messageSourceWrapper.getMessage(
                    "discord.bot.withdraw.validate.address-invalid",
                    locale,
                    author)
            );
            return false;
        }

        if ("all".equals(amountString)) {
            return validateMinAmount(balance.getBalance(), locale, eventAdapter, author);
        } else if (Util.invalidAmount(amountString)) {
            eventAdapter.sendMessage(messageSourceWrapper.getMessage(
                    "discord.bot.withdraw.validate.amount-invalid",
                    locale,
                    author)
            );
            return false;
        } else if (!validateMinAmount(new BigDecimal(amountString), locale, eventAdapter, author)) {
            return false;
        } else if (Util.compare(balance.getBalance(), new BigDecimal(amountString)) == CompareResult.Lower) {
            shortfall(eventAdapter, locale, balance, author);
            return false;
        }
        return true;
    }

    private void shortfall(MessageReceivedEventAdapter eventAdapter, Locale locale, BalanceValue balance, IUser author) {
        eventAdapter.sendMessage(messageSourceWrapper.getMessage(
                "discord.bot.withdraw.validate.amount-shortfall",
                locale,
                author,
                "" + BOLD + balance.getBalancePlainString() + BOLD
        ));
    }

    private boolean validateMinAmount(BigDecimal amount, Locale locale, MessageReceivedEventAdapter eventAdapter, IUser author) {
        BigDecimal withdrawMinAmount = applicationProperties.getDiscordEventTextCommandWithdrawMinAmount();
        if (Util.compare(amount, withdrawMinAmount) == CompareResult.Lower) {
            eventAdapter.sendMessage(messageSourceWrapper.getMessage(
                    "discord.bot.withdraw.validate.minimum-amount-shortfall",
                    locale,
                    author,
                    "" + Styles.BOLD + withdrawMinAmount + Styles.BOLD
            ));
            return false;
        }
        return true;
    }
}
