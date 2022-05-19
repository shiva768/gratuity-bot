package jp.vcoin.gratuitybot.command.text.impl.admin;

import jp.vcoin.gratuitybot.adapter.MessageReceivedEventAdapter;
import jp.vcoin.gratuitybot.domain.DynamicSettingDomain;
import jp.vcoin.gratuitybot.enumeration.DynamicSettingType;
import jp.vcoin.gratuitybot.service.CommandService;
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
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class RemoveAdminUserTest {

    @MockBean
    DynamicSettingService dynamicSettingService;
    @MockBean
    MessageSourceWrapper messageSourceWrapper;
    @MockBean
    CommandService commandService;
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
        when(dynamicSettingService.delete(eq(DynamicSettingType.AdminUser.getKey()), anyString(), anyLong())).thenReturn(true);
    }

    @Test
    public void 正常_数値のuserId() {
        String content = "100";
        when(eventAdapter.getContent(0)).thenReturn(content);
        when(eventAdapter.getUserByStringId(any())).thenReturn(mock(IUser.class));
        when(dynamicSettingService.get(eq(DynamicSettingType.AdminUser.getKey()), any(), anyLong(), any())).thenReturn(Optional.of(mock(DynamicSettingDomain.class)));
        new RemoveAdminUser(dynamicSettingService, messageSourceWrapper, commandService, discordValidatorService).execute(eventAdapter, locale);
        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.remove-specific-xxx.success"), any(), any());
    }

    @Test
    public void 正常_user形式的なやつ() {
        String content = "<@100>";
        when(eventAdapter.getContent(0)).thenReturn(content);
        when(eventAdapter.getUserByStringId(any())).thenReturn(mock(IUser.class));
        when(dynamicSettingService.get(eq(DynamicSettingType.AdminUser.getKey()), any(), anyLong(), any())).thenReturn(Optional.of(mock(DynamicSettingDomain.class)));
        new RemoveAdminUser(dynamicSettingService, messageSourceWrapper, commandService, discordValidatorService).execute(eventAdapter, locale);
        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.remove-specific-xxx.success"), any(), any());
    }

    @Test
    public void 異常_userIdが空() {
        when(eventAdapter.getContent(0)).thenReturn("");
        new RemoveAdminUser(dynamicSettingService, messageSourceWrapper, commandService, discordValidatorService).execute(eventAdapter, locale);
        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.common.validate.user.invalid.required"), any(), any());
    }

    @Test
    public void 異常_user形式的なやつが存在しない() {
        String content = "<@0>";
        when(eventAdapter.getContent(0)).thenReturn(content);
        new RemoveAdminUser(dynamicSettingService, messageSourceWrapper, commandService, discordValidatorService).execute(eventAdapter, locale);
        verify(eventAdapter, times(0)).getUserByStringId(eq("0"));
        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.remove-specific-xxx.validate.not-exists-setting"), any(), any());
    }

    @Test
    public void 異常_user形式的なやつっぽいけどただの文字列() {
        String content = "hogege";
        when(eventAdapter.getContent(0)).thenReturn(content);
        new RemoveAdminUser(dynamicSettingService, messageSourceWrapper, commandService, discordValidatorService).execute(eventAdapter, locale);
        verify(eventAdapter, times(0)).getUserByStringId(eq("hogege"));
        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.remove-specific-xxx.validate.not-exists-setting"), any(), any());
    }

    @Test
    public void 異常_削除しようとしたユーザの設定が存在しない() {
        String content = "100";
        when(eventAdapter.getContent(0)).thenReturn(content);
        when(eventAdapter.getUserByStringId(any())).thenReturn(mock(IUser.class));
        when(dynamicSettingService.get(eq(DynamicSettingType.AdminUser.getKey()), any(), anyLong(), any())).thenReturn(Optional.empty());
        new RemoveAdminUser(dynamicSettingService, messageSourceWrapper, commandService, discordValidatorService).execute(eventAdapter, locale);
        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.remove-specific-xxx.validate.not-exists-setting"), any(), any());
    }

    @Test
    public void 異常_1件も削除していない() {
        String content = "100";
        when(eventAdapter.getContent(0)).thenReturn(content);
        when(eventAdapter.getUserByStringId(any())).thenReturn(mock(IUser.class));
        when(dynamicSettingService.delete(eq(DynamicSettingType.AdminUser.getKey()), anyString(), anyLong())).thenReturn(false);
        when(dynamicSettingService.get(eq(DynamicSettingType.AdminUser.getKey()), any(), anyLong(), any())).thenReturn(Optional.ofNullable(mock(DynamicSettingDomain.class)));
        new RemoveAdminUser(dynamicSettingService, messageSourceWrapper, commandService, discordValidatorService).execute(eventAdapter, locale);
        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.remove-specific-xxx.failed"), any(), any());
    }
}