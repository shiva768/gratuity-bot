package jp.vcoin.gratuitybot.command.text.impl;

import jp.vcoin.gratuitybot.adapter.MessageReceivedEventAdapter;
import jp.vcoin.gratuitybot.config.ApplicationProperties;
import jp.vcoin.gratuitybot.domain.BalanceValue;
import jp.vcoin.gratuitybot.domain.TransactionValue;
import jp.vcoin.gratuitybot.exception.BalanceShortfallException;
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
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Locale;
import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class WithdrawTest {


    @MockBean
    ApplicationProperties applicationProperties;
    @MockBean
    MessageSourceWrapper messageSourceWrapper;
    @MockBean
    VirtualCoinWalletService virtualCoinWalletService;

    MessageReceivedEventAdapter eventAdapter;
    IUser actionUser;
    IUser targetUser;
    String accountId;
    String targetAddress;
    String transactionId;

    @Before
    public void setUp() throws BalanceShortfallException {
        eventAdapter = mock(MessageReceivedEventAdapter.class);
        actionUser = mock(IUser.class);
        targetUser = mock(IUser.class);
        accountId = "accountId";
        targetAddress = "targetAddress";
        transactionId = "transactionId";

        when(actionUser.getStringID()).thenReturn(accountId);
        when(eventAdapter.getAuthor()).thenReturn(actionUser);
        when(eventAdapter.getActiveUsersWithoutAuthor()).thenReturn(Collections.singletonList(targetUser));
        when(virtualCoinWalletService.validateAddress(targetAddress)).thenReturn(true);
        when(virtualCoinWalletService.sendMany(eq(accountId), eq(targetAddress), any(BigDecimal.class))).thenReturn(Optional.of(transactionId));
        when(virtualCoinWalletService.getTransaction(transactionId)).thenReturn(
                new TransactionValue(transactionId, new BigDecimal(900), new BigDecimal(100), LocalDateTime.now(), LocalDateTime.now())
        );
        when(applicationProperties.getDiscordEventTextCommandWithdrawMinAmount()).thenReturn(new BigDecimal(10));

    }

    @Test
    public void 正常() {
        final BalanceValue balanceValue = new BalanceValue(new BigDecimal(1000), new BigDecimal(1000));
        when(virtualCoinWalletService.getBalance(accountId)).thenReturn(balanceValue);
        when(eventAdapter.getContent(eq(0))).thenReturn(targetAddress);
        when(eventAdapter.getContent(eq(1))).thenReturn("1000");

        final Withdraw withdraw = new Withdraw(virtualCoinWalletService, messageSourceWrapper, applicationProperties);
        withdraw.execute(eventAdapter, Locale.JAPANESE);

        verify(messageSourceWrapper).getMessage(eq("discord.bot.withdraw.success"), any(), any());
    }

    @Test
    public void 正常_全額() {
        final BalanceValue balanceValue = new BalanceValue(new BigDecimal(1000), new BigDecimal(1000));
        when(virtualCoinWalletService.getBalance(accountId)).thenReturn(balanceValue);
        when(eventAdapter.getContent(eq(0))).thenReturn(targetAddress);
        when(eventAdapter.getContent(eq(1))).thenReturn("all");

        final Withdraw withdraw = new Withdraw(virtualCoinWalletService, messageSourceWrapper, applicationProperties);
        withdraw.execute(eventAdapter, Locale.JAPANESE);

        verify(messageSourceWrapper).getMessage(eq("discord.bot.withdraw.success"), any(), any());
    }

    @Test
    public void 異常_指定アドレス不正_ダマで終了() {
        final BalanceValue balanceValue = new BalanceValue(new BigDecimal(1000), new BigDecimal(1000));
        when(virtualCoinWalletService.getBalance(accountId)).thenReturn(balanceValue);
        when(eventAdapter.getContent(eq(0))).thenReturn(targetAddress);
        when(eventAdapter.getContent(eq(1))).thenReturn("1000");
        when(virtualCoinWalletService.validateAddress(targetAddress)).thenReturn(false);

        final Withdraw withdraw = new Withdraw(virtualCoinWalletService, messageSourceWrapper, applicationProperties);
        withdraw.execute(eventAdapter, Locale.JAPANESE);

        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.bot.withdraw.validate.address-invalid"), any(), any());
        verify(messageSourceWrapper, times(0)).getMessage(eq("discord.bot.withdraw.success"), any(), any());
    }

    @Test
    public void 異常_引き出し最少額以下() {
        final BalanceValue balanceValue = new BalanceValue(new BigDecimal(1000), new BigDecimal(1000));
        when(virtualCoinWalletService.getBalance(accountId)).thenReturn(balanceValue);
        when(eventAdapter.getContent(eq(0))).thenReturn(targetAddress);
        when(eventAdapter.getContent(eq(1))).thenReturn("10");
        when(applicationProperties.getDiscordEventTextCommandWithdrawMinAmount()).thenReturn(new BigDecimal(100));

        final Withdraw withdraw = new Withdraw(virtualCoinWalletService, messageSourceWrapper, applicationProperties);
        withdraw.execute(eventAdapter, Locale.JAPANESE);

        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.bot.withdraw.validate.minimum-amount-shortfall"), any(), any());
    }

    @Test
    public void 異常_全額引き出し最少額以下() {
        final BalanceValue balanceValue = new BalanceValue(new BigDecimal(10), new BigDecimal(10));
        when(virtualCoinWalletService.getBalance(accountId)).thenReturn(balanceValue);
        when(eventAdapter.getContent(eq(0))).thenReturn(targetAddress);
        when(eventAdapter.getContent(eq(1))).thenReturn("all");
        when(applicationProperties.getDiscordEventTextCommandWithdrawMinAmount()).thenReturn(new BigDecimal(100));

        final Withdraw withdraw = new Withdraw(virtualCoinWalletService, messageSourceWrapper, applicationProperties);
        withdraw.execute(eventAdapter, Locale.JAPANESE);

        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.bot.withdraw.validate.minimum-amount-shortfall"), any(), any());
    }

    @Test
    public void 異常_金額不正() {
        final BalanceValue balanceValue = new BalanceValue(new BigDecimal(1000), new BigDecimal(1000));
        when(virtualCoinWalletService.getBalance(accountId)).thenReturn(balanceValue);
        when(eventAdapter.getContent(eq(0))).thenReturn(targetAddress);
        when(eventAdapter.getContent(eq(1))).thenReturn("hoge");

        final Withdraw withdraw = new Withdraw(virtualCoinWalletService, messageSourceWrapper, applicationProperties);
        withdraw.execute(eventAdapter, Locale.JAPANESE);

        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.bot.withdraw.validate.amount-invalid"), any(), any());
    }

    @Test
    public void 異常_金額不足() {
        final BalanceValue balanceValue = new BalanceValue(new BigDecimal(1000), new BigDecimal(1000));
        when(virtualCoinWalletService.getBalance(accountId)).thenReturn(balanceValue);
        when(eventAdapter.getContent(eq(0))).thenReturn(targetAddress);
        when(eventAdapter.getContent(eq(1))).thenReturn("10000");

        final Withdraw withdraw = new Withdraw(virtualCoinWalletService, messageSourceWrapper, applicationProperties);
        withdraw.execute(eventAdapter, Locale.JAPANESE);

        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.bot.withdraw.validate.amount-shortfall"), any(), any());
    }

    @Test
    public void 異常_送金失敗_ダマで終了() throws BalanceShortfallException {
        final BalanceValue balanceValue = new BalanceValue(new BigDecimal(1000), new BigDecimal(1000));
        when(virtualCoinWalletService.getBalance(accountId)).thenReturn(balanceValue);
        when(eventAdapter.getContent(eq(0))).thenReturn(targetAddress);
        when(eventAdapter.getContent(eq(1))).thenReturn("1000");
        when(virtualCoinWalletService.sendMany(eq(accountId), eq(targetAddress), any(BigDecimal.class))).thenReturn(Optional.empty());

        final Withdraw withdraw = new Withdraw(virtualCoinWalletService, messageSourceWrapper, applicationProperties);
        withdraw.execute(eventAdapter, Locale.JAPANESE);

        verify(messageSourceWrapper, times(0)).getMessage(eq("discord.bot.withdraw.success"), any(), any());
    }
}