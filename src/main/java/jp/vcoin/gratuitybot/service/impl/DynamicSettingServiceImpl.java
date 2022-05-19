package jp.vcoin.gratuitybot.service.impl;

import jp.vcoin.gratuitybot.domain.DynamicSettingDomain;
import jp.vcoin.gratuitybot.repository.DynamicSettingRepository;
import jp.vcoin.gratuitybot.StreamHelper;
import jp.vcoin.gratuitybot.domain.DynamicSetting;
import jp.vcoin.gratuitybot.service.DynamicSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DynamicSettingServiceImpl implements DynamicSettingService {

    private final DynamicSettingServiceInner dynamicSettingServiceInner;

    @Autowired
    public DynamicSettingServiceImpl(DynamicSettingServiceInner dynamicSettingServiceInner) {
        this.dynamicSettingServiceInner = dynamicSettingServiceInner;
    }

    @Override
    public <T extends DynamicSettingDomain> Optional<T> get(String key, long server, Class<T> clazz) {
        return dynamicSettingServiceInner.get(key, null, server, clazz);
    }

    @Override
    public <T extends DynamicSettingDomain> Optional<T> get(String key, String secondKey, long server, Class<T> clazz) {
        return dynamicSettingServiceInner.get(key, secondKey, server, clazz);
    }

    @Override
    public <T extends DynamicSettingDomain> List<T> getList(String key, long server, Class<T> clazz) {
        return dynamicSettingServiceInner.get(key, server, clazz);
    }

    @Transactional(rollbackOn = Exception.class)
    @Override
    public void save(String key, long server, String content) {
        dynamicSettingServiceInner.save(key, server, content);
        dynamicSettingServiceInner.evict();
    }

    @Transactional(rollbackOn = Exception.class)
    @Override
    public void save(String key, String secondKey, long server, String content) {
        dynamicSettingServiceInner.save(key, secondKey, server, content);
        dynamicSettingServiceInner.evict();
    }

    @Transactional(rollbackOn = Exception.class)
    @Override
    public boolean delete(String key, long server) {
        final int deleteCount = dynamicSettingServiceInner.delete(key, server);
        dynamicSettingServiceInner.evict();
        return deleteCount > 0;
    }

    @Transactional(rollbackOn = Exception.class)
    @Override
    public boolean delete(String key, String secondKey, long server) {
        final int deleteCount = dynamicSettingServiceInner.delete(key, secondKey, server);
        dynamicSettingServiceInner.evict();
        return deleteCount > 0;
    }

    @Override
    public void evict() {
        dynamicSettingServiceInner.evict();
    }

    @Override
    public List<String> getKeyList(long server) {
        return dynamicSettingServiceInner.getKeyList(server);
    }

    @CacheConfig(cacheNames = "settings")
    @Service
    public static class DynamicSettingServiceInner {

        private final DynamicSettingRepository dynamicSettingRepository;

        @Autowired
        public DynamicSettingServiceInner(DynamicSettingRepository dynamicSettingRepository) {
            this.dynamicSettingRepository = dynamicSettingRepository;
        }

        @Cacheable(key = "{#key, #server}", sync = true)
        public <T extends DynamicSettingDomain> List<T> get(String key, long server, Class<T> clazz) {
            final List<DynamicSetting> results = dynamicSettingRepository.findByKeyAndServer(key, server, Sort.by("id"));
            return results.stream().map(StreamHelper.throwingFunction(d -> d.convert(clazz))).collect(Collectors.toList());
        }

        @Cacheable(key = "{#key, #secondKey, #server}", sync = true)
        public <T extends DynamicSettingDomain> Optional<T> get(String key, String secondKey, long server, Class<T> clazz) {
            final Optional<DynamicSetting> result = dynamicSettingRepository.findByKeyAndSecondKeyAndServer(key, secondKey, server);
            return result.map(d -> {
                try {
                    return d.convert(clazz);
                } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        void save(String key, long server, String content) {
            save(key, null, server, content);
        }

        void save(String key, String secondKey, long server, String content) {
            dynamicSettingRepository.save(new DynamicSetting(key, secondKey, server, content));
        }

        int delete(String key, long server) {
            return delete(key, null, server);
        }

        int delete(String key, String secondKey, long server) {
            return dynamicSettingRepository.deleteByKeyAndSecondKeyAndServer(key, secondKey, server);
        }

        @CacheEvict(allEntries = true)
        public void evict() {
        }

        List<String> getKeyList(long server) {
            return dynamicSettingRepository.findAByServer(server, Sort.by("id")).stream().map(DynamicSetting::getKey).distinct().collect(Collectors.toList());
        }
    }
}
