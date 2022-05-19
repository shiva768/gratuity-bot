package jp.vcoin.gratuitybot.service;

import jp.vcoin.gratuitybot.domain.DiscordObject;
import jp.vcoin.gratuitybot.adapter.MessageReceivedEventAdapter;
import sx.blah.discord.handle.obj.IUser;

import java.util.Locale;

public interface DiscordValidatorService {

    boolean validate(Locale locale, IUser author, MessageReceivedEventAdapter eventAdapter, boolean required, boolean delete, DiscordObject discordObject);
}
