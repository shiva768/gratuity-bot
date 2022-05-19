package jp.vcoin.gratuitybot.domain;

import jp.vcoin.gratuitybot.adapter.MessageReceivedEventAdapter;

public interface DiscordObject {
    boolean isEmpty();

    String getConvertedId();

    boolean isNotExists(MessageReceivedEventAdapter eventAdapter);

    String getKey();
}
