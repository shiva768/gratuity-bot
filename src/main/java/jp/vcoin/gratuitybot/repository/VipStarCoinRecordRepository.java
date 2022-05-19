package jp.vcoin.gratuitybot.repository;

import jp.vcoin.gratuitybot.domain.RecordValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VirtualCoinRecordRepository extends JpaRepository<RecordValue, String> {
}
