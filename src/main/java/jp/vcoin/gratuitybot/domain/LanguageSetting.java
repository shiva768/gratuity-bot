package jp.vcoin.gratuitybot.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static sx.blah.discord.util.MessageBuilder.Styles.BOLD;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class LanguageSetting implements DynamicSettingDomain {

    private DynamicSettingKey key;
    private String channelId;
    private String content;

    public LanguageSetting(String language) {
        this.content = language;
    }

    public LanguageSetting(String language, String channelId) {
        this.content = language;
        this.channelId = channelId;
    }

    @Override
    public DynamicSettingKey getKey() {
        return key;
    }

    @Override
    public String serialize() {
        return content;
    }

    @Override
    public String format() {
        return String.join("\n",
                String.format("[channel]:%s%s%s", BOLD, channelId != null ? channelId : "Server Default", BOLD),
                String.format("[language]:%s%s%s", BOLD, content, BOLD)
        );
    }

    @Override
    public void bind(DynamicSetting dynamicSetting) {
        this.key = dynamicSetting.getKeys();
        this.channelId = this.key.getSecondKey();
        this.content = dynamicSetting.getContent();
    }
}
