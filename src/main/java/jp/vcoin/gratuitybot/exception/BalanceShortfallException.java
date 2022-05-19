package jp.vcoin.gratuitybot.exception;

public class BalanceShortfallException extends Exception {
    public BalanceShortfallException(String s) {
        super(s);
    }
}
