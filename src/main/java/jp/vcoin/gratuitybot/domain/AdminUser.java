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
public class AdminUser implements DynamicSettingDomain {

    private DynamicSettingKey key;
    private String userId;

    public AdminUser(String userId) {
        this.userId = userId;
    }

    @Override
    public DynamicSettingKey getKey() {
        return key;
    }

    @Override
    public String serialize() {
        return userId;
    }

    @Override
    public String format() {
        return String.join("\n",
                String.format("<@%s>(%s%s%s)", userId, BOLD, userId, BOLD)
        );
    }

    @Override
    public void bind(DynamicSetting dynamicSetting) {
        this.key = dynamicSetting.getKeys();
        this.userId = this.key.getSecondKey();
    }
}
