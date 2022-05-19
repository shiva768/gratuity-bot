package jp.vcoin.gratuitybot.command.text.impl.admin;

import jp.vcoin.gratuitybot.config.ApplicationProperties;
import jp.vcoin.gratuitybot.domain.DynamicSettingDomain;
import jp.vcoin.gratuitybot.domain.LinkageBotUser;
import jp.vcoin.gratuitybot.enumeration.DynamicSettingType;
import jp.vcoin.gratuitybot.service.DynamicSettingService;
import jp.vcoin.gratuitybot.adapter.MessageReceivedEventAdapter;
import jp.vcoin.gratuitybot.command.text.MessageCommand;
import jp.vcoin.gratuitybot.command.text.ShowDynamicSettingCommand;
import jp.vcoin.gratuitybot.marker.Text;
import jp.vcoin.gratuitybot.util.MessageSourceWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Component("show-linkage-bot-users")
@Text
@Slf4j
public class ShowLinkageBotUsers implements MessageCommand<MessageReceivedEvent>, ShowDynamicSettingCommand {
    private final DynamicSettingService dynamicSettingService;
    private final ApplicationProperties applicationProperties;
    private final MessageSourceWrapper messageSourceWrapper;

    @Autowired
    public ShowLinkageBotUsers(DynamicSettingService dynamicSettingService, ApplicationProperties applicationProperties, MessageSourceWrapper messageSourceWrapper) {
        this.dynamicSettingService = dynamicSettingService;
        this.applicationProperties = applicationProperties;
        this.messageSourceWrapper = messageSourceWrapper;
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void execute(MessageReceivedEventAdapter eventAdapter, Locale locale) {
        final EmbedBuilder builder = new EmbedBuilder();
        buildContent(eventAdapter, locale, builder);
        if (builder.getFieldCount() > 0) {
            final EmbedObject embedObject = builder
                    .withColor(
                            applicationProperties.getDiscordEventTextColorRed(),
                            applicationProperties.getDiscordEventTextColorGreen(),
                            applicationProperties.getDiscordEventTextColorBlue()
                    ).build();
            eventAdapter.sendMessage(eventAdapter.getAuthor() + "\n", embedObject);
        } else {
            eventAdapter.sendMessage(messageSourceWrapper.getMessage(
                    "discord.admin.show-dynamic-setting.empty",
                    locale,
                    eventAdapter.getAuthor()
            ));
        }
    }

    @Override
    public void buildContent(MessageReceivedEventAdapter eventAdapter, Locale locale, EmbedBuilder builder) {
        final List<LinkageBotUser> linkageBotList = getDynamicSettingDomains(eventAdapter.getGuildId());
        linkageBotList.forEach(d -> builder.appendField(
                messageSourceWrapper.getMessage(DynamicSettingDomain.SETTING_NAME_KEY_PREFIX + StringUtils.substringAfterLast(d.getKey().getKey(), "."), locale)
                        + (d.getKey().getSecondKey() != null ? String.format("[%s]",
                        Optional.ofNullable(eventAdapter.getUserByStringId(d.getKey().getSecondKey()))
                                .map(IUser::getName)
                                .orElse(messageSourceWrapper.getMessage("discord.admin.word.show-linkage-bot-user.not-exists-bot-user", locale)))
                        : ""),
                d.format(),
                false));
    }

    private List<LinkageBotUser> getDynamicSettingDomains(long guildId) {
        final DynamicSettingType linkageBotSetting = DynamicSettingType.LinkageBotUser;
        return dynamicSettingService.getList(linkageBotSetting.getKey(), guildId, LinkageBotUser.class);
    }
}
