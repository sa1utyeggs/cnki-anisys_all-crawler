package com.hh.utils;

import java.util.Collection;
import java.util.Map;

/**
 * @author ab875
 */
public class CheckUtils {

    /**
     * 判断参数是否合法
     * @param args 可变参数
     * @return bool
     */
    public static boolean checkArgs(Object... args) {
        for (Object arg : args) {
            // 判断是否为 null
            if (arg == null) {
                return false;
            }

            // map 非空检查
            if (arg instanceof Map && ((Map<?, ?>) arg).isEmpty()) {
                return false;
            }

            // collection 非空检查
            if (arg instanceof Collection && ((Collection<?>) arg).isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
