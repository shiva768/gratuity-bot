package jp.vcoin.gratuitybot.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;
import java.lang.reflect.InvocationTargetException;

@Entity
@Table(name = "dynamic_settings", indexes = {@Index(name = "IDX_KEY", columnList = "key,second_key", unique = true)})
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class DynamicSetting {

    @Id
    @Column(name = "id")
    private String id;


    @Column(name = "key")
    private String key;

    @Column(name = "second_key")
    private String secondKey;

    @Column
    private long server;

    @NonNull
    @Column(name = "content")
    private String content;

    public DynamicSetting(String key, String secondKey, long server, String content) {
        this.id = createId(key, secondKey, server);
        this.key = key;
        this.secondKey = secondKey;
        this.server = server;
        this.content = content;
    }

    public <T extends DynamicSettingDomain> T convert(Class<T> clazz) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        final T instance = clazz.getDeclaredConstructor().newInstance();
        instance.bind(this);
        return instance;
    }

    public DynamicSettingKey getKeys() {
        return new DynamicSettingKey(key, secondKey, server);
    }

    private String createId(String key, String secondKey, long server) {
        return key + (secondKey != null ? "." + secondKey : "") + "." + server;
    }
}
