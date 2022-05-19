package jp.vcoin.gratuitybot.repository.impl;

import jp.vcoin.gratuitybot.json.VirtualCoinRequestJson;
import jp.vcoin.gratuitybot.repository.VirtualCoinWalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestOperations;

@Repository
public class VirtualCoinWalletRepositoryImpl implements VirtualCoinWalletRepository {

    private RestOperations restOperations;

    @Autowired
    public VirtualCoinWalletRepositoryImpl(RestOperations restOperations) {
        this.restOperations = restOperations;
    }

    @Override
    public <T> T request(VirtualCoinRequestJson requestJson, Class<? extends T> responseType) {
        return restOperations.postForObject("/", requestJson, responseType);
    }
}
