package com.hh;

/**
 * @author 86183
 */
public class AssertUtils {

    public static void sysIsError(Boolean flag, String message) throws Exception {
        if (flag) {
            throw new Exception(message);
        }
    }
}
