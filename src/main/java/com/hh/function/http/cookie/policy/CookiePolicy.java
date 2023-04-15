package com.hh.function.http.cookie.policy;

import com.hh.utils.CheckUtils;

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
    String getCookie(List<String> cookies);

    /**
     * 获得默认的 cookie
     *
     * @param cookies array
     * @return string
     */
    default String getDefaultCookie(List<String> cookies) {
        if (!CheckUtils.checkArgs(cookies)) {
            return null;
        }
        return cookies.get(0);
    }
}
