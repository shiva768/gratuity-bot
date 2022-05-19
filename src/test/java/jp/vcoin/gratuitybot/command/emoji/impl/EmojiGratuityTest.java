package jp.vcoin.gratuitybot.command.emoji.impl;

import jp.vcoin.gratuitybot.adapter.ReactionAddEventAdapter;
import jp.vcoin.gratuitybot.config.ApplicationProperties;
import jp.vcoin.gratuitybot.domain.BalanceValue;
import jp.vcoin.gratuitybot.exception.BalanceShortfallException;
import jp.vcoin.gratuitybot.exception.RPCException;
import jp.vcoin.gratuitybot.service.DynamicSettingService;
import jp.vcoin.gratuitybot.service.VirtualCoinWalletService;
import jp.vcoin.gratuitybot.util.MessageSourceWrapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;

import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class EmojiGratuityTest {

    @MockBean
    ApplicationProperties applicationProperties;
    @MockBean
    MessageSourceWrapper messageSourceWrapper;
    @MockBean
    VirtualCoinWalletService virtualCoinWalletService;
    @Mock
    DynamicSettingService dynamicSettingService;

    ReactionAddEventAdapter eventAdapter;
    long serverId;
    long channelId;
    String messageString;
    IUser actionUser;
    String accountId;
    BalanceValue balanceValue;

    @Before
    public void setUp() {
        long emojiId = 1000L;
        BigDecimal value = new BigDecimal(1000);
        serverId = 200L;
        channelId = 300L;
        accountId = "accountId";
        messageString = "";
        eventAdapter = mock(ReactionAddEventAdapter.class);
        actionUser = mock(IUser.class);
        IUser author = mock(IUser.class);
        balanceValue = mock(BalanceValue.class);

        when(applicationProperties.getDiscordEventEmojiNotificationChannelId()).thenReturn(channelId);
        Map<Long, BigDecimal> map = mock(Map.class);
        when(map.get(emojiId)).thenReturn(value);
        when(applicationProperties.getDiscordEventEmojiGratuityEmojiMap()).thenReturn(map);

        when(actionUser.getStringID()).thenReturn(accountId);
        when(eventAdapter.getEmojiId()).thenReturn(emojiId);
        when(eventAdapter.getEventOccurrenceUser()).thenReturn(actionUser);
        when(eventAdapter.getMessageAuthor()).thenReturn(author);
        when(eventAdapter.getGuild()).thenReturn(mock(IGuild.class));

        when(virtualCoinWalletService.getBalance(accountId)).thenReturn(balanceValue);
        when(balanceValue.getBalance()).thenReturn(value);
        when(messageSourceWrapper.getMessage(any(), any(), any(), any(), any())).thenReturn(messageString);
    }

    @Test
    public void 正常_アクション発生ユーザの金額が投げ銭より多い() throws RPCException, BalanceShortfallException {
        when(balanceValue.getBalance()).thenReturn(new BigDecimal(10000));
        doNothing().when(virtualCoinWalletService).moveEmoji(anyString(), any(), any());

        final EmojiGratuity emojiGratuity = new EmojiGratuity(virtualCoinWalletService, messageSourceWrapper, applicationProperties, dynamicSettingService);
        Locale locale = Locale.JAPANESE;
        emojiGratuity.execute(eventAdapter, locale);

        verify(messageSourceWrapper).getMessage(eq("discord.bot.emoji-gratuity.success"), eq(locale), any());
        verify(eventAdapter).sendMessage(channelId, messageString);
    }

    @Test
    public void 正常_アクション発生ユーザの金額が投げ銭とピッタリ() throws RPCException, BalanceShortfallException {
        when(balanceValue.getBalance()).thenReturn(new BigDecimal(1000));
        doNothing().when(virtualCoinWalletService).moveEmoji(anyString(), any(), any());

        final EmojiGratuity emojiGratuity = new EmojiGratuity(virtualCoinWalletService, messageSourceWrapper, applicationProperties, dynamicSettingService);
        Locale locale = Locale.JAPANESE;
        emojiGratuity.execute(eventAdapter, locale);

        verify(messageSourceWrapper).getMessage(eq("discord.bot.emoji-gratuity.success"), eq(locale), any());
        verify(eventAdapter).sendMessage(channelId, messageString);
    }

    @Test
    public void 異常_アクション発生ユーザと発言ユーザが同一_だまで終了() {

        when(eventAdapter.getMessageAuthor()).thenReturn(actionUser);

        final EmojiGratuity emojiGratuity = new EmojiGratuity(virtualCoinWalletService, messageSourceWrapper, applicationProperties, dynamicSettingService);
        emojiGratuity.execute(eventAdapter, Locale.JAPANESE);

        verify(eventAdapter).removeReaction(actionUser);
        verify(messageSourceWrapper, times(0)).getMessage(any(), any(), any());
    }

    @Test
    public void 異常_アクション発生ユーザの金額が足りない_だまで終了() {
        when(balanceValue.getBalance()).thenReturn(new BigDecimal(50));

        final EmojiGratuity emojiGratuity = new EmojiGratuity(virtualCoinWalletService, messageSourceWrapper, applicationProperties, dynamicSettingService);
        emojiGratuity.execute(eventAdapter, Locale.JAPANESE);

        verify(eventAdapter).removeReaction(actionUser);
        verify(messageSourceWrapper, times(0)).getMessage(any(), any(), any());
    }

    @Test
    public void 異常_送金に失敗() throws RPCException, BalanceShortfallException {
        when(balanceValue.getBalance()).thenReturn(new BigDecimal(1300));
        doThrow(new RPCException("failed")).when(virtualCoinWalletService).moveEmoji(anyString(), any(), any());

        final EmojiGratuity emojiGratuity = new EmojiGratuity(virtualCoinWalletService, messageSourceWrapper, applicationProperties, dynamicSettingService);
        emojiGratuity.execute(eventAdapter, Locale.JAPANESE);

        verify(eventAdapter).removeReaction(actionUser);
        verify(messageSourceWrapper, times(0)).getMessage(any(), any(), any());
    }

    // TODO locale
}