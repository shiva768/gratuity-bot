package jp.vcoin.gratuitybot.command.text.impl.admin;

import jp.vcoin.gratuitybot.domain.LinkageBotUser;
import jp.vcoin.gratuitybot.enumeration.DynamicSettingType;
import jp.vcoin.gratuitybot.service.DynamicSettingService;
import jp.vcoin.gratuitybot.adapter.MessageReceivedEventAdapter;
import jp.vcoin.gratuitybot.command.CommandType;
import jp.vcoin.gratuitybot.command.text.MessageCommand;
import jp.vcoin.gratuitybot.marker.Text;
import jp.vcoin.gratuitybot.util.MessageSourceWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IUser;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Component("set-linkage-bot-user")
@Text
@Slf4j
public class SetLinkageBotUser implements MessageCommand<MessageReceivedEvent> {

    private final DynamicSettingService dynamicSettingService;
    private final MessageSourceWrapper messageSourceWrapper;
    private final ApplicationContext applicationContext;

    @Autowired
    public SetLinkageBotUser(DynamicSettingService dynamicSettingService, MessageSourceWrapper messageSourceWrapper, ApplicationContext applicationContext) {
        this.dynamicSettingService = dynamicSettingService;
        this.messageSourceWrapper = messageSourceWrapper;
        this.applicationContext = applicationContext;
    }

    @Override
    public void execute(MessageReceivedEventAdapter eventAdapter, Locale locale) {

        final IUser author = eventAdapter.getAuthor();
        try {
            final LinkageBotUser linkageBotUser = new LinkageBotUser(eventAdapter.getContent(0));
            if (!validate(eventAdapter, locale, linkageBotUser))
                return;
            dynamicSettingService.save(DynamicSettingType.LinkageBotUser.getKey(), String.valueOf(linkageBotUser.getId()), eventAdapter.getGuildId(), linkageBotUser.serialize());
            eventAdapter.sendMessage(messageSourceWrapper.getMessage(
                    "discord.admin.set-specific-xxx.success",
                    locale,
                    author,
                    linkageBotUser.format()
            ));

        } catch (Throwable t) {
            failed(eventAdapter, locale, author);
        }
    }

    private void failed(MessageReceivedEventAdapter eventAdapter, Locale locale, IUser author) {
        eventAdapter.sendMessage(messageSourceWrapper.getMessage(
                "discord.admin.set-specific-xxx.failed",
                locale,
                author
        ));
    }

    private boolean validate(MessageReceivedEventAdapter eventAdapter, Locale locale, LinkageBotUser linkageBotUser) {
        final IUser author = eventAdapter.getAuthor();
        if (linkageBotUser.getId() == null) {
            eventAdapter.sendMessage(messageSourceWrapper.getMessage(
                    "discord.admin.set-linkage-bot-user.validate.required-user-id",
                    locale,
                    author
            ));
            return false;
        }
        final IUser user = eventAdapter.getUserByID(linkageBotUser.getId());
        if (user == null || !user.isBot()) {
            eventAdapter.sendMessage(messageSourceWrapper.getMessage(
                    "discord.admin.set-linkage-bot-user.validate.not-exists-bot-user",
                    locale,
                    author
            ));
            return false;
        }
        if (linkageBotUser.getPublicMessageChannelId() != null
                && eventAdapter.getChannelById(linkageBotUser.getPublicMessageChannelId()) == null) {
            eventAdapter.sendMessage(messageSourceWrapper.getMessage(
                    "discord.admin.set-linkage-bot-user.validate.not-exists-channel-id",
                    locale,
                    author
            ));
            return false;
        }
        final Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(CommandType.Text.getMarker());
        final List<String> commands = linkageBotUser.getCommands();
        if (commands != null) {
            final List<String> invalidCommand = commands.stream().filter(c -> !beansWithAnnotation.keySet().contains(c)).collect(Collectors.toList());
            if (!invalidCommand.isEmpty()) {
                eventAdapter.sendMessage(messageSourceWrapper.getMessage(
                        "discord.admin.set-linkage-bot-user.validate.contains-invalid-command",
                        locale,
                        author,
                        String.join(",", invalidCommand)
                ));
                return false;
            }
        }
        return true;
    }

}
