package jp.vcoin.gratuitybot.command.text.impl.admin;

import jp.vcoin.gratuitybot.adapter.MessageReceivedEventAdapter;
import jp.vcoin.gratuitybot.config.ApplicationProperties;
import jp.vcoin.gratuitybot.domain.EmojiAmount;
import jp.vcoin.gratuitybot.enumeration.DynamicSettingType;
import jp.vcoin.gratuitybot.service.CommandService;
import jp.vcoin.gratuitybot.service.DiscordValidatorService;
import jp.vcoin.gratuitybot.service.DynamicSettingService;
import jp.vcoin.gratuitybot.service.impl.DiscordValidatorServiceImpl;
import jp.vcoin.gratuitybot.util.MessageSourceWrapper;
import jp.vcoin.gratuitybot.domain.DynamicSetting;
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
import sx.blah.discord.handle.obj.IEmoji;
import sx.blah.discord.handle.obj.IUser;

import java.lang.reflect.InvocationTargetException;
import java.util.Locale;

import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class SetEmojiAmountTest {
    @MockBean
    DynamicSettingService dynamicSettingService;
    @MockBean
    MessageSourceWrapper messageSourceWrapper;
    @MockBean
    ApplicationProperties applicationProperties;
    @MockBean
    CommandService commandService;
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
        doNothing().when(dynamicSettingService).save(eq(DynamicSettingType.EmojiAmount.getKey()), anyString(), anyLong(), captor.capture());
    }

    @Test
    public void 成功_numeric() throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        when(eventAdapter.getContent(0)).thenReturn("10");
        when(eventAdapter.getContent(1)).thenReturn("100");
        when(eventAdapter.getEmojiByStringId(anyString())).thenReturn(mock(IEmoji.class));

        new SetEmojiAmount(dynamicSettingService, messageSourceWrapper, applicationProperties, commandService, discordValidatorService).execute(eventAdapter, locale);
        verify(eventAdapter, times(1)).getEmojiByStringId("10");
        verify(dynamicSettingService, times(1)).save(eq(DynamicSettingType.EmojiAmount.getKey()), eq("10"), anyLong(), anyString());
        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.set-specific-xxx.success"), any(), any(), any());
        final EmojiAmount value = new DynamicSetting(captor.getValue()).convert(EmojiAmount.class);
        Assert.assertThat(value.getAmount(), is("100"));
    }

    @Test
    public void 成功_emoji() throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        when(eventAdapter.getContent(0)).thenReturn("<:hogehoge:10>");
        when(eventAdapter.getContent(1)).thenReturn("100");
        when(eventAdapter.getEmojiByStringId("10")).thenReturn(mock(IEmoji.class));

        new SetEmojiAmount(dynamicSettingService, messageSourceWrapper, applicationProperties, commandService, discordValidatorService).execute(eventAdapter, locale);
        verify(eventAdapter, times(1)).getEmojiByStringId("10");
        verify(dynamicSettingService, times(1)).save(eq(DynamicSettingType.EmojiAmount.getKey()), eq("10"), anyLong(), anyString());
        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.set-specific-xxx.success"), any(), any(), any());
        final EmojiAmount value = new DynamicSetting(captor.getValue()).convert(EmojiAmount.class);
        Assert.assertThat(value.getAmount(), is("100"));
    }

    @Test
    public void 失敗_ID空() {
        when(eventAdapter.getContent(0)).thenReturn("");
        when(eventAdapter.getContent(1)).thenReturn("100");

        new SetEmojiAmount(dynamicSettingService, messageSourceWrapper, applicationProperties, commandService, discordValidatorService).execute(eventAdapter, locale);
        verify(dynamicSettingService, times(0)).save(eq(DynamicSettingType.EmojiAmount.getKey()), any(), anyLong(), anyString());
        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.common.validate.emoji.invalid.required"), any(), any());
    }

    @Test
    public void 失敗_IDが存在しないemojiId() {
        when(eventAdapter.getContent(0)).thenReturn("10");
        when(eventAdapter.getContent(1)).thenReturn("100");
        when(eventAdapter.getEmojiByStringId("10")).thenReturn(null);

        new SetEmojiAmount(dynamicSettingService, messageSourceWrapper, applicationProperties, commandService, discordValidatorService).execute(eventAdapter, locale);
        verify(eventAdapter, times(1)).getEmojiByStringId("10");
        verify(dynamicSettingService, times(0)).save(eq(DynamicSettingType.EmojiAmount.getKey()), any(), anyLong(), anyString());
        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.common.validate.emoji.invalid.not-exists"), any(), any());
    }

    @Test
    public void 失敗_IDが存在しないemoji() {
        when(eventAdapter.getContent(0)).thenReturn("<:hogehoge:10>");
        when(eventAdapter.getContent(1)).thenReturn("100");
        when(eventAdapter.getEmojiByStringId("10")).thenReturn(null);

        new SetEmojiAmount(dynamicSettingService, messageSourceWrapper, applicationProperties, commandService, discordValidatorService).execute(eventAdapter, locale);
        verify(eventAdapter, times(1)).getEmojiByStringId("10");
        verify(dynamicSettingService, times(0)).save(eq(DynamicSettingType.EmojiAmount.getKey()), any(), anyLong(), anyString());
        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.common.validate.emoji.invalid.not-exists"), any(), any());
    }

    @Test
    public void 失敗_IDがただの文字列() {
        when(eventAdapter.getContent(0)).thenReturn("hogeeeeee");
        when(eventAdapter.getContent(1)).thenReturn("100");
        when(eventAdapter.getEmojiByStringId(null)).thenReturn(null);

        new SetEmojiAmount(dynamicSettingService, messageSourceWrapper, applicationProperties, commandService, discordValidatorService).execute(eventAdapter, locale);
        verify(eventAdapter, times(1)).getEmojiByStringId("hogeeeeee");
        verify(dynamicSettingService, times(0)).save(eq(DynamicSettingType.EmojiAmount.getKey()), any(), anyLong(), anyString());
        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.common.validate.emoji.invalid.not-exists"), any(), any());
    }

    @Test
    public void 失敗_価格が不正() {
        when(eventAdapter.getContent(0)).thenReturn("10");
        when(eventAdapter.getContent(1)).thenReturn("xxxxxxxxx");
        when(eventAdapter.getEmojiByStringId(anyString())).thenReturn(mock(IEmoji.class));

        new SetEmojiAmount(dynamicSettingService, messageSourceWrapper, applicationProperties, commandService, discordValidatorService).execute(eventAdapter, locale);
        verify(dynamicSettingService, times(0)).save(eq(DynamicSettingType.EmojiAmount.getKey()), any(), anyLong(), anyString());
        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.set-emoji-amount.validate.invalid-amount"), any(), any());
    }
}