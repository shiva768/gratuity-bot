package jp.vcoin.gratuitybot.service;

import jp.vcoin.gratuitybot.domain.RecordValue;

public interface VirtualCoinRecordService {

    RecordValue getRecord(String authorId);

    void gratuity(RecordValue... targetValues);
}
