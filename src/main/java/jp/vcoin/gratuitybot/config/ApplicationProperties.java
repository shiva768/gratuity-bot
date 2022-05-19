package jp.vcoin.gratuitybot.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Configuration
@Component
@Getter
public class ApplicationProperties {

    @Value("${application.version}")
    private String version;

    @Value("${application.author}")
    private String author;

    @Value("${application.http.virtualcoin-url}")
    private String virtualcoinUrl;

    @Value("${application.http.connection-timeout-msec}")
    private int httpConnectionTimeout;

    @Value("${application.http.read-timeout-msec}")
    private int readTimeout;


    @Value("${application.http.rpc-user}")
    private String rpcUser;

    @Value("${application.http.rpc-password}")
    private String rpcPassword;

    @Value("${discord.reconnect-attempt}")
    private int discordReconnectAttempt;

    @Value("${discord.message.cache}")
    private int discordMessageCache;

    @Value("${discord.max-dispatch-thread-count}")
    private int discordMaxDispatchThreadCount;

    @Value("${discord.min-dispatch-thread-count}")
    private int discordMinDispatchThreadCount;

    @Value("${discord.bot.token}")
    private String discordBotToken;

    @Value("${discord.bot.notification-channel-id:#{null}}")
    private Long discordBotNotificationChannelId;

    @Value("${discord.bot.notification-user-id:#{null}}")
    private Long discordBotNotificationUserId;

    @Value("#{new Boolean('${discord.bot.silent-startup:false}')}")
    private Boolean discordBotSilentStartUp;

    @Value("${discord.event.command-max-count}")
    private int discordEventCommandMaxCount;

    @Value("${discord.event.trigger-prefix}")
    private String discordEventTriggerPrefix;

    @Value("${discord.event.text.command.gratuity.default-random-min-amount}")
    private BigDecimal discordEventTextCommandGratuityDefaultRandomMinAmount;

    @Value("${discord.event.text.command.withdraw.min-amount}")
    private BigDecimal discordEventTextCommandWithdrawMinAmount;

    @Value("${discord.event.text.footer-icon}")
    private String discordEventTextFooterIcon;

    @Value("${discord.event.text.color.r}")
    private int discordEventTextColorRed;

    @Value("${discord.event.text.color.g}")
    private int discordEventTextColorGreen;

    @Value("${discord.event.text.color.b}")
    private int discordEventTextColorBlue;

    @Value("${discord.event.emoji.notification-channel-id:#{null}}")
    private Long discordEventEmojiNotificationChannelId;

    @Value("${application.debug.mining:#{null}}")
    private String debugMiningCommand;


    private List<Long> discordEventAdminUserIds;

    private List<String> discordEventTextEnableChannelCommands;

    private List<String> discordEventTextAdditionalEnableChannelCommands = new ArrayList<>();

    private List<String> discordEventTextEnableDmCommands;

    private List<String> discordEventAdminCommands;

    private List<Long> discordEventTextChannelCommandChannelIds;

    private Map<Long, Locale> discordChannelDefaultLanguageMap;

    private Map<Long, BigDecimal> discordEventEmojiGratuityEmojiMap;

    private List<String> discordSupportLanguages;

    @Autowired
    public ApplicationProperties(ApplicationArrayValueInjection applicationArrayValueInjection) {
        this.discordEventAdminUserIds = Optional.ofNullable(applicationArrayValueInjection.getEvent().getAdmin().getUser().getIds()).orElse(new ArrayList<>());
        this.discordEventAdminCommands = Optional.ofNullable(applicationArrayValueInjection.getEvent().getAdmin().getCommands()).orElse(new ArrayList<>());
        this.discordEventTextEnableChannelCommands = Optional.ofNullable(applicationArrayValueInjection.getEvent().getText().getEnableChannelCommands()).orElse(new ArrayList<>());
        Optional.ofNullable(applicationArrayValueInjection.getEvent().getText().getAdditionalEnableChannelCommands()).ifPresent(commands -> {
            this.discordEventTextEnableChannelCommands.addAll(commands);
            this.discordEventTextAdditionalEnableChannelCommands = commands;
        });
        this.discordEventTextEnableDmCommands = Optional.ofNullable(applicationArrayValueInjection.getEvent().getText().getEnableDmCommands()).orElse(new ArrayList<>());
        this.discordEventTextChannelCommandChannelIds = Optional.ofNullable(applicationArrayValueInjection.getEvent().getText().getChannelCommandChannelIds()).orElse(new ArrayList<>());
        initLanguages(applicationArrayValueInjection.getChannelDefaultLanguage());
        initEmojis(applicationArrayValueInjection.getEvent().getEmoji().getCommand().getGratuity().getEmojis());
        this.discordSupportLanguages = applicationArrayValueInjection.getSupportLanguage();
    }

    private void initEmojis(List<ApplicationArrayValueInjection.Event.Emoji.Command.Gratuity.EmojiElement> gratuityEmojis) {
        this.discordEventEmojiGratuityEmojiMap = gratuityEmojis.stream().collect(Collectors.toMap(ApplicationArrayValueInjection.Event.Emoji.Command.Gratuity.EmojiElement::getId, ApplicationArrayValueInjection.Event.Emoji.Command.Gratuity.EmojiElement::getValue));
    }

    private void initLanguages(Map<String, List<Long>> discordChannelDefaultLanguage) {
        final Map<Long, Locale> languageMap = new HashMap<>();
        discordChannelDefaultLanguage.keySet().forEach(k -> {
            Locale locale = new Locale(k.replace(".list", ""));
            final List<Long> channelIdList = discordChannelDefaultLanguage.get(k);
            final Map<Long, Locale> collect = channelIdList.stream().collect(Collectors.toMap(id -> id, id -> locale));
            languageMap.putAll(collect);
        });
        discordChannelDefaultLanguageMap = languageMap;
    }
}
