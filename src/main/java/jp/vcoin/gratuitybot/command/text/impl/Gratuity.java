package jp.vcoin.gratuitybot.command.text.impl;

import jp.vcoin.gratuitybot.config.ApplicationProperties;
import jp.vcoin.gratuitybot.domain.BalanceValue;
import jp.vcoin.gratuitybot.domain.GeneralSetting;
import jp.vcoin.gratuitybot.domain.LinkageBotUser;
import jp.vcoin.gratuitybot.enumeration.DynamicSettingType;
import jp.vcoin.gratuitybot.exception.BalanceShortfallException;
import jp.vcoin.gratuitybot.exception.RPCException;
import jp.vcoin.gratuitybot.service.DynamicSettingService;
import jp.vcoin.gratuitybot.service.VirtualCoinWalletService;
import jp.vcoin.gratuitybot.adapter.MessageReceivedEventAdapter;
import jp.vcoin.gratuitybot.command.text.MessageCommand;
import jp.vcoin.gratuitybot.marker.Text;
import jp.vcoin.gratuitybot.util.DiscordUtil;
import jp.vcoin.gratuitybot.util.Formatter;
import jp.vcoin.gratuitybot.util.MessageSourceWrapper;
import jp.vcoin.gratuitybot.util.Util;
import jp.vcoin.gratuitybot.util.Util.CompareResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IUser;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

import static sx.blah.discord.util.MessageBuilder.Styles.BOLD;

@Component
@Text
@Slf4j
public class Gratuity implements MessageCommand<MessageReceivedEvent> {

    private final VirtualCoinWalletService virtualCoinWalletService;
    private final MessageSourceWrapper messageSourceWrapper;
    private final ApplicationProperties applicationProperties;
    private final DynamicSettingService dynamicSettingService;

    @Autowired
    public Gratuity(VirtualCoinWalletService virtualCoinWalletService, MessageSourceWrapper messageSourceWrapper, ApplicationProperties applicationProperties, DynamicSettingService dynamicSettingService) {
        this.virtualCoinWalletService = virtualCoinWalletService;
        this.messageSourceWrapper = messageSourceWrapper;
        this.applicationProperties = applicationProperties;
        this.dynamicSettingService = dynamicSettingService;
    }

    @Override
    public void execute(MessageReceivedEventAdapter eventAdapter, Locale locale) {
        if (!validate(eventAdapter, eventAdapter.getContent(0), eventAdapter.getContent(1), locale)) return;

        final IUser author = eventAdapter.getAuthor();
        final String tempTargetId = eventAdapter.getContent(0);
        final IUser target = "random".equals(tempTargetId) ? getRandomUser(eventAdapter) : eventAdapter.getUserByID(Long.parseLong(DiscordUtil.convertUserId(tempTargetId)));
        // TODO 送る対象が存在しない場合の処理
        if (target == null) return;
        final BigDecimal amount = new BigDecimal(eventAdapter.getContent(1));
        try {
            virtualCoinWalletService.move(author.getStringID(), target.getStringID(), amount);
        } catch (RPCException e) {
            // RPCエラーが起きても、エラーをユーザに返さないと
            log.error("rpc move error", e);
            return;
        } catch (BalanceShortfallException e) {
            log.warn("shortfall");
            final BalanceValue balance = virtualCoinWalletService.getBalance(author.getStringID());
            shortfall(eventAdapter, locale, author, balance);
            return;
        }
        log.info("send gratuity {} to {}#{} from {}#{}", Formatter.formatValue().format(amount), target.getName(), target.getStringID(), author.getName(), author.getStringID());
        eventAdapter.sendPublicMessage(messageSourceWrapper.getMessage(
                "discord.bot.gratuity.success",
                locale,
                target,
                author,
                "" + BOLD + Formatter.formatValue().format(amount) + BOLD
        ));
    }

