package jp.vcoin.gratuitybot.service;

import jp.vcoin.gratuitybot.domain.BalanceValue;
import jp.vcoin.gratuitybot.domain.TransactionValue;
import jp.vcoin.gratuitybot.exception.BalanceShortfallException;
import jp.vcoin.gratuitybot.exception.RPCException;

import java.math.BigDecimal;
import java.util.Optional;

public interface VirtualCoinWalletService {

    BalanceValue getBalance(String authorId);

    String getNewAddress(String authorId);

    void move(String authorId, String targetId, BigDecimal amount) throws RPCException, BalanceShortfallException;

    void moveEmoji(String authorId, String targetId, BigDecimal amount) throws RPCException, BalanceShortfallException;

    boolean validateAddress(String targetAddress);

    Optional<String> sendMany(String authorId, String targetAddress, BigDecimal amount) throws BalanceShortfallException;

    TransactionValue getTransaction(String id);
}
