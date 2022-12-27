package com.hh.utils;

/**
 * @author 86183
 */
public class AssertUtils {

    public static void sysIsError(Boolean flag, String message) {
        if (flag) {
            throw new RuntimeException(message);
        }
    }
}
