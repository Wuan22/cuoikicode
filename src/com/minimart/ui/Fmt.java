package com.minimart.ui;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/** Tiện ích định dạng tiền tệ. */
public final class Fmt {
    private Fmt() {
    }

    public static String money(double v) {
        return new DecimalFormat("#,##0", DecimalFormatSymbols.getInstance(Locale.US)).format(v) + " đ";
    }
}
