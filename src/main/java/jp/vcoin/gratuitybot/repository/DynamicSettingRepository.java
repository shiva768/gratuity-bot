package jp.vcoin.gratuitybot.repository;

import jp.vcoin.gratuitybot.domain.DynamicSetting;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DynamicSettingRepository extends JpaRepository<DynamicSetting, String> {
    List<DynamicSetting> findByKeyAndServer(String key, long server, Sort by);

    Optional<DynamicSetting> findByKeyAndSecondKeyAndServer(String key, String secondKey, long server);

    List<DynamicSetting> findAByServer(long server, Sort id);

    int deleteByKeyAndSecondKeyAndServer(String key, String secondKey, long server);
}
