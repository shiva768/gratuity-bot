package jp.vcoin.gratuitybot.util;

import java.text.DecimalFormat;

public class Formatter {

    private static DecimalFormat format = new DecimalFormat("0");

    static {
        format.setMaximumFractionDigits(8);
        format.setMinimumFractionDigits(1);
    }

    public static DecimalFormat formatValue() {
        return format;
    }
}
