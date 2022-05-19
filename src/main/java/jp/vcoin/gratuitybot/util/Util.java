package jp.vcoin.gratuitybot.util;

import java.math.BigDecimal;

import static jp.vcoin.gratuitybot.util.Util.CompareResult.*;

public class Util {

    public static boolean invalidAmount(String amount) {
        if (amount == null) return true;
        try {
            new BigDecimal(amount);
        } catch (NumberFormatException e) {
            return true;
        }

        return !amount.matches("^([1-9]\\d*|0)(\\.[0-9]{1,8})?$") || compare(new BigDecimal(amount), new BigDecimal(0)) != Higher;
    }

    /**
     * 第一引数が第二引数に対して下か同じか上かを返す
     *
     * @param comp1 比較結果になる値
     * @param comp2 比較対象になる値
     * @param <T>   {@link Comparable} を実装したclass
     * @return 比較結果
     */
    @SuppressWarnings("unchecked")
    public static <T extends Comparable> CompareResult compare(T comp1, T comp2) {
        final int i = comp1.compareTo(comp2);
        if (i < 0)
            return Lower;
        else if (i == 0)
            return Equal;
        return Higher;
    }

    public enum CompareResult {
        Lower, Equal, Higher
    }
}
