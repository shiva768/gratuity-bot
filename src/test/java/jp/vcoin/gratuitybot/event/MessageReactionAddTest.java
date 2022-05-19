package jp.vcoin.gratuitybot.event;

import jp.vcoin.gratuitybot.domain.LinkageBotUser;
import jp.vcoin.gratuitybot.adapter.ReactionAddEventAdapter;
import jp.vcoin.gratuitybot.adapter.factory.ReactionAddEventAdapterFactory;
import jp.vcoin.gratuitybot.command.emoji.ReactionCommand;
import jp.vcoin.gratuitybot.config.ApplicationProperties;
import jp.vcoin.gratuitybot.enumeration.DynamicSettingType;
import jp.vcoin.gratuitybot.service.CommandService;
import jp.vcoin.gratuitybot.service.DynamicSettingService;
import jp.vcoin.gratuitybot.service.LocaleService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent;
import sx.blah.discord.handle.obj.IUser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;

import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class MessageReactionAddTest {

    @MockBean
    ReactionAddEventAdapterFactory reactionAddEventAdapterFactory;
    @MockBean
    ApplicationProperties applicationProperties;
    @MockBean
    LocaleService localeService;
    @MockBean
    CommandService commandService;
    @Mock
    ReactionAddEventAdapter reactionAddEventAdapter;
    @MockBean
    DynamicSettingService dynamicSettingService;

    @Before
    public void setUp() {
        when(reactionAddEventAdapter.getGuildId()).thenReturn(1L);
    }

    @Test
    public void 正常() {
        // setup
        long emoji = 12L;
        final IUser messageAuthor = mock(IUser.class);
        when(messageAuthor.isBot()).thenReturn(false);
        when(messageAuthor.getLongID()).thenReturn(1L);
        when(reactionAddEventAdapter.getEventOccurrenceUser()).thenReturn(mock(IUser.class));
        when(reactionAddEventAdapter.getMessageAuthor()).thenReturn(messageAuthor);
        when(reactionAddEventAdapterFactory.getAdapter(any())).thenReturn(reactionAddEventAdapter);
        when(reactionAddEventAdapter.getEmojiId()).thenReturn(emoji);
        when(localeService.getLocale(reactionAddEventAdapter)).thenReturn(Locale.JAPANESE);
        final ReactionCommand reactionCommand = mock(ReactionCommand.class);
        doNothing().when(reactionCommand).execute(any(), any());
        when(commandService.getReactionCommandMap(anyLong())).thenReturn(new HashMap<Long, ReactionCommand>() {{
            put(emoji, reactionCommand);
        }});

        // when
        new MessageReactionAdd(localeService, commandService, reactionAddEventAdapterFactory, dynamicSettingService).handle(mock(ReactionAddEvent.class));

        // then
        verify(reactionCommand, times(1)).execute(any(), any());
    }

    @Test
    public void 準異常_BOT() {
        // setup
        long emoji = 12L;
        final IUser messageAuthor = mock(IUser.class);
        when(messageAuthor.isBot()).thenReturn(true);
        when(messageAuthor.getLongID()).thenReturn(1L);
        when(reactionAddEventAdapter.getEventOccurrenceUser()).thenReturn(mock(IUser.class));
        when(reactionAddEventAdapter.getMessageAuthor()).thenReturn(messageAuthor);
        when(reactionAddEventAdapter.getChannelId()).thenReturn(1L);
        when(reactionAddEventAdapterFactory.getAdapter(any())).thenReturn(reactionAddEventAdapter);
        when(reactionAddEventAdapter.getEmojiId()).thenReturn(emoji);
        when(dynamicSettingService.getList(eq(DynamicSettingType.LinkageBotUser.getKey()), anyLong(), eq(LinkageBotUser.class))).thenReturn(Arrays.asList(LinkageBotUser.builder().id(0L).forEmojiGratuity(true).build()));
        when(localeService.getLocale(reactionAddEventAdapter)).thenReturn(Locale.JAPANESE);
        final ReactionCommand reactionCommand = mock(ReactionCommand.class);
        doNothing().when(reactionCommand).execute(any(), any());
        when(commandService.getReactionCommandMap(anyLong())).thenReturn(new HashMap<Long, ReactionCommand>() {{
            put(emoji, reactionCommand);
        }});

        // when
        new MessageReactionAdd(localeService, commandService, reactionAddEventAdapterFactory, dynamicSettingService).handle(mock(ReactionAddEvent.class));

        // then
        verify(reactionCommand, times(0)).execute(any(), any());
    }

    @Test
    public void 正常_連携BOT() {
        // setup
        long emoji = 12L;
        final IUser messageAuthor = mock(IUser.class);
        when(messageAuthor.isBot()).thenReturn(true);
        when(messageAuthor.getLongID()).thenReturn(1L);
        when(reactionAddEventAdapter.getEventOccurrenceUser()).thenReturn(mock(IUser.class));
        when(reactionAddEventAdapter.getMessageAuthor()).thenReturn(messageAuthor);
        when(reactionAddEventAdapter.getChannelId()).thenReturn(1L);
        when(reactionAddEventAdapterFactory.getAdapter(any())).thenReturn(reactionAddEventAdapter);
        when(reactionAddEventAdapter.getEmojiId()).thenReturn(emoji);
        when(dynamicSettingService.getList(eq(DynamicSettingType.LinkageBotUser.getKey()), anyLong(), eq(LinkageBotUser.class))).thenReturn(Arrays.asList(
                LinkageBotUser.builder().id(1L).forEmojiGratuity(true).build(),
                LinkageBotUser.builder().id(2L).forEmojiGratuity(true).build(),
                LinkageBotUser.builder().id(3L).forEmojiGratuity(true).build()));
        when(localeService.getLocale(reactionAddEventAdapter)).thenReturn(Locale.JAPANESE);
        final ReactionCommand reactionCommand = mock(ReactionCommand.class);
        doNothing().when(reactionCommand).execute(any(), any());
        when(commandService.getReactionCommandMap(anyLong())).thenReturn(new HashMap<Long, ReactionCommand>() {{
            put(emoji, reactionCommand);
        }});

        // when
        new MessageReactionAdd(localeService, commandService, reactionAddEventAdapterFactory, dynamicSettingService).handle(mock(ReactionAddEvent.class));

        // then
        verify(reactionCommand, times(1)).execute(any(), any());
    }
}