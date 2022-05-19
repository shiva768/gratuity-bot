package jp.vcoin.gratuitybot.domain;

import jp.vcoin.gratuitybot.adapter.MessageReceivedEventAdapter;
import jp.vcoin.gratuitybot.util.DiscordUtil;

public class Emoji implements DiscordObject {
    private static String KEY = "emoji";
    private String id;
    private String original;

    public Emoji(String id) {
        this.original = id;
        String tmp = convert(id);
        this.id = tmp == null ? id : convert(id);
    }

    @Override
    public boolean isEmpty() {
        return this.original == null || this.original.isEmpty();
    }

    @Override
    public String getConvertedId() {
        return this.id;
    }

    private String convert(String id) {
        return DiscordUtil.convertEmojiId(id);
    }

    @Override
    public boolean isNotExists(MessageReceivedEventAdapter eventAdapter) {
        return eventAdapter.getEmojiByStringId(this.id) == null;
    }

    @Override
    public String getKey() {
        return KEY;
    }
}
