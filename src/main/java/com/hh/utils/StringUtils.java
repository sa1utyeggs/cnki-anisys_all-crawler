package com.hh.utils;

import java.util.Locale;

/**
 * @author ab875
 */
public class StringUtils {
    public static boolean isEmpty(String str) {
        return str == null || "".equals(str);
    }

    public static String formatComma(String string) {
        return string.replace(",", "，");
    }

}
