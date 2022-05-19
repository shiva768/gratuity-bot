package jp.vcoin.gratuitybot.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@NoArgsConstructor
public class GeneralSetting implements DynamicSettingDomain {

    @NonNull
    public String content;
    @Getter
    private DynamicSettingKey key;

    @Override
    public String serialize() {
        return content;
    }

    @Override
    public String format() {
        return content;
    }

    @Override
    public void bind(DynamicSetting dynamicSetting) {
        this.key = dynamicSetting.getKeys();
        this.content = dynamicSetting.getContent();
    }
}
