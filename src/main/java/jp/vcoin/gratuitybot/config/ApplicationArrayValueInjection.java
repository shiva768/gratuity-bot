package jp.vcoin.gratuitybot.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@SuppressWarnings("WeakerAccess")
@Configuration
@ConfigurationProperties(prefix = "discord")
public class ApplicationArrayValueInjection {
    private Event event;
    private Map<String, List<Long>> channelDefaultLanguage;
    private List<String> supportLanguage;

    @Setter
    @Getter
    public static class Event {
        private Admin admin;
        private Text text;
        private Emoji emoji;

        @Setter
        @Getter
        public static class Admin {
            private User user;
            private List<String> commands;

            @Getter
            @Setter
            public static class User {
                private List<Long> ids;
            }
        }

        @Setter
        @Getter
        public static class Text {
            private List<String> enableChannelCommands;
            private List<String> additionalEnableChannelCommands;
            private List<String> enableDmCommands;
            private List<Long> channelCommandChannelIds;
        }

        @Setter
        @Getter
        public static class Emoji {
            private Command command;

            @Setter
            @Getter
            public static class Command {
                private Gratuity gratuity;

                @Setter
                @Getter
                public static class Gratuity {
                    private List<EmojiElement> emojis;

                    @Setter
                    @Getter
                    public static class EmojiElement {
                        private Long id;
                        private BigDecimal value;
                    }
                }
            }
        }
    }

}
