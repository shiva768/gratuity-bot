package jp.vcoin.gratuitybot.command.text.impl.admin;

import jp.vcoin.gratuitybot.adapter.MessageReceivedEventAdapter;
import jp.vcoin.gratuitybot.domain.LinkageBotUser;
import jp.vcoin.gratuitybot.enumeration.DynamicSettingType;
import jp.vcoin.gratuitybot.service.DiscordValidatorService;
import jp.vcoin.gratuitybot.service.DynamicSettingService;
import jp.vcoin.gratuitybot.service.impl.DiscordValidatorServiceImpl;
import jp.vcoin.gratuitybot.util.MessageSourceWrapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import sx.blah.discord.handle.obj.IUser;

import java.util.Locale;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class RemoveLinkageBotUserTest {

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
        when(dynamicSettingService.delete(eq(DynamicSettingType.LinkageBotUser.getKey()), anyString(), anyLong())).thenReturn(true);
    }

    @Test
    public void 正常() {
        String content = "100";
        when(eventAdapter.getContent(0)).thenReturn(content);
        final IUser botUser = mock(IUser.class);
        when(botUser.isBot()).thenReturn(true);
        when(eventAdapter.getUserByStringId(content)).thenReturn(botUser);
        when(dynamicSettingService.get(eq(DynamicSettingType.LinkageBotUser.getKey()), any(), anyLong(), any())).thenReturn(Optional.of(mock(LinkageBotUser.class)));
        new RemoveLinkageBotUser(dynamicSettingService, messageSourceWrapper, discordValidatorService).execute(eventAdapter, locale);
        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.remove-specific-xxx.success"), any(), any());
    }

    @Test
    public void 正常_user形式的なやつ() {
        String content = "<@100>";
        when(eventAdapter.getContent(0)).thenReturn(content);
        final IUser botUser = mock(IUser.class);
        when(botUser.isBot()).thenReturn(true);
        when(eventAdapter.getUserByStringId("100")).thenReturn(botUser);
        when(dynamicSettingService.get(eq(DynamicSettingType.LinkageBotUser.getKey()), any(), anyLong(), any())).thenReturn(Optional.of(mock(LinkageBotUser.class)));
        new RemoveLinkageBotUser(dynamicSettingService, messageSourceWrapper, discordValidatorService).execute(eventAdapter, locale);
        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.remove-specific-xxx.success"), any(), any());
    }

    @Test
    public void 異常_userIdが空() {
        when(eventAdapter.getContent(0)).thenReturn("");
        new RemoveLinkageBotUser(dynamicSettingService, messageSourceWrapper, discordValidatorService).execute(eventAdapter, locale);
        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.common.validate.user.invalid.required"), any(), any());
    }

    @Test
    public void 異常_user形式的なやつが存在しない() {
        String content = "<@0>";
        when(eventAdapter.getContent(0)).thenReturn(content);
        when(eventAdapter.getUserByStringId("0")).thenReturn(null);
        new RemoveLinkageBotUser(dynamicSettingService, messageSourceWrapper, discordValidatorService).execute(eventAdapter, locale);
        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.remove-specific-xxx.validate.not-exists-setting"), any(), any());
    }

    @Test
    public void 異常_user形式的なやつっぽいけどただの文字列() {
        String content = "hogege";
        when(eventAdapter.getContent(0)).thenReturn(content);
        when(eventAdapter.getUserByStringId("0")).thenReturn(null);
        new RemoveLinkageBotUser(dynamicSettingService, messageSourceWrapper, discordValidatorService).execute(eventAdapter, locale);
        verify(eventAdapter, times(1)).getUserByStringId(eq("hogege"));
        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.remove-specific-xxx.validate.not-exists-setting"), any(), any());
    }

    @Test
    public void 異常_BOTではない() {
        String content = "100";
        when(eventAdapter.getContent(0)).thenReturn(content);
        final IUser botUser = mock(IUser.class);
        when(botUser.isBot()).thenReturn(false);
        when(eventAdapter.getUserByStringId(content)).thenReturn(botUser);
        new RemoveLinkageBotUser(dynamicSettingService, messageSourceWrapper, discordValidatorService).execute(eventAdapter, locale);
        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.remove-linkage-bot-user.validate.not-exists-bot-user"), any(), any());
    }
}