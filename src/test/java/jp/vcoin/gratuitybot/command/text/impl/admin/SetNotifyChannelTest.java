package jp.vcoin.gratuitybot.command.text.impl.admin;

import jp.vcoin.gratuitybot.adapter.MessageReceivedEventAdapter;
import jp.vcoin.gratuitybot.enumeration.DynamicSettingType;
import jp.vcoin.gratuitybot.service.DiscordValidatorService;
import jp.vcoin.gratuitybot.service.DynamicSettingService;
import jp.vcoin.gratuitybot.service.impl.DiscordValidatorServiceImpl;
import jp.vcoin.gratuitybot.util.MessageSourceWrapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

import java.util.Locale;

import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class SetNotifyChannelTest {
    @MockBean
    DynamicSettingService dynamicSettingService;
    @MockBean
    MessageSourceWrapper messageSourceWrapper;
    @SpyBean(DiscordValidatorServiceImpl.class)
    DiscordValidatorService discordValidatorService;

    ArgumentCaptor<String> captor;
    @Mock
    MessageReceivedEventAdapter eventAdapter;
    Locale locale = Locale.JAPANESE;

    @Before
    public void setup() {
        IUser user = mock(IUser.class);
        when(eventAdapter.getAuthor()).thenReturn(user);
        doNothing().when(eventAdapter).sendMessage(any());
        captor = ArgumentCaptor.forClass(String.class);
        doNothing().when(dynamicSettingService).save(eq(DynamicSettingType.NotifyChannel.getKey()), anyLong(), captor.capture());
    }

    @Test
    public void 成功_numeric() {
        when(eventAdapter.getContent(0)).thenReturn("10");
        when(eventAdapter.getChannelByStringId(anyString())).thenReturn(mock(IChannel.class));

        new SetNotifyChannel(dynamicSettingService, messageSourceWrapper, discordValidatorService).execute(eventAdapter, locale);
        verify(eventAdapter, times(1)).getChannelByStringId("10");
        verify(dynamicSettingService, times(1)).save(eq(DynamicSettingType.NotifyChannel.getKey()), anyLong(), anyString());
        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.set-specific-xxx.success"), any(), any(), any());
        Assert.assertThat(captor.getValue(), is("10"));
    }

    @Test
    public void 成功_channel() {
        when(eventAdapter.getContent(0)).thenReturn("<#10>");
        when(eventAdapter.getChannelByStringId("10")).thenReturn(mock(IChannel.class));

        new SetNotifyChannel(dynamicSettingService, messageSourceWrapper, discordValidatorService).execute(eventAdapter, locale);
        verify(eventAdapter, times(1)).getChannelByStringId("10");
        verify(dynamicSettingService, times(1)).save(eq(DynamicSettingType.NotifyChannel.getKey()), anyLong(), anyString());
        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.set-specific-xxx.success"), any(), any(), any());
        Assert.assertThat(captor.getValue(), is("10"));
    }

    @Test
    public void 失敗_ID空() {
        when(eventAdapter.getContent(0)).thenReturn("");

        new SetNotifyChannel(dynamicSettingService, messageSourceWrapper, discordValidatorService).execute(eventAdapter, locale);
        verify(dynamicSettingService, times(0)).save(eq(DynamicSettingType.NotifyChannel.getKey()), anyLong(), anyString());
        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.common.validate.channel.invalid.required"), any(), any());
    }

    @Test
    public void 失敗_IDが存在しないchannelId() {
        when(eventAdapter.getContent(0)).thenReturn("10");
        when(eventAdapter.getChannelByStringId("10")).thenReturn(null);

        new SetNotifyChannel(dynamicSettingService, messageSourceWrapper, discordValidatorService).execute(eventAdapter, locale);
        verify(eventAdapter, times(1)).getChannelByStringId("10");
        verify(dynamicSettingService, times(0)).save(eq(DynamicSettingType.NotifyChannel.getKey()), anyLong(), anyString());
        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.common.validate.channel.invalid.not-exists"), any(), any());
    }

    @Test
    public void 失敗_IDが存在しないchannel() {
        when(eventAdapter.getContent(0)).thenReturn("<#10>");
        when(eventAdapter.getChannelByStringId("10")).thenReturn(null);

        new SetNotifyChannel(dynamicSettingService, messageSourceWrapper, discordValidatorService).execute(eventAdapter, locale);
        verify(eventAdapter, times(1)).getChannelByStringId("10");
        verify(dynamicSettingService, times(0)).save(eq(DynamicSettingType.NotifyChannel.getKey()), anyLong(), anyString());
        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.common.validate.channel.invalid.not-exists"), any(), any());
    }

    @Test
    public void 失敗_IDがただの文字列() {
        when(eventAdapter.getContent(0)).thenReturn("<#hohohoh>");
        when(eventAdapter.getChannelByStringId("<#hohohoh>")).thenReturn(null);

        new SetNotifyChannel(dynamicSettingService, messageSourceWrapper, discordValidatorService).execute(eventAdapter, locale);
        verify(eventAdapter, times(1)).getChannelByStringId("<#hohohoh>");
        verify(dynamicSettingService, times(0)).save(eq(DynamicSettingType.NotifyChannel.getKey()), anyLong(), anyString());
        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.common.validate.channel.invalid.not-exists"), any(), any());
    }
}