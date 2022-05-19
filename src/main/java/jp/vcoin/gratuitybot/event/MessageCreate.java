package jp.vcoin.gratuitybot.event;

import jp.vcoin.gratuitybot.config.ApplicationProperties;
import jp.vcoin.gratuitybot.domain.AdminUser;
import jp.vcoin.gratuitybot.domain.GeneralSetting;
import jp.vcoin.gratuitybot.enumeration.DynamicSettingType;
import jp.vcoin.gratuitybot.service.CommandService;
import jp.vcoin.gratuitybot.service.DynamicSettingService;
import jp.vcoin.gratuitybot.service.LocaleService;
import jp.vcoin.gratuitybot.adapter.MessageReceivedEventAdapter;
import jp.vcoin.gratuitybot.adapter.factory.MessageReceivedEventAdapterFactory;
import jp.vcoin.gratuitybot.command.text.MessageCommand;
import jp.vcoin.gratuitybot.marker.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IUser;

import java.util.*;

@Component
@Slf4j
@Event
public class MessageCreate implements IListener<MessageReceivedEvent> {

    private final MessageReceivedEventAdapterFactory messageReceivedEventAdapterFactory;
    private final ApplicationProperties applicationProperties;
    private final LocaleService localeService;
    private final Map<String, MessageCommand> messageCommandMap;
    private final Set<Long> adminUserIdSet;
    private final Set<String> channelCommandSet;
    private final Set<String> dmCommandSet;
    private final Set<String> adminCommandSet;
    private final DynamicSettingService dynamicSettingService;

    @Autowired
    public MessageCreate(MessageReceivedEventAdapterFactory messageReceivedEventAdapterFactory,
                         ApplicationProperties applicationProperties,
                         LocaleService localeService,
                         CommandService commandService, DynamicSettingService dynamicSettingService) {
        this.messageReceivedEventAdapterFactory = messageReceivedEventAdapterFactory;
        this.applicationProperties = applicationProperties;
        this.localeService = localeService;
        this.messageCommandMap = commandService.getMessageCommandMap();
        this.channelCommandSet = new HashSet<>(applicationProperties.getDiscordEventTextEnableChannelCommands());
        this.dmCommandSet = new HashSet<>(applicationProperties.getDiscordEventTextEnableDmCommands());
        this.adminCommandSet = new HashSet<>(applicationProperties.getDiscordEventAdminCommands());
        this.adminUserIdSet = new HashSet<>(applicationProperties.getDiscordEventAdminUserIds());
        this.dynamicSettingService = dynamicSettingService;
    }

    @Override
    public void handle(MessageReceivedEvent event) {
        final MessageReceivedEventAdapter eventAdapter = messageReceivedEventAdapterFactory.getAdapter(event);
        if (isExclude(eventAdapter)) return;

        final String command = eventAdapter.getCommand().substring(applicationProperties.getDiscordEventTriggerPrefix().length());
        IUser author = eventAdapter.getAuthor();
        if (!isValidCommand(eventAdapter, command))
            return;
        final Locale locale = localeService.getLocale(eventAdapter);

        log.info("author name:{}, id:{}, command:{}", author.getName(), author.getLongID(), command);
        Optional.ofNullable(messageCommandMap.get(command)).ifPresent(c -> c.execute(eventAdapter, locale));
    }

    private boolean isValidCommand(MessageReceivedEventAdapter eventAdapter, String command) {
        IUser author = eventAdapter.getAuthor();
        if (eventAdapter.isDM()) {
            if (!eventAdapter.isLinkageBotUser())
                return dmCommandSet.contains(command);
        } else {
            if (isEnableCommandChannel(eventAdapter)) {
                if (eventAdapter.isLinkageBotUser())
                    return eventAdapter.getLinkageBotUserCommands().contains(command);
                if (adminCommandSet.contains(command)
                        && (
                        adminUserIdSet.contains(author.getLongID())
                                || dynamicSettingService.getList(
                                DynamicSettingType.AdminUser.getKey(),
                                eventAdapter.getGuildId(),
                                AdminUser.class
                        ).stream().anyMatch(s -> author.getStringID().equals(s.getKey().getSecondKey()))
                ))
                    return true;
                return channelCommandSet.contains(command);
            }
        }
        return false;
    }

    // TODO ここ
    private boolean isEnableCommandChannel(MessageReceivedEventAdapter eventAdapter) {
        if (applicationProperties.getDiscordEventTextChannelCommandChannelIds().stream().anyMatch(id -> id == eventAdapter.getChannelId()))
            return true;
        List<GeneralSetting> dynamicSettingList = dynamicSettingService.getList(DynamicSettingType.CommandEnableChannel.getKey(),
                eventAdapter.getGuildId(),
                GeneralSetting.class);
        if (!dynamicSettingList.isEmpty()) {
            return dynamicSettingList.stream().anyMatch(s -> Long.parseLong(s.content) == eventAdapter.getChannelId());
        } else {
            return true;
        }
    }

    private boolean isExclude(MessageReceivedEventAdapter eventAdapter) {
        return (eventAdapter.getAuthor().isBot() && !eventAdapter.isLinkageBotUser())
                || eventAdapter.isNullContent()
                || !eventAdapter.getCommand().trim().startsWith(applicationProperties.getDiscordEventTriggerPrefix());
    }
}
