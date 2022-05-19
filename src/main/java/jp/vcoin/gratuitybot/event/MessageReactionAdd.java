package jp.vcoin.gratuitybot.event;

import jp.vcoin.gratuitybot.command.emoji.ReactionCommand;
import jp.vcoin.gratuitybot.domain.LinkageBotUser;
import jp.vcoin.gratuitybot.enumeration.DynamicSettingType;
import jp.vcoin.gratuitybot.service.CommandService;
import jp.vcoin.gratuitybot.service.DynamicSettingService;
import jp.vcoin.gratuitybot.service.LocaleService;
import jp.vcoin.gratuitybot.adapter.ReactionAddEventAdapter;
import jp.vcoin.gratuitybot.adapter.factory.ReactionAddEventAdapterFactory;
import jp.vcoin.gratuitybot.marker.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent;
import sx.blah.discord.handle.obj.IUser;

import java.util.Locale;
import java.util.Optional;

@Component
@Slf4j
@Event
public class MessageReactionAdd implements IListener<ReactionAddEvent> {

    private final ReactionAddEventAdapterFactory reactionAddEventAdapterFactory;
    private final LocaleService localeService;
    private final DynamicSettingService dynamicSettingService;
    private final CommandService commandService;

    @Autowired
    public MessageReactionAdd(LocaleService localeService, CommandService commandService, ReactionAddEventAdapterFactory reactionAddEventAdapterFactory, DynamicSettingService dynamicSettingService) {
        this.localeService = localeService;
        this.commandService = commandService;
        this.reactionAddEventAdapterFactory = reactionAddEventAdapterFactory;
        this.dynamicSettingService = dynamicSettingService;
    }

    @Override
    public void handle(ReactionAddEvent event) {
        final ReactionAddEventAdapter eventAdapter = reactionAddEventAdapterFactory.getAdapter(event);
        if (isExclude(eventAdapter)) return;

        final long emojiId = eventAdapter.getEmojiId();
        IUser eventOccurrenceUser = eventAdapter.getEventOccurrenceUser();
        log.debug("user name:{}, id:{}, emoji:{}", eventOccurrenceUser.getName(), eventOccurrenceUser.getStringID(), emojiId);
        final ReactionCommand command = commandService.getReactionCommandMap(eventAdapter.getGuildId()).get(emojiId);
        if (command != null) {
            final Long channelId = Optional.ofNullable(command.getNotifyChannelId(eventAdapter.getGuild())).orElse(eventAdapter.getChannelId());
            final Locale locale = localeService.getLocale(eventAdapter.getGuildId(), channelId);
            command.execute(eventAdapter, locale);
        }
    }


    private boolean isExclude(ReactionAddEventAdapter eventAdapter) {
        return eventAdapter.getEventOccurrenceUser().isBot()
                || (eventAdapter.getMessageAuthor().isBot() && !isEnableEmoji(eventAdapter.getMessageAuthor().getLongID(), eventAdapter.getGuildId()));
    }

    private boolean isEnableEmoji(long id, long guildId) {
        return dynamicSettingService.getList(DynamicSettingType.LinkageBotUser.getKey(), guildId, LinkageBotUser.class).stream()
                .filter(LinkageBotUser::isForEmojiGratuity)
                .anyMatch(l -> l.getId() == id);
    }
}