    private void shortfall(MessageReceivedEventAdapter eventAdapter, Locale locale, IUser author, BalanceValue balance) {
        eventAdapter.sendMessage(messageSourceWrapper.getMessage(
                "discord.bot.gratuity.validate.amount-shortfall",
                locale,
                author,
                BOLD + balance.getBalancePlainString() + BOLD
        ));
    }

    private IUser getRandomUser(MessageReceivedEventAdapter eventAdapter) {
        final List<IUser> activeUsers = eventAdapter.getActiveUsersWithoutAuthor();
        if (activeUsers.isEmpty()) return null;
        return activeUsers.get(RandomUtils.nextInt(0, activeUsers.size()));
    }

    private boolean validate(MessageReceivedEventAdapter eventAdapter, String target, String amountString, Locale locale) {
        final IUser author = eventAdapter.getAuthor();

        if ("random".equals(target)) {
            if (Util.invalidAmount(amountString)) {
                invalidAmount(eventAdapter, locale, author);
                return false;
            }
            final BigDecimal minAmount = getGratuityMinAmount(eventAdapter.getGuildId());
            if (Util.compare(new BigDecimal(amountString), minAmount) == CompareResult.Lower) {
                eventAdapter.sendMessage(messageSourceWrapper.getMessage(
                        "discord.bot.gratuity.validate.random.min-amount",
                        locale,
                        author,
                        BOLD + Formatter.formatValue().format(minAmount) + BOLD
                ));
                return false;
            }
        } else {
            String targetId = DiscordUtil.convertUserId(target);
            if (targetId == null) {
                eventAdapter.sendMessage(messageSourceWrapper.getMessage(
                        "discord.bot.gratuity.validate.target-user-invalid",
                        locale,
                        author
                ));
                return false;
            }
            final IUser targetUser = eventAdapter.getUserByID(Long.parseLong(targetId));
            if (targetUser == null) {
                eventAdapter.sendMessage(messageSourceWrapper.getMessage(
                        "discord.bot.gratuity.validate.target-user-not.exists",
                        locale,
                        author
                ));
                return false;
            }
            if (targetUser.isBot() && !isEnableGratuity(targetUser.getLongID(), eventAdapter.getGuildId()))
                return false;
            if (targetUser.getStringID().equals(author.getStringID())) {
                eventAdapter.sendMessage(messageSourceWrapper.getMessage(
                        "discord.bot.gratuity.validate.target-user-self",
                        locale,
                        author
                ));
                return false;
            }
            if (Util.invalidAmount(amountString)) {
                invalidAmount(eventAdapter, locale, author);
                return false;
            }
        }
        final BalanceValue balance = virtualCoinWalletService.getBalance(author.getStringID());

        if (Util.compare(balance.getBalance(), new BigDecimal(amountString)) == CompareResult.Lower) {
            shortfall(eventAdapter, locale, author, balance);
            return false;
        }
        return true;
    }

    private void invalidAmount(MessageReceivedEventAdapter eventAdapter, Locale locale, IUser author) {
        eventAdapter.sendMessage(messageSourceWrapper.getMessage(
                "discord.bot.gratuity.validate.amount-invalid",
                locale,
                author
        ));
    }

    private BigDecimal getGratuityMinAmount(long guildId) {
        final GeneralSetting generalSetting = dynamicSettingService.get("gratuity.random-min-amount", null, guildId, GeneralSetting.class)
                .orElse(new GeneralSetting(String.valueOf(applicationProperties.getDiscordEventTextCommandGratuityDefaultRandomMinAmount())));
        return new BigDecimal(generalSetting.content);
    }

    private boolean isEnableGratuity(long id, long guildId) {
        return dynamicSettingService.getList(DynamicSettingType.LinkageBotUser.getKey(), guildId, LinkageBotUser.class).stream()
                .filter(LinkageBotUser::isForGratuity)
                .anyMatch(l -> l.getId() == id);
    }
}
