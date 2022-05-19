package jp.vcoin.gratuitybot.command.text.impl.admin;

import jp.vcoin.gratuitybot.adapter.MessageReceivedEventAdapter;
import jp.vcoin.gratuitybot.config.ApplicationProperties;
import jp.vcoin.gratuitybot.service.DynamicSettingService;
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
public class SetRandomGratuityMinLimitTest {

    @MockBean
    DynamicSettingService dynamicSettingService;
    @MockBean
    MessageSourceWrapper messageSourceWrapper;
    @MockBean
    ApplicationProperties applicationProperties;

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
    }

    @Test
    public void success() {
        // setup
        when(eventAdapter.getContent(eq(0))).thenReturn("1000");

        // when
        final SetRandomGratuityMinLimit setRandomGratuityMinLimit = new SetRandomGratuityMinLimit(dynamicSettingService, messageSourceWrapper, applicationProperties);
        setRandomGratuityMinLimit.execute(eventAdapter, Locale.JAPANESE);

        // then
        verify(messageSourceWrapper, times(0)).getMessage(eq("discord.admin.set-xxx.validate.amount-invalid"), any());
        verify(dynamicSettingService, times(1)).save(eq("gratuity.random-min-amount"), any(), anyLong(), eq("1000"));
        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.set-xxx.success"), any(), any(), anyString(), anyString());
    }

    @Test
    public void failed() {
        // setup
        when(eventAdapter.getContent(eq(0))).thenReturn("hoge");

        // when
        final SetRandomGratuityMinLimit setRandomGratuityMinLimit = new SetRandomGratuityMinLimit(dynamicSettingService, messageSourceWrapper, applicationProperties);
        setRandomGratuityMinLimit.execute(eventAdapter, Locale.JAPANESE);

        // then
        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.set-xxx.validate.amount-invalid"), any(), any());
        verify(dynamicSettingService, times(0)).save(eq("gratuity.random-min-amount"), anyLong(), eq("hoge"));
        verify(messageSourceWrapper, times(0)).getMessage(eq("discord.admin.set-xxx.success"), any(), any(), anyString(), anyString());
    }
}