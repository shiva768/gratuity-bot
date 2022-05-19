package jp.vcoin.gratuitybot.domain;

import jp.vcoin.gratuitybot.util.Formatter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@ToString
public class TransactionValue {

    private String transactionId;
    private BigDecimal amount;
    private BigDecimal fee;
    private LocalDateTime time;
    private LocalDateTime timereceived;

    public String getAmountPlainString() {
        return Formatter.formatValue().format(amount);
    }

    public String getFeePlainString() {
        return Formatter.formatValue().format(fee);
    }

}
