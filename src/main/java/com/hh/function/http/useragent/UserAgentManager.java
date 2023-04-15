package com.hh.function.http.useragent;

/**
 * @author ab875
 */
public interface UserAgentManager {

    /**
     * 获取 UserAgent
     * @return string
     */
    public String getUserAgent();

    /**
     * 增加 UserAgent
     * @param sUserAgent string
     */
    public void addUserAgent(String sUserAgent);

    /**
     * 删除 UserAgent
     * @param sUserAgent string
     */
    public void removeUserAgent(String sUserAgent);

    /**
     * 获取默认 UserAgent
     * @return string
     */
    public String getDefaultUserAgent();
}
