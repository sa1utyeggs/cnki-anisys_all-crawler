package com.hh.utils;

import org.apache.logging.log4j.Logger;

/**
 * @author 86183
 */
public class AssertUtils {

    public static void sysIsError(Boolean flag, String message) {
        if (flag) {
            throw new RuntimeException(message);
        }
    }

    public static void sysIsErrorLogger(Boolean flag, Logger logger, String message) {
        if (flag) {
            logger.error(message);
        }
    }
}
