package jp.vcoin.gratuitybot.command.text.impl;

import jp.vcoin.gratuitybot.adapter.MessageReceivedEventAdapter;
import jp.vcoin.gratuitybot.config.ApplicationProperties;
import jp.vcoin.gratuitybot.domain.BalanceValue;
import jp.vcoin.gratuitybot.domain.GeneralSetting;
import jp.vcoin.gratuitybot.domain.LinkageBotUser;
import jp.vcoin.gratuitybot.enumeration.DynamicSettingType;
import jp.vcoin.gratuitybot.exception.BalanceShortfallException;
import jp.vcoin.gratuitybot.exception.RPCException;
import jp.vcoin.gratuitybot.service.DynamicSettingService;
import jp.vcoin.gratuitybot.service.VirtualCoinWalletService;
import jp.vcoin.gratuitybot.util.Formatter;
import jp.vcoin.gratuitybot.util.MessageSourceWrapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import sx.blah.discord.handle.obj.IUser;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Locale;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static sx.blah.discord.util.MessageBuilder.Styles.BOLD;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class GratuityTest {

    @MockBean
    ApplicationProperties applicationProperties;
    @MockBean
    MessageSourceWrapper messageSourceWrapper;
    @MockBean
    VirtualCoinWalletService virtualCoinWalletService;
    @MockBean
    DynamicSettingService dynamicSettingService;

    MessageReceivedEventAdapter eventAdapter;
    IUser actionUser;
    IUser targetUser;
    String accountId;

    @Before
    public void setUp() {
        eventAdapter = mock(MessageReceivedEventAdapter.class);
        actionUser = mock(IUser.class);
        targetUser = mock(IUser.class);
        accountId = "accountId";

        when(actionUser.getStringID()).thenReturn(accountId);
        when(eventAdapter.getAuthor()).thenReturn(actionUser);
        when(eventAdapter.getGuildId()).thenReturn(1L);
        when(eventAdapter.getActiveUsersWithoutAuthor()).thenReturn(Collections.singletonList(targetUser));
        when(dynamicSettingService.get(eq("gratuity.random-min-amount"), anyLong(), eq(GeneralSetting.class))).thenReturn(Optional.of(new GeneralSetting("100")));

        when(applicationProperties.getDiscordEventTextCommandGratuityDefaultRandomMinAmount()).thenReturn(new BigDecimal(1000));
    }

    @Test
    public void 正常_ランダム() throws RPCException, BalanceShortfallException {
        final BalanceValue balanceValue = new BalanceValue(new BigDecimal(1000), new BigDecimal(1000));
        when(virtualCoinWalletService.getBalance(accountId)).thenReturn(balanceValue);
        when(eventAdapter.getContent(eq(0))).thenReturn("random");
        when(eventAdapter.getContent(eq(1))).thenReturn("1000");
        doNothing().when(virtualCoinWalletService).move(anyString(), any(), any());

        final Gratuity gratuity = new Gratuity(virtualCoinWalletService, messageSourceWrapper, applicationProperties, dynamicSettingService);
        gratuity.execute(eventAdapter, Locale.JAPANESE);

        verify(virtualCoinWalletService).move(anyString(), any(), eq(new BigDecimal(1000)));
        verify(messageSourceWrapper).getMessage(eq("discord.bot.gratuity.success"), any(), any());
    }

    @Test
    public void 異常_ランダム金額不正() throws RPCException, BalanceShortfallException {
        final BalanceValue balanceValue = new BalanceValue(new BigDecimal(100), new BigDecimal(100));
        when(virtualCoinWalletService.getBalance(accountId)).thenReturn(balanceValue);
        when(eventAdapter.getContent(eq(0))).thenReturn("random");
        when(eventAdapter.getContent(eq(1))).thenReturn("iiniku");

        final Gratuity gratuity = new Gratuity(virtualCoinWalletService, messageSourceWrapper, applicationProperties, dynamicSettingService);
        gratuity.execute(eventAdapter, Locale.JAPANESE);

        verify(virtualCoinWalletService, times(0)).move(anyString(), any(), any());
        verify(messageSourceWrapper).getMessage(eq("discord.bot.gratuity.validate.amount-invalid"), any(), any());
    }

    @Test
    public void 異常_ランダム最低額() throws RPCException, BalanceShortfallException {
        final BalanceValue balanceValue = new BalanceValue(new BigDecimal(1000), new BigDecimal(1000));
        when(virtualCoinWalletService.getBalance(accountId)).thenReturn(balanceValue);
        when(eventAdapter.getContent(eq(0))).thenReturn("random");
        when(eventAdapter.getContent(eq(1))).thenReturn("10");

        final Gratuity gratuity = new Gratuity(virtualCoinWalletService, messageSourceWrapper, applicationProperties, dynamicSettingService);
        gratuity.execute(eventAdapter, Locale.JAPANESE);

        verify(virtualCoinWalletService, times(0)).move(anyString(), any(), any());
        final BigDecimal bigDecimal = new BigDecimal(1000);
        verify(messageSourceWrapper).getMessage(eq("discord.bot.gratuity.validate.random.min-amount"), any(), any(), eq(BOLD + Formatter.formatValue().format(bigDecimal) + BOLD));
    }

    @Test
    public void 異常_ランダム金額不足() throws RPCException, BalanceShortfallException {
        final BalanceValue balanceValue = new BalanceValue(new BigDecimal(100), new BigDecimal(100));
        when(virtualCoinWalletService.getBalance(accountId)).thenReturn(balanceValue);
        when(eventAdapter.getContent(eq(0))).thenReturn("random");
        when(eventAdapter.getContent(eq(1))).thenReturn("10000");

        final Gratuity gratuity = new Gratuity(virtualCoinWalletService, messageSourceWrapper, applicationProperties, dynamicSettingService);
        gratuity.execute(eventAdapter, Locale.JAPANESE);

        verify(virtualCoinWalletService, times(0)).move(anyString(), any(), any());
        verify(messageSourceWrapper).getMessage(eq("discord.bot.gratuity.validate.amount-shortfall"), any(), any());
    }

    @Test
    public void 正常_ユーザ指定() throws RPCException, BalanceShortfallException {
        final BalanceValue balanceValue = new BalanceValue(new BigDecimal(100), new BigDecimal(100));
        long id = 449456319871713280L;
        when(virtualCoinWalletService.getBalance(accountId)).thenReturn(balanceValue);
        when(eventAdapter.getContent(eq(0))).thenReturn("<@" + id + ">");
        when(eventAdapter.getContent(eq(1))).thenReturn("100");
        when(eventAdapter.getUserByID(id)).thenReturn(targetUser);
        when(targetUser.isBot()).thenReturn(false);
        when(targetUser.getStringID()).thenReturn(String.valueOf(id));
        doNothing().when(virtualCoinWalletService).move(anyString(), any(), any());

        final Gratuity gratuity = new Gratuity(virtualCoinWalletService, messageSourceWrapper, applicationProperties, dynamicSettingService);
        gratuity.execute(eventAdapter, Locale.JAPANESE);

        verify(virtualCoinWalletService).move(anyString(), any(), eq(new BigDecimal(100)));
        verify(messageSourceWrapper).getMessage(eq("discord.bot.gratuity.success"), any(), any());
    }

    @Test
    public void 異常_ユーザ指定不正() throws RPCException, BalanceShortfallException {
        final BalanceValue balanceValue = new BalanceValue(new BigDecimal(100), new BigDecimal(100));
        when(virtualCoinWalletService.getBalance(accountId)).thenReturn(balanceValue);
        when(eventAdapter.getContent(eq(0))).thenReturn("@hoge");
        when(eventAdapter.getContent(eq(1))).thenReturn("100");

        final Gratuity gratuity = new Gratuity(virtualCoinWalletService, messageSourceWrapper, applicationProperties, dynamicSettingService);
        gratuity.execute(eventAdapter, Locale.JAPANESE);

        verify(virtualCoinWalletService, times(0)).move(anyString(), any(), any());
        verify(messageSourceWrapper).getMessage(eq("discord.bot.gratuity.validate.target-user-invalid"), any(), any());
    }

    @Test
    public void 異常_ユーザ指定未存在() throws RPCException, BalanceShortfallException {
        final BalanceValue balanceValue = new BalanceValue(new BigDecimal(100), new BigDecimal(100));
        long id = 449456319871713280L;
        when(virtualCoinWalletService.getBalance(accountId)).thenReturn(balanceValue);
        when(eventAdapter.getContent(eq(0))).thenReturn("<@" + id + ">");
        when(eventAdapter.getContent(eq(1))).thenReturn("100");
        when(eventAdapter.getUserByID(id)).thenReturn(null);

        final Gratuity gratuity = new Gratuity(virtualCoinWalletService, messageSourceWrapper, applicationProperties, dynamicSettingService);
        gratuity.execute(eventAdapter, Locale.JAPANESE);

        verify(virtualCoinWalletService, times(0)).move(anyString(), any(), any());
        verify(messageSourceWrapper).getMessage(eq("discord.bot.gratuity.validate.target-user-not.exists"), any(), any());
    }

    @Test
    public void 異常_ユーザ指定BOT_だまで終了() throws RPCException, BalanceShortfallException {
        final BalanceValue balanceValue = new BalanceValue(new BigDecimal(100), new BigDecimal(100));
        long id = 449456319871713280L;
        when(virtualCoinWalletService.getBalance(accountId)).thenReturn(balanceValue);
        when(eventAdapter.getContent(eq(0))).thenReturn("<@" + id + ">");
        when(eventAdapter.getContent(eq(1))).thenReturn("100");
        when(eventAdapter.getUserByID(id)).thenReturn(targetUser);
        when(targetUser.isBot()).thenReturn(true);

        final Gratuity gratuity = new Gratuity(virtualCoinWalletService, messageSourceWrapper, applicationProperties, dynamicSettingService);
        gratuity.execute(eventAdapter, Locale.JAPANESE);

        verify(virtualCoinWalletService, times(0)).move(anyString(), any(), any());
        verify(messageSourceWrapper, times(0)).getMessage(any(), any(), any());
    }

    @Test
    public void 正常_ユーザ指定連携BOT() throws RPCException, BalanceShortfallException {
        final BalanceValue balanceValue = new BalanceValue(new BigDecimal(100), new BigDecimal(100));
        long id = 449456319871713280L;
        when(virtualCoinWalletService.getBalance(accountId)).thenReturn(balanceValue);
        when(eventAdapter.getContent(eq(0))).thenReturn("<@" + id + ">");
        when(eventAdapter.getContent(eq(1))).thenReturn("100");
        when(eventAdapter.getUserByID(id)).thenReturn(targetUser);
        when(targetUser.isBot()).thenReturn(true);
        when(targetUser.getLongID()).thenReturn(id);
        when(targetUser.getStringID()).thenReturn(String.valueOf(id));
        when(dynamicSettingService.getList(eq(DynamicSettingType.LinkageBotUser.getKey()), anyLong(), eq(LinkageBotUser.class))).thenReturn(java.util.Arrays.asList(LinkageBotUser.builder().id(id).forGratuity(true).build()));

        final Gratuity gratuity = new Gratuity(virtualCoinWalletService, messageSourceWrapper, applicationProperties, dynamicSettingService);
        gratuity.execute(eventAdapter, Locale.JAPANESE);

        verify(virtualCoinWalletService).move(anyString(), any(), eq(new BigDecimal(100)));
        verify(messageSourceWrapper).getMessage(eq("discord.bot.gratuity.success"), any(), any());
    }

    @Test
    public void 異常_ユーザ指定自身() throws RPCException, BalanceShortfallException {
        final BalanceValue balanceValue = new BalanceValue(new BigDecimal(100), new BigDecimal(100));
        long id = 449456319871713280L;
        when(virtualCoinWalletService.getBalance(accountId)).thenReturn(balanceValue);
        when(eventAdapter.getContent(eq(0))).thenReturn("<@" + id + ">");
        when(eventAdapter.getContent(eq(1))).thenReturn("100");
        when(eventAdapter.getUserByID(id)).thenReturn(actionUser);
        when(targetUser.isBot()).thenReturn(false);

        final Gratuity gratuity = new Gratuity(virtualCoinWalletService, messageSourceWrapper, applicationProperties, dynamicSettingService);
        gratuity.execute(eventAdapter, Locale.JAPANESE);

        verify(virtualCoinWalletService, times(0)).move(anyString(), any(), any());
        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.bot.gratuity.validate.target-user-self"), any(), any());
    }

    @Test
    public void 異常_ユーザ指定金額不正() throws RPCException, BalanceShortfallException {
        final BalanceValue balanceValue = new BalanceValue(new BigDecimal(100), new BigDecimal(100));
        long id = 449456319871713280L;
        when(virtualCoinWalletService.getBalance(accountId)).thenReturn(balanceValue);
        when(eventAdapter.getContent(eq(0))).thenReturn("<@" + id + ">");
        when(eventAdapter.getContent(eq(1))).thenReturn("10@0");
        when(eventAdapter.getUserByID(id)).thenReturn(targetUser);
        when(targetUser.isBot()).thenReturn(false);
        when(targetUser.getStringID()).thenReturn(String.valueOf(id));

        final Gratuity gratuity = new Gratuity(virtualCoinWalletService, messageSourceWrapper, applicationProperties, dynamicSettingService);
        gratuity.execute(eventAdapter, Locale.JAPANESE);

        verify(virtualCoinWalletService, times(0)).move(anyString(), any(), any());
        verify(messageSourceWrapper).getMessage(eq("discord.bot.gratuity.validate.amount-invalid"), any(), any());
    }

    @Test
    public void 正常_ユーザ指定金額不足() throws RPCException, BalanceShortfallException {
        final BalanceValue balanceValue = new BalanceValue(new BigDecimal(100), new BigDecimal(100));
        long id = 449456319871713280L;
        when(virtualCoinWalletService.getBalance(accountId)).thenReturn(balanceValue);
        when(eventAdapter.getContent(eq(0))).thenReturn("<@" + id + ">");
        when(eventAdapter.getContent(eq(1))).thenReturn("10000");
        when(eventAdapter.getUserByID(id)).thenReturn(targetUser);
        when(targetUser.isBot()).thenReturn(false);
        when(targetUser.getStringID()).thenReturn(String.valueOf(id));

        final Gratuity gratuity = new Gratuity(virtualCoinWalletService, messageSourceWrapper, applicationProperties, dynamicSettingService);
        gratuity.execute(eventAdapter, Locale.JAPANESE);

        verify(virtualCoinWalletService, times(0)).move(anyString(), any(), eq(new BigDecimal(10000)));
        verify(messageSourceWrapper).getMessage(eq("discord.bot.gratuity.validate.amount-shortfall"), any(), any());
    }
}