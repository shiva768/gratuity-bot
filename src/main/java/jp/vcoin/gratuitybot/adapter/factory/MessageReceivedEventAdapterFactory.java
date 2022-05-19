package jp.vcoin.gratuitybot.adapter.factory;

import jp.vcoin.gratuitybot.config.ApplicationProperties;
import jp.vcoin.gratuitybot.domain.LinkageBotUser;
import jp.vcoin.gratuitybot.enumeration.DynamicSettingType;
import jp.vcoin.gratuitybot.service.DynamicSettingService;
import jp.vcoin.gratuitybot.adapter.MessageReceivedEventAdapter;
import jp.vcoin.gratuitybot.adapter.impl.MessageReceivedEventAdapterImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.util.List;
import java.util.Optional;

@Service
public class MessageReceivedEventAdapterFactory {

    private final ApplicationProperties applicationProperties;
    private final DynamicSettingService dynamicSettingService;

    @Autowired
    public MessageReceivedEventAdapterFactory(ApplicationProperties applicationProperties, DynamicSettingService dynamicSettingService) {
        this.applicationProperties = applicationProperties;
        this.dynamicSettingService = dynamicSettingService;
    }


    public MessageReceivedEventAdapter getAdapter(MessageReceivedEvent event) {
        List<LinkageBotUser> linkageBots = Optional.ofNullable(event.getGuild())
                .map(g -> dynamicSettingService.getList(DynamicSettingType.LinkageBotUser.getKey(), g.getLongID(), LinkageBotUser.class))
                .orElse(null);

        return new MessageReceivedEventAdapterImpl(
                event,
                applicationProperties.getDiscordEventCommandMaxCount(),
                linkageBots
        );
    }
}
