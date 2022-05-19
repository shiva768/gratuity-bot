package jp.vcoin.gratuitybot.command.text.impl;

import jp.vcoin.gratuitybot.service.CommandService;
import jp.vcoin.gratuitybot.service.DynamicSettingService;
import jp.vcoin.gratuitybot.adapter.MessageReceivedEventAdapter;
import jp.vcoin.gratuitybot.command.text.MessageCommand;
import jp.vcoin.gratuitybot.marker.Text;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.util.Locale;

@Component("clear-cache")
@Text
@Slf4j
@Profile({"stage", "dev"})
public class ClearCache implements MessageCommand<MessageReceivedEvent> {

    private final DynamicSettingService dynamicSettingService;
    private final CommandService commandService;

    @Autowired
    public ClearCache(DynamicSettingService dynamicSettingService, CommandService commandService) {
        this.dynamicSettingService = dynamicSettingService;
        this.commandService = commandService;
    }

    @Override
    public void execute(MessageReceivedEventAdapter eventAdapter, Locale locale) {
        dynamicSettingService.evict();
        commandService.evict(eventAdapter.getGuildId());
        eventAdapter.sendMessage("clear!!");
    }
}
