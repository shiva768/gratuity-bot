package jp.vcoin.gratuitybot.command.text.impl.admin;

import jp.vcoin.gratuitybot.adapter.MessageReceivedEventAdapter;
import jp.vcoin.gratuitybot.config.ApplicationProperties;
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

import java.util.Arrays;
import java.util.Locale;

import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class SetLanguageTest {
    @MockBean
    DynamicSettingService dynamicSettingService;
    @MockBean
    MessageSourceWrapper messageSourceWrapper;
    @MockBean
    ApplicationProperties applicationProperties;
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
        doNothing().when(dynamicSettingService).save(eq(DynamicSettingType.Language.getKey()), anyString(), anyLong(), captor.capture());
        doNothing().when(dynamicSettingService).save(eq(DynamicSettingType.Language.getKey()), isNull(), anyLong(), captor.capture());
    }

    @Test
    public void 成功_server() {
        when(eventAdapter.getContent(0)).thenReturn("en");
        when(applicationProperties.getDiscordSupportLanguages()).thenReturn(Arrays.asList("en", "ja"));

        new SetLanguage(dynamicSettingService, messageSourceWrapper, applicationProperties, discordValidatorService).execute(eventAdapter, locale);
        verify(dynamicSettingService, times(1)).save(eq(DynamicSettingType.Language.getKey()), isNull(), anyLong(), anyString());
        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.set-specific-xxx.success"), any(), any(), any());
        Assert.assertThat(captor.getValue(), is("en"));
    }

    @Test
    public void 成功_channel_numeric() {
        when(eventAdapter.getContent(0)).thenReturn("en");
        when(eventAdapter.getContent(1)).thenReturn("10");
        when(applicationProperties.getDiscordSupportLanguages()).thenReturn(Arrays.asList("en", "ja"));
        when(eventAdapter.getChannelByStringId("10")).thenReturn(mock(IChannel.class));

        new SetLanguage(dynamicSettingService, messageSourceWrapper, applicationProperties, discordValidatorService).execute(eventAdapter, locale);
        verify(dynamicSettingService, times(1)).save(eq(DynamicSettingType.Language.getKey()), anyString(), anyLong(), anyString());
        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.set-specific-xxx.success"), any(), any(), any());
        Assert.assertThat(captor.getValue(), is("en"));
    }

    @Test
    public void 成功_channel_channel() {
        when(eventAdapter.getContent(0)).thenReturn("en");
        when(eventAdapter.getContent(1)).thenReturn("<#10>");
        when(applicationProperties.getDiscordSupportLanguages()).thenReturn(Arrays.asList("en", "ja"));
        when(eventAdapter.getChannelByStringId("10")).thenReturn(mock(IChannel.class));

        new SetLanguage(dynamicSettingService, messageSourceWrapper, applicationProperties, discordValidatorService).execute(eventAdapter, locale);
        verify(dynamicSettingService, times(1)).save(eq(DynamicSettingType.Language.getKey()), anyString(), anyLong(), anyString());
        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.set-specific-xxx.success"), any(), any(), any());
        Assert.assertThat(captor.getValue(), is("en"));
    }


    @Test
    public void 失敗_言語が空() {
        when(eventAdapter.getContent(0)).thenReturn("");
        when(eventAdapter.getContent(1)).thenReturn("<#10>");
        when(applicationProperties.getDiscordSupportLanguages()).thenReturn(Arrays.asList("en", "ja"));
        when(eventAdapter.getChannelByStringId("10")).thenReturn(mock(IChannel.class));

        new SetLanguage(dynamicSettingService, messageSourceWrapper, applicationProperties, discordValidatorService).execute(eventAdapter, locale);
        verify(dynamicSettingService, times(0)).save(eq(DynamicSettingType.Language.getKey()), anyString(), anyLong(), anyString());
        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.set-language.validate.language-required"), any(), any());
    }

    @Test
    public void 失敗_存在しないchannelId() {
        when(eventAdapter.getContent(0)).thenReturn("en");
        when(eventAdapter.getContent(1)).thenReturn("10");
        when(eventAdapter.getChannelByStringId("10")).thenReturn(null);

        new SetLanguage(dynamicSettingService, messageSourceWrapper, applicationProperties, discordValidatorService).execute(eventAdapter, locale);
        verify(eventAdapter, times(1)).getChannelByStringId("10");
        verify(dynamicSettingService, times(0)).save(eq(DynamicSettingType.Language.getKey()), anyString(), anyLong(), anyString());
        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.common.validate.channel.invalid.not-exists"), any(), any());
    }

    @Test
    public void 失敗_非対応言語() {
        when(eventAdapter.getContent(0)).thenReturn("en1");
        when(eventAdapter.getContent(1)).thenReturn("10");
        when(applicationProperties.getDiscordSupportLanguages()).thenReturn(Arrays.asList("en", "ja"));
        when(eventAdapter.getChannelByStringId("10")).thenReturn(mock(IChannel.class));

        new SetLanguage(dynamicSettingService, messageSourceWrapper, applicationProperties, discordValidatorService).execute(eventAdapter, locale);
        verify(eventAdapter, times(1)).getChannelByStringId("10");
        verify(dynamicSettingService, times(0)).save(eq(DynamicSettingType.Language.getKey()), anyString(), anyLong(), anyString());
        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.set-language.validate.not-support-language"), any(), any());
    }
}