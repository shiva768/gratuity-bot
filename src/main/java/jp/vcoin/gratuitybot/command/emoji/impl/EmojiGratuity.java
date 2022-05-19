package jp.vcoin.gratuitybot.command.emoji.impl;

import jp.vcoin.gratuitybot.config.ApplicationProperties;
import jp.vcoin.gratuitybot.domain.EmojiAmount;
import jp.vcoin.gratuitybot.domain.GeneralSetting;
import jp.vcoin.gratuitybot.enumeration.DynamicSettingType;
import jp.vcoin.gratuitybot.exception.BalanceShortfallException;
import jp.vcoin.gratuitybot.exception.DefaultNotifyChannelNotFoundException;
import jp.vcoin.gratuitybot.exception.RPCException;
import jp.vcoin.gratuitybot.service.DynamicSettingService;
import jp.vcoin.gratuitybot.service.VirtualCoinWalletService;
import jp.vcoin.gratuitybot.adapter.ReactionAddEventAdapter;
import jp.vcoin.gratuitybot.command.emoji.ReactionCommand;
import jp.vcoin.gratuitybot.marker.Emoji;
import jp.vcoin.gratuitybot.util.DiscordUtil;
import jp.vcoin.gratuitybot.util.Formatter;
import jp.vcoin.gratuitybot.util.MessageSourceWrapper;
import jp.vcoin.gratuitybot.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import static sx.blah.discord.util.MessageBuilder.Styles.BOLD;

@Component
@Emoji
@Slf4j
public class EmojiGratuity implements ReactionCommand<ReactionAddEvent> {

    private final VirtualCoinWalletService virtualCoinWalletService;
    private final MessageSourceWrapper messageSourceWrapper;
    private final ApplicationProperties applicationProperties;
    private final DynamicSettingService dynamicSettingService;

    @Autowired
    public EmojiGratuity(VirtualCoinWalletService virtualCoinWalletService, MessageSourceWrapper messageSourceWrapper, ApplicationProperties applicationProperties, DynamicSettingService dynamicSettingService) {
        this.virtualCoinWalletService = virtualCoinWalletService;
        this.messageSourceWrapper = messageSourceWrapper;
        this.applicationProperties = applicationProperties;
        this.dynamicSettingService = dynamicSettingService;
    }

    @Override
    public void execute(ReactionAddEventAdapter eventAdapter, Locale locale) {
        final long emojiId = eventAdapter.getEmojiId();
        final BigDecimal value = getEmojiValue(emojiId, eventAdapter.getGuildId());
        final IUser user = eventAdapter.getEventOccurrenceUser();
        final IUser target = eventAdapter.getMessageAuthor();
        if (!validate(eventAdapter, value)) {
            eventAdapter.removeReaction(user);
            return;
        }
        log.info("send emoji gratuity {} to {}#{} from {}#{}", value, target.getName(), target.getStringID(), user.getName(), user.getStringID());
        try {
            virtualCoinWalletService.moveEmoji(user.getStringID(), target.getStringID(), value);
        } catch (RPCException e) {
            eventAdapter.removeReaction(user);
            log.error("rpc move error", e);
            return;
        } catch (BalanceShortfallException e) {
            eventAdapter.removeReaction(user);
            log.warn("shortfall");
            return;
        }
        eventAdapter.sendMessage(
                getNotifyChannelId(eventAdapter.getGuild()),
                messageSourceWrapper.getMessage(
                        "discord.bot.emoji-gratuity.success",
                        locale,
                        target,
                        user,
                        "" + BOLD + Formatter.formatValue().format(value) + BOLD
                )
        );
    }

    private boolean validate(ReactionAddEventAdapter eventAdapter, BigDecimal value) {
        final IUser user = eventAdapter.getEventOccurrenceUser();
        return value == null || (!eventAdapter.getMessageAuthor().equals(user)
                && Util.compare(virtualCoinWalletService.getBalance(user.getStringID()).getBalance(), value) != Util.CompareResult.Lower
        );
    }

    private BigDecimal getEmojiValue(long emojiId, long serverId) {
        Optional<EmojiAmount> dynamic = dynamicSettingService.get(DynamicSettingType.EmojiAmount.getKey(), String.valueOf(emojiId), serverId, EmojiAmount.class);
        return dynamic.map(d -> new BigDecimal(d.getAmount())).orElseGet(() -> applicationProperties.getDiscordEventEmojiGratuityEmojiMap().get(emojiId));
    }

    @Override
    public List<Long> getTriggerEmoji(long serverId) {
        ArrayList<Long> defaults = new ArrayList<>(applicationProperties.getDiscordEventEmojiGratuityEmojiMap().keySet());
        List<EmojiAmount> emojiAmounts = dynamicSettingService.getList(DynamicSettingType.EmojiAmount.getKey(), serverId, EmojiAmount.class);
        ArrayList<Long> list = new ArrayList<>(defaults);
        list.addAll(
                emojiAmounts.stream().map(e -> Long.parseLong(e.getEmojiId())).collect(Collectors.toList())
        );
        return list.stream().distinct().collect(Collectors.toList());
    }

    @Override
    public Long getNotifyChannelId(IGuild guild) {
        final Optional<GeneralSetting> emojiNotify = dynamicSettingService.get(DynamicSettingType.EmojiNotifyChannel.getKey(), guild.getLongID(), GeneralSetting.class);
        return emojiNotify.map(s -> Long.parseLong(s.content)).orElse(
                Optional.ofNullable(applicationProperties.getDiscordEventEmojiNotificationChannelId()).orElseGet(() -> {
                    try {
                        return DiscordUtil.getDefaultChannel(guild).getLongID();
                    } catch (DefaultNotifyChannelNotFoundException e) {
                        return null;
                    }
                })
        );
    }
}
