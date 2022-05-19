package jp.vcoin.gratuitybot.repository;

import jp.vcoin.gratuitybot.json.VirtualCoinRequestJson;

public interface VirtualCoinWalletRepository {

    <T> T request(VirtualCoinRequestJson requestJson, Class<? extends T> responseType);
}
