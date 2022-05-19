package jp.vcoin.gratuitybot.service.impl;

import jp.vcoin.gratuitybot.adapter.EventAdapter;
import jp.vcoin.gratuitybot.domain.LanguageSetting;
import jp.vcoin.gratuitybot.service.DynamicSettingService;
import jp.vcoin.gratuitybot.service.LocaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.util.Locale;
import java.util.Optional;

@Service
public class LocaleServiceImpl implements LocaleService {

    private final DynamicSettingService dynamicSettingService;

    @Autowired
    public LocaleServiceImpl(DynamicSettingService dynamicSettingService) {
        this.dynamicSettingService = dynamicSettingService;
    }

    @Override
    public Locale getLocale(EventAdapter eventAdapter) {
        return getLocale(eventAdapter.getGuildId(), eventAdapter.getChannelId());
    }

    @Override
    public Locale getLocale(MessageReceivedEvent event) {
        return getLocale(event.getGuild().getLongID(), event.getChannel().getLongID());
    }

    @Override
    public Locale getLocale(long serverId, long channelId) {
        final Optional<LanguageSetting> channelLanguage = dynamicSettingService.get("discord.language", String.valueOf(channelId), serverId, LanguageSetting.class);
        return channelLanguage.map(s -> new Locale(s.getContent()))
                .orElse(
                        dynamicSettingService.get("discord.language", null, serverId, LanguageSetting.class).map(s -> new Locale(s.getContent()))
                                .orElse(Locale.JAPANESE)
                );
    }
}
