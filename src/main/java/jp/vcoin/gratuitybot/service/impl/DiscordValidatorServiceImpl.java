package jp.vcoin.gratuitybot.service.impl;

import jp.vcoin.gratuitybot.domain.DiscordObject;
import jp.vcoin.gratuitybot.adapter.MessageReceivedEventAdapter;
import jp.vcoin.gratuitybot.service.DiscordValidatorService;
import jp.vcoin.gratuitybot.util.MessageSourceWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sx.blah.discord.handle.obj.IUser;

import java.util.Locale;

@Service
public class DiscordValidatorServiceImpl implements DiscordValidatorService {

    private final MessageSourceWrapper messageSourceWrapper;

    @Autowired
    public DiscordValidatorServiceImpl(MessageSourceWrapper messageSourceWrapper) {
        this.messageSourceWrapper = messageSourceWrapper;
    }

    @Override
    public boolean validate(Locale locale, IUser author, MessageReceivedEventAdapter eventAdapter, boolean required, boolean delete, DiscordObject discordObject) {

        if (discordObject.isEmpty()) {
            if (!required) return true;
            eventAdapter.sendMessage(
                    messageSourceWrapper.getMessage(
                            String.format("discord.admin.common.validate.%s.invalid.required", discordObject.getKey()),
                            locale,
                            author
                    )
            );
            return false;
        }
        if (!delete && discordObject.isNotExists(eventAdapter)) {
            eventAdapter.sendMessage(
                    messageSourceWrapper.getMessage(
                            String.format("discord.admin.common.validate.%s.invalid.not-exists", discordObject.getKey()),
                            locale,
                            author
                    )
            );
            return false;
        }
        return true;
    }
}
