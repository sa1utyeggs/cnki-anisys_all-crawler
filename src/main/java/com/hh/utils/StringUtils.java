package com.hh.utils;

import java.util.Locale;
import java.util.Set;

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

    public static boolean startWith(String sentence, Set<String> exclusions) {
        for (String exclusion : exclusions) {
            if (sentence.startsWith(exclusion)) {
                return true;
            }
        }
        return false;
    }

}
