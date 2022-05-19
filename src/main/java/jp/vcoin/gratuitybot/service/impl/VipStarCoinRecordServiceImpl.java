package jp.vcoin.gratuitybot.service.impl;

import jp.vcoin.gratuitybot.domain.RecordValue;
import jp.vcoin.gratuitybot.repository.VirtualCoinRecordRepository;
import jp.vcoin.gratuitybot.service.VirtualCoinRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class VirtualCoinRecordServiceImpl implements VirtualCoinRecordService {

    private final VirtualCoinRecordRepository virtualCoinRecordRepository;

    @Autowired
    public VirtualCoinRecordServiceImpl(VirtualCoinRecordRepository virtualCoinRecordRepository) {
        this.virtualCoinRecordRepository = virtualCoinRecordRepository;
    }

    @Override
    public RecordValue getRecord(String authorId) {
        return virtualCoinRecordRepository.findById(authorId).orElse(RecordValue.defaultValue(authorId));
    }

    @Override
    public void gratuity(RecordValue... targetValues) {
        Arrays.stream(targetValues).forEach(virtualCoinRecordRepository::save);
    }
}
