package jp.vcoin.gratuitybot.command.text.impl.admin;

import jp.vcoin.gratuitybot.adapter.MessageReceivedEventAdapter;
import jp.vcoin.gratuitybot.domain.DynamicSettingDomain;
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
public class AddAdminUserTest {

    @MockBean
    DynamicSettingService dynamicSettingService;
    @MockBean
    MessageSourceWrapper messageSourceWrapper;
    @SpyBean(DiscordValidatorServiceImpl.class)
    DiscordValidatorService discordValidatorServiceImpl;

    @Mock
    MessageReceivedEventAdapter eventAdapter;
    Locale locale = Locale.JAPANESE;

    @Before
    public void setup() {
        IUser user = mock(IUser.class);
        when(eventAdapter.getAuthor()).thenReturn(user);
        doNothing().when(eventAdapter).sendMessage(any());
        doNothing().when(dynamicSettingService).save(eq(DynamicSettingType.AdminUser.getKey()), anyString(), anyLong(), any());
    }

    @Test
    public void 正常_数値のuserId() {
        String content = "100";
        when(eventAdapter.getContent(0)).thenReturn(content);
        when(eventAdapter.getUserByID(Long.parseLong(content))).thenReturn(mock(IUser.class));
        when(eventAdapter.getUserByStringId(any())).thenReturn(mock(IUser.class));

        when(dynamicSettingService.get(eq(DynamicSettingType.AdminUser.getKey()), any(), anyLong(), any())).thenReturn(Optional.empty());
        new AddAdminUser(dynamicSettingService, messageSourceWrapper, discordValidatorServiceImpl).execute(eventAdapter, locale);
        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.set-specific-xxx.success"), any(), any(), any());
    }

    @Test
    public void 正常_user形式的なやつ() {
        String content = "<@100>";
        when(eventAdapter.getContent(0)).thenReturn(content);
        when(eventAdapter.getUserByStringId(any())).thenReturn(mock(IUser.class));
        when(dynamicSettingService.get(eq(DynamicSettingType.AdminUser.getKey()), any(), anyLong(), any())).thenReturn(Optional.empty());
        new AddAdminUser(dynamicSettingService, messageSourceWrapper, discordValidatorServiceImpl).execute(eventAdapter, locale);
        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.set-specific-xxx.success"), any(), any(), any());
    }

    @Test
    public void 異常_userIdが空() {
        when(eventAdapter.getContent(0)).thenReturn("");
        new AddAdminUser(dynamicSettingService, messageSourceWrapper, discordValidatorServiceImpl).execute(eventAdapter, locale);
        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.common.validate.user.invalid.required"), any(), any());
    }

    @Test
    public void 異常_数値のuserIdが存在しない() {
        when(eventAdapter.getContent(0)).thenReturn("0");
        new AddAdminUser(dynamicSettingService, messageSourceWrapper, discordValidatorServiceImpl).execute(eventAdapter, locale);
        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.common.validate.user.invalid.not-exists"), any(), any());
    }

    @Test
    public void 異常_user形式的なやつが存在しない() {
        String content = "<@0>";
        when(eventAdapter.getContent(0)).thenReturn(content);
        new AddAdminUser(dynamicSettingService, messageSourceWrapper, discordValidatorServiceImpl).execute(eventAdapter, locale);
        verify(eventAdapter, times(1)).getUserByStringId(eq("0"));
        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.common.validate.user.invalid.not-exists"), any(), any());
    }

    @Test
    public void 異常_user形式的なやつっぽいけどただの文字列() {
        String content = "hogege";
        when(eventAdapter.getContent(0)).thenReturn(content);
        new AddAdminUser(dynamicSettingService, messageSourceWrapper, discordValidatorServiceImpl).execute(eventAdapter, locale);
        verify(eventAdapter, times(1)).getUserByStringId(eq(content));
        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.common.validate.user.invalid.not-exists"), any(), any());
    }

    @Test
    public void 異常_BOT() {
        String content = "100";
        when(eventAdapter.getContent(0)).thenReturn(content);
        IUser bot = mock(IUser.class);
        when(bot.isBot()).thenReturn(true);
        when(eventAdapter.getUserByID(Long.parseLong(content))).thenReturn(mock(IUser.class));
        when(eventAdapter.getUserByStringId(any())).thenReturn(bot);
        new AddAdminUser(dynamicSettingService, messageSourceWrapper, discordValidatorServiceImpl).execute(eventAdapter, locale);
        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.add-admin-user.validate.cannot-add-bot-user"), any(), any());
    }

    @Test
    public void 異常_登録しようとしたユーザが登録済み() {
        String content = "100";
        when(eventAdapter.getContent(0)).thenReturn(content);
        when(eventAdapter.getUserByID(Long.parseLong(content))).thenReturn(mock(IUser.class));
        when(eventAdapter.getUserByStringId(any())).thenReturn(mock(IUser.class));
        when(dynamicSettingService.get(eq(DynamicSettingType.AdminUser.getKey()), any(), anyLong(), any())).thenReturn(Optional.ofNullable(mock(DynamicSettingDomain.class)));
        new AddAdminUser(dynamicSettingService, messageSourceWrapper, discordValidatorServiceImpl).execute(eventAdapter, locale);
        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.add-admin-user.validate.setting-exists-user"), any(), any());
    }
}