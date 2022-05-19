package jp.vcoin.gratuitybot.domain;

import jp.vcoin.gratuitybot.util.Formatter;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@AllArgsConstructor
public class BalanceValue {

    @Getter
    private BigDecimal balance;
    private BigDecimal allBalance;

    public String getBalancePlainString() {
        return Formatter.formatValue().format(balance);
    }

    public BigDecimal difference() {
        return allBalance.subtract(balance);
    }

    public boolean isDuringVerification() {
        return 0 < difference().compareTo(new BigDecimal(0));
    }
}
