package jp.vcoin.gratuitybot.command.text.impl;

import jp.vcoin.gratuitybot.adapter.MessageReceivedEventAdapter;
import jp.vcoin.gratuitybot.domain.BalanceValue;
import jp.vcoin.gratuitybot.service.VirtualCoinWalletService;
import jp.vcoin.gratuitybot.util.MessageSourceWrapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import sx.blah.discord.handle.obj.IUser;

import java.math.BigDecimal;
import java.util.Locale;

import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class BalanceTest {

    @MockBean
    MessageSourceWrapper messageSourceWrapper;
    @MockBean
    VirtualCoinWalletService virtualCoinWalletService;

    MessageReceivedEventAdapter eventAdapter;
    long serverId;
    long channelId;
    String messageString;
    IUser actionUser;
    String accountId;

    @Before
    public void setUp() {
        serverId = 200L;
        channelId = 300L;
        accountId = "accountId";
        messageString = "";
        eventAdapter = mock(MessageReceivedEventAdapter.class);
        actionUser = mock(IUser.class);

        when(actionUser.getStringID()).thenReturn(accountId);
        when(eventAdapter.getAuthor()).thenReturn(actionUser);

        when(messageSourceWrapper.getMessage(any(), any(), any(), any(), any())).thenReturn(messageString);
    }

    @Test
    public void 正常_全額と検証済みが同一() {
        final BalanceValue balanceValue = new BalanceValue(new BigDecimal(100), new BigDecimal(100));
        when(virtualCoinWalletService.getBalance(accountId)).thenReturn(balanceValue);

        final Balance balance = new Balance(virtualCoinWalletService, messageSourceWrapper);
        balance.execute(eventAdapter, Locale.JAPANESE);

        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.bot.balance"), any(), any(), eq("**100.0**"));
        verify(messageSourceWrapper, times(0)).getMessage(eq("discord.bot.balance_verification"), any(), any());
    }

    @Test
    public void 正常_全額と検証済みが同一でない() {
        final BalanceValue balanceValue = new BalanceValue(new BigDecimal(100), new BigDecimal(1000));
        when(virtualCoinWalletService.getBalance(accountId)).thenReturn(balanceValue);

        final Balance balance = new Balance(virtualCoinWalletService, messageSourceWrapper);
        balance.execute(eventAdapter, Locale.JAPANESE);

        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.bot.balance"), any(), any(), eq("**100.0**"));
        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.bot.balance_verification"), any(), eq("**900.0**"));
    }
}