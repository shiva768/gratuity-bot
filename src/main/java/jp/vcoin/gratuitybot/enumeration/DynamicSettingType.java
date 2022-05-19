package jp.vcoin.gratuitybot.enumeration;

import lombok.Getter;

import java.util.Optional;

public enum DynamicSettingType {
    RandomGratuityMinLimit("gratuity.random-min-amount"),
    Language("discord.language", "show-languages"),
    LinkageBotUser("array.linkage-bot-users", "show-linkage-bot-users"),
    EmojiAmount("emoji-amounts", "show-emoji-amounts"),
    AdminUser("admin-user", "show-admin-users"),
    NotifyChannel("notify-channel"),
    EmojiNotifyChannel("emoji-notify-channel"),
    CommandEnableChannel("command-enable-channel");

    @Getter
    private String key;
    private String showCommand;

    DynamicSettingType(String key) {
        this.key = key;
    }

    DynamicSettingType(String key, String showCommand) {
        this.key = key;
        this.showCommand = showCommand;
    }

    public Optional<String> getShowCommand() {
        return Optional.ofNullable(showCommand);
    }

    public boolean isChannelSetting() {
        return key.endsWith("channel");
    }
}
