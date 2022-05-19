package jp.vcoin.gratuitybot.command.text.impl;

import jp.vcoin.gratuitybot.adapter.MessageReceivedEventAdapter;
import jp.vcoin.gratuitybot.service.VirtualCoinWalletService;
import jp.vcoin.gratuitybot.util.MessageSourceWrapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import sx.blah.discord.handle.obj.IUser;

import java.util.Locale;

import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class DepositTest {

    @MockBean
    MessageSourceWrapper messageSourceWrapper;
    @MockBean
    VirtualCoinWalletService virtualCoinWalletService;

    MessageReceivedEventAdapter eventAdapter;
    String messageString;
    IUser actionUser;
    String accountId;

    @Before
    public void setUp() {

        accountId = "accountId";
        messageString = "";
        eventAdapter = mock(MessageReceivedEventAdapter.class);
        actionUser = mock(IUser.class);

        when(actionUser.getStringID()).thenReturn(accountId);
        when(eventAdapter.getAuthor()).thenReturn(actionUser);

        when(messageSourceWrapper.getMessage(any(), any(), any(), any(), any())).thenReturn(messageString);
    }

    @Test
    public void execute() {
        final Deposit deposit = new Deposit(virtualCoinWalletService, messageSourceWrapper);
        deposit.execute(eventAdapter, Locale.JAPANESE);

        verify(virtualCoinWalletService).getNewAddress(accountId);
        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.bot.deposit"), any(), any(), anyString());
    }
}