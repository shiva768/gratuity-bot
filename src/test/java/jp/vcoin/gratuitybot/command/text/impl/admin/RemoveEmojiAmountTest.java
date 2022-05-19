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
import sx.blah.discord.handle.obj.IEmoji;
import sx.blah.discord.handle.obj.IUser;

import java.util.Locale;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class RemoveEmojiAmountTest {

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
        when(dynamicSettingService.delete(eq(DynamicSettingType.EmojiAmount.getKey()), anyString(), anyLong())).thenReturn(true);
    }

    @Test
    public void 正常_数値のemojiId() {
        String content = "100";
        when(eventAdapter.getContent(0)).thenReturn(content);
        when(eventAdapter.getEmojiByStringId(any())).thenReturn(mock(IEmoji.class));
        when(dynamicSettingService.get(eq(DynamicSettingType.EmojiAmount.getKey()), any(), anyLong(), any())).thenReturn(Optional.of(mock(DynamicSettingDomain.class)));
        new RemoveEmojiAmount(dynamicSettingService, messageSourceWrapper, commandService, discordValidatorService).execute(eventAdapter, locale);
        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.remove-specific-xxx.success"), any(), any());
    }

    @Test
    public void 正常_emoji形式的なやつ() {
        String content = "<:hogehoge:100>";
        when(eventAdapter.getContent(0)).thenReturn(content);
        when(eventAdapter.getEmojiByStringId(any())).thenReturn(mock(IEmoji.class));
        when(dynamicSettingService.get(eq(DynamicSettingType.EmojiAmount.getKey()), any(), anyLong(), any())).thenReturn(Optional.of(mock(DynamicSettingDomain.class)));
        new RemoveEmojiAmount(dynamicSettingService, messageSourceWrapper, commandService, discordValidatorService).execute(eventAdapter, locale);
        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.remove-specific-xxx.success"), any(), any());
    }

    @Test
    public void 異常_emojiIdが空() {
        when(eventAdapter.getContent(0)).thenReturn("");
        new RemoveEmojiAmount(dynamicSettingService, messageSourceWrapper, commandService, discordValidatorService).execute(eventAdapter, locale);
        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.common.validate.emoji.invalid.required"), any(), any());
    }

    @Test
    public void 異常_emoji形式的なやつが存在しない() {
        String content = "<:hoge:0>";
        when(eventAdapter.getContent(0)).thenReturn(content);
        new RemoveEmojiAmount(dynamicSettingService, messageSourceWrapper, commandService, discordValidatorService).execute(eventAdapter, locale);
        verify(eventAdapter, times(0)).getEmojiByStringId(eq("0"));
        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.remove-specific-xxx.validate.not-exists-setting"), any(), any());
    }

    @Test
    public void 異常_emoji形式的なやつっぽいけどただの文字列() {
        String content = "hogege";
        when(eventAdapter.getContent(0)).thenReturn(content);
        new RemoveEmojiAmount(dynamicSettingService, messageSourceWrapper, commandService, discordValidatorService).execute(eventAdapter, locale);
        verify(eventAdapter, times(0)).getEmojiByStringId(eq("hogege"));
        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.remove-specific-xxx.validate.not-exists-setting"), any(), any());
    }

    @Test
    public void 異常_削除しようとした絵文字の設定が存在しない() {
        String content = "100";
        when(eventAdapter.getContent(0)).thenReturn(content);
        when(eventAdapter.getEmojiByStringId(any())).thenReturn(mock(IEmoji.class));
        when(dynamicSettingService.get(eq(DynamicSettingType.EmojiAmount.getKey()), any(), anyLong(), any())).thenReturn(Optional.empty());
        new RemoveEmojiAmount(dynamicSettingService, messageSourceWrapper, commandService, discordValidatorService).execute(eventAdapter, locale);
        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.remove-specific-xxx.validate.not-exists-setting"), any(), any());
    }

    @Test
    public void 異常_1件も削除していない() {
        String content = "100";
        when(eventAdapter.getContent(0)).thenReturn(content);
        when(eventAdapter.getEmojiByStringId(any())).thenReturn(mock(IEmoji.class));
        when(dynamicSettingService.delete(eq(DynamicSettingType.EmojiAmount.getKey()), anyString(), anyLong())).thenReturn(false);
        when(dynamicSettingService.get(eq(DynamicSettingType.EmojiAmount.getKey()), any(), anyLong(), any())).thenReturn(Optional.ofNullable(mock(DynamicSettingDomain.class)));
        new RemoveEmojiAmount(dynamicSettingService, messageSourceWrapper, commandService, discordValidatorService).execute(eventAdapter, locale);
        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.remove-specific-xxx.failed"), any(), any());
    }
}