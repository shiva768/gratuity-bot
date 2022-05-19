package jp.vcoin.gratuitybot.service;

import jp.vcoin.gratuitybot.domain.DynamicSettingDomain;

import java.util.List;
import java.util.Optional;

public interface DynamicSettingService {

    <T extends DynamicSettingDomain> Optional<T> get(String key, long server, Class<T> clazz);

    <T extends DynamicSettingDomain> Optional<T> get(String key, String secondKey, long server, Class<T> clazz);

    <T extends DynamicSettingDomain> List<T> getList(String key, long server, Class<T> clazz);

    void save(String key, long server, String content);

    void save(String key, String secondKey, long server, String content);

    boolean delete(String key, long server);

    boolean delete(String key, String secondKey, long server);

    void evict();

    List<String> getKeyList(long server);
}
