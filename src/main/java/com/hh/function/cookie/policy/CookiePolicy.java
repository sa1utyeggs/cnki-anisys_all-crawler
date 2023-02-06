package com.hh.function.cookie.policy;

import javafx.beans.binding.ObjectExpression;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author ab875
 */
public interface CookiePolicy {

    /**
     * 获取 cookie
     *
     * @param cookies collection
     * @return String
     */
    public String getCookie(List<String> cookies);

    /**
     * 获得默认的 cookie
     * @param cookies array
     * @return string
     */
    public default String getDefaultCookie(List<String> cookies){
        if (!checkArgs(cookies)){
            return null;
        }
        return cookies.get(0);
    }

    /**
     * 保证参数不为空
     *
     * @param args 参数
     * @return boolean
     */
    public default boolean checkArgs(Object... args) {
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
