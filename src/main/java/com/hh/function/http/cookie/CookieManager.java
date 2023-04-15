package com.hh.function.http.cookie;

/**
 * @author ab875
 */
public interface CookieManager {

    /**
     * 获取 Cookie
     * @return string
     */
    public String getCookie();

    /**
     * 增加 Cookie
     * @param sCookie string
     */
    public void addCookie(String sCookie);

    /**
     * 删除 Cookie
     * @param sCookie string
     */
    public void removeCookie(String sCookie);

    /**
     * 获取默认 Cookie
     * @return string
     */
    public String getDefaultCookie();
}
