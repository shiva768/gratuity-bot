package jp.vcoin.gratuitybot.domain;

public interface DynamicSettingDomain {

    String SETTING_NAME_KEY_PREFIX = "discord.admin.word.dynamic-setting.";

    DynamicSettingKey getKey();

    String serialize();

    String format();

    void bind(DynamicSetting dynamicSetting);
}
