package jp.vcoin.gratuitybot.command.text.impl.admin;

import jp.vcoin.gratuitybot.adapter.MessageReceivedEventAdapter;
import jp.vcoin.gratuitybot.command.text.MessageCommand;
import jp.vcoin.gratuitybot.domain.LinkageBotUser;
import jp.vcoin.gratuitybot.enumeration.DynamicSettingType;
import jp.vcoin.gratuitybot.service.DynamicSettingService;
import jp.vcoin.gratuitybot.util.MessageSourceWrapper;
import jp.vcoin.gratuitybot.domain.DynamicSetting;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;

import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class SetLinkageBotUserTest {

    @MockBean
    DynamicSettingService dynamicSettingService;
    @MockBean
    MessageSourceWrapper messageSourceWrapper;
    @MockBean
    ApplicationContext applicationContext;
    @Mock
    MessageReceivedEventAdapter eventAdapter;

    @Before
    public void setUp() {
        final MessageCommand mock = mock(MessageCommand.class);
        when(applicationContext.getBeansWithAnnotation(any())).thenReturn(new HashMap<String, Object>() {{
            put("gratuity", mock);
            put("balance", mock);
            put("deposit", mock);
        }});
        when(eventAdapter.getGuildId()).thenReturn(1L);
    }

    @Test
    public void 正常_idとcommands() throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        final IUser mock = mock(IUser.class);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        when(mock.isBot()).thenReturn(true);
        doNothing().when(dynamicSettingService).save(eq(DynamicSettingType.LinkageBotUser.getKey()), anyString(), anyLong(), captor.capture());
        when(eventAdapter.getContent(eq(0))).thenReturn("id:1000;commands:gratuity");
        when(eventAdapter.getUserByID(anyLong())).thenReturn(mock);

        new SetLinkageBotUser(dynamicSettingService, messageSourceWrapper, applicationContext).execute(eventAdapter, Locale.JAPANESE);

        verify(dynamicSettingService, times(1)).save(eq(DynamicSettingType.LinkageBotUser.getKey()), eq("1000"), anyLong(), anyString());
        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.set-specific-xxx.success"), any(), any(), any());
        final LinkageBotUser value = new DynamicSetting(captor.getValue()).convert(LinkageBotUser.class);
        Assert.assertThat(value.getId(), is(1000L));
        Assert.assertThat(value.getCommands(), is(Collections.singletonList("gratuity")));
        Assert.assertThat(value.isForEmojiGratuity(), is(false));
        Assert.assertThat(value.isForGratuity(), is(false));
    }

    @Test
    public void 正常_idのみ() throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        final IUser mock = mock(IUser.class);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        when(mock.isBot()).thenReturn(true);
        doNothing().when(dynamicSettingService).save(eq(DynamicSettingType.LinkageBotUser.getKey()), anyString(), anyLong(), captor.capture());
        when(eventAdapter.getContent(eq(0))).thenReturn("id:1000;");
        when(eventAdapter.getUserByID(anyLong())).thenReturn(mock);

        new SetLinkageBotUser(dynamicSettingService, messageSourceWrapper, applicationContext).execute(eventAdapter, Locale.JAPANESE);

        verify(dynamicSettingService, times(1)).save(eq(DynamicSettingType.LinkageBotUser.getKey()), any(), anyLong(), anyString());
        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.set-specific-xxx.success"), any(), any(), any());
        final LinkageBotUser value = new DynamicSetting(captor.getValue()).convert(LinkageBotUser.class);
        Assert.assertThat(value.getId(), is(1000L));
        Assert.assertThat(value.getCommands(), is(CoreMatchers.nullValue()));
        Assert.assertThat(value.isForEmojiGratuity(), is(false));
        Assert.assertThat(value.isForGratuity(), is(false));
    }

    @Test
    public void 正常_idとgratuity() throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        final IUser mock = mock(IUser.class);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        when(mock.isBot()).thenReturn(true);
        doNothing().when(dynamicSettingService).save(eq(DynamicSettingType.LinkageBotUser.getKey()), anyString(), anyLong(), captor.capture());
        when(eventAdapter.getContent(eq(0))).thenReturn("id:1000;for-gratuity:true;for-emoji-gratuity:false");
        when(eventAdapter.getUserByID(anyLong())).thenReturn(mock);

        new SetLinkageBotUser(dynamicSettingService, messageSourceWrapper, applicationContext).execute(eventAdapter, Locale.JAPANESE);

        verify(dynamicSettingService, times(1)).save(eq("array.linkage-bot-users"), eq("1000"), anyLong(), any());
        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.set-specific-xxx.success"), any(), any(), any());
        final LinkageBotUser value = new DynamicSetting(captor.getValue()).convert(LinkageBotUser.class);
        Assert.assertThat(value.getId(), is(1000L));
        Assert.assertThat(value.getCommands(), is(CoreMatchers.nullValue()));
        Assert.assertThat(value.isForEmojiGratuity(), is(false));
        Assert.assertThat(value.isForGratuity(), is(true));
    }

    @Test
    public void 正常_全部() throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        final IUser mock = mock(IUser.class);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        when(mock.isBot()).thenReturn(true);
        doNothing().when(dynamicSettingService).save(eq(DynamicSettingType.LinkageBotUser.getKey()), anyString(), anyLong(), captor.capture());
        when(eventAdapter.getContent(eq(0))).thenReturn("id:1000;commands:gratuity,balance,deposit;for-gratuity:false;for-emoji-gratuity:true;public-message-channel-id:1");
        when(eventAdapter.getUserByID(anyLong())).thenReturn(mock);
        when(eventAdapter.getChannelById(anyLong())).thenReturn(mock(IChannel.class));


        new SetLinkageBotUser(dynamicSettingService, messageSourceWrapper, applicationContext).execute(eventAdapter, Locale.JAPANESE);

        verify(dynamicSettingService, times(1)).save(eq("array.linkage-bot-users"), eq("1000"), anyLong(), any());
        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.set-specific-xxx.success"), any(), any(), any());
        final LinkageBotUser value = new DynamicSetting(captor.getValue()).convert(LinkageBotUser.class);
        Assert.assertThat(value.getId(), is(1000L));
        Assert.assertThat(value.getCommands(), is(Arrays.asList("gratuity", "balance", "deposit")));
        Assert.assertThat(value.isForEmojiGratuity(), is(true));
        Assert.assertThat(value.isForGratuity(), is(false));
        Assert.assertThat(value.getPublicMessageChannelId(), is(1L));
    }

    @Test
    public void 異常_idなし() {

        final IUser mock = mock(IUser.class);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        when(mock.isBot()).thenReturn(true);
        doNothing().when(dynamicSettingService).save(eq(DynamicSettingType.LinkageBotUser.getKey()), anyLong(), captor.capture());
        when(eventAdapter.getContent(eq(0))).thenReturn("commands:gratuity,balance,deposit;for-gratuity:false;for-emoji-gratuity:true;public-message-channel-id:1");
        when(eventAdapter.getUserByID(anyLong())).thenReturn(mock);
        when(eventAdapter.getChannelById(anyLong())).thenReturn(mock(IChannel.class));


        new SetLinkageBotUser(dynamicSettingService, messageSourceWrapper, applicationContext).execute(eventAdapter, Locale.JAPANESE);

        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.set-linkage-bot-user.validate.required-user-id"), any(), any());
        verify(dynamicSettingService, times(0)).save(eq("array.linkage-bot-users"), anyLong(), any());
    }

    @Test
    public void 異常_存在しないid() {

        final IUser mock = mock(IUser.class);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        when(mock.isBot()).thenReturn(true);
        doNothing().when(dynamicSettingService).save(eq(DynamicSettingType.LinkageBotUser.getKey()), anyLong(), captor.capture());
        when(eventAdapter.getContent(eq(0))).thenReturn("id:123;commands:gratuity,balance,deposit;for-gratuity:false;for-emoji-gratuity:true;public-message-channel-id:1");
        when(eventAdapter.getUserByID(anyLong())).thenReturn(null);
        when(eventAdapter.getChannelById(anyLong())).thenReturn(mock(IChannel.class));


        new SetLinkageBotUser(dynamicSettingService, messageSourceWrapper, applicationContext).execute(eventAdapter, Locale.JAPANESE);

        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.set-linkage-bot-user.validate.not-exists-bot-user"), any(), any());
        verify(dynamicSettingService, times(0)).save(eq("array.linkage-bot-users"), anyLong(), any());
    }

    @Test
    public void 異常_BOTではないid() {

        final IUser mock = mock(IUser.class);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        when(mock.isBot()).thenReturn(false);
        doNothing().when(dynamicSettingService).save(eq(DynamicSettingType.LinkageBotUser.getKey()), anyLong(), captor.capture());
        when(eventAdapter.getContent(eq(0))).thenReturn("id:123;commands:gratuity,balance,deposit;for-gratuity:false;for-emoji-gratuity:true;public-message-channel-id:1");
        when(eventAdapter.getUserByID(anyLong())).thenReturn(mock);
        when(eventAdapter.getChannelById(anyLong())).thenReturn(mock(IChannel.class));


        new SetLinkageBotUser(dynamicSettingService, messageSourceWrapper, applicationContext).execute(eventAdapter, Locale.JAPANESE);

        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.set-linkage-bot-user.validate.not-exists-bot-user"), any(), any());
        verify(dynamicSettingService, times(0)).save(eq("array.linkage-bot-users"), anyLong(), any());
    }

    @Test
    public void 異常_存在しないチャンネルID() {

        final IUser mock = mock(IUser.class);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        when(mock.isBot()).thenReturn(true);
        doNothing().when(dynamicSettingService).save(eq(DynamicSettingType.LinkageBotUser.getKey()), anyLong(), captor.capture());
        when(eventAdapter.getContent(eq(0))).thenReturn("id:123;commands:gratuity,balance,deposit;for-gratuity:false;for-emoji-gratuity:true;public-message-channel-id:1");
        when(eventAdapter.getUserByID(anyLong())).thenReturn(mock);
        when(eventAdapter.getChannelById(anyLong())).thenReturn(null);


        new SetLinkageBotUser(dynamicSettingService, messageSourceWrapper, applicationContext).execute(eventAdapter, Locale.JAPANESE);

        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.set-linkage-bot-user.validate.not-exists-channel-id"), any(), any());
        verify(dynamicSettingService, times(0)).save(eq("array.linkage-bot-users"), anyLong(), any());
    }

    @Test
    public void 異常_存在しないコマンドが含まれている() {

        final IUser mock = mock(IUser.class);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        when(mock.isBot()).thenReturn(true);
        doNothing().when(dynamicSettingService).save(eq(DynamicSettingType.LinkageBotUser.getKey()), anyLong(), captor.capture());
        when(eventAdapter.getContent(eq(0))).thenReturn("id:123;commands:gratuity,balance,deposit,hoge;for-gratuity:false;for-emoji-gratuity:true;public-message-channel-id:1");
        when(eventAdapter.getUserByID(anyLong())).thenReturn(mock);
        when(eventAdapter.getChannelById(anyLong())).thenReturn(mock(IChannel.class));


        new SetLinkageBotUser(dynamicSettingService, messageSourceWrapper, applicationContext).execute(eventAdapter, Locale.JAPANESE);

        verify(messageSourceWrapper, times(1)).getMessage(eq("discord.admin.set-linkage-bot-user.validate.contains-invalid-command"), any(), any(), any());
        verify(dynamicSettingService, times(0)).save(eq("array.linkage-bot-users"), anyLong(), any());
    }
}