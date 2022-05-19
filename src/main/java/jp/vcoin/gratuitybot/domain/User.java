package jp.vcoin.gratuitybot.domain;

import jp.vcoin.gratuitybot.adapter.MessageReceivedEventAdapter;
import jp.vcoin.gratuitybot.util.DiscordUtil;

public class User implements DiscordObject {
    private static String KEY = "user";
    private String id;
    private String original;

    public User(String id) {
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
        return DiscordUtil.convertUserId(id);
    }

    @Override
    public boolean isNotExists(MessageReceivedEventAdapter eventAdapter) {
        return eventAdapter.getUserByStringId(this.id) == null;
    }

    @Override
    public String getKey() {
        return KEY;
    }
}
