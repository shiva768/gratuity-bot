package jp.vcoin.gratuitybot.command.text.impl.admin;

import jp.vcoin.gratuitybot.adapter.MessageReceivedEventAdapter;
import jp.vcoin.gratuitybot.enumeration.DynamicSettingType;
import jp.vcoin.gratuitybot.service.DiscordValidatorService;
import jp.vcoin.gratuitybot.service.DynamicSettingService;
import jp.vcoin.gratuitybot.service.impl.DiscordValidatorServiceImpl;
import jp.vcoin.gratuitybot.util.MessageSourceWrapper;
import jp.vcoin.gratuitybot.domain.LanguageSetting;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

import java.util.Locale;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class RemoveLanguageTest {

    @MockBean
    DynamicSettingService dynamicSettingService;
    @MockBean
    MessageSourceWrapper messageSourceWrapper;
    @SpyBean(DiscordValidatorServiceImpl.class)
    DiscordValidatorService discordValidatorService;

    @Mock
    MessageReceivedEventAdapter eventAdapter;
    Locale locale = Locale.JAPANESE;

    @Before
    public void setup() {
        IUser user = mock(IUser.class);
        when(eventAdapter.getAuthor()).thenReturn(user);
        doNothing().when(eventAdapter).sendMessage(any());
        when(dynamicSettingService.delete(eq(DynamicSettingType.Language.getKey()), anyString(), anyLong())).thenReturn(true);
    }

    @Test
    public void 正常() {
        String content = "100";
        when(eventAdapter.getContent(0)).thenReturn(content);
        when(eventAdapter.getChannelByStringId(content)).thenReturn(mock(IChannel.class));
        when(dynamicSettingService.get(eq(DynamicSettingType.Language.getKey()), any(), anyLong(), any())).thenReturn(Optional.of(mock(LanguageSetting.class)));
        new RemoveLanguage(dynamicSettingService, messageSourceWrapper, discordValidatorService).execute(eventAdapter, locale);
        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.remove-specific-xxx.success"), any(), any());
    }

    @Test
    public void 異常_channelIdが空() {
        when(eventAdapter.getContent(0)).thenReturn("");
        new RemoveLanguage(dynamicSettingService, messageSourceWrapper, discordValidatorService).execute(eventAdapter, locale);
        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.common.validate.channel.invalid.required"), any(), any());
    }

    @Test
    public void 異常_channelIdが数字でない() {
        String content = "hogege";
        when(eventAdapter.getContent(0)).thenReturn(content);
        new RemoveLanguage(dynamicSettingService, messageSourceWrapper, discordValidatorService).execute(eventAdapter, locale);
        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.remove-specific-xxx.validate.not-exists-setting"), any(), any());
    }

    @Test
    public void 異常_channelが存在しない() {
        String content = "100";
        when(eventAdapter.getContent(0)).thenReturn(content);
        when(eventAdapter.getChannelByStringId(content)).thenReturn(null);
        new RemoveLanguage(dynamicSettingService, messageSourceWrapper, discordValidatorService).execute(eventAdapter, locale);
        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.remove-specific-xxx.validate.not-exists-setting"), any(), any());
    }

    @Test
    public void 異常_削除しようとした言語の設定が存在しない() {
        String content = "100";
        when(eventAdapter.getContent(0)).thenReturn(content);
        when(eventAdapter.getChannelByStringId(content)).thenReturn(mock(IChannel.class));
        when(dynamicSettingService.get(eq(DynamicSettingType.Language.getKey()), any(), anyLong(), any())).thenReturn(Optional.empty());
        new RemoveLanguage(dynamicSettingService, messageSourceWrapper, discordValidatorService).execute(eventAdapter, locale);
        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.remove-specific-xxx.validate.not-exists-setting"), any(), any());
    }

    @Test
    public void 異常_1件も削除していない() {
        String content = "100";
        when(eventAdapter.getContent(0)).thenReturn(content);
        when(eventAdapter.getChannelByStringId(content)).thenReturn(mock(IChannel.class));
        when(dynamicSettingService.delete(eq(DynamicSettingType.Language.getKey()), anyString(), anyLong())).thenReturn(false);
        when(dynamicSettingService.get(eq(DynamicSettingType.Language.getKey()), any(), anyLong(), any())).thenReturn(Optional.of(mock(LanguageSetting.class)));
        new RemoveLanguage(dynamicSettingService, messageSourceWrapper, discordValidatorService).execute(eventAdapter, locale);
        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.remove-specific-xxx.failed"), any(), any());
    }
}