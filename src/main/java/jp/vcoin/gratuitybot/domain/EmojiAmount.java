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
public class EmojiAmount implements DynamicSettingDomain {

    private DynamicSettingKey key;
    private String emojiId;
    private String amount;

    public EmojiAmount(String emojiId, String amount) {
        this.emojiId = emojiId;
        this.amount = amount;
    }

    @Override
    public DynamicSettingKey getKey() {
        return key;
    }

    @Override
    public String serialize() {
        return amount;
    }

    /**
     * memo:絵文字の表示は、理屈はわからないが、<:なんか文字列:emoji id>の形式にすると表示されるよう
     */
    @Override
    public String format() {
        return String.join("\n",
                String.format("[emojiId]:<:xxx:%s>(%s%s%s)", emojiId, BOLD, emojiId, BOLD),
                String.format("[amount]:%s%s%s", BOLD, amount, BOLD)
        );
    }

    @Override
    public void bind(DynamicSetting dynamicSetting) {
        this.key = dynamicSetting.getKeys();
        this.emojiId = this.key.getSecondKey();
        this.amount = dynamicSetting.getContent();
    }
}
