package jp.vcoin.gratuitybot.service.impl;

import jp.vcoin.gratuitybot.domain.BalanceValue;
import jp.vcoin.gratuitybot.domain.RecordValue;
import jp.vcoin.gratuitybot.domain.TransactionValue;
import jp.vcoin.gratuitybot.exception.BalanceShortfallException;
import jp.vcoin.gratuitybot.exception.RPCException;
import jp.vcoin.gratuitybot.repository.VirtualCoinWalletRepository;
import jp.vcoin.gratuitybot.json.VirtualCoinRequestJson;
import jp.vcoin.gratuitybot.service.VirtualCoinRecordService;
import jp.vcoin.gratuitybot.service.VirtualCoinWalletService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class VirtualCoinWalletServiceImpl implements VirtualCoinWalletService {

    private final static short MINIMUM_CONFIRMATIONS = 10;
    private final static short MINIMUM_CONFIRMATIONS_ALL = 0;

    private final VirtualCoinWalletRepository virtualCoinWalletRepository;
    private final VirtualCoinRecordService virtualCoinRecordService;

    @Autowired
    public VirtualCoinWalletServiceImpl(VirtualCoinWalletRepository virtualCoinWalletRepository, VirtualCoinRecordService virtualCoinRecordService) {
        this.virtualCoinWalletRepository = virtualCoinWalletRepository;
        this.virtualCoinRecordService = virtualCoinRecordService;
    }

    @Override
    public BalanceValue getBalance(String authorId) {
        final GetBalanceResponse balance = virtualCoinWalletRepository.request(
                new VirtualCoinRequestJson("balance", "getbalance", authorId, MINIMUM_CONFIRMATIONS),
                GetBalanceResponse.class
        );
        final GetBalanceResponse allBalance = virtualCoinWalletRepository.request(
                new VirtualCoinRequestJson("balance", "getbalance", authorId, MINIMUM_CONFIRMATIONS_ALL),
                GetBalanceResponse.class
        );
        return new BalanceValue(balance.result, allBalance.result);
    }

    @Override
    public String getNewAddress(String authorId) {
        final GetNewAddressResponse newAddressResponse = virtualCoinWalletRepository.request(
                new VirtualCoinRequestJson("deposit", "getnewaddress", authorId),
                GetNewAddressResponse.class
        );
        return newAddressResponse.result;
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void move(String authorId, String targetId, BigDecimal amount) throws RPCException, BalanceShortfallException {
        if (getBalance(authorId).getBalance().compareTo(amount) < 0)
            throw new BalanceShortfallException("shortfall move");
        final RecordValue from = virtualCoinRecordService.getRecord(authorId);
        final RecordValue to = virtualCoinRecordService.getRecord(targetId);
        from.send(amount);
        to.receive(amount);
        virtualCoinRecordService.gratuity(from, to);
        if (requestMove(authorId, targetId, amount))
            throw new RPCException("rpc move failed");
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void moveEmoji(String authorId, String targetId, BigDecimal amount) throws RPCException, BalanceShortfallException {
        if (getBalance(authorId).getBalance().compareTo(amount) < 0)
            throw new BalanceShortfallException("shortfall move emoji");
        final RecordValue from = virtualCoinRecordService.getRecord(authorId);
        final RecordValue to = virtualCoinRecordService.getRecord(targetId);
        from.sendEmoji(amount);
        to.receiveEmoji(amount);
        virtualCoinRecordService.gratuity(from, to);
        if (requestMove(authorId, targetId, amount))
            throw new RPCException("rpc move emoji failed");
    }

    private boolean requestMove(String authorId, String targetId, BigDecimal amount) {
        final MoveResponse moveResponse = virtualCoinWalletRepository.request(
                // TODO move rpc api deprecated
                new VirtualCoinRequestJson("gratuity", "move", authorId, targetId, amount),
                MoveResponse.class
        );
        return !moveResponse.result;
    }

    @Override
    public boolean validateAddress(String targetAddress) {
        final ValidateAddressResponse validateAddressResponse = virtualCoinWalletRepository.request(
                new VirtualCoinRequestJson("withdraw-validate_address", "validateaddress", targetAddress),
                ValidateAddressResponse.class
        );
        return validateAddressResponse != null && validateAddressResponse.result.isvalid;
    }

    @Override
    public Optional<String> sendMany(String authorId, String targetAddress, BigDecimal amount) throws BalanceShortfallException {
        if (getBalance(authorId).getBalance().compareTo(amount) < 0)
            throw new BalanceShortfallException("shortfall sendMany");
        final SendManyResponse sendManyResponse = virtualCoinWalletRepository.request(
                new VirtualCoinRequestJson("withdraw", "sendmany", authorId, Collections.singletonMap(targetAddress, amount), MINIMUM_CONFIRMATIONS, "", Collections.singletonList(targetAddress)),
                SendManyResponse.class
        );
        return Optional.ofNullable(sendManyResponse.result);
    }

    @Override
    public TransactionValue getTransaction(String transactionId) {
        return virtualCoinWalletRepository.request(
                new VirtualCoinRequestJson("withdraw-transaction", "gettransaction", transactionId),
                GetTransactionResponse.class
        ).getSendDetail();
    }

    @Data
    public static class GetBalanceResponse {
        BigDecimal result;
    }

    @Data
    public static class GetNewAddressResponse {
        String result;
    }

    @Data
    public static class MoveResponse {
        boolean result;
    }

    @Data
    public static class ValidateAddressResponse {
        Result result;

        @Data
        @SuppressWarnings("WeakerAccess")
        public static class Result {
            boolean isvalid;
        }
    }

    @Data
    public static class SendManyResponse {
        String result;
    }

    @Data
    public static class GetTransactionResponse {
        Result result;

        TransactionValue getSendDetail() {
            final Optional<Result.Detail> detail = result.details.stream().filter(d -> "send".equals(d.category)).findFirst();
            if (detail.isPresent()) {
                final Result.Detail d = detail.get();
                return new TransactionValue(result.txid, d.amount.abs(), d.fee.abs(), LocalDateTime.ofEpochSecond(result.time, 0, ZoneOffset.UTC), LocalDateTime.ofEpochSecond(result.timereceived, 0, ZoneOffset.UTC));
            }
            return null;
        }

        @Data
        @SuppressWarnings("WeakerAccess")
        public static class Result {
            String txid;
            long time;
            long timereceived;
            List<GetTransactionResponse.Result.Detail> details;

            @Data
            @SuppressWarnings("WeakerAccess")
            public static class Detail {
                String account;
                String address;
                String category;
                BigDecimal amount;
                BigDecimal fee;
            }
        }
    }
}
