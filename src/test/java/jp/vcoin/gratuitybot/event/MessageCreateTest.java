package jp.vcoin.gratuitybot.event;

import jp.vcoin.gratuitybot.adapter.MessageReceivedEventAdapter;
import jp.vcoin.gratuitybot.adapter.factory.MessageReceivedEventAdapterFactory;
import jp.vcoin.gratuitybot.command.text.MessageCommand;
import jp.vcoin.gratuitybot.config.ApplicationProperties;
import jp.vcoin.gratuitybot.service.CommandService;
import jp.vcoin.gratuitybot.service.DynamicSettingService;
import jp.vcoin.gratuitybot.service.LocaleService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IUser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;

import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class MessageCreateTest {

    @MockBean
    MessageReceivedEventAdapterFactory messageReceivedEventAdapterFactory;
    @MockBean
    ApplicationProperties applicationProperties;
    @MockBean
    LocaleService localeService;
    @MockBean
    CommandService commandService;
    @MockBean
    DynamicSettingService dynamicSettingService;
    @Mock
    MessageReceivedEventAdapter messageReceivedEventAdapter;

    @Test
    public void 正常() {
        // setup
        when(messageReceivedEventAdapter.getAuthor()).thenReturn(mock(IUser.class));
        when(messageReceivedEventAdapter.isLinkageBotUser()).thenReturn(false);
        when(messageReceivedEventAdapter.isNullContent()).thenReturn(false);
        when(messageReceivedEventAdapter.getCommand()).thenReturn("./gratuity");
        when(messageReceivedEventAdapter.getChannelId()).thenReturn(1L);
        when(messageReceivedEventAdapterFactory.getAdapter(any())).thenReturn(messageReceivedEventAdapter);
        when(applicationProperties.getDiscordEventTriggerPrefix()).thenReturn("./");
        when(applicationProperties.getDiscordEventTextChannelCommandChannelIds()).thenReturn(Arrays.asList(1L, 2L, 3L));
        when(localeService.getLocale(messageReceivedEventAdapter)).thenReturn(Locale.JAPANESE);
        final MessageCommand messageCommand = mock(MessageCommand.class);
        doNothing().when(messageCommand).execute(any(), any());
        when(commandService.getMessageCommandMap()).thenReturn(new HashMap<String, MessageCommand>() {{
            put("gratuity", messageCommand);
            put("record", messageCommand);
        }});
        when(applicationProperties.getDiscordEventTextEnableChannelCommands()).thenReturn(Arrays.asList("gratuity", "record"));
        when(applicationProperties.getDiscordEventTextEnableDmCommands()).thenReturn(Arrays.asList("gratuity", "record"));
        when(applicationProperties.getDiscordEventAdminCommands()).thenReturn(Arrays.asList("stop"));
        when(applicationProperties.getDiscordEventAdminUserIds()).thenReturn(Arrays.asList(1L));

        // when
        new MessageCreate(messageReceivedEventAdapterFactory, applicationProperties, localeService, commandService, dynamicSettingService).handle(mock(MessageReceivedEvent.class));

        // then
        verify(messageCommand, times(1)).execute(any(), any());
    }

    @Test
    public void 準正常_BOT() {
        // setup
        final IUser user = mock(IUser.class);
        when(user.isBot()).thenReturn(true);
        when(messageReceivedEventAdapter.getAuthor()).thenReturn(user);
        when(messageReceivedEventAdapter.isLinkageBotUser()).thenReturn(false);
        when(messageReceivedEventAdapter.isNullContent()).thenReturn(false);
        when(messageReceivedEventAdapter.getCommand()).thenReturn("./gratuity");
        when(messageReceivedEventAdapter.getChannelId()).thenReturn(1L);
        when(messageReceivedEventAdapterFactory.getAdapter(any())).thenReturn(messageReceivedEventAdapter);
        when(applicationProperties.getDiscordEventTriggerPrefix()).thenReturn("./");
        when(applicationProperties.getDiscordEventTextChannelCommandChannelIds()).thenReturn(Arrays.asList(1L, 2L, 3L));
        when(localeService.getLocale(messageReceivedEventAdapter)).thenReturn(Locale.JAPANESE);
        final MessageCommand messageCommand = mock(MessageCommand.class);
        doNothing().when(messageCommand).execute(any(), any());
        when(commandService.getMessageCommandMap()).thenReturn(new HashMap<String, MessageCommand>() {{
            put("gratuity", messageCommand);
            put("record", messageCommand);
        }});
        when(applicationProperties.getDiscordEventTextEnableChannelCommands()).thenReturn(Arrays.asList("gratuity", "record"));
        when(applicationProperties.getDiscordEventTextEnableDmCommands()).thenReturn(Arrays.asList("gratuity", "record"));
        when(applicationProperties.getDiscordEventAdminCommands()).thenReturn(Arrays.asList("stop"));
        when(applicationProperties.getDiscordEventAdminUserIds()).thenReturn(Arrays.asList(1L));

        // when
        new MessageCreate(messageReceivedEventAdapterFactory, applicationProperties, localeService, commandService, dynamicSettingService).handle(mock(MessageReceivedEvent.class));

        // then
        verify(messageCommand, times(0)).execute(any(), any());
    }

    @Test
    public void 正常_BOT_コマンド有効_ぬるぽ連携用の実装テスト() {
        // setup
        final IUser user = mock(IUser.class);
        when(user.isBot()).thenReturn(true);
        when(messageReceivedEventAdapter.getAuthor()).thenReturn(user);
        when(messageReceivedEventAdapter.isLinkageBotUser()).thenReturn(true);
        when(messageReceivedEventAdapter.getLinkageBotUserCommands()).thenReturn(new HashSet<>(Arrays.asList("gratuity")));
        when(messageReceivedEventAdapter.isNullContent()).thenReturn(false);
        when(messageReceivedEventAdapter.getCommand()).thenReturn("./gratuity");
        when(messageReceivedEventAdapter.getChannelId()).thenReturn(1L);
        when(messageReceivedEventAdapterFactory.getAdapter(any())).thenReturn(messageReceivedEventAdapter);
        when(applicationProperties.getDiscordEventTriggerPrefix()).thenReturn("./");
        when(applicationProperties.getDiscordEventTextChannelCommandChannelIds()).thenReturn(Arrays.asList(1L, 2L, 3L));
        when(localeService.getLocale(messageReceivedEventAdapter)).thenReturn(Locale.JAPANESE);
        final MessageCommand messageCommand = mock(MessageCommand.class);
        doNothing().when(messageCommand).execute(any(), any());
        when(commandService.getMessageCommandMap()).thenReturn(new HashMap<String, MessageCommand>() {{
            put("gratuity", messageCommand);
            put("record", messageCommand);
        }});
        when(applicationProperties.getDiscordEventTextEnableChannelCommands()).thenReturn(Arrays.asList("gratuity", "record"));
        when(applicationProperties.getDiscordEventTextEnableDmCommands()).thenReturn(Arrays.asList("gratuity", "record"));
        when(applicationProperties.getDiscordEventAdminCommands()).thenReturn(Arrays.asList("stop"));
        when(applicationProperties.getDiscordEventAdminUserIds()).thenReturn(Arrays.asList(1L));

        // when
        new MessageCreate(messageReceivedEventAdapterFactory, applicationProperties, localeService, commandService, dynamicSettingService).handle(mock(MessageReceivedEvent.class));

        // then
        verify(messageCommand, times(1)).execute(any(), any());
    }

    @Test
    public void 準正常_BOT_コマンド有効_対応するコマンドがない_ぬるぽ連携用の実装テスト() {
        // setup
        String command = "./gratuity random 100";
        final IUser user = mock(IUser.class);
        when(user.isBot()).thenReturn(true);
        when(messageReceivedEventAdapter.getAuthor()).thenReturn(user);
        when(messageReceivedEventAdapter.isLinkageBotUser()).thenReturn(true);
        when(messageReceivedEventAdapter.getLinkageBotUserCommands()).thenReturn(new HashSet<>(Arrays.asList("record")));
        when(messageReceivedEventAdapter.isNullContent()).thenReturn(false);
        when(messageReceivedEventAdapter.getCommand()).thenReturn("./gratuity");
        when(messageReceivedEventAdapter.getChannelId()).thenReturn(1L);
        when(messageReceivedEventAdapterFactory.getAdapter(any())).thenReturn(messageReceivedEventAdapter);
        when(applicationProperties.getDiscordEventTriggerPrefix()).thenReturn("./");
        when(applicationProperties.getDiscordEventTextChannelCommandChannelIds()).thenReturn(Arrays.asList(1L, 2L, 3L));
        when(localeService.getLocale(messageReceivedEventAdapter)).thenReturn(Locale.JAPANESE);
        final MessageCommand messageCommand = mock(MessageCommand.class);
        doNothing().when(messageCommand).execute(any(), any());
        when(commandService.getMessageCommandMap()).thenReturn(new HashMap<String, MessageCommand>() {{
            put("gratuity", messageCommand);
            put("record", messageCommand);
        }});
        when(applicationProperties.getDiscordEventTextEnableChannelCommands()).thenReturn(Arrays.asList("gratuity", "record"));
        when(applicationProperties.getDiscordEventTextEnableDmCommands()).thenReturn(Arrays.asList("gratuity", "record"));
        when(applicationProperties.getDiscordEventAdminCommands()).thenReturn(Arrays.asList("stop"));
        when(applicationProperties.getDiscordEventAdminUserIds()).thenReturn(Arrays.asList(1L));

        // when
        new MessageCreate(messageReceivedEventAdapterFactory, applicationProperties, localeService, commandService, dynamicSettingService).handle(mock(MessageReceivedEvent.class));

        // then
        verify(messageCommand, times(0)).execute(any(), any());
    }

    @Test
    public void 準正常_コマンド空() {
        // setup
        String command = "";
        final IUser user = mock(IUser.class);
        when(user.isBot()).thenReturn(true);
        when(messageReceivedEventAdapter.getAuthor()).thenReturn(user);
        when(messageReceivedEventAdapter.isLinkageBotUser()).thenReturn(true);
        when(messageReceivedEventAdapter.isNullContent()).thenReturn(true);
        when(messageReceivedEventAdapter.getCommand()).thenReturn("");
        when(messageReceivedEventAdapter.getChannelId()).thenReturn(1L);
        when(messageReceivedEventAdapterFactory.getAdapter(any())).thenReturn(messageReceivedEventAdapter);
        when(applicationProperties.getDiscordEventTriggerPrefix()).thenReturn("./");
        when(applicationProperties.getDiscordEventTextChannelCommandChannelIds()).thenReturn(Arrays.asList(1L, 2L, 3L));
        when(localeService.getLocale(messageReceivedEventAdapter)).thenReturn(Locale.JAPANESE);
        final MessageCommand messageCommand = mock(MessageCommand.class);
        doNothing().when(messageCommand).execute(any(), any());
        when(commandService.getMessageCommandMap()).thenReturn(new HashMap<String, MessageCommand>() {{
            put("gratuity", messageCommand);
            put("record", messageCommand);
        }});
        when(applicationProperties.getDiscordEventTextEnableChannelCommands()).thenReturn(Arrays.asList("gratuity", "record"));
        when(applicationProperties.getDiscordEventTextEnableDmCommands()).thenReturn(Arrays.asList("gratuity", "record"));
        when(applicationProperties.getDiscordEventAdminCommands()).thenReturn(Arrays.asList("stop"));
        when(applicationProperties.getDiscordEventAdminUserIds()).thenReturn(Arrays.asList(1L));

        // when
        new MessageCreate(messageReceivedEventAdapterFactory, applicationProperties, localeService, commandService, dynamicSettingService).handle(mock(MessageReceivedEvent.class));

        // then
        verify(messageCommand, times(0)).execute(any(), any());
    }

    @Test
    public void 準正常_コマンドが規定の文字列から始まらない() {
        // setup
        String command = "ほげほげ";
        final IUser user = mock(IUser.class);
        when(user.isBot()).thenReturn(true);
        when(messageReceivedEventAdapter.getAuthor()).thenReturn(user);
        when(messageReceivedEventAdapter.isLinkageBotUser()).thenReturn(true);
        when(messageReceivedEventAdapter.isNullContent()).thenReturn(true);
        when(messageReceivedEventAdapter.getCommand()).thenReturn("ほげほげ");
        when(messageReceivedEventAdapter.getChannelId()).thenReturn(1L);
        when(messageReceivedEventAdapterFactory.getAdapter(any())).thenReturn(messageReceivedEventAdapter);
        when(applicationProperties.getDiscordEventTriggerPrefix()).thenReturn("./");
        when(applicationProperties.getDiscordEventTextChannelCommandChannelIds()).thenReturn(Arrays.asList(1L, 2L, 3L));
        when(localeService.getLocale(messageReceivedEventAdapter)).thenReturn(Locale.JAPANESE);
        final MessageCommand messageCommand = mock(MessageCommand.class);
        doNothing().when(messageCommand).execute(any(), any());
        when(commandService.getMessageCommandMap()).thenReturn(new HashMap<String, MessageCommand>() {{
            put("gratuity", messageCommand);
            put("record", messageCommand);
        }});
        when(applicationProperties.getDiscordEventTextEnableChannelCommands()).thenReturn(Arrays.asList("gratuity", "record"));
        when(applicationProperties.getDiscordEventTextEnableDmCommands()).thenReturn(Arrays.asList("gratuity", "record"));
        when(applicationProperties.getDiscordEventAdminCommands()).thenReturn(Arrays.asList("stop"));
        when(applicationProperties.getDiscordEventAdminUserIds()).thenReturn(Arrays.asList(1L));

        // when
        new MessageCreate(messageReceivedEventAdapterFactory, applicationProperties, localeService, commandService, dynamicSettingService).handle(mock(MessageReceivedEvent.class));

        // then
        verify(messageCommand, times(0)).execute(any(), any());
    }

    @Test
    public void 正常_コマンドの開始文字列変更() {
        // setup
        String command = "ほげほげgratuity";
        final IUser user = mock(IUser.class);
        when(user.isBot()).thenReturn(false);
        when(messageReceivedEventAdapter.getAuthor()).thenReturn(user);
        when(messageReceivedEventAdapter.isLinkageBotUser()).thenReturn(false);
        when(messageReceivedEventAdapter.isNullContent()).thenReturn(false);
        when(messageReceivedEventAdapter.getCommand()).thenReturn("ほげほげgratuity");
        when(messageReceivedEventAdapter.getChannelId()).thenReturn(1L);
        when(messageReceivedEventAdapterFactory.getAdapter(any())).thenReturn(messageReceivedEventAdapter);
        when(applicationProperties.getDiscordEventTriggerPrefix()).thenReturn("ほげほげ");
        when(applicationProperties.getDiscordEventTextChannelCommandChannelIds()).thenReturn(Arrays.asList(1L, 2L, 3L));
        when(localeService.getLocale(messageReceivedEventAdapter)).thenReturn(Locale.JAPANESE);
        final MessageCommand messageCommand = mock(MessageCommand.class);
        doNothing().when(messageCommand).execute(any(), any());
        when(commandService.getMessageCommandMap()).thenReturn(new HashMap<String, MessageCommand>() {{
            put("gratuity", messageCommand);
            put("record", messageCommand);
        }});
        when(applicationProperties.getDiscordEventTextEnableChannelCommands()).thenReturn(Arrays.asList("gratuity", "record"));
        when(applicationProperties.getDiscordEventTextEnableDmCommands()).thenReturn(Arrays.asList("gratuity", "record"));
        when(applicationProperties.getDiscordEventAdminCommands()).thenReturn(Arrays.asList("stop"));
        when(applicationProperties.getDiscordEventAdminUserIds()).thenReturn(Arrays.asList(1L));

        // when
        new MessageCreate(messageReceivedEventAdapterFactory, applicationProperties, localeService, commandService, dynamicSettingService).handle(mock(MessageReceivedEvent.class));

        // then
        verify(messageCommand, times(1)).execute(any(), any());
    }

    @Test
    public void 準正常_対応するコマンドがない() {
        // setup
        String command = "ほげほげgratuitydesu";
        final IUser user = mock(IUser.class);
        when(user.isBot()).thenReturn(false);
        when(messageReceivedEventAdapter.getAuthor()).thenReturn(user);
        when(messageReceivedEventAdapter.isLinkageBotUser()).thenReturn(false);
        when(messageReceivedEventAdapter.isNullContent()).thenReturn(false);
        when(messageReceivedEventAdapter.getCommand()).thenReturn("ほげほげgratuitydesu");
        when(messageReceivedEventAdapter.getChannelId()).thenReturn(1L);
        when(messageReceivedEventAdapterFactory.getAdapter(any())).thenReturn(messageReceivedEventAdapter);
        when(applicationProperties.getDiscordEventTriggerPrefix()).thenReturn("ほげほげ");
        when(applicationProperties.getDiscordEventTextChannelCommandChannelIds()).thenReturn(Arrays.asList(1L, 2L, 3L));
        when(localeService.getLocale(messageReceivedEventAdapter)).thenReturn(Locale.JAPANESE);
        final MessageCommand messageCommand = mock(MessageCommand.class);
        doNothing().when(messageCommand).execute(any(), any());
        when(commandService.getMessageCommandMap()).thenReturn(new HashMap<String, MessageCommand>() {{
            put("gratuity", messageCommand);
            put("record", messageCommand);
        }});
        when(applicationProperties.getDiscordEventTextEnableChannelCommands()).thenReturn(Arrays.asList("gratuity", "record"));
        when(applicationProperties.getDiscordEventTextEnableDmCommands()).thenReturn(Arrays.asList("gratuity", "record"));
        when(applicationProperties.getDiscordEventAdminCommands()).thenReturn(Arrays.asList("stop"));
        when(applicationProperties.getDiscordEventAdminUserIds()).thenReturn(Arrays.asList(1L));

        // when
        new MessageCreate(messageReceivedEventAdapterFactory, applicationProperties, localeService, commandService, dynamicSettingService).handle(mock(MessageReceivedEvent.class));

        // then
        verify(messageCommand, times(0)).execute(any(), any());
    }

}